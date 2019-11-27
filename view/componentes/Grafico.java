/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 24/12/17
* Ultima alteracao: 14/03/18
* Nome: Grafico
* Funcao: Representar os sinais enviados em forma de Onda
***********************************************************************/

package view.componentes;


import img.Imagem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Platform;

import view.Painel;
import model.camadas.CamadaFisicaTransmissora;
import model.camadas.CamadaFisicaReceptora;

import view.componentes.Computador;


public class Grafico extends AnchorPane {
  private final int width = 382;//Largura deste Componente
  private final int height = 150;//200;//Altura deste Componente

  protected int posLinha[] = {10,70,130,190,250,310,370};
  protected int velocidade = 80;//450;//Velocidade de transicao das Ondas

  private Computador transmissor;//Referencia do Computador que esta enviando a mensagem

  public Semaphore semaphoroInicio = new Semaphore(0);//semaphoro para controlar a Thread Onda
  public Semaphore semaphoroFim = new Semaphore(0);//semaphoro para controlar a Thread Onda
  private Onda onda;//Objeto Onda

  protected ComboBox<Codificacao> codificacaoComboBox;//Armazena os tipo de Codificacao
  protected Slider velocidadeSlider;//Slider para controlar a velocidade das Ondas
  private Label velocidadeLabel;//Label velocidade

  private Queue<Integer> filaEntradaDeBits = new LinkedList<Integer>();//Fila com os Bits de Entrada

