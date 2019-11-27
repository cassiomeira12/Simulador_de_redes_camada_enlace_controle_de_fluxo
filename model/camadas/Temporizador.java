/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 17/03/18
* Ultima alteracao: 27/03/18
* Nome: Temporizador
* Funcao: Cria o temporizador para reenviar a mensagem para o receptor
***********************************************************************/

package model.camadas;

import img.Imagem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.ImageView;
import view.componentes.Computador;
import model.MeioDeComunicacao;

public class Temporizador extends AnchorPane {
  private int id;//Identificador do Temporizador
  private Quadro quadro;//Dados do Quadro
  private int tempo;//Tempo contador do Temporizador

  private Computador computador;//Computador que criou o Temporizador

  private Imagem allImage = new Imagem();
  private Relogio relogio;//Relogio que conta o temporizador

  private ImageView relogioImagem;//Imagem do Relogio
  private ImageView ponteiroTempo;//Imagem do Ponteiro de Tempo
  private ImageView ponteiroTentativas;//Imagem do Ponteiro de Tentativas

  private int[] angulos = {0,45,90,135,180,225,270,315,360};//Angulos dos Ponteiros

  /*********************************************
  * Metodo: Temporizador - Construtor
  * Funcao: Criar objetos da Classe Temporizador
  * Parametros: quadro : Quadro
  *********************************************/
  public Temporizador(Quadro quadro, Computador computador) {
    this.id = quadro.getId();
    this.quadro = quadro;
    this.computador = computador;

    this.relogioImagem = allImage.getImageView("temporizador_" + computador.tipo);
    this.relogioImagem.setFitWidth(50);
    this.relogioImagem.setPreserveRatio(true);

    this.ponteiroTempo = allImage.getImageView("ponteiro_tempo");
    this.ponteiroTempo.setFitWidth(10);
    this.ponteiroTempo.setPreserveRatio(true);
    this.allImage.moverImagem(ponteiroTempo,20,8,0);

    this.ponteiroTentativas = allImage.getImageView("ponteiro_tentativas");
    this.ponteiroTentativas.setFitWidth(10);
    this.ponteiroTentativas.setPreserveRatio(true);
    this.allImage.moverImagem(ponteiroTentativas,20,12,0);

    this.relogio = new Relogio();
    this.relogio.start();

    this.getChildren().addAll(relogioImagem,ponteiroTempo,ponteiroTentativas);
  }

  /*********************************************
  * Metodo: getId
  * Funcao: Retorna o ID do temporizador
  * Parametros: void
  * Retorno: id : int
  *********************************************/
  public int getIdTemporizador() {
    return id;
  }

  /*********************************************
  * Metodo: getBits
  * Funcao: Retorna os Bits do Quadro que compoe esse Temporizador
  * Parametros: void
  * Retorno: int[] quadros
  *********************************************/
  public int[] getBits() {
    return quadro.getBits();
  }

  /*********************************************
  * Metodo: finalizar
  * Funcao: Para a thread do relogio
  * Parametros: void
  * Retorno: void
  *********************************************/
  public void finalizar() {
    this.relogio.stop();
  }

  /*********************************************
  * Metodo: getComputador
  * Funcao: Retorna o computador que criou esse Temporizador
  * Parametros: void
  * Retorno: computador : Computador
  *********************************************/
  public Computador getComputador() {
    return this.computador;
  }

  /*********************************************
  * Metodo: enviarQuadro
  * Funcao: Envia o Quadro para a Camada Fisica
  * Parametros: void
  * Retorno: void
  *********************************************/
  public void enviarQuadro() {
    try {
      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      this.computador.camadaEnlace.addText("\n\tENVIANDO QUADRO " + id);
      this.computador.aplicacaoTransmissora.camadaEnlace.chamarProximaCamada(quadro.getBits());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /*********************************************
  * Nome: Relogio
  * Funcao: Criar a Thread relogio do Temporizador
  *********************************************/
  public class Relogio extends Thread {
    private int tentativas = 0;//Numeor de tentativas do Relogio
    private int tempo = 8;//Tempo de sleep do Relogio
    private int i=0;

    public void run() {
      try {

        while (true) {
          if (tempo == 0) {
            tempo = 8;
            allImage.moverImagem(ponteiroTentativas,20,12,angulos[tentativas+1]);
            tentativas++;
            enviarQuadro();//Reenviando o Quadro
            if (tentativas == angulos.length) {
              tentativas = 0;
            }
          }
          allImage.moverImagem(ponteiroTempo,20,8,angulos[i]);
          if (i == (angulos.length-1)) {
            i=0;
          }
          Thread.sleep(5000);
          tempo--;
          i++;
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }//FIm class Relegio

}//Fim class