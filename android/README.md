# Contas do Mês — app Android

App de controle de despesas mensais: despesas fixas, faturas de cartão e compras
parceladas, com salário e saldo restante. Os dados ficam salvos no aparelho.

O app em si é a página `app/src/main/assets/index.html` (HTML/CSS/JS, sem
dependências externas). O projeto Android é só um WebView em tela cheia que
carrega essa página — ver `app/src/main/java/com/luiz/contas/MainActivity.java`.

## Compilar

Precisa de JDK 17+, Gradle 8.x e o Android SDK (plataforma 34, build-tools 34.0.0).

```sh
export ANDROID_HOME=/caminho/para/o/android-sdk
cd android
gradle assembleDebug
```

O APK sai em `app/build/outputs/apk/debug/app-debug.apk`, assinado com a chave de
debug — serve para instalar direto no celular, não para publicar na Play Store.
Para publicar, gere uma chave própria e configure um `signingConfig` de release
em `app/build.gradle`.

Para atualizar o conteúdo do app, edite `app/src/main/assets/index.html` e
compile de novo.