  /*********************************************
  * Metodo: Grafico - Construtor
  * Funcao: Constroi objetos da classe Grafico
  * Parametros: void
  * Retorno: void
  *********************************************/
  public Grafico(Computador transmissor) {
    this.transmissor = transmissor;
    this.setPrefSize(width, height);//Atribuindo tamanho
    Separator linha = new Separator();
    linha.setLayoutY(85);//Adicionando posicao Y
    linha.setOrientation(Orientation.HORIZONTAL);//Atribuindo a orientacao Horizontal
    linha.setPrefWidth(382);//Adicionando largura
    this.getChildren().add(linha);//Adicionando a este painel

    for (int i=0; i<7; i++) {
      Separator separador = new Separator();//Criando uma nova barra
      separador.setOrientation(Orientation.VERTICAL);//Atribuindo a orientacao Vertical
      separador.setPrefHeight(90);//Adicionando altura
      separador.setLayoutX(posLinha[i]);//Adicionando posicao X
      separador.setLayoutY(40);//Adicionando posicao Y
      this.getChildren().add(separador);//Adicionando a este painel
    }

    /*******************************************
    * Adicionar ComboBox Codificacoes
    ********************************************/
    this.codificacaoComboBox = new ComboBox<>();
    this.codificacaoComboBox.setPrefHeight(30);//Adicioando altura
    //Atribuir posicao (X,Y)
    this.codificacaoComboBox.setLayoutX(28);
    this.codificacaoComboBox.setLayoutY(5);
    this.codificacaoComboBox.getItems().add(Codificacao.CODIFICACAO_BINARIA);
    this.codificacaoComboBox.getItems().add(Codificacao.CODIFICACAO_MANCHESTER);
    this.codificacaoComboBox.getItems().add(Codificacao.CODIFICACAO_MANCHESTER_DIFERENCIAL);
    this.codificacaoComboBox.getSelectionModel().select(0);//Selecioando o primeiro item
    this.getChildren().add(this.codificacaoComboBox);//Adicionando ao painel

    /*******************************************
    * Adicionar Label de Velocidade
    ********************************************/
    this.velocidadeLabel = new Label("Velocidade");
    this.velocidadeLabel.setLayoutX(87);
    this.velocidadeLabel.setLayoutY(136);
    this.getChildren().add(velocidadeLabel);

    /*******************************************
    * Adicionar Slider Velocidade Trem 1
    ********************************************/
    this.velocidadeSlider = new Slider();
    this.velocidadeSlider.setPrefWidth(200);
    this.velocidadeSlider.setLayoutX(87);
    this.velocidadeSlider.setLayoutY(157);
    this.velocidadeSlider.setMin(50);//Valor minimo
    this.velocidadeSlider.setMax(100);//Valor maximo
    this.velocidadeSlider.setValue(75);//Valor inicial
    this.velocidadeSlider.setBlockIncrement(1);
    this.velocidadeSlider.setShowTickLabels(true);
    this.velocidadeSlider.setShowTickMarks(true);
    //this.getChildren().add(this.velocidadeSlider);//Adicionando o Slider ao painel

    this.velocidadeSlider.setVisible(false);
    this.velocidadeLabel.setVisible(false);

    //Funcao do Slider para alterar velocidade da Variavel
    this.velocidadeSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          int valor = newValue.intValue();
          velocidade = (1000 - (valor-50)*18);//Calculando novo valor da Velocidade
        }
    });

    this.codificacaoComboBox.setOnAction((e) -> {
      int index = codificacaoComboBox.getSelectionModel().getSelectedIndex();
      Painel.TIPO_CODIFICACAO_GRAFICO = index;//adicionando o index da codificacao selecionada
    });

    this.onda = new Onda();//Iniciando Onda
    this.onda.start();//Iniciando Thread
  }

  public enum Codificacao {
    CODIFICACAO_BINARIA, CODIFICACAO_MANCHESTER, CODIFICACAO_MANCHESTER_DIFERENCIAL
  }

  /*********************************************
  * Metodo: entradaDeBit
  * Funcao: Recebe um Bit e envia para gerar a Onda
  * Parametros: bit : int
  * Retorno: void
  *********************************************/
  public void entradaDeBit(int bit) {
    this.onda.entradaBit(bit, this.codificacaoSelecionada());
  }

  /*********************************************
  * Metodo: setTransmissor
  * Funcao: Atribui o Computador transmissor da Mensagem
  * Parametros: transmissor : Computador
  * Retorno: void
  *********************************************/
  // public void setTransmissor(Computador transmissor) {
  //   this.transmissor = transmissor;
  // }

  /*********************************************
  * Metodo: setVisibleCodificacao
  * Funcao: Deixa o ComboBox visivel ou invisivel
  * Parametros: visible : boolean
  * Retorno: void
  *********************************************/
  public void setVisibleCodificacao(boolean visible) {
    this.codificacaoComboBox.setVisible(visible);
  }

  /*********************************************
  * Metodo: codificacaoSelecionada
  * Funcao: Retorna a Codificacao Selecionada pelo ComboBox
  * Parametros: void
  * Retorno: Codificacao
  *********************************************/
  public Codificacao codificacaoSelecionada() {
    //return this.codificacaoComboBox.getSelectionModel().getSelectedItem();
    return this.codificacaoComboBox.getItems().get(Painel.TIPO_CODIFICACAO_GRAFICO);
  }

  /*********************************************
  * Metodo: setCodificacao
  * Funcao: Atribui uma codificacao no ComoBox
  * Parametros: condificacao : Codificacao
  * Retorno: void
  *********************************************/
  public void setCodificacao(Codificacao codificacao) {
    Platform.runLater(new Runnable(){
      @Override
      public void run() {
        codificacaoComboBox.getSelectionModel().select(codificacao);
      }
    });
  }

  /*********************************************
  * Metodo: setDisableCodificacao
  * Funcao: Ativa / Desativa o ComboBox de Codificacao
  * Parametros: void
  * Retorno: void
  *********************************************/
  public void setDisableCodificacao(Boolean disable) {
    this.codificacaoComboBox.setDisable(disable);//Ativa / Desativa o componente
  }

  /*********************************************
  * Metodo: setPosicao
  * Funcao: Definir a posicao desse componente na Interface
  * Parametros: posX : int, posY : int
  * Retorno: void
  *********************************************/
  public void setPosicao(int posX, int posY) {
    this.setLayoutX(posX);//Atribuindo posicao X
    this.setLayoutY(posY);//Atribuindo posicao Y
  }

  /*********************************************
  * Metodo: selecionarReceptor
  * Funcao: Retorna o computador Receptor, com base no Computador transmissor
  * Parametros: transmissor : Computador
  * Retorno: Computador
  *********************************************/
  private Computador selecionaReceptor(Computador transmissor) {
    if (transmissor == Painel.COMPUTADOR_TRANSMISSOR) {
      return Painel.COMPUTADOR_RECEPTOR;
    } else if (transmissor == Painel.COMPUTADOR_RECEPTOR) {
      return Painel.COMPUTADOR_TRANSMISSOR;
    }
    return null;
  }

  /*********************************************
  * Nome: Classe Onda
  * Funcao: Armazena um conjunto de Celulas pra fazer a Onda
  *********************************************/
  private class Onda extends Thread {
    private Celula[] celulas = new Celula[6];
    private Codificacao codificacao;//Tipo de Codificacao

    /*********************************************
    * Metodo: Onda - Construtor
    * Funcao: Constroi objeto da Classe Onda
    * Parametros: void
    * Retorno: void
    *********************************************/
    public Onda() {
      Celula celula1 = new Celula(10, 120);
      Celula celula2 = new Celula(70, 120);
      Celula celula3 = new Celula(130,120);
      Celula celula4 = new Celula(190,120);
      Celula celula5 = new Celula(250,120);
      Celula celula6 = new Celula(310,120);

      celula1.setCelulasVizinhas(null, celula2);
      celula2.setCelulasVizinhas(celula1, celula3);
      celula3.setCelulasVizinhas(celula2, celula4);
      celula4.setCelulasVizinhas(celula3, celula5);
      celula5.setCelulasVizinhas(celula4, celula6);
      celula6.setCelulasVizinhas(celula5, null);

      celulas[0] = celula1;
      celulas[1] = celula2;
      celulas[2] = celula3;
      celulas[3] = celula4;
      celulas[4] = celula5;
      celulas[5] = celula6;
    }

    /*********************************************
    * Metodo: entradaBit
    * Funcao: Recebe o Bit e a Codificacao
    * Parametros: void
    * Retorno: void
    *********************************************/
    public void entradaBit(int bit, Codificacao codificacao) {
      filaEntradaDeBits.add(bit);//Adicionando bit na Fila
      this.codificacao = codificacao;//Atribuindo a Codificacao Selecionada
    }

    public void run() {
      try {
        //Loop infinito
        while (true) {
          semaphoroInicio.acquire();//A thread do Grafico trava e aguarda a entrada de um novo Bit

          Computador receptor = selecionaReceptor(transmissor);

          int sinal1;//Sinal1 de bit 0 ou 1
          int sinaisEnviados = 1;
          int sinaisRecebidos = 1;

          while (!filaEntradaDeBits.isEmpty()) {//Executa enquanto a fila nao estiver vazia
            sinal1 = filaEntradaDeBits.poll();//Pegando o primeiro bit da Fila
            
            if (transmissor.tipo.equals("transmissor")) {
              celulas[5].atualizarSinal1();//Atualizando sinal da DIREITA para ESQUERDA
            } else {
              celulas[0].atualizarSinal2();//Atualizando sinal da ESQUERDA para DIREITA
            }

            transmissor.camadaFisica.addText(sinal1 + " ");
            sinaisEnviados++;

            if (codificacao == Codificacao.CODIFICACAO_BINARIA) {
              if (transmissor.tipo.equals("transmissor")) {
                celulas[0].adicionarSinal(sinal1);
              } else {
                celulas[5].adicionarSinal(sinal1);
              }
            } else {
              int sinal2;
              if (sinal1 != 0 && sinal1 != 1) {//Verificando se o sinal eh -1 (Fim da transmissao)
                sinal2 = sinal1;
              } else {
                sinal2 = filaEntradaDeBits.poll();
                transmissor.camadaFisica.addText(sinal2 + " ");
                sinaisEnviados++;
              }
              if (transmissor.tipo.equals("transmissor")) {
                celulas[0].adicionarSinal(sinal1,sinal2);
              } else {
                celulas[5].adicionarSinal(sinal1,sinal2);
              }
            }

            if (sinaisEnviados >= 27) {
              transmissor.camadaFisica.addText("\n");
              sinaisEnviados = 1;
            }

            for (int i=0; i<celulas.length; i++) {
              celulas[i].mudarCelula(this.codificacao);
              if (i == 5 && celulas[i].sinal1 != -1) {
                int bit = celulas[i].sinal1;
                receptor.camadaFisica.addText(bit + " ");//Mostra o Bit na Camada Fisica Receptora
                sinaisRecebidos++;
                if (codificacao != Codificacao.CODIFICACAO_BINARIA) {
                  bit = celulas[i].sinal2;
                  receptor.camadaFisica.addText(bit + " ");//Mostra o Bit na Camada Fisica Receptora
                  sinaisRecebidos++;
                }
              }
              if (sinaisRecebidos >= 27) {
                receptor.camadaFisica.addText("\n");
                sinaisRecebidos = 1;
              }
            }
            Thread.sleep(velocidade);//Velocidade da Onda
          }

          transmissor.camadaFisica.addText("\n\n");
          transmissor.terminouCamadas();

          for (int x=0; x<celulas.length; x++) {
            if (transmissor.tipo.equals("transmissor")) {
              celulas[5].atualizarSinal1();
              celulas[0].adicionarSinal(-1);
            } else {
              celulas[0].atualizarSinal2();
              celulas[5].adicionarSinal(-1);
            }
            for (int i=0; i<celulas.length; i++) {
              celulas[i].mudarCelula(this.codificacao);
              if (i == 5 && celulas[i].sinal1 != -1) {
                int bit = celulas[i].sinal1;
                receptor.camadaFisica.addText(bit + " ");//Mostra o Bit na Camada Fisica Receptora
                sinaisRecebidos++;
                if (codificacao != Codificacao.CODIFICACAO_BINARIA) {
                  bit = celulas[i].sinal2;
                  receptor.camadaFisica.addText(bit + " ");//Mostra o Bit na Camada Fisica Receptora
                  sinaisRecebidos++;
                }
                if (sinaisRecebidos >= 27) {
                  receptor.camadaFisica.addText("\n");
                  sinaisRecebidos = 1;
                }
              }
            }
            Thread.sleep(velocidade);
          }
          //Libera a Thread das Camadas quando todos os Bits ja passaram pelo Grafico
          semaphoroFim.release();
        }//Fim do while

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }//Fim do Run

  }//Fim class Onda

  /*********************************************
  * Nome: Classe Celula
  * Funcao: Armazena um conjunto de Linhas pra fazer uma Celula
  *********************************************/
  private class Celula {
    private Celula celulaProxima;//Referencia da Proxima Celula
    private Celula celulaAnterior;//Referencia da Celula Anterior
    protected int sinal1;//Sinal 1 da Celula
    protected int sinal2;//Sinal 2 da Celula

    private ImageView[] linhas = new ImageView[5];//Linhas que compoe uma celula de Onda
    private Imagem allImage = new Imagem();//Objeto que possui todas as imagens

    private int larguraPonto = 5;//Largura da Imagem como Ponto
    private int larguraLinha = 25;//Largura da Imagem como Linha
    private int alturaPonto = 5;//Altura da Imagem como Ponto
    private int alturaLinha = 80;//Altura da Imagem como Linha

    private int posYAlto = 45;//Posicao Alta [Y]
    private int posYBaixo = 120;//Posicao Baixa [Y]

    /*********************************************
    * Metodo: Celula - Construtor
    * Funcao: Constroi objetos da Classe Celula
    * Parametros: posX : int, posY : int
    * Retorno: void
    *********************************************/
    public Celula(int posX, int posY) {
      this.sinal1 = -1;//Valor para representar "Sem sinal"
      this.sinal2 = -1;//Valor para representar "Sem sinal" 

      this.linhas[0] = new ImageView();
      this.allImage.trocarImagem(this.linhas[0], "pulso");
      this.linhas[0].setFitWidth(larguraPonto);
      this.linhas[0].setFitHeight(alturaPonto);
      this.linhas[0].setLayoutX(posX);
      this.linhas[0].setLayoutY(posY);
      getChildren().add(this.linhas[0]);

      this.linhas[1] = new ImageView();
      this.allImage.trocarImagem(this.linhas[1], "pulso");
      this.linhas[1].setFitWidth(larguraLinha);
      this.linhas[1].setFitHeight(alturaPonto);
      this.linhas[1].setLayoutX(posX+5);
      this.linhas[1].setLayoutY(posY);
      getChildren().add(this.linhas[1]);

      this.linhas[2] = new ImageView();
      this.allImage.trocarImagem(this.linhas[2], "pulso");
      this.linhas[2].setFitWidth(larguraPonto);
      this.linhas[2].setFitHeight(alturaPonto);
      this.linhas[2].setLayoutX(posX+30);
      this.linhas[2].setLayoutY(posY);
      getChildren().add(this.linhas[2]);
      
      this.linhas[3] = new ImageView();
      this.allImage.trocarImagem(this.linhas[3], "pulso");
      this.linhas[3].setFitWidth(larguraLinha);
      this.linhas[3].setFitHeight(alturaPonto);
      this.linhas[3].setLayoutX(posX+35);
      this.linhas[3].setLayoutY(posY);
      getChildren().add(this.linhas[3]);

      this.linhas[4] = new ImageView();
      this.allImage.trocarImagem(this.linhas[4], "pulso");
      this.linhas[4].setFitWidth(larguraPonto);
      this.linhas[4].setFitHeight(alturaPonto);
      this.linhas[4].setLayoutX(posX+60);
      this.linhas[4].setLayoutY(posY);
      getChildren().add(this.linhas[4]);
    }

    /*********************************************
    * Metodo: mudarCelula
    * Funcao: Altera a forma da Onda da celula de Acordo com a Codificacao e os Sinais 1 e 2
    * Parametros: codificacao : Codificacao
    * Retorno: void
    *********************************************/
    public void mudarCelula(Codificacao codificacao) {

      if (sinal1 == 0 && sinal2 == 1) {//CASO O SINAL FOR [01]

        if (celulaAnterior != null) {
          if (celulaAnterior.sinal1 == 1) {
            this.pontoAlto(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == 0) {
            this.barraAlta(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == -1) {
            this.barraAlta(this.linhas[0]);
          }
        } else {
          this.barraAlta(this.linhas[0]);
        }

        this.linhaAlta(this.linhas[1]);
        this.barraAlta(this.linhas[2]);
        this.linhaBaixa(this.linhas[3]);

        if (celulaProxima != null) {
          if (celulaProxima.sinal2 == 1) {
            this.barraAlta(this.linhas[4]);
          } else if (celulaProxima.sinal2 == 0) {
            this.pontoBaixo(this.linhas[4]);
          } else if (celulaProxima.sinal2 == -1) {
            this.pontoBaixo(this.linhas[4]);
          }
        } else {
          this.pontoBaixo(this.linhas[4]);
        }

      } else if (sinal1 == 1 && sinal2 == 0) {//CASO O SINAL FOR [10]

        if (celulaAnterior != null) {
          if (celulaAnterior.sinal1 == 1) {
            this.barraAlta(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == 0) {
            this.pontoBaixo(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == -1) {
            this.pontoBaixo(this.linhas[0]);
          }
        } else {
          this.pontoBaixo(this.linhas[0]);
        }

        this.linhaBaixa(this.linhas[1]);
        this.barraAlta(this.linhas[2]);
        this.linhaAlta(this.linhas[3]);

        if (celulaProxima != null) {
          if (celulaProxima.sinal2 == 1) {
            this.pontoAlto(this.linhas[4]);
          } else if (celulaProxima.sinal2 == 0) {
            this.barraAlta(this.linhas[4]);
          } else if (celulaProxima.sinal2 == -1) {
            this.barraAlta(this.linhas[4]);
          }
        } else {
          this.barraAlta(this.linhas[4]);
        }

      } else if (sinal1 == 1 && sinal2 == 1) {//CASO O SINAL FOR [11]
        
        if (celulaAnterior != null) {
          if (celulaAnterior.sinal1 == 1) {
            this.pontoAlto(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == 0 || celulaAnterior.sinal1 == -1) {
            this.barraAlta(this.linhas[0]);
          }
        } else {
          this.barraAlta(this.linhas[0]);
        }

        this.linhaAlta(this.linhas[1]);
        this.pontoAlto(this.linhas[2]);
        this.linhaAlta(this.linhas[3]);

        if (celulaProxima != null) {
          if (celulaProxima.sinal2 == 1) {
            this.pontoAlto(this.linhas[4]);
          } else if (celulaProxima.sinal2 == 0 || celulaProxima.sinal2 == -1) {
            this.barraAlta(this.linhas[4]);
          }
        } else {
          this.barraAlta(this.linhas[4]);
        }
      
      } else if (sinal1 == 0 && sinal2 == 0) {//CASO O SINAL FOR [00]
        
        if (celulaAnterior != null) {
          if (celulaAnterior.sinal1 == 1) {
            this.barraAlta(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == 0 || celulaAnterior.sinal1 == -1) {
            this.pontoBaixo(this.linhas[0]);
          }
        } else {
          this.pontoBaixo(this.linhas[0]);
        }

        this.linhaBaixa(this.linhas[1]);
        this.pontoBaixo(this.linhas[2]);
        this.linhaBaixa(this.linhas[3]);

        if (celulaProxima != null) {
          if (celulaProxima.sinal2 == 1) {
            this.barraAlta(this.linhas[4]);
          } else if (celulaProxima.sinal2 == 0 || celulaProxima.sinal2 == -1) {
            this.pontoBaixo(this.linhas[4]);
          }
        } else {
          this.pontoBaixo(this.linhas[4]);
        }

      } else {//CASO O SINAL FOR [-1]

        if (celulaAnterior != null) {
          if (celulaAnterior.sinal1 == 1) {
            this.barraAlta(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == 0) {
            this.pontoBaixo(this.linhas[0]);
          } else if (celulaAnterior.sinal1 == -1) {
            this.pontoBaixo(this.linhas[0]);
          }
        } else {
          this.pontoBaixo(this.linhas[0]);
        }

        this.linhaBaixa(this.linhas[1]);
        this.pontoBaixo(this.linhas[2]);
        this.linhaBaixa(this.linhas[3]);

        if (celulaProxima != null) {
          if (celulaProxima.sinal2 == 1) {
            this.barraAlta(this.linhas[4]);
          } else if (celulaProxima.sinal2 == 0) {
            this.pontoBaixo(this.linhas[4]);
          } else if (celulaProxima.sinal2 == -1) {
            this.pontoBaixo(this.linhas[4]);
          }
        } else {
          this.pontoBaixo(this.linhas[4]);
        }

      }

    }//Fim mudarCelula

    /*********************************************
    * Metodo: atualizarSinal1
    * Funcao: Passando sinal1 de uma celula para outra (Recurssao)
    * Parametros: void
    * Retorno: void
    *********************************************/
    public void atualizarSinal1() {
      if (celulaAnterior != null) {
        this.sinal1 = celulaAnterior.sinal1;
        this.sinal2 = celulaAnterior.sinal2;
        celulaAnterior.atualizarSinal1();
      }
    }

    /*********************************************
    * Metodo: atualizarSinal2
    * Funcao: Atualiza o sinal de cada celula Recussivamente
    * Parametros: void
    * Retorno: void
    *********************************************/
    public void atualizarSinal2() {
      if (celulaProxima != null) {
        this.sinal1 = celulaProxima.sinal1;
        this.sinal2 = celulaProxima.sinal2;
        celulaProxima.atualizarSinal2();
      }
    }

    /*********************************************
    * Metodo: adicionarSinal
    * Funcao: Adiciona um sinal
    * Parametros: sinal1 : int
    * Retorno: void
    *********************************************/
    public void adicionarSinal(int sinal) {
      this.sinal1 = sinal;
      this.sinal2 = sinal;
    }

    /*********************************************
    * Metodo: adicionarSinal
    * Funcao: Adiciona um sinal
    * Parametros: sinal1 : int
    * Retorno: void
    *********************************************/
    public void adicionarSinal(int sinal1, int sinal2) {
      this.sinal1 = sinal1;
      this.sinal2 = sinal2;
    }

    /*********************************************
    * Metodo: barraAlta
    * Funcao: Altera uma ImageView para uma posicao Barra Alta
    * Parametros: imagem : ImageView
    * Retorno: void
    *********************************************/
    private void barraAlta(ImageView imagem) {
      Platform.runLater(new Runnable(){
       @Override
       public void run() {
          imagem.setFitWidth(larguraPonto);
          imagem.setFitHeight(alturaLinha);
          imagem.setLayoutY(posYAlto);
       }
      });
    }

    /*********************************************
    * Metodo: pontoAlto
    * Funcao: Altera uma ImageView para uma posicao Ponto Alto
    * Parametros: imagem : ImageView
    * Retorno: void
    *********************************************/
    private void pontoAlto(ImageView imagem) {
      Platform.runLater(new Runnable(){
       @Override
       public void run() {
          imagem.setFitWidth(larguraPonto);
          imagem.setFitHeight(alturaPonto);
          imagem.setLayoutY(posYAlto);
       }
      });
    }

    /*********************************************
    * Metodo: pontoBaixo
    * Funcao: Altera uma ImageView para uma posicao Ponto Baixo
    * Parametros: imagem : ImageView
    * Retorno: void
    *********************************************/
    private void pontoBaixo(ImageView imagem) {
      Platform.runLater(new Runnable(){
       @Override
       public void run() {
          imagem.setFitWidth(larguraPonto);
          imagem.setFitHeight(alturaPonto);
          imagem.setLayoutY(posYBaixo);
       }
      });
    }

    /*********************************************
    * Metodo: linhaAlta
    * Funcao: Altera uma ImageView para uma posicao Linha Alta
    * Parametros: imagem : ImageView
    * Retorno: void
    *********************************************/
    private void linhaAlta(ImageView imagem) {
      Platform.runLater(new Runnable(){
       @Override
       public void run() {
          imagem.setFitWidth(larguraLinha);
          imagem.setFitHeight(alturaPonto);
          imagem.setLayoutY(posYAlto);
       }
      });
    }

    /*********************************************
    * Metodo: linhaBaixa
    * Funcao: Altera uma ImageView para uma posicao Linha Baixa
    * Parametros: imagem : ImageView
    * Retorno: void
    *********************************************/
    private void linhaBaixa(ImageView imagem) {
      Platform.runLater(new Runnable(){
       @Override
       public void run() {
          imagem.setFitWidth(larguraLinha);
          imagem.setFitHeight(alturaPonto);
          imagem.setLayoutY(posYBaixo);
       }
      });
    }

    /*********************************************
    * Metodo: setCelulasVizinhas
    * Funcao: Atribui as celulas vizinhas de uma Celula
    * Parametros: anterior : Celula, proximo : Celula
    * Retorno: void
    *********************************************/
    public void setCelulasVizinhas(Celula anterior, Celula proxima) {
      this.celulaAnterior = anterior;
      this.celulaProxima = proxima;
    }
  }//Fim class Celula

}//Fim class