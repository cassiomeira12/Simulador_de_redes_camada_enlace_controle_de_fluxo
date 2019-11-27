/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 01/02/18
* Ultima alteracao: 13/03/18
* Nome: AplicacaoTransmissora
* Funcao: Iniciar o envio de uma Mensagem para a Camda de Aplicacao Transmissora
***********************************************************************/

package model;

import model.camadas.*;
import view.*;
import view.componentes.Computador;


public class AplicacaoTransmissora {
  private Computador computador;//Referencia do Computador que possui essa classe
  public static final int VELOCIDADE = 150;//Velocidade de Sleep das Camadas Transmissora
  public CamadaAplicacaoTransmissora camadaAplicacao;//Camada de Aplicacao Transmissor
  public CamadaEnlaceDadosTransmissora camadaEnlace;//Camada de Enlace Transmissora
  public CamadaFisicaTransmissora camadaFisica;//Camada Fisica Transmissora


  /*********************************************
  * Metodo: AplicacaoTransmissora
  * Funcao: Cria objetos da classe AplicacaoTransmissora
  * Parametros: computador : Computador
  *********************************************/
  public AplicacaoTransmissora(Computador computador) {
    this.computador = computador;
  }

  /*********************************************
  * Metodo: aplicacaoTransmissora
  * Funcao: Recebe uma String da Interface Grafica e envia para a Camada de Aplicacao Transmissora
  * Parametros: mensagem : String
  * Retorno: void
  *********************************************/
  public void aplicacaoTransmissora(String mensagem) {
    System.out.println("\n\n-----------------------------------------------------");
    System.out.println("APLICACAO TRANSMISSORA");
    try {
      chamarProximaCamada(mensagem);
    } catch (Exception e) {
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
    this.camadaAplicacao = new CamadaAplicacaoTransmissora(computador);
    this.camadaAplicacao.camadaAplicacaoTransmissora(mensagem);
  }

}//Fim class