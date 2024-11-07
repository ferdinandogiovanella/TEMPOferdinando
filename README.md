# TEMPOferdinando
 Trabalho da disciplina de Programação de Dispositivos Móveis da Unipar EAD

Este projeto propõe a criação de um aplicativo de previsão do tempo utilizando Android Studio, seguindo os seguintes requisitos e padrões de design:

Linguagem e Atividades: O aplicativo será desenvolvido em Java e conterá três atividades principais:

Duas atividades no fluxo do aplicativo (principal e secundária).
Uma atividade dedicada à seção "Sobre", onde serão exibidas informações pessoais do desenvolvedor, como nome, RA (Registro Acadêmico) e curso.
Interface de Lista: Será implementado um layout de lista utilizando RecyclerView e CardView, garantindo uma exibição eficiente e moderna de itens na lista de previsão.

Menu e AppBar: O aplicativo contará com um menu exibido na appBar. Este menu, acessível na tela principal, permitirá navegação para a tela de informações pessoais.

Splash Screen: Uma SplashScreen será criada para exibir uma imagem ou logo do aplicativo durante 3 segundos ao ser inicializado. O carregamento será gerenciado com um Handler. O nome da Activity desta tela será único para evitar problemas de compilação.

Segunda Tela com TabBar:

A TabBar terá duas abas distintas:
Primeira Aba: Exibirá uma lista de previsão do tempo com dados consumidos pela API da HG Brasil Weather, apresentada em CardViews.
Segunda Aba: Mostrará um mapa com um marcador fixo que indica a cidade consultada pela API.
Funcionalidade Extra com FloatActionButton: Um FloatActionButton será adicionado em uma das abas para permitir a troca de cidade via escaneamento de QR code. O recurso usará a biblioteca ZXing para implementar a funcionalidade de leitura de QR codes.
