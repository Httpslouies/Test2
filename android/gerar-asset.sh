#!/bin/sh
# Monta app/src/main/assets/index.html a partir da página do app ($1),
# acrescentando o cabeçalho HTML que o artifact adiciona sozinho.
set -e
SRC="$1"
DEST="$(dirname "$0")/app/src/main/assets/index.html"
{
  cat <<'HEAD'
<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<style>
/* --sat/--sab/--sal/--sar chegam da MainActivity: altura das barras do sistema */
:root{color-scheme:light;
  padding-top:var(--sat,env(safe-area-inset-top,0px));
  padding-bottom:var(--sab,env(safe-area-inset-bottom,0px))}
html{background:var(--bg,#0b0f1c)}
body{margin:0;font:14px system-ui,sans-serif}
img{max-width:100%}
[hidden]{display:none!important}
body>.wrap{padding-left:max(16px,var(--sal,env(safe-area-inset-left,0px)));
      padding-right:max(16px,var(--sar,env(safe-area-inset-right,0px)))}
</style>
HEAD
  cat "$SRC"
  printf '</body>\n</html>\n'
} > "$DEST"
python3 - "$DEST" <<'PY'
import sys
p=sys.argv[1]; t=open(p,encoding='utf-8').read()
t=t.replace('<div class="wrap">','</head>\n<body>\n<div class="wrap">',1)
open(p,'w',encoding='utf-8').write(t)
PY
echo "asset gerado em $DEST"
