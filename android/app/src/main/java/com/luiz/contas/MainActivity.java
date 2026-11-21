package com.luiz.contas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * O app ocupa a tela inteira: o conteúdo passa por baixo da barra de status e da
 * barra de navegação, que ficam transparentes. As alturas dessas barras são
 * repassadas à página como as variáveis CSS --sat, --sab, --sal e --sar, que o
 * HTML usa como respiro para o conteúdo não ficar embaixo delas.
 */
public class MainActivity extends Activity {

    private WebView web;
    private boolean carregado = false;
    private int[] barras = {0, 0, 0, 0}; // topo, baixo, esquerda, direita (px)
    private int teclado = 0;             // altura do teclado (px)

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        telaInteira();

        web = new WebView(this);
        web.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        web.setBackgroundColor(0xFF0B0F1C);
        web.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                carregado = true;
                enviarMedidas();
            }
        });
        web.setOnApplyWindowInsetsListener((v, ins) -> {
            medir(ins);
            enviarMedidas();
            return ins;
        });
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    /** Barras transparentes e conteúdo desenhado atrás delas. */
    private void telaInteira() {
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(false);
        } else {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            w.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        iconesDasBarras();
    }

    /** Ícones escuros quando o tema do aparelho é claro, claros quando é escuro. */
    private void iconesDasBarras() {
        boolean claro = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                int mascara = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                c.setSystemBarsAppearance(claro ? mascara : 0, mascara);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View d = w.getDecorView();
            int flags = d.getSystemUiVisibility();
            if (claro) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            d.setSystemUiVisibility(flags);
        }
    }

    private void medir(WindowInsets ins) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.graphics.Insets b = ins.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            barras = new int[]{b.top, b.bottom, b.left, b.right};
            teclado = ins.getInsets(WindowInsets.Type.ime()).bottom;
        } else {
            barras = new int[]{ins.getSystemWindowInsetTop(), ins.getSystemWindowInsetBottom(),
                    ins.getSystemWindowInsetLeft(), ins.getSystemWindowInsetRight()};
            teclado = 0;
        }
    }

    /** Repassa as medidas à página, já convertidas de pixels para CSS pixels. */
    private void enviarMedidas() {
        if (!carregado || web == null) return;
        float d = getResources().getDisplayMetrics().density;
        if (d <= 0) d = 1f;
        int baixo = Math.round(Math.max(barras[1], teclado) / d);
        String js = "(function(){var r=document.documentElement.style;"
                + "r.setProperty('--sat','" + Math.round(barras[0] / d) + "px');"
                + "r.setProperty('--sab','" + baixo + "px');"
                + "r.setProperty('--sal','" + Math.round(barras[2] / d) + "px');"
                + "r.setProperty('--sar','" + Math.round(barras[3] / d) + "px');"
                + (teclado > 0
                    ? "var a=document.activeElement;if(a&&a.scrollIntoView)a.scrollIntoView({block:'center'});"
                    : "")
                + "})()";
        web.evaluateJavascript(js, null);
    }

    @Override
    public void onConfigurationChanged(Configuration novaConfig) {
        super.onConfigurationChanged(novaConfig);
        iconesDasBarras();
        enviarMedidas();
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
