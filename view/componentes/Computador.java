/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 17/03/18
* Ultima alteracao: 20/05/18
* Nome: Computador
* Funcao: Classe que compoe a interface grafica de um computador com suas camadas
***********************************************************************/

package view.componentes;

import javafx.scene.layout.VBox;
import img.Imagem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.application.Platform;
import java.util.concurrent.Semaphore;
import java.util.List;
import java.util.ArrayList;

import util.Alerta;
import model.*;
import view.Painel;
import model.camadas.Quadro;


public class Computador extends VBox {
  private Imagem allImage = new Imagem();
  public Semaphore SEMAPHORO_TRANSMITIR;//Semaphoro para transmitir um Quadro de cada vez

  public int id_temporizador = 1;

  public Grafico grafico_comunicacao;//Grafico por onde os quadros sao transmitidos

  public AplicacaoTransmissora aplicacaoTransmissora;//Aplicacao Transmissora com Camadas
  public AplicacaoReceptora aplicacaoReceptora;//Aplicacao Receptora com Camadas

  public static final String TRANSMISSOR = "transmissor";//O Computador eh o Transmissor
  public static final String RECEPTOR = "receptor";//O Comoputador eh o Receptor

  public String tipo;//O tipo do computador

  private int width = 400;//Largura
  private int height = 190;//Altura
  private int posX, posY;//Posicao X e Y da Imagem

  //COMPUTADOR
  private ImageView computadorImage;//ImageView do Computador
  private TextArea textArea;//Area onde o texto sera adicionado
  private TextField mensagemText;//Campo onde digita a mensagem
  private Button enviarButton;//Botao para enviar mensagem

  //Adicionar as camadas dentro do Accordion
  private Accordion camadas;//Componentes pra organizar as Camadas
  private ImageView setaImagem;//ImageView da Seta

  //CAMADAS
  public Camada camadaAplicacao;//Componente de Interface Camada Aplicacao
  public Camada camadaEnlace;//Componente de Interface Camada Enlace
  public Camada camadaFisica;//Componente de Interface Camada Fisica

  public int idQuadroEsperado = 1;
  private List<Quadro> quadrosRecebidosNConfirmados;

  /*********************************************
  * Metodo: Computador - Construtor
  * Funcao: Constroi objetos da classe Computador2
  * Parametros: void
  * Retorno: void
  *********************************************/
  public Computador(String tipo, int posX, int posY) {
    this.tipo = tipo;//Tipo do Computador (Transmissor|Receptor)
    this.SEMAPHORO_TRANSMITIR = new Semaphore(1);
    this.setLayoutX(posX);//Posicao X
    this.setLayoutY(posY);//Posicao Y

    this.aplicacaoTransmissora = new AplicacaoTransmissora(this);//Instanciando Aplicacao Transmissora
    this.aplicacaoReceptora = new AplicacaoReceptora(this);//Instanciando Aplicacao Receptora

    this.getChildren().add(construirComputador());//Adicionando Computador na interface
    this.getChildren().add(construirCamadas());//Adicionando Camadas na interface
  
    this.quadrosRecebidosNConfirmados = new ArrayList<>();
  }


