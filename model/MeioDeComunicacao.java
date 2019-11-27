/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 01/02/18
* Ultima alteracao: 20/05/18
* Nome: MeioDeComunicacao
* Funcao: Receber os Bits da Camada de Fisica Transmissora e enviar
          para a Camada Fisica Receptora
***********************************************************************/

package model;

import view.Painel;
import model.camadas.*;
import view.componentes.Computador;
import util.ManipuladorDeBit;
import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ArrayList;


public class MeioDeComunicacao {
  private Computador transmissor;//Computador transmissor
  private Computador receptor;//Computtador receptor
  
  public static Semaphore SEMAPHORO_FLUXO;
  public static Semaphore SEMAPHORO_QUADRO;

  public static int PERCENTUAL_ERRO = 0;

  /*********************************************
  * Metodo: MeioDeComunicacao - Construtor
  * Funcao: Cria objetos da classe MeioDeConstrutor
  * Parametros: transmissor : Computador, receptor : Computador
  *********************************************/
  public MeioDeComunicacao(Computador transmissor, Computador receptor) {
    inicarTransmissao();
    this.transmissor = transmissor;
    this.receptor = receptor;
  }

  /*********************************************
  * Metodo: iniciarTransmissao
  * Funcao: Reiniciar os Sempahoros para uma nota transmissao
  * Parametros: fluxoBrutoDeBits : int[]
  * Retorno: void
  *********************************************/
  public static void inicarTransmissao() {
    SEMAPHORO_FLUXO = new Semaphore(1);
    SEMAPHORO_QUADRO = new Semaphore(1);
  }

  /*********************************************
  * Metodo: meioDeComunicacao
  * Funcao: Simular um meio por onde os Bits passam ate chegar na proxima camada
  * Parametros: fluxoBrutoDeBits : int[]
  * Retorno: void
  *********************************************/
  public void meioDeComunicacao(Computador transmissor, int... fluxoBrutoDeBits) {
    System.out.println("\nINICIO DO MEIO DE COMUNICACAO\n");
    try {

      //Recebe o computador Receptor com base no Transmissor
      Computador receptor = selecionaReceptor(transmissor);

      //Painel.GRAFICO.setTransmissor(transmissor);//Atribui o Transmissor no GRAFICO

      int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
      int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBits.length];

      boolean camadaFisicaVIOLADA = CamadaFisicaTransmissora.VIOLADA;

      //System.out.println("ENVIANDO BITS:\n");
      //ENVIANDO OS BITs PARA O GRAFICO
      for (int indicePosicao=0; indicePosicao<fluxoBrutoDeBitsPontoA.length; indicePosicao++) {//Percorrendo o vetor de Inteiros
        int numero = fluxoBrutoDeBitsPontoA[indicePosicao];//Numero com os Bits
        int numeroDeBits = Integer.toBinaryString(numero).length();//Quantidade de Bits que o inteiro possui

        numeroDeBits = ManipuladorDeBit.quantidadeDeBits(numero);

        //numero <<= (32-numeroDeBits);//Deslocando um valor de Bits para a esquerda
        numero = ManipuladorDeBit.deslocarBits(numero);

        //VIOLANDO A CAMADA FISICA, ADICIONANDO BITS 11 DE INICIO DO QUADRO
        if (camadaFisicaVIOLADA) {
          camadaFisicaVIOLADA = false;
          //Painel.GRAFICO.entradaDeBit(1);//ENVIANDO BIT 1
          //Painel.GRAFICO.entradaDeBit(1);//ENVIANDO BIT 1
          transmissor.getGrafico().entradaDeBit(1);//ENVIANDO BIT 1
          transmissor.getGrafico().entradaDeBit(1);//ENVIANDO BIT 1
        }

        //Inteiro com todos os bits 0s
        int novoInteiro = 0;//00000000 00000000 00000000 00000000
        //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
        int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
        //Percorrendo os 32 bits do numero
        for (int i=1; i<=numeroDeBits; i++) {
          int bit = (numero & displayMask) == 0 ? 0 : 1;//Pegando bit do numero

          Random numeroRandomico = new Random();
          int erro = numeroRandomico.nextInt(200 + 1);

          if (erro < PERCENTUAL_ERRO) {
            bit = bit == 1 ? 0 : 1;
            System.out.println("DEU ERRO");
          }

          /***************************************************************/
          //Painel.GRAFICO.entradaDeBit(bit);//ENVIANDO O BIT PARA O GRAFICO
          transmissor.getGrafico().entradaDeBit(bit);//ENVIANDO O BIT PARA O GRAFICO
          /***************************************************************/

          novoInteiro <<= 1;//Desloca 1 Bit para a esquerda
          novoInteiro = novoInteiro | bit;//Adicionando novo Bit ao Inteiro
          numero <<= 1;//Desloca 1 Bit para a esquerda

          //Terminou de adicionar os bits no novo Inteiro
          if (i == numeroDeBits) {
            fluxoBrutoDeBitsPontoB[indicePosicao] = novoInteiro;//Adicionando no vetor
          }
        }

        //VIOLANDO A CAMADA FISICA, ADICIONANDO BITS 11 DE FINAL DO QUADRO
        if (CamadaFisicaTransmissora.VIOLADA && indicePosicao == (fluxoBrutoDeBitsPontoA.length-1)) {
          //Painel.GRAFICO.entradaDeBit(1);//ENVIANDO BIT 1
          //Painel.GRAFICO.entradaDeBit(1);//ENVIANDO BIT 1
          transmissor.getGrafico().entradaDeBit(1);//ENVIANDO BIT 1
          transmissor.getGrafico().entradaDeBit(1);//ENVIANDO BIT 1
        }

      }
      //TERMINOU DE ENVIAR

      transmissor.camadaFisica.addText("\nEnviando os Bits\n");
      receptor.camadaFisica.addText("Recebendo Bits\n");
      //Libera o Grafico para mostrar os Bits passando
      //Painel.GRAFICO.semaphoroInicio.release();
      transmissor.getGrafico().semaphoroInicio.release();
      
      //Painel.GRAFICO.semaphoroFim.acquire();//Aguarda o fim da transmissao para o computador Receptor
      transmissor.getGrafico().semaphoroFim.acquire();
      transmissor.SEMAPHORO_TRANSMITIR.release();//Libera o transmissor para enviar o proximo quadro


      System.out.println("\n\nFIM DO MEIO DE COMUNICACAO\n");

      receptor.aplicacaoReceptora.camadaFisica = new CamadaFisicaReceptora(receptor);
      receptor.aplicacaoReceptora.camadaFisica.camadaFisicaReceptora(fluxoBrutoDeBitsPontoB);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: selecionarReceptor
  * Funcao: Retorna o Computador Receptor com base no Transmissor
  * Parametros: transmissor : Computador
  * Retorno: receptor : Computador
  *********************************************/
  private Computador selecionaReceptor(Computador transmissor) {
    if (transmissor == this.transmissor) {
      return this.receptor;
    } else if (transmissor == this.receptor) {
      return this.transmissor;
    }
    return null;
  }

}//Fim class