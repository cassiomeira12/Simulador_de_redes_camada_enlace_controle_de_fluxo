/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 01/02/18
* Ultima alteracao: 17/02/18
* Nome: AplicacaoReceptora
* Funcao: Receber o envio de uma Mensagem da Camda de Aplicacao Receptora
***********************************************************************/

package model;

import model.camadas.*;
import view.Painel;
import view.componentes.Computador;


public class AplicacaoReceptora {
  private Computador computador;//Referencia do Computador que possui essa classe
  public static final int VELOCIDADE = 150;//Velocidade de Sleep das Camadas Receptoras
  public CamadaAplicacaoReceptora camadaAplicacao;//Camada de Aplicacao Receptora
  public CamadaEnlaceDadosReceptora camadaEnlace;//Camada de Enlace Receptora
  public CamadaFisicaReceptora camadaFisica;//Camada Fisica Receptora


  /*********************************************
  * Metodo: AplicacaoReceptora
  * Funcao: Cria objetos da classe AplicacaoReceptora
  * Parametros: computador : Computador
  *********************************************/
  public AplicacaoReceptora(Computador computador) {
    this.computador = computador;
  }


  /*********************************************
  * Metodo: aplicacaoReceptora
  * Funcao: Envia uma String (Mensagem) para a Interface Grafica
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void aplicacaoReceptora(String mensagem) {
    System.out.println("\nAPLICACAO RECEPTORA");
    try {
      chamarProximaCamada(mensagem);
    } catch (Exception e) {
      System.out.println("[ERRO] - Aplicacao Receptora");
      e.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: chamarProximaCamada
  * Funcao: Passa os dados a serem enviados dessa Camada para a proxima
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  private void chamarProximaCamada(String mensagem) throws Exception {
    computador.receberMensagem(mensagem);
  }

}//Fim class