  /*********************************************
  * Metodo: enviarMensagem
  * Funcao: Envia uma mensagem para o outro computador
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void enviarMensagem(String mensagem) {
    //Painel.COMPUTADOR_TRANSMISSOR.limparTextoCamadas();//Limpando texto do Computador Transmissor
    //Painel.COMPUTADOR_RECEPTOR.limparTextoCamadas();//Limpando texto do Computador Receptor

    //Painel.COMPUTADOR_TRANSMISSOR.limparTela();//Limpando texto da Tela
    //Painel.COMPUTADOR_RECEPTOR.limparTela();//Limpando texto da Tela

    //this.id_temporizador = 1;
    //this.idQuadroEsperado = 1;
    //MeioDeComunicacao.inicarTransmissao();

    adicionarMensagem("Msg Enviada: " + mensagem);//Adiciona a mensagem enviada no Computador Transmissor

    aplicacaoTransmissora.aplicacaoTransmissora(mensagem);//Enviando a mensagem para aplicacao transmissora
  }

  /*********************************************
  * Metodo: receberMensagem
  * Funcao: Recebe uma mensagem do outro computador
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void receberMensagem(String mensagem) {
    this.textArea.setText(textArea.getText() + mensagem);//Limpa a Tela do Computador
    this.textArea.appendText("");//Descendo Scroll da tela do Computador
  }

  /*********************************************
  * Metodo: adicionarMensagem
  * Funcao: Adiciona uma mensagem ao TextArea
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void adicionarMensagem(String mensagem) {
    Platform.runLater(() -> {
      try {
        textArea.setText(textArea.getText() + "\n" + mensagem + "\n");//Limpa a Tela do Computador
        textArea.appendText("");//Descendo Scroll da tela do Computador
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  /*********************************************
  * Metodo: limparTela
  * Funcao: Adiciona uma mensagem ao TextArea
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void limparTela() {
    this.textArea.setText("");
  }

  /*********************************************
  * Metodo: terminouCamadas
  * Funcao: Oculta a Seta desta Camada
  * Parametros: void
  * Retorno: void
  *********************************************/
  public void terminouCamadas() {
    Platform.runLater(() -> {
      camadas.setExpandedPane(null);
      setaImagem.setVisible(false);
    });
  }

  /*********************************************
  * Metodo: limparTextoCamadas
  * Funcao: Limpa o texto dos componentes de interface
  * Parametros: void
  * Retorno: void
  *********************************************/
  public void limparTextoCamadas() {
    Platform.runLater(() -> {
      camadaAplicacao.limpar();
      camadaEnlace.limpar();
      camadaFisica.limpar();
    });
  }

  /*********************************************
  * Metodo: setGrafico
  * Funcao: Atribui o grafico do computador
  * Parametros: grafico : Grafico
  * Retorno: void
  *********************************************/
  public void setGrafico(Grafico grafico) {
    this.grafico_comunicacao = grafico;
  }

  /*********************************************
  * Metodo: getGrafico
  * Funcao: Retorna o grafico do computador
  * Parametros: void
  * Retorno: Grafico
  *********************************************/
  public Grafico getGrafico() {
    return this.grafico_comunicacao;
  }

  /*********************************************
  * Metodo: adicionarQuadroRecebidoNConfirmado
  * Funcao: Adiciona um Quadro que foi recebido, mas nao confirmado
  * Parametros: quadro : Quadro
  * Retorno: void
  *********************************************/
  public void adicionarQuadroRecebidoNConfirmado(Quadro quadro) {
    this.quadrosRecebidosNConfirmados.add(quadro);
  }

  public List<Quadro> getQuadrosRecebidosNConfirmado() {
    return quadrosRecebidosNConfirmados;
  }

  /*********************************************
  * Metodo: construirComputador
  * Funcao: Retorna a interface do Computador pronta
  * Parametros: void
  * Retorno: computador : AnchorPane
  *********************************************/
  private AnchorPane construirComputador() {
    AnchorPane painelComputador = new AnchorPane();
    painelComputador.setPrefSize(400,190);

    this.computadorImage = new ImageView();//Inicializando a ImageView do Computador
    this.allImage.trocarImagem(this.computadorImage, this.tipo);//Adicionando a Imagem do Computador
    this.computadorImage.setPreserveRatio(true);
    this.computadorImage.setFitHeight(160);//Adicionando tamanho da Imagem
    this.computadorImage.setLayoutX(109);//Adicionando posicao X
    this.computadorImage.setLayoutY(0);//Adicionando posicao Y
    painelComputador.getChildren().add(computadorImage);//Adicionando ao Painel

    this.textArea = new TextArea();//Inicializando o TextArea
    this.textArea.setPrefSize(170, 109);
    this.textArea.setLayoutX(122);//Adicionando posicao X
    this.textArea.setLayoutY(10);//Adicionando posicao Y
    this.textArea.setWrapText(true);//O texto quebra a linha automaticamente
    this.textArea.setEditable(false);//Desabilita a edicao do componente
    this.textArea.setFocusTraversable(false);//Desabilita o foco inicial
    painelComputador.getChildren().add(textArea);

    this.mensagemText = this.textFieldMensagem(91,160);//Inicializnado TextField de entrada de texto
    painelComputador.getChildren().add(mensagemText);

    this.enviarButton = this.buttonEnviar(295,160);
    painelComputador.getChildren().add(enviarButton);

    return painelComputador;
  }

  /*********************************************
  * Metodo: construirCamadas
  * Funcao: Retorna a interface das Camadas pronta
  * Parametros: void
  * Retorno: camadas : AnchorPane
  *********************************************/
  private AnchorPane construirCamadas() {
    AnchorPane painelCamadas = new AnchorPane();
    painelCamadas.setPrefSize(400, 400);

    /*******************************************
    * [ACORDEON]
    ********************************************/
    this.camadas = new Accordion();
    this.camadas.setPrefSize(370,350);
    this.camadas.setLayoutX(30);
    this.camadas.setLayoutY(10);
    painelCamadas.getChildren().add(camadas);

    /*******************************************
    * [IMAGEVIEW SETA]
    ********************************************/
    this.setaImagem = allImage.getImageView("seta");
    this.setaImagem.setFitWidth(26);
    this.setaImagem.setFitHeight(26);
    this.setaImagem.setLayoutY(10);
    this.setaImagem.setVisible(false);
    painelCamadas.getChildren().add(setaImagem);


    /*******************************************
    * [CAMADA DE APLICACAO]
    ********************************************/
    camadaAplicacao = new Camada("Camada Aplicacao",setaImagem,10,camadas);
    this.camadas.getPanes().add(camadaAplicacao);
    
    /*******************************************
    * [CAMADA ENLACE DE DADOS]
    ********************************************/
    camadaEnlace = new Camada("Camada Enlace de Dados",setaImagem,37,camadas);
    this.camadas.getPanes().add(camadaEnlace);

    /*******************************************
    * [CAMADA FISICA]
    ********************************************/
    camadaFisica = new Camada("Camada Fisica",setaImagem,62,camadas);
    this.camadas.getPanes().add(camadaFisica);

    return painelCamadas;
  }

  /*********************************************
  * Metodo: textFieldMensagem
  * Funcao: Cria um TextField para entrada de texto
  * Parametros: posX : int, posY : int
  * Retorno: TextField
  *********************************************/
  private TextField textFieldMensagem(int posX, int posY) {
    TextField entradaTexto = new TextField();//Criando uma nova entrada de Texto
    entradaTexto.setPrefWidth(230);//Adicionando Largura
    entradaTexto.setPrefHeight(26);//Adicionando Altura
    entradaTexto.setLayoutX(posX);//Adicioanndo posicao X
    entradaTexto.setLayoutY(posY);//Adicionando posicao Y
    entradaTexto.setFocusTraversable(false);//Desabilita o foco inicial
    entradaTexto.setPromptText("Digite e aperte ENTER");//Adicionando texto base
    entradaTexto.setPadding(new Insets(0,30,0,5));

    //Adiciona um funcao ao apertar botao [ENTER]
    entradaTexto.setOnKeyReleased((KeyEvent key) -> {
      if (key.getCode() == KeyCode.ENTER) {
        this.enviarMensagem(entradaTexto.getText());
        entradaTexto.setText("");//Limpa a caixa de Texto
      }
    });

    return entradaTexto;
  }

  /*********************************************
  * Metodo: buttonEnviar
  * Funcao: Cria um Button para enviar a Mensagem
  * Parametros: posX : int, posY : int
  * Retorno: Button
  *********************************************/
  private Button buttonEnviar(int posX, int posY) {
    Button botao = new Button();//Criando um novo botao
    botao.setLayoutX(posX);//Adicionando posicao X
    botao.setLayoutY(posY);//Adicionando posicao Y
    botao.setMinWidth(26);//Adicionando Largura Minima
    botao.setMinHeight(26);//Adicionando Altura Minima
    botao.setMaxWidth(26);//Adicionando Largura Maxima
    botao.setMaxHeight(26);//Adicionando Altura Maxima

    botao.setOnAction((ActionEvent e) -> {

      //Verificando se nenhum texto foi digitado
      if (this.mensagemText.getText().isEmpty()) {
        Alerta.erro("Erro ao enviar mensagem","Erro: O campo de mensagem esta vazio");
      } else {
        this.limparTextoCamadas();
        this.enviarMensagem(mensagemText.getText());
        this.mensagemText.setText("");//Limpa a caixa de Texto
      }
      
    });//Atribuindo acao no botao

    botao.setGraphic(allImage.getImageView("enviar"));

    return botao;
  }


  /***********************************************************************
  * Autor: Cassio Meira Silva
  * Inicio: 20/03/18
  * Ultima alteracao: 26/03/18
  * Nome: Camada
  * Funcao: Cria o componente de Camada para interface grafica onde adiciona os textos
  ***********************************************************************/
  public class Camada extends TitledPane {
    private String titulo;//Titulo da camada
    private TextArea textArea;//Area de texto da camada
    private ImageView seta;//Referencia da Imagem da seta
    private int posYSeta;//Posicao Y da seta dessa camada
    private Accordion camadas;//Componente que adiciona todas as camadas

    public Camada(String titulo, ImageView seta, int posYSeta, Accordion camadas) {
      this.titulo = titulo;
      this.seta = seta;
      this.posYSeta = posYSeta;
      this.camadas = camadas;

      this.setText(titulo);//Adicionando titulo da camada
      this.setAlignment(Pos.CENTER);//Alinenho ao centro
      this.textArea = new TextArea();//Instanciando area de texto
      this.textArea.setEditable(false);//Desabilitando a escrita
      this.textArea.setFocusTraversable(false);//Desabilitando o foco
      this.setContent(textArea);//Adicionando area de texto na interface
    }


    /*********************************************
    * Metodo: addText
    * Funcao: Adiciona um texto na area de texto da camada
    * Parametros: texto : String
    * Retorno: void
    *********************************************/
    public void addText(String texto) {
      Platform.runLater(() -> {
        try {
          camadas.setExpandedPane(this);//Expandindo a camada
          //Thread.sleep(100);
          seta.setVisible(true);//Deixando a imagem da seta visivel
          seta.setLayoutY(posYSeta);//Alterando a posicao Y da seta
          textArea.setText(textArea.getText() + texto);//Adicionando o texto
          textArea.appendText("");//Movendo o scroll bar da Area de texto
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      // Platform.runLater(new Runnable(){
      //   @Override
      //   public void run() {
      //     try {
      //       seta.setVisible(true);//Deixando a imagem da seta visivel
      //       seta.setLayoutY(posYSeta);//Alterando a posicao Y da seta
      //       textArea.setText(textArea.getText() + texto);//Adicionando o texto
      //       textArea.appendText("");//Movendo o scroll bar da Area de texto
      //     } catch (Exception e) {
      //       System.out.println("[ERRO] - Adicionar texto na " + titulo);
      //       e.printStackTrace();
      //     }
      //   }
      // });
    }

    /*********************************************
    * Metodo: limpar
    * Funcao: Limpar o texto que esta na Area de Texto da Camada
    * Parametros: void
    * Retorno: void
    *********************************************/
    public void limpar() {
      textArea.setText("");
    }
  }

}//Fim class
