/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 06/03/18
* Ultima alteracao: 20/05/18
* Nome: CamadaEnlaceDadosReceptora
* Funcao: Receber um Quadro, processar e enviar para a proxima Camada
***********************************************************************/

package model.camadas;

import view.componentes.Computador;
import view.Painel;
import java.util.List;
import java.util.concurrent.Semaphore;
import model.camadas.Quadro;
import util.ManipuladorDeBit;
import model.*;


public class CamadaEnlaceDadosReceptora extends Thread {
  private Computador computador;
  private final String nomeDaCamada = "CAMADA ENLACE DE DADOS RECEPTORA";
  private int[] quadro;//Informacao recebida da Camada Superior
  private Semaphore semaphoro_JDeslizandeSeletiva;

  /*********************************************
  * Metodo: CamadaEnlaceDadosReceptora
  * Funcao: Cria objetos da Classe CamadaEnalceReceptora
  * Parametros: computador : Computador
  *********************************************/
  public CamadaEnlaceDadosReceptora(Computador computador) {
    this.computador = computador;
    this.semaphoro_JDeslizandeSeletiva = new Semaphore(0);
  }


  public void run() {
    System.out.println(nomeDaCamada);
    try {

      camadaEnlaceReceptoraControleDeFluxo(quadro);
      quadro = camadaEnlaceReceptoraControleDeErro(quadro);
      quadro = camadaEnlaceReceptoraEnquadramento(quadro);

      chamarProximaCamada(quadro);

      int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      if (tipoDeFluxo == 2) {
        this.semaphoro_JDeslizandeSeletiva.release();
      }
      
      Painel.CONFIGURACOES.setDisabilitar(false);
      Painel.CONFIGURACOES.setDesabilitarSlider(false);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: camadaEnlaceDadosReceptora
  * Funcao: Recebe a informacao da Camada Fisica e inicia o processamento desta Camada
  * Parametros: quadro : int[]
  * Retorno: void
  *********************************************/
  public void camadaEnlaceDadosReceptora(int[] quadro) {
    this.quadro = quadro;
    this.start();//Iniciando a Thread dessa Camada
  }

  /*********************************************
  * Metodo: chamarProximaCamada
  * Funcao: Passa os dados a serem enviados dessa Camada para a proxima
  * Parametros: quadro : int[]
  * Retorno: void
  *********************************************/
  private void chamarProximaCamada(int... quadro) throws Exception {
    this.computador.aplicacaoReceptora.camadaAplicacao = new CamadaAplicacaoReceptora(computador);
    this.computador.aplicacaoReceptora.camadaAplicacao.camadaAplicacaoReceptora(quadro);
  }
  
  /*********************************************
  * Metodo: camadaEnlaceReceptorEnquadramento
  * Funcao: Divide o Quadro recebido em informacoes de Carga Util
  * Parametros: quadro : int[]
  * Retorno: quadroDesenquadrado : int[]
  *********************************************/
  private int[] camadaEnlaceReceptoraEnquadramento(int[] quadro) {
    System.out.println("\tENQUADRAMENTO");
    this.computador.camadaEnlace.addText("ENQUADRAMENTO\n");
    
    int[] quadroDesenquadrado = null;
    
    try {
      Thread.sleep(AplicacaoReceptora.VELOCIDADE);

      int tipoDeEnquadramento = Painel.CONFIGURACOES.enquadramento.getIndiceSelecionado();
    
      switch(tipoDeEnquadramento) {
        case 0:
          quadroDesenquadrado = enquadramentoContagemDeCaracteres(quadro);
          break;
        case 1:
          quadroDesenquadrado = enquadramentoInsercaoDeBytes(quadro);
          break;
        case 2:
          quadroDesenquadrado = enquadramentoInsercaoDeBits(quadro);
          break;
        case 3:
          quadroDesenquadrado = enquadramentoViolacaoCamadaFisica(quadro);
          break;
      }

    } catch (Exception e) {
      System.out.println("Quandro com ERRO");
      this.stop();
    }

    this.computador.camadaEnlace.addText("\n");
    return quadroDesenquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoContagemDeCaracteres
  * Funcao: Dividir a mensagem em quadros levando em consideracao o espaco entre as palavras (" ")
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : int[]
  *********************************************/
  private int[] enquadramentoContagemDeCaracteres(int[] quadro) throws Exception {
    System.out.println("\n\t[Contagem de Caracteres]");
    this.computador.camadaEnlace.addText("\n\t[Contagem de Caracteres]\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    int informacaoDeControle = ManipuladorDeBit.getPrimeiroByte(quadro[0]);//Quantidade de Bits do quadro
    //Quantidade de Bits de carga util do quadro
    int quantidadeDeBitsCargaUtil = informacaoDeControle;
    System.out.println("IC: " + informacaoDeControle);
    this.computador.camadaEnlace.addText("\n\tIC ["+informacaoDeControle+"] ");


    int novoTamanho = quantidadeDeBitsCargaUtil/8;

    int[] quadroDesenquadrado = new int[novoTamanho];//Novo vetor de Carga Util
    int posQuadro = 0;//Posicao do Vetor de Quadros

    int cargaUtil = 0;//Nova Carga Util

    quadro[0] = ManipuladorDeBit.deslocarBits(quadro[0]);//Deslocando os bits 0's a esquerda
    //Primeiro inteiro do Quadro - Contem a informacao de Controle IC nos primeiros 8 bits
    quadro[0] <<= 8;//Deslocando 8 bits para a esquerda, descartar a IC

    this.computador.camadaEnlace.addText("Carga Util [ ");
    for (int i=1; (i<=3) && (i<=novoTamanho); i++) {
      cargaUtil = ManipuladorDeBit.getPrimeiroByte(quadro[0]);
      quadroDesenquadrado[posQuadro++] = cargaUtil;
      this.computador.camadaEnlace.addText(cargaUtil + " ");
      quadro[0] <<= 8;//Desloca 8 bits para a esquerda
    }

    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    //Caso o quadro for composto por mais de um inteiro do vetor
    for (int i=1, quantidadeByte; posQuadro<novoTamanho; i++) {
      quantidadeByte = ManipuladorDeBit.quantidadeDeBytes(quadro[i]);
      quadro[i] = ManipuladorDeBit.deslocarBits(quadro[i]);

      for (int x=1; (x<=quantidadeByte) && (x<=4); x++) {
        cargaUtil = ManipuladorDeBit.getPrimeiroByte(quadro[i]);
        quadroDesenquadrado[posQuadro++] = cargaUtil;
        this.computador.camadaEnlace.addText(cargaUtil + " ");
        quadro[i] <<= 8;//Desloca 8 bits para a esquerda
      }
      Thread.sleep(AplicacaoReceptora.VELOCIDADE);
    }
    
    this.computador.camadaEnlace.addText("]\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    return quadroDesenquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoInsercaoDeBytes
  * Funcao: Dividir a mensagem em quadros adicionando o byte [S] no inicio do quadro e o byte [E] no final
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : int[]
  *********************************************/
  private int[] enquadramentoInsercaoDeBytes(int[] quadro) throws Exception {
    System.out.println("\n\t[Insercao de Bytes]");
    this.computador.camadaEnlace.addText("\n\t[Insercao de Bytes]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    final char byteFlagStart = 'S';//Identificar o INICIO do quadro (Start)
    final char byteFlagEnd = 'E';//Identificar o FIM do quadro (End)
    final char byteDeEscape = '/';//Caractere de escape especial

    this.computador.camadaEnlace.addText("\n\tByte de Inicio de Quadro ["+byteFlagStart+"]");
    this.computador.camadaEnlace.addText("\n\tByte de Fim de Quadro ["+byteFlagEnd+"]");
    this.computador.camadaEnlace.addText("\n\tByte de Escape de Quadro ["+byteDeEscape+"]\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);


    String auxiliar = "";
    Boolean SE = true;

    for (int inteiro : quadro) {

      int quantidadeByte = ManipuladorDeBit.quantidadeDeBytes(inteiro);
      inteiro = ManipuladorDeBit.deslocarBits(inteiro);

      int inteiroByte = ManipuladorDeBit.getPrimeiroByte(inteiro);

      this.computador.camadaEnlace.addText("\n\tIC ["+(char) inteiroByte+"] ");
      Thread.sleep(AplicacaoReceptora.VELOCIDADE);

      if (inteiroByte == (int) byteFlagStart) {//Inicio do Quadro
        SE = !SE;//Iniciar a Busca pelo Byte de Fim de Quadro
        inteiro <<= 8;//Deslocando 8 bits para a esquerda
        quantidadeByte--;
      }

      if (!SE) {

        for (int i=1; i<=quantidadeByte; i++) {
          int dado = ManipuladorDeBit.getPrimeiroByte(inteiro);

          if (dado == (int) byteDeEscape) {//Verificando se o dado eh um Byte de Escape
            inteiro <<= 8;//Deslocando 8 bits para a esquerda
            this.computador.camadaEnlace.addText("IC ["+(char) dado+"] ");
            Thread.sleep(AplicacaoReceptora.VELOCIDADE);
            dado = ManipuladorDeBit.getPrimeiroByte(inteiro);//Adicionando o Byte
            auxiliar += (char) dado;
            this.computador.camadaEnlace.addText("Carga Util ["+dado+"] ");
            Thread.sleep(AplicacaoReceptora.VELOCIDADE);
            inteiro <<= 8;//Deslocando 8 bits para a esquerda
            i++;

          } else if (dado == (int) byteFlagEnd) {//Verificando se o dado eh um Byte End
            SE = !SE;//Encontrou o Byte de Fim de Quadro
            this.computador.camadaEnlace.addText("IC ["+(char) dado+"]\n");
            Thread.sleep(AplicacaoReceptora.VELOCIDADE);
          } else {//Caso for um Byte de Carga Util
            auxiliar += (char) ManipuladorDeBit.getPrimeiroByte(inteiro);
            inteiro <<= 8;//Deslocando 8 bits para a esquerda
            this.computador.camadaEnlace.addText("Carga Util ["+dado+"] ");
            Thread.sleep(AplicacaoReceptora.VELOCIDADE);
          }
        
        }

      }
    }

    //Novo Quadro de Carga Util
    int[] quadroDesenquadrado = new int[auxiliar.length()];
    //Adicionando as informacoes de Carga Util no QuadroDesenquadrado
    for (int i=0; i<auxiliar.length(); i++) {
      quadroDesenquadrado[i] = (int) auxiliar.charAt(i);
      ManipuladorDeBit.imprimirBits(quadroDesenquadrado[i]);
    }

    return quadroDesenquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoInsercaoDeBits
  * Funcao: Dividir o quadro recebido em Carga Util
  * Parametros: quadro : int[]
  * Retorno: quadroDesenquadrado : int[]
  *********************************************/
  private int[] enquadramentoInsercaoDeBits(int[] quadro) throws Exception {
    System.out.println("\n\t[Insercao de Bits]");
    this.computador.camadaEnlace.addText("\n\t[Insercao de Bits]\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    //Byte Flag que contem a sequencia de bits "0111110"
    final int byteFlag = 126;//00000000 00000000 00000000 01111110
    this.computador.camadaEnlace.addText("\t[Bits de Flag [01111110] = 126]\n\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    String auxiliar = "";
    Boolean SE = true;

    for (int inteiro : quadro) {
      int quantidadeByte = ManipuladorDeBit.quantidadeDeBytes(inteiro);
      ManipuladorDeBit.imprimirBits(inteiro);

      inteiro = ManipuladorDeBit.deslocarBits(inteiro);
      ManipuladorDeBit.imprimirBits(inteiro);
      int inteiroByte = ManipuladorDeBit.getPrimeiroByte(inteiro);

      if (inteiroByte == byteFlag) {//Inicio do Quadro
        SE = !SE;//Iniciar a Busca pelo Byte de Fim de Quadro
        inteiro <<= 8;//Deslocando 8 bits para a esquerda
        quantidadeByte--;
        this.computador.camadaEnlace.addText("\tIC [" + inteiroByte + "] ");
      }

      if (!SE) {

        ManipuladorDeBit.imprimirBits(inteiro);

        for (int i=1; i<=quantidadeByte; i++) {
          int dado = ManipuladorDeBit.getPrimeiroByte(inteiro);
          System.out.println("asdfasdf\n");
          ManipuladorDeBit.imprimirBits(dado);

          if (dado == byteFlag) {//Verificando se encontrou o Byte de Flag
           SE = !SE;//Encontrou o Byte de Fim de Quadro
          } else {
            this.computador.camadaEnlace.addText("Carga Util [ ");

            int novoQuadro = 0;

            Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);
            if (cincoBits1) {

              dado = ManipuladorDeBit.deslocarBits(dado);
              ManipuladorDeBit.imprimirBits(dado);

              //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
              int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
              //Para cada bit exibe 0 ou 1
              for (int b=1, cont=0; b<=8; b++) {
                //Utiliza displayMask para isolar o bit
                int bit = (dado & displayMask) == 0 ? 0 : 1;

                if (cont == 5) {
                  cont = 0;//Zerando o contador
                  dado <<= 1;//Desloca 1 bit para a esquerda
                  bit = (dado & displayMask) == 0 ? 0 : 1;
                }

                if (b == 8) {//Quando chegar no Ultimo Bit
                  inteiro <<= 8;//Deslocando 8 bits para esquerda
                  dado = ManipuladorDeBit.getPrimeiroByte(inteiro);
                  dado = ManipuladorDeBit.deslocarBits(dado);
                  ManipuladorDeBit.imprimirBits(dado);

                  novoQuadro <<= 1;//Deslocando 1 bit para a esquerda
                  novoQuadro |= ManipuladorDeBit.pegarBitNaPosicao(dado,0);//Adicionando o bit ao novoDado

                  ManipuladorDeBit.imprimirBits(novoQuadro);

                  auxiliar += (char) novoQuadro;
                  this.computador.camadaEnlace.addText(novoQuadro + " ]\n");
                  Thread.sleep(AplicacaoReceptora.VELOCIDADE);
                  i++;

                } else {//Colocando o Bit no novoQuadro
                  novoQuadro <<= 1;//Deslocando 1 bit para a esquerda
                  novoQuadro |= bit;//Adicionando o bit ao novoDado
                  dado <<= 1;//Desloca 1 bit para a esquerda
                }

                if (bit == 1) {//Quando for um bit 1
                  cont++;
                } else {//Caso vinher um bit 0
                  cont = 0;
                }
              }

            } else {//Caso nao tem uma sequencia de 5 Bits 1's
              auxiliar += (char) dado;
              this.computador.camadaEnlace.addText(dado + " ]\n");
              Thread.sleep(AplicacaoReceptora.VELOCIDADE);
            }
          }
          
          inteiro <<= 8;//Deslocando 8 bits para a esquerda;
        }

      }
    }

    //Novo Quadro de Carga Util
    int[] quadroDesenquadrado = new int[auxiliar.length()];
    //Adicionando as informacoes de Carga Util no QuadroDesenquadrado
    for (int i=0; i<auxiliar.length(); i++) {
      quadroDesenquadrado[i] = (int) auxiliar.charAt(i);
      ManipuladorDeBit.imprimirBits(quadroDesenquadrado[i]);
    }

    return quadroDesenquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoViolacaoCamadaFisica
  * Funcao: Violar a Camada Fisica para que ela determine o inicio e o fim de um quadro
  * Parametros: quadro : int[]
  * Retorno: quadro : int[]
  *********************************************/
  private int[] enquadramentoViolacaoCamadaFisica(int[] quadro) throws Exception {
    System.out.println("\n\t[Violacao da Camada Fisica]");
    this.computador.camadaEnlace.addText("\n\t[Violacao da Camada Fisica]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    final int byteFlag = 255;//00000000 00000000 00000000 11111111
    this.computador.camadaEnlace.addText("\n\t[Bits de Flag [11111111] = 255]\n\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    String auxiliar = "";
    Boolean SE = true;

    for (int inteiro : quadro) {
      int quantidadeByte = ManipuladorDeBit.quantidadeDeBytes(inteiro);
      ManipuladorDeBit.imprimirBits(inteiro);
      inteiro = ManipuladorDeBit.deslocarBits(inteiro);
      ManipuladorDeBit.imprimirBits(inteiro);
      int inteiroByte = ManipuladorDeBit.getPrimeiroByte(inteiro);

      if (inteiroByte == byteFlag) {//Inicio do Quadro
        SE = !SE;//Iniciar a Busca pelo Byte de Fim de Quadro
        inteiro <<= 8;//Deslocando 8 bits para a esquerda
        quantidadeByte--;
        this.computador.camadaEnlace.addText("\tIC [" + inteiroByte + "] ");
      }

      if (!SE) {

        ManipuladorDeBit.imprimirBits(inteiro);

        for (int i=1; i<=quantidadeByte; i++) {
          int dado = ManipuladorDeBit.getPrimeiroByte(inteiro);
          ManipuladorDeBit.imprimirBits(dado);

          if (dado == byteFlag) {//Verificando se encontrou o Byte de Flag
           SE = !SE;//Encontrou o Byte de Fim de Quadro
          } else {
            this.computador.camadaEnlace.addText("Carga Util [ ");

            int novoQuadro = 0;

            Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);
            if (cincoBits1) {

              dado = ManipuladorDeBit.deslocarBits(dado);
              ManipuladorDeBit.imprimirBits(dado);

              //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
              int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
              //Para cada bit exibe 0 ou 1
              for (int b=1, cont=0; b<=8; b++) {
                //Utiliza displayMask para isolar o bit
                int bit = (dado & displayMask) == 0 ? 0 : 1;

                if (cont == 5) {
                  cont = 0;//Zerando o contador
                  dado <<= 1;//Desloca 1 bit para a esquerda
                  bit = (dado & displayMask) == 0 ? 0 : 1;
                }

                if (b == 8) {//Quando chegar no Ultimo Bit
                  inteiro <<= 8;//Deslocando 8 bits para esquerda
                  dado = ManipuladorDeBit.getPrimeiroByte(inteiro);
                  dado = ManipuladorDeBit.deslocarBits(dado);
                  ManipuladorDeBit.imprimirBits(dado);

                  novoQuadro <<= 1;//Deslocando 1 bit para a esquerda
                  novoQuadro |= ManipuladorDeBit.pegarBitNaPosicao(dado,0);//Adicionando o bit ao novoDado

                  ManipuladorDeBit.imprimirBits(novoQuadro);

                  auxiliar += (char) novoQuadro;
                  this.computador.camadaEnlace.addText(novoQuadro + " ]\n");
                  Thread.sleep(AplicacaoReceptora.VELOCIDADE);
                  i++;

                } else {//Colocando o Bit no novoQuadro
                  novoQuadro <<= 1;//Deslocando 1 bit para a esquerda
                  novoQuadro |= bit;//Adicionando o bit ao novoDado
                  dado <<= 1;//Desloca 1 bit para a esquerda
                }

                if (bit == 1) {//Quando for um bit 1
                  cont++;
                } else {//Caso vinher um bit 0
                  cont = 0;
                }
              }

            } else {//Caso nao tem uma sequencia de 5 Bits 1's
              auxiliar += (char) dado;
              this.computador.camadaEnlace.addText(dado + " ]\n");
              Thread.sleep(AplicacaoReceptora.VELOCIDADE);
            }
          }
          
          inteiro <<= 8;//Deslocando 8 bits para a esquerda;
        }

      }
    }

    //Novo Quadro de Carga Util
    int[] quadroDesenquadrado = new int[auxiliar.length()];
    //Adicionando as informacoes de Carga Util no QuadroDesenquadrado
    for (int i=0; i<auxiliar.length(); i++) {
      quadroDesenquadrado[i] = (int) auxiliar.charAt(i);
      ManipuladorDeBit.imprimirBits(quadroDesenquadrado[i]);
    }

    return quadroDesenquadrado;
  }



  /*********************************************
  * Metodo: camadaEnlaceReceptoraControleDeErro
  * Funcao: Passa em algum Algoritmo de Constrole de Erro
  * Parametros: quadro : int[]
  * Retorno: int[]
  *********************************************/
  private int[] camadaEnlaceReceptoraControleDeErro(int[] quadro) throws Exception {
    System.out.println("\n\tCONTROLE DE ERRO");
    this.computador.camadaEnlace.addText("\n\tCONTROLE DE ERRO\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    Quadro quadroControleErro = new Quadro(quadro);

    //Recebendo o controle de erro escolhido pelo usuario
    int tipoDeControleErro = Painel.CONFIGURACOES.controleErro.getIndiceSelecionado();

    switch(tipoDeControleErro) {
      case 0:
        quadroControleErro = controleDeErroBitParidadePar(quadroControleErro);
        break;
      case 1:
        quadroControleErro = controleDeErroBitParidadeImpar(quadroControleErro);
        break;
      case 2:
        quadroControleErro = controleDeErroCRC(quadroControleErro);
        break;
      case 3:
        quadroControleErro = controleDeErroCodigoDeHamming(quadroControleErro);
        break;
    }

    this.computador.camadaEnlace.addText("\n");

    System.out.println("VERIFICADOR DE ACK");
    Ack ack = new Ack(this, computador);

    quadro = ack.verificarQuadro(quadroControleErro.getBits());

    //if (computador.idQuadroEsperado == 1) {
      // computador.idQuadroEsperado = ack.getIdAck() + 1;//Quado esperado eh o quadro atual + 1
    //} //else if (ack.getIdAck == computador.idQuadroEsperado) {
    //   computador.idQuadroEsperado = ack.getIdAck() + 1;//Novo quadro esperado
    ///}

    return quadro;
  }

  /*********************************************
  * Metodo: controleDeErroBitParidadePar
  * Funcao: Controle de Erro adicionando o Bit de Paridade PAR
  * Parametros: quadro : Quadro
  * Retorno: 
  *********************************************/
  private Quadro controleDeErroBitParidadePar(Quadro quadro) throws Exception {
    System.out.println("\n\t[Bit de Paridade Par]");
    this.computador.camadaEnlace.addText("\n\t[Bit de Paridade Par]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    int bits1 = ManipuladorDeBit.quantidadeBits1(quadro.getBits());

    System.out.println("Quantidade Bits 1's: " + bits1);


    if (bits1 % 2 ==0) {//Caso dor PAR
      int[] bits = quadro.getBits();
      System.out.println("Quantidade PAR");
      bits[bits.length-1] >>= 8;//Deslocando 8 bits para direita para remover a informacao da Paridade Par
      

      int[] novoQuadro = new int[bits.length-1];
      if (bits[bits.length-1] == 0) {//Verificando se o ultimo inteiro eh todo zero
        for (int i=0; i<bits.length-1; i++) {
          novoQuadro[i] = bits[i];
          ManipuladorDeBit.imprimirBits(novoQuadro[i]);
        }
        bits = novoQuadro;
      }

      quadro.setBits(bits);//Adicionando no Quadro


    } else {//Caso for IMPAR
      System.out.println("Quantidade IMPAR");
      this.computador.camadaEnlace.addText("\n\t[QUARO COM ERRO]");
      //this.computador.terminouCamadas();
      System.out.println("QUADRO COM ERRO");
      Painel.erroNoQuadro(computador);
      Painel.CONFIGURACOES.setDisabilitar(false);
      //Painel.CONFIGURACOES.setDesabilitarSlider(false);
      //MeioDeComunicacao.SEMAPHORO_QUADRO.release();


      //-----------------------------------------------------------------------------
      int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      if (tipoDeFluxo > 0) {//Quando nao for a janela deslizante de 1 bit

        if (tipoDeFluxo == 2) {//Janela deslizante com retransmissao seletiva
          System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
          new Nack(computador);
          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        }

        MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      }
      //-----------------------------------------------------------------------------

      // int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      // if (tipoDeFluxo == 2) {
      //   System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
      //   new Nack(computador);
      // }

      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      this.stop();
    }
    this.computador.camadaEnlace.addText("\n\t[QUARO SEM ERRO]");
    System.out.println("QUADRO CORRETO");

    this.computador.camadaEnlace.addText("\n");
    return quadro;
  }

  /*********************************************
  * Metodo: controleDeErroBitParidadeImpar
  * Funcao: Controle de Erro adicionando o Bit de Paridade IMPAR
  * Parametros: quadro : Quadro
  * Retorno: quadro
  *********************************************/
  private Quadro controleDeErroBitParidadeImpar(Quadro quadro) throws Exception {
    System.out.println("\n\t[Bit de Paridade Impar]");
    this.computador.camadaEnlace.addText("\n\t[Bit de Paridade Impar]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    int bits1 = ManipuladorDeBit.quantidadeBits1(quadro.getBits());

    System.out.println("Quantidade Bits 1's: " + bits1);


    if (bits1 % 2 != 0) {//Caso for IMPAR
      int[] bits = quadro.getBits();
      System.out.println("Quantidade IMPAR");
      bits[bits.length-1] >>= 8;//Deslocando 8 bits para direita para remover a informacao da Paridade Par
      

      int[] novoQuadro = new int[bits.length-1];
      if (bits[bits.length-1] == 0) {//Verificando se o ultimo inteiro eh todo zero
        for (int i=0; i<bits.length-1; i++) {
          novoQuadro[i] = bits[i];
          ManipuladorDeBit.imprimirBits(novoQuadro[i]);
        }
        bits = novoQuadro;
      }

      quadro.setBits(bits);//Adicionando no Quadro


    } else {//Caso for par
      System.out.println("Quantidade PAR");
      this.computador.camadaEnlace.addText("\n\t[QUARO COM ERRO]");
      //this.computador.terminouCamadas();
      System.out.println("QUADRO COM ERRO");
      Painel.erroNoQuadro(computador);
      Painel.CONFIGURACOES.setDisabilitar(false);
      //Painel.CONFIGURACOES.setDesabilitarSlider(false);
      //MeioDeComunicacao.SEMAPHORO_QUADRO.release();



      //-----------------------------------------------------------------------------
      int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      if (tipoDeFluxo > 0) {//Quando nao for a janela deslizante de 1 bit

        if (tipoDeFluxo == 2) {//Janela deslizante com retransmissao seletiva
          System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
          new Nack(computador);
          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        }

        MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      }
      //-----------------------------------------------------------------------------



      // int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      // if (tipoDeFluxo == 2) {
      //   System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
      //   new Nack(computador);
      // }

      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      this.stop();
    }
    this.computador.camadaEnlace.addText("\n\t[QUARO SEM ERRO]");
    System.out.println("QUADRO CORRETO");

    this.computador.camadaEnlace.addText("\n");
    return quadro;
  }

  /*********************************************
  * Metodo: controleDeErroCRC
  * Funcao: Controle de Erro com algoritmo do CRC
  * Parametros: quadro : Quadro
  * Retorno: quadro
  *********************************************/
  private Quadro controleDeErroCRC(Quadro quadro) throws Exception {
    System.out.println("\n\t[CRC]");
    this.computador.camadaEnlace.addText("\n\t[CRC]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    final int[] polinomioCRC = {1,0,0,0,0,0,1,0,0,1,1,0,0,0,0,0,1,0,0,0,1,1,1,0,1,1,0,1,1,0,1,1,1};
    final int grauPolinomio = polinomioCRC.length-1;

    int[] resto =  quadro.bitsVetor();

    int cont=0;
    while (true) {
      for(int i=0;i<polinomioCRC.length;i++) {
        resto[cont+i]=(resto[cont+i]^polinomioCRC[i]);
      }
      while(resto[cont]==0 && cont!=resto.length-1) {
        cont++;
      }
      if((resto.length-cont)<polinomioCRC.length) {
        break;
      }
    }
     
    for(int i=0; i< resto.length; i++) {
      if(resto[i]!=0) {
        System.out.println("QUADRO COM ERRO");
        this.computador.camadaEnlace.addText("\n\t[QUARO COM ERRO]");
        Painel.erroNoQuadro(computador);
        Painel.CONFIGURACOES.setDisabilitar(false);
        //Painel.CONFIGURACOES.setDesabilitarSlider(false);
        //MeioDeComunicacao.SEMAPHORO_QUADRO.release();


        // int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
        // if (tipoDeFluxo == 2) {
        //   System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
        //   new Nack(computador);
        // }


        //-----------------------------------------------------------------------------
        int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
        if (tipoDeFluxo > 0) {//Quando nao for a janela deslizante de 1 bit

          if (tipoDeFluxo == 2) {//Janela deslizante com retransmissao seletiva
            System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
            new Nack(computador);
            MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
            MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
          }

          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        }
        //-----------------------------------------------------------------------------




        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        this.stop();
        break;
      }
      if(i==resto.length-1) {
        System.out.println("QUADRO SEM ERRO");
        this.computador.camadaEnlace.addText("\n\t[QUARO SEM ERRO]");
      }
    }
    
    //Removendo o resto da divisao
    //------------------------
    resto = quadro.bitsVetor();
    int tam = (resto.length - grauPolinomio)/32;
    if ((resto.length - grauPolinomio)%32 != 0) {
      tam++;
    }
    System.out.println("Novo tam " + tam);
    int[] vetor = new int[tam];
    int novoInteiro = 0;
    for (int i=1, pos=0; i<=resto.length-grauPolinomio; i++) {
      novoInteiro <<= 1;//Deslocando 1 bit para esquerda
      novoInteiro |= resto[i-1];
      if (i%32 == 0) {//Completou os 32 bits
        vetor[pos++] = novoInteiro;
        novoInteiro = 0;
      } else if (i == (resto.length-grauPolinomio) && novoInteiro != 0) {
        vetor[pos++] = novoInteiro;
      }
    }

    for (int i : vetor) {
      ManipuladorDeBit.imprimirBits(i);
    }

    quadro.setBits(vetor);


    return quadro;
  }

  /*********************************************
  * Metodo: controleDeErroCodigoDeHamming
  * Funcao: Controle de Erro com algotirmo do Codigo de Hamming
  * Parametros: quadro : Quadro
  * Retorno: quadro
  *********************************************/
  private Quadro controleDeErroCodigoDeHamming(Quadro quadro) throws Exception {
    System.out.println("\n\t[Codigo de Hamming]");
    this.computador.camadaEnlace.addText("\n\t[Codigo de Hamming]");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    String bitsMensagem = "";
    System.out.println("Bits do Quadro Sem o Bit 1 de Controle de cada Inteiro");
    for (int numero : quadro.getBits()) {
      String bits = Integer.toBinaryString(numero);//Bits do inteiro
      for (int i=1; i<bits.length(); i++) {
        bitsMensagem += bits.charAt(i);//Adicionando cada bit na String, execto o primeiro bit '1'
      }
    }

    int[] novoVetor = new int[bitsMensagem.length()];
    for (int i=0; i<bitsMensagem.length(); i++) {
      novoVetor[i] = Integer.parseInt(String.valueOf(bitsMensagem.charAt(i)));
      System.out.print(novoVetor[i]);
    }
    System.out.println("\n\n");

    //CALCULANDO A QUANTIDADE DE BITS DE CONTROLE
    int quantidadeBitsDeControle = 0;//Numero de bits de controle para adicionar na mensagem
    for (int i=0; i<novoVetor.length;) {
      if (Math.pow(2,quantidadeBitsDeControle) == i+1) {
        quantidadeBitsDeControle++;
      } else {
        i++;
      }
    }
    System.out.println("QUANTIDADE DE BITS DE CONTROLE");
    System.out.println(quantidadeBitsDeControle);

    int quantidadeDeErros = 0;

    int potencia = 0;
    int bitsParidade[] = new int[quantidadeBitsDeControle];
    String bitsErro = new String();
    for(; potencia<quantidadeBitsDeControle; potencia++) {
      for(int i=0; i<novoVetor.length; i++) {
        int k = i+1;
        String base2 = Integer.toBinaryString(k);
        int bit = ((Integer.parseInt(base2))/((int) Math.pow(10, potencia)))%10;
        if(bit == 1) {
          if(novoVetor[i] == 1) {
            bitsParidade[potencia] = (bitsParidade[potencia]+1)%2;
          }
        }
      }
      quantidadeDeErros += bitsParidade[potencia] == 1 ? 1 : 0;
      bitsErro = bitsParidade[potencia] + bitsErro;
      System.out.println("Bits erro " + bitsErro);
    }

    System.out.println("\n\n\nQUANTIDADE DE ERRO " + quantidadeDeErros);
    if (quantidadeDeErros > 1) {
      System.out.println("QUADRO COM ERRO");
      this.computador.camadaEnlace.addText("\n\t[QUARO COM ERRO]");
      Painel.erroNoQuadro(computador);
      Painel.CONFIGURACOES.setDisabilitar(false);
      //Painel.CONFIGURACOES.setDesabilitarSlider(false);


      //-----------------------------------------------------------------------------
      int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
      if (tipoDeFluxo > 0) {//Quando nao for a janela deslizante de 1 bit

        if (tipoDeFluxo == 2) {//Janela deslizante com retransmissao seletiva
          System.out.println("ENVIAR NACK DO QUADRO ESPERADO: " + computador.idQuadroEsperado);
          new Nack(computador);
          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        }

        MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      }
      //-----------------------------------------------------------------------------

      
      this.stop();
    }
    this.computador.camadaEnlace.addText("\n\t[QUARO SEM ERRO]");

    int posicaoErro = Integer.parseInt(bitsErro, 2);
    if(posicaoErro != 0) {
      System.out.println("Error localizado na posicao: " + posicaoErro + ".");
      novoVetor[posicaoErro-1] = (novoVetor[posicaoErro-1]+1)%2;
      
      System.out.print("Mensagem correta: [");
      for(int i=0; i<novoVetor.length; i++) {
        System.out.print(novoVetor[novoVetor.length-i-1]);
      }
      System.out.println("]");
    
    } else {
      System.out.println("Nao encontrou erro na transmissao");
    }

    int bitsUteis = novoVetor.length - quantidadeBitsDeControle;
    int[] vetorBits = new int[bitsUteis];

    System.out.println("MENSAGEM ORIGINAL:");
    potencia = 0;
    int pos = 0;
    for (int i=0; i<novoVetor.length; i++) {
      if (Math.pow(2,potencia) != i+1) {
        System.out.print(novoVetor[i]);
        vetorBits[pos++] = novoVetor[i];
      } else {
        potencia++;
      }
    }

    System.out.println();
    int tam = bitsUteis/32;
    if (bitsUteis%32 != 0) {
      tam++;
    }
    System.out.println("Novo tamanho " + tam);
    novoVetor = new int[tam];
    int novoInteiro = 0;
    pos = 0;
    int bits = 0;
    for (int i=0; i<vetorBits.length; i++) {
      int bit = vetorBits[i];
      System.out.print(bit);
      novoInteiro <<= 1;//Desloca 1 bit para esquerda
      novoInteiro |= bit;
      bits++;

      if (bits == 32) {
        novoVetor[pos++] = novoInteiro;
        novoInteiro = 0;
        bits = 0;
      } else if (i == (vetorBits.length-1) && novoInteiro != 0) {
        novoVetor[pos++] = novoInteiro;
      }
    }

    System.out.println("\nMensagem original");
    for (int h : novoVetor) {
      ManipuladorDeBit.imprimirBits(h);
    }

    quadro.setBits(novoVetor);

    return quadro;
  }

  /*********************************************
  * Metodo: obterParidadePar
  * Funcao: Retorna a paridade Par para o codigo de Hamming
  * Parametros: vetor : int[], potencia : int
  * Retorno: int paridadePar
  *********************************************/
  private int obterParidadePar(int[] vetor, int potencia) {
    int bitParidade = 0;
    int j=0;
    int k=0;
    for (int i=1; i<= vetor.length; i++) {
      if (Math.pow(2,j) == i) {
        j++;
      } else {
        int h = i;
        String s = Integer.toBinaryString(h);
        int x = ((Integer.parseInt(s))/((int) Math.pow(10, potencia)))%10;
        if(x == 1) {
          if(vetor[i-1] == 1) {
            bitParidade = (bitParidade+1)%2;
          }
        }
        k++;
      }
    }
    return bitParidade;
  }


  /*********************************************
  * Metodo: camadaEnlaceTransmissoraControleDeFluxo
  * Funcao: Faz o controle do tipo de Janela Deslizantes escolhio pelo usuario
  * Parametros: quadros : int[]
  * Retorno: void
  *********************************************/
  private void camadaEnlaceReceptoraControleDeFluxo(int... quadros) throws Exception {
    System.out.println("\n\tCONTROLE DE FLUXO");
    this.computador.camadaEnlace.addText("\nCONTROLE DE FLUXO\n");
    Thread.sleep(AplicacaoReceptora.VELOCIDADE);

    //Recebendo o controle de erro escolhido pelo usuario
    int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();

    switch(tipoDeFluxo) {
      case 0:
        controleDeFluxoJanelaDeslizante1Bit(quadros);
        break;
      case 1:
        controleDeFluxoJanelaDeslizanteGoBackN(quadros);
        break;
      case 2:
        controleDeFluxoJanelaDeslizanteComRestricaoSeletiva(quadros);
        break;
    }

    this.computador.camadaEnlace.addText("\n");
  }

  /*********************************************
  * Metodo: controleDeFluxoJanelaDeslizante1Bit
  * Funcao: Faz o controle de Fluxo da Janela Deslizante de 1 bit
  * Parametros: quadros : int[]
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizante1Bit(int... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante 1 Bit]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante 1 Bit]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    MeioDeComunicacao.SEMAPHORO_QUADRO.release();

  }

  /*********************************************
  * Metodo: controleDeFluxoJanelaDeslizanteGoBackN
  * Funcao: Faz o controle de Fluxo da Janela Deslizante Go Back N
  * Parametros: quadros : int[]
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizanteGoBackN(int... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante Go Back N]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante Go Back N]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    MeioDeComunicacao.SEMAPHORO_FLUXO.release();
    MeioDeComunicacao.SEMAPHORO_QUADRO.release(); 
    MeioDeComunicacao.SEMAPHORO_QUADRO.release();

    System.out.println("Recebeu Quadro");
    System.out.println("SEMAPHORO_FLUXO: " + MeioDeComunicacao.SEMAPHORO_FLUXO.availablePermits());
    System.out.println("SEMAPHORO_QUADRO: " + MeioDeComunicacao.SEMAPHORO_QUADRO.availablePermits());

  }

  /*********************************************
  * Metodo: controleDeFluxoJanelaDeslizanteComRestricaoSeletiva
  * Funcao: Faz controle de Fluxo da Janela Deslizante Seletiva
  * Parametros: quadros : int[]
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizanteComRestricaoSeletiva(int... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante com Restricao Seletiva]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante com Restricao Seletiva]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);
  
    MeioDeComunicacao.SEMAPHORO_FLUXO.release();
    MeioDeComunicacao.SEMAPHORO_QUADRO.release();
    MeioDeComunicacao.SEMAPHORO_QUADRO.release();

  }





  /***********************************************************************
  * Nome: Ack
  * Funcao: Cria uma Thrad que envia um ACK para o outro Computador
  ***********************************************************************/
  public class Ack extends Thread {
    private CamadaEnlaceDadosReceptora camadaEnlace;//Referencia da CamadaEnlaceDadosReceptora
    private Computador computador;//Referencia do Computador
    private int idTemporizador;//ID do temporizador que enviou o quadro
    private int bitsACK = 223;//Bits que caracterizam o ACK - 11011111
    private int bitsNACK = 224;//Bits que caracterizam o NACK

    /*********************************************
    * Metodo: Ack
    * Funcao: Cria um objeto da classe ACK
    * Parametros: camada : CamadaEnlaceDadosReceptora, computador : Computador
    *********************************************/
    public Ack(CamadaEnlaceDadosReceptora camada, Computador computador) {
      this.camadaEnlace = camada;
      this.computador = computador;
    }

    public void run() {
      try {
        //Semaphoro para impedir de enviar o Ack quando ja tem um transmissao acontecendo
        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();

        System.out.println("Enviando ACK");
        System.out.println("SEMAPHORO_FLUXO: " + MeioDeComunicacao.SEMAPHORO_FLUXO.availablePermits());
        System.out.println("SEMAPHORO_QUADRO: " + MeioDeComunicacao.SEMAPHORO_QUADRO.availablePermits());
        this.enviarACK(idTemporizador);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /*********************************************
    * Metodo: verificarQuadro
    * Funcao: Verifica se o quadro recebido eh um ACK
    * Parametros: quadro : int[]
    * Retorno: quadro : int[]
    *********************************************/
    public int[] verificarQuadro(int[] quadro) throws Exception {
      int tam = quadro.length;
      int ultimoInteiro = quadro[tam-1];

      int primeiroByte = ManipuladorDeBit.getPrimeiroByte(ultimoInteiro);

      if (primeiroByte == this.bitsACK) {//CASO O QUADRO SEJA UM ACK
        System.out.println("eh ACK");
        ultimoInteiro = ManipuladorDeBit.deslocarBits(ultimoInteiro);
        ultimoInteiro <<= 8;//Deslocando 8 bits
        primeiroByte = ManipuladorDeBit.getPrimeiroByte(ultimoInteiro);

        System.out.println("Interromper Temporizador ["+primeiroByte+"]");
        this.computador.camadaEnlace.addText("\tInterromper Temporizador ["+primeiroByte+"]\n");
        
        
        int controleFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
        if (controleFluxo == 2) {//Janela Deslizante com retransmissao seletiva
          for (int i=primeiroByte; i>0; i--) {
            Painel.TEMPORIZADORES.removerTemporizador(i, computador);
          }
          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        } else {
          Painel.TEMPORIZADORES.removerTemporizador(primeiroByte, computador);
        }

        if (controleFluxo == 0) {
          MeioDeComunicacao.SEMAPHORO_FLUXO.release();
        } else {
          MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
          MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        }

        System.out.println("Recebeu ACK");
        System.out.println("SEMAPHORO_FLUXO: " + MeioDeComunicacao.SEMAPHORO_FLUXO.availablePermits());
        System.out.println("SEMAPHORO_QUADRO: " + MeioDeComunicacao.SEMAPHORO_QUADRO.availablePermits());

        //MeioDeComunicacao.SEMAPHORO_QUADRO.release();
          

        this.computador.terminouCamadas();
        this.camadaEnlace.stop();
      
      } else if (primeiroByte == this.bitsNACK) {//CASO O QUADRO SEJA UM NACK
        System.out.println("eh NACK");
        ultimoInteiro = ManipuladorDeBit.deslocarBits(ultimoInteiro);
        ultimoInteiro <<= 8;//Deslocando 8 bits
        primeiroByte = ManipuladorDeBit.getPrimeiroByte(ultimoInteiro);

        System.out.println("Enviar Quadro ["+primeiroByte+"]");
        this.computador.camadaEnlace.addText("\tEnviar Quadro ["+primeiroByte+"]\n");


        Temporizador temporizador = Painel.TEMPORIZADORES.getTemporizador(primeiroByte);
        if (temporizador == null) {




          System.out.println("oooooooooooooooooooooooooooooo " + primeiroByte);






        } else {
          temporizador.enviarQuadro();
        }


      } else {//K = 223;//BCASO O QUADRO SEJA UM MENSAGEM
        System.out.println("NAO EH ACK");
        ultimoInteiro <<= 24;
        this.idTemporizador = ManipuladorDeBit.getPrimeiroByte(ultimoInteiro);

        if (computador.idQuadroEsperado == 1 && idTemporizador == 1) {
          computador.idQuadroEsperado = idTemporizador + 1;//Quado esperado eh o quadro atual + 1
          System.out.println("Primeiro Quadro----------------------------------------");
          this.start();
        } else if (idTemporizador == computador.idQuadroEsperado) {
          computador.idQuadroEsperado = idTemporizador + 1;//Novo quadro esperado
          System.out.println("Quadros seguintes-----------------------------------------");
          
          int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
          
          if (tipoDeFluxo > 0) {//transmissao Full-Duplex

            if (tipoDeFluxo == 1) {

              System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
              //MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
              //MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
              this.start();

            } else if (tipoDeFluxo == 2) {

              List<Quadro> quadrosRecebidosNConfirmados = computador.getQuadrosRecebidosNConfirmado();
              
              if (quadrosRecebidosNConfirmados.isEmpty()) {
                this.start();
              } else {

                int ultimo = quadrosRecebidosNConfirmados.size()-1;
                Quadro ultimoQuadro = quadrosRecebidosNConfirmados.get(ultimo);


                System.out.println("\n\n\n\n\n\n\n" + quadrosRecebidosNConfirmados.size());

                for (int i=0; i<quadrosRecebidosNConfirmados.size(); i++) {
                  Quadro q = quadrosRecebidosNConfirmados.get(i);
                  int[] bits = camadaEnlaceReceptoraEnquadramento(q.getBits());
                  chamarProximaCamada(bits);
                }

                quadrosRecebidosNConfirmados.clear();
                System.out.println("\n\n\n\n\n\nNOVO TAMANHO : " + quadrosRecebidosNConfirmados.size());

                System.out.println("ENVIR O ACK DO MAIOR QUADRO KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
                new Ack(camadaEnlace,computador).verificarQuadro(ultimoQuadro.getBits());

              }
            }

          } else {
            this.start();
          }


        } else if (idTemporizador < computador.idQuadroEsperado) {
          System.out.println("Quadro que teve ACK perdido---------------------------------");
          this.start();

          //---------------------------------------------------------------------------
          int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
          if (tipoDeFluxo == 1) {
            MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
            MeioDeComunicacao.SEMAPHORO_QUADRO.acquire(); 
          }
          //--------------------------------------------------------------------------- 

          this.camadaEnlace.stop();
        
        } else {

          int tipoDeFluxo = Painel.CONFIGURACOES.controleFluxo.getIndiceSelecionado();
          
          if (tipoDeFluxo == 1) {

            MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
            MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();

          } else if (tipoDeFluxo == 2) {//Janela Deslizante com Retransmissao seletiva
            Quadro novoQuadro = new Quadro(quadro);
            novoQuadro.setId(idTemporizador);

            List<Quadro> quadrosRecebidosNConfirmados = computador.getQuadrosRecebidosNConfirmado();
            if (quadrosRecebidosNConfirmados.isEmpty()) {
              computador.adicionarQuadroRecebidoNConfirmado(novoQuadro);
              System.out.println("****************************************ADICIONOU UM QUADRO");
            } else {
              boolean adicionar = true;
              for (Quadro q : quadrosRecebidosNConfirmados) {
                if (q.getId() == idTemporizador) {
                  adicionar = false;
                  break;
                }
              }
              if (adicionar) {
                computador.adicionarQuadroRecebidoNConfirmado(novoQuadro);
              }
            }



          }

          this.computador.terminouCamadas();
          this.camadaEnlace.stop();
        }
      }

      ManipuladorDeBit.imprimirBits(quadro[tam-1]);
      quadro[tam-1] >>= 8;//Desloca 8 bits para direita

      int[] novoQuadro = new int[quadro.length-1];
      if (quadro[tam-1] == 0) {//Verificando se o ultimo inteiro eh todo zero
        for (int i=0; i<quadro.length-1; i++) {
          novoQuadro[i] = quadro[i];
          ManipuladorDeBit.imprimirBits(novoQuadro[i]);
        }
        quadro = novoQuadro;
      } else {
        ManipuladorDeBit.imprimirBits(quadro[tam-1]);
      }

      return quadro;
    }

    /*********************************************
    * Metodo: enviarACK
    * Funcao: Envia um ACK com o codigo do Temporizador
    * Parametros: idTemporizador : int
    * Retorno: void
    *********************************************/
    private void enviarACK(int idTemporizador) throws Exception {
      System.out.println("[Enviando ACK]");
      computador.camadaEnlace.addText("\n\t[Enviando ACK]");
      Thread.sleep(AplicacaoReceptora.VELOCIDADE);

      bitsACK <<= 8;//Deslocando 8 bits para esquerda
      bitsACK = bitsACK | idTemporizador;

      Quadro quadro = new Quadro(bitsACK);

      this.computador.aplicacaoTransmissora.camadaEnlace = new CamadaEnlaceDadosTransmissora(computador);
      Quadro[] quadros = this.computador.aplicacaoTransmissora.camadaEnlace.camadaEnlaceTransmissoraControleDeErro(quadro);
      
      this.computador.aplicacaoTransmissora.camadaEnlace.chamarProximaCamada(quadros[0].getBits());
    }

    /*********************************************
    * Metodo: getIdAck
    * Funcao: Retorna o ID do ACK
    * Parametros: void
    * Retorno: id : int
    *********************************************/
    public int getIdAck() {
      return idTemporizador;
    }

  }//Fim class


  /***********************************************************************
  * Nome: Nack
  * Funcao: Cria uma Thrad que envia um NACK para o outro Computador
  ***********************************************************************/
  public class Nack extends Thread {
    private CamadaEnlaceDadosReceptora camadaEnlace;//Referencia da CamadaEnlaceDadosReceptora
    private Computador computador;//Referencia do Computador
    private int idTemporizador;//ID do temporizador que enviou o quadro
    private int bitsNACK = 224;//Bits que caracterizam o ACK - 11011111

    public Nack(Computador computador) {
      this.computador = computador;
      this.idTemporizador = computador.idQuadroEsperado;
      this.start();
    }

    public void run() {
      try {
        //Semaphoro para impedir de enviar o Ack quando ja tem um transmissao acontecendo
        MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
        this.enviarQuadro();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /*********************************************
    * Metodo: enviarQuadro
    * Funcao: Envia um NACK com o codigo do Temporizador
    * Parametros: idTemporizador : int
    * Retorno: void
    *********************************************/
    public void enviarQuadro() throws Exception {
      System.out.println("[Enviando NACK]");
      computador.camadaEnlace.addText("\n\t[Enviando NACK]");
      Thread.sleep(AplicacaoReceptora.VELOCIDADE);

      bitsNACK <<= 8;//Deslocando 8 bits para esquerda
      bitsNACK = bitsNACK | idTemporizador;

      Quadro quadro = new Quadro(bitsNACK);

      this.computador.aplicacaoTransmissora.camadaEnlace = new CamadaEnlaceDadosTransmissora(computador);
      Quadro[] quadros = this.computador.aplicacaoTransmissora.camadaEnlace.camadaEnlaceTransmissoraControleDeErro(quadro);
      
      this.computador.aplicacaoTransmissora.camadaEnlace.chamarProximaCamada(quadros[0].getBits());
    }

  }//Fim class

}//Fim class