/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 20/03/18
* Ultima alteracao: 20/05/17
* Nome: PainelTemporizador
* Funcao: Adiciona os Temporizadores na interface grafica
***********************************************************************/

package view.componentes;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.application.Platform;
import model.camadas.Temporizador;


public class PainelTemporizador extends ScrollPane {

  private FlowPane painel;//Painel onde adiciona os temporizadores

  /*********************************************
  * Metodo: PainelTemporizador - Construtor
  * Funcao: Constroi objetos da classe PainelTemporizador
  * Parametros: void
  * Retorno: void
  *********************************************/
  public PainelTemporizador(int posX, int posY) {
    this.painel = new FlowPane();
    this.painel.setPrefSize(300,200);
    this.setPrefSize(330,202);
    this.setLayoutX(posX);
    this.setLayoutY(posY);
    this.setContent(painel);
    this.setVisible(false);
  }

  /*********************************************
  * Metodo: adicionarTemporizador
  * Funcao: Adiciona um Temporizador na interface grafica
  * Parametros: temporizador : Temporizador
  * Retorno: void
  *********************************************/
  public void adicionarTemporizador(Temporizador temporizador) throws Exception {
    Platform.runLater(new Runnable(){
      @Override
      public void run() {
        setVisible(true);
        painel.getChildren().add(temporizador);
      }
    });
  }

  /*********************************************
  * Metodo: removerTemporizador
  * Funcao: Remove o Temporizador da interface grafica
  * Parametros: idTemporizador : int
  * Retorno: void
  *********************************************/
  public void removerTemporizador(int idTemporizador, Computador computador) {
    try {
      Platform.runLater(new Runnable(){
        @Override
        public void run() {
          Temporizador temporizador = null;
          for (int i=0; i<painel.getChildren().size(); i++) {
            temporizador = (Temporizador) painel.getChildren().get(i);
            if (idTemporizador == temporizador.getIdTemporizador() && temporizador.getComputador() == computador) {
              painel.getChildren().remove(temporizador);
              temporizador.finalizar();
              break;
            }
          }
          if (painel.getChildren().isEmpty()) {
            setVisible(false);
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: getTemporizador
  * Funcao: Retorna um Temporizador
  * Parametros: idTemporizador : int
  * Retorno: Temporizador
  *********************************************/
  public Temporizador getTemporizador(int idTemporizador) {
    Temporizador temporizador = null;
    for (int i=0; i<painel.getChildren().size(); i++) {
      temporizador = (Temporizador) painel.getChildren().get(i);
      if (idTemporizador == temporizador.getIdTemporizador()/*&& temporizador.getComputador() == computador*/) {
        return temporizador;
      }
    }
    return null;
  }

}//Fim class