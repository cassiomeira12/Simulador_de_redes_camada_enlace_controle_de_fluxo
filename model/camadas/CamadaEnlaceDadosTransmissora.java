/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 06/03/18
* Ultima alteracao: 20/05/18
* Nome: CamadaEnlaceDadosTransmissora
* Funcao: Dividir a Mensagem em Quadros menores
***********************************************************************/

package model.camadas;

import view.componentes.Computador;
import view.Painel;
import util.ManipuladorDeBit;
import model.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.lang.Math;


public class CamadaEnlaceDadosTransmissora extends Thread {
  private Computador computador;
  private final String nomeDaCamada = "CAMADA ENLACE DE DADOS TRANSMISSORA";
  private int[] quadro;//Informacao recebida da Camada Superior


  /*********************************************
  * Metodo: CamadaEnlaceDadosTransmissora - Construtor
  * Funcao: Cria objetos da classe CamadaEnlaceDadosTransmissora
  * Parametros: computador : Computador
  *********************************************/
  public CamadaEnlaceDadosTransmissora(Computador computador) {
    this.computador = computador;
  }

  public void run() {
    System.out.println(nomeDaCamada);
    try {
      Painel.CONFIGURACOES.setDisabilitar(true);//Desativando a mudanca das opcoes
      this.imprimirBitsCadaInteiro(quadro);

      Quadro[] quadroEnquadrado;

      quadroEnquadrado = camadaEnlaceTransmissoraEnquadramento(quadro);
      quadroEnquadrado = camadaEnlaceTransmissoraControleDeErro(quadroEnquadrado);
      camadaEnlaceTransmissoraControleDeFluxo(quadroEnquadrado);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: camadaEnlaceDadosTransmissora
  * Funcao: Recebe a informacao da Camada Superior e inicia o processamento desta Camada
  * Parametros: quadro : int[]
  * Retorno: void
  *********************************************/
  public void camadaEnlaceDadosTransmissora(int... quadro) {
    this.quadro = quadro;
    this.start();//Iniciando a Thread dessa Camada
  }

  /*********************************************
  * Metodo: chamarProximaCamada
  * Funcao: Passa os dados a serem enviados dessa Camada para a proxima
  * Parametros: quadro : int[]
  * Retorno: void
  *********************************************/
  public void chamarProximaCamada(int... quadro) throws Exception {
    this.computador.aplicacaoTransmissora.camadaFisica = new CamadaFisicaTransmissora(computador);
    this.computador.aplicacaoTransmissora.camadaFisica.camadaFisicaTransmissora(quadro);
  }

  /*********************************************
  * Metodo: imprimirBitsCadaInteiro
  * Funcao: Imprimir na Interface Grafica o Inteiro e os Bits que o compoe
  * Parametros: quadro : int[]
  * Retorno: void
  *********************************************/
  private void imprimirBitsCadaInteiro(int[] quadro) throws Exception {
    System.out.println("\n\tBits de cada Inteiro");
    this.computador.camadaEnlace.addText("BITS DE CADA INTEIRO\n");
    for (int c : quadro) {
      System.out.print("\tInteiro ["+c+"] - ");
      if (c > 99) {
        computador.camadaEnlace.addText("["+c+"] " + ManipuladorDeBit.imprimirBits(c) + "\n");
      } else {
        computador.camadaEnlace.addText("["+c+"]   " + ManipuladorDeBit.imprimirBits(c) + "\n");
      }
      Thread.sleep(AplicacaoTransmissora.VELOCIDADE);
    }
  }

  /*********************************************
  * Metodo: camadaEnlaceTransmissoraEnquadramento
  * Funcao: Divide a informacao recebida da Camada Superior em Quadros
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : Quadro[]
  *********************************************/
  public Quadro[] camadaEnlaceTransmissoraEnquadramento(int... quadro) throws Exception {
    System.out.println("\n\tENQUADRAMENTO");
    this.computador.camadaEnlace.addText("\nENQUADRAMENTO\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    Quadro[] quadroEnquadrado = null;

    //Recebendo o enquadramento escolhido pelo usuario
    int tipoDeEnquadramento = Painel.CONFIGURACOES.enquadramento.getIndiceSelecionado();

    switch(tipoDeEnquadramento) {
      case 0:
        quadroEnquadrado = enquadramentoContagemDeCaracteres(quadro);
        break;
      case 1:
        quadroEnquadrado = enquadramentoInsercaoDeBytes(quadro);
        break;
      case 2:
        quadroEnquadrado = enquadramentoInsercaoDeBits(quadro);
        break;
      case 3:
        quadroEnquadrado = enquadramentoViolacaoCamadaFisica(quadro);
        break;
    }

    //Adicionando IDs aos Quadros
    for (int i=0; i<quadroEnquadrado.length; i++) {
      //quadroEnquadrado[i].setId(Painel.ID_TEMPORIZADOR++);
      quadroEnquadrado[i].setId(computador.id_temporizador++);
    }

    computador.camadaEnlace.addText("\n");

    return quadroEnquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoContagemDeCaracteres
  * Funcao: Realiza o enquadramento contando uma quantidade de Caracteres
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : Quadro[]
  *********************************************/
  private Quadro[] enquadramentoContagemDeCaracteres(int... quadro) throws Exception {
    System.out.println("\n\t[Contagem de Caracteres]");
    this.computador.camadaEnlace.addText("\n\t[Contagem de Caracteres]\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);
    
    //Quantidade de Bytes de CargaUtil em cada Quadro = Cada Quadro tem no maximo 32 bits
    final int quantidadeDeCaracteres = 3;//24 de bits de carga util (IC)

    int quantidadeDeBytes = 0;//Armazenar a quantidade Bytes que tem no vetor de Inteiros
    for (int inteiro : quadro) {//Verificando o vetor de Inteiros
      quantidadeDeBytes += ManipuladorDeBit.quantidadeDeBytes(inteiro);//Adicionando a quantidade de Bytes
    }

    //Calculando o novo tamanho do vetor de Quadros
    int novoTamanho = quantidadeDeBytes/3;
    if (quantidadeDeBytes % 3 != 0) {
      novoTamanho++;//Aumentando 1 posicao no tamanho do Vetor
    }

    Quadro[] quadroEnquadrado = new Quadro[novoTamanho];//Novo vetor com os Quadros
    int posQuadro = 0;//Posicao do Vetor de Quadros

    int novoQuadro = 0;//Novo quadro
    Queue<Integer> cargaUtil = new LinkedList<>();//Fila de Carga Util

    //Pesquisando todo o vetor de Inteiros
    for (int i=0; i<quadro.length; i++) {
      int inteiro = quadro[i];
      int bytesInteiro = ManipuladorDeBit.quantidadeDeBytes(inteiro);

      //Pegando todos os Bytes do Inteiro e colocando na Fila de Carga Util
      for (int x=0; x<bytesInteiro; x++) {
        cargaUtil.add(ManipuladorDeBit.getPrimeiroByte(inteiro));
        inteiro <<= 8;
      }

      //Colocando uma quantidade de Caracteres como Carga Util no novo Quadro
      if (cargaUtil.size() == quantidadeDeCaracteres) {
        int header = cargaUtil.size() >= quantidadeDeCaracteres ? quantidadeDeCaracteres*8 : cargaUtil.size()*8;
        
        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();
          novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);
          this.computador.camadaEnlace.addText(dado + " ");
        }


        this.computador.camadaEnlace.addText("]");
        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);
        
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro

      } else if (i == (quadro.length-1)) {
        int header = cargaUtil.size() >= quantidadeDeCaracteres ? quantidadeDeCaracteres*8 : cargaUtil.size()*8;
        
        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();
          novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
          this.computador.camadaEnlace.addText(dado + " ");
        }

        //ManipuladorDeBit.imprimirBits(novoQuadro);
        this.computador.camadaEnlace.addText("]");
        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);
        
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro
      }

    }

    return quadroEnquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoInsercaoDeBytes
  * Funcao: Dividir a mensagem em quadros adicionando o byte [S] no inicio do quadro e o byte [E] no final
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : Quadro[]
  *********************************************/
  private Quadro[] enquadramentoInsercaoDeBytes(int... quadro) throws Exception {
    System.out.println("\n\t[Insercao de Bytes]");
    this.computador.camadaEnlace.addText("\n\t[Insercao de Bytes]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    final char byteFlagStart = 'S';//Identificar o INICIO do quadro (Start)
    final char byteFlagEnd = 'E';//Identificar o FIM do quadro (End)
    final char byteDeEscape = '/';//Caractere de escape especial

    this.computador.camadaEnlace.addText("\n\tByte de Inicio de Quadro ["+byteFlagStart+"]");
    this.computador.camadaEnlace.addText("\n\tByte de Fim de Quadro ["+byteFlagEnd+"]");
    this.computador.camadaEnlace.addText("\n\tByte de Escape de Quadro ["+byteDeEscape+"]\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);


    //Quantidade de Bytes de CargaUtil em cada Quadro = Cada Quadro tem no maximo 32 bits
    final int quantidadeDeCaracteres = 1;//8 de bits de carga util (IC)

    int quantidadeDeBytes = 0;//Armazenar a quantidade Bytes que tem no vetor de Inteiros
    for (int inteiro : quadro) {//Verificando o vetor de Inteiros
      quantidadeDeBytes += ManipuladorDeBit.quantidadeDeBytes(inteiro);//Adicionando a quantidade de Bytes
    }

    //Calculando o novo tamanho do vetor de Quadros
    int novoTamanho = quadro.length;
    Quadro[] quadroEnquadrado = new Quadro[novoTamanho];//Novo Vetor com os Quadros
    int posQuadro = 0;//Posicao do Vetor de Quadros

    int novoQuadro = 0;//Novo Quadro
    Queue<Integer> cargaUtil = new LinkedList<>();//Fila de Carga Util

    //Pesquisando todo o vetor de Inteiros
    for (int i=0; i<quadro.length; i++) {
      int inteiro = quadro[i];
      int bytesInteiro = ManipuladorDeBit.quantidadeDeBytes(inteiro);

      //Pegando todos os Bytes do Inteiro e colocando na Fila de Carga Util
      for (int x=0; x<bytesInteiro; x++) {
        cargaUtil.add(ManipuladorDeBit.getPrimeiroByte(inteiro));
        inteiro <<= 8;
      }

      if (cargaUtil.size() >= quantidadeDeCaracteres) {
        int header = (int) byteFlagStart;//Informacao de Controle (IC) Inicio do Quadro

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + byteFlagStart + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          //VERIFICANDO SE O DADO EH UM DOS BYTES DE CONTROLE
          if (dado == (int) byteFlagStart) {//Caso for o byte de Flag (Start)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText(byteDeEscape + " ");
          } else if (dado == (int) byteFlagEnd) {//Caso for o byte de Flag (End)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText(byteDeEscape + " ");
          } else if (dado == (int) byteDeEscape) {//Caso for o byte de (Escape)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText(byteDeEscape + " ");
          }

          novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
          this.computador.camadaEnlace.addText(dado + " ");
        }

        int tail = (int) byteFlagEnd;//Informacao de Controle (IC) Fim do Quadro

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText(byteFlagEnd + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro

      } else if (i == (quadro.length-1)) {
        int header = (int) byteFlagStart;//Informacao de Controle (IC) Inicio do Quadro
        
        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + byteFlagStart + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          //VERIFICANDO SE O DADO EH UM DOS BYTES DE CONTROLE
          if (dado == (int) byteFlagStart) {//Caso for o byte de Flag (Start)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText((int) byteDeEscape + " ");
          } else if (dado == (int) byteFlagEnd) {//Caso for o byte de Flag (End)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText((int) byteDeEscape + " ");
          } else if (dado == (int) byteDeEscape) {//Caso for o byte de (Escape)
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro, (int) byteDeEscape);//Adicionando Byte de Escape
            this.computador.camadaEnlace.addText((int) byteDeEscape + " ");
          }

          novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
          this.computador.camadaEnlace.addText(dado + " ");
        }

        int tail = (int) byteFlagEnd;//Informacao de Controle (IC) Fim do Quadro

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText(byteFlagEnd + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro
      
      }
    }

    return quadroEnquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoInsercaoDeBits
  * Funcao: Dividir a mensagem em quadros adicionando os bits [01111110] como Informacao de Controle
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : Quadro[]
  *********************************************/
  private Quadro[] enquadramentoInsercaoDeBits(int... quadro) throws Exception {
    System.out.println("\n\t[Insercao de Bits]\n");
    this.computador.camadaEnlace.addText("\n\t[Insercao de Bits]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    //Byte Flag que contem a sequencia de bits "0111110"
    final int byteFlag = 126;//00000000 00000000 00000000 01111110
    this.computador.camadaEnlace.addText("\n\t[Bits de Flag [01111110] = 126]\n\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    //Quantidade de Bytes de CargaUtil em cada Quadro = Cada Quadro tem no maximo 32 bits
    final int quantidadeDeCaracteres = 1;//8 de bits de carga util (IC)

    int quantidadeDeBytes = 0;//Armazenar a quantidade Bytes que tem no vetor de Inteiros
    for (int inteiro : quadro) {//Verificando o vetor de Inteiros
      quantidadeDeBytes += ManipuladorDeBit.quantidadeDeBytes(inteiro);//Adicionando a quantidade de Bytes
    }

    //Calculando o novo tamanho do vetor de Quadros
    int novoTamanho = quadro.length;
    Quadro[] quadroEnquadrado = new Quadro[novoTamanho];//Novo Vetor de Quadros
    int posQuadro = 0;//Posicao do Vetor de Quadros

    int novoQuadro = 0;//Novo Quadro
    Queue<Integer> cargaUtil = new LinkedList<>();//Fila de Carga Util


    //Pesquisando todo o vetor de Inteiros
    for (int i=0; i<quadro.length; i++) {
      int inteiro = quadro[i];
      int bytesInteiro = ManipuladorDeBit.quantidadeDeBytes(inteiro);

      //Pegando todos os Bytes do Inteiro e colocando na Fila de Carga Util
      for (int x=0; x<bytesInteiro; x++) {
        cargaUtil.add(ManipuladorDeBit.getPrimeiroByte(inteiro));
        inteiro <<= 8;
      }

      if (cargaUtil.size() >= quantidadeDeCaracteres) {
        int header = byteFlag;//Inforacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);

          if (cincoBits1) {
            int auxiliar = ManipuladorDeBit.deslocarBits(dado);
            int novoDado1 = 0;
            int novoDado2 = 0;

            //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
            int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
            //Para cada bit exibe 0 ou 1
            for (int b=1, cont=0; b<=8; b++) {
              //Utiliza displayMask para isolar o bit
              int bit = (auxiliar & displayMask) == 0 ? 0 : 1;

              if (cont == 5) {//Quando encontrar os cinco 1's seguidos
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= 0;//Adicionando o bit ao novoDado
                cont = 0;//Zerando o contador
              }

              if (b == 8) {//Caso chegou no ultimo bit
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado1);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado1 + " ");
                novoDado2 =  ManipuladorDeBit.adicionarBitNaPosicao(novoDado2,bit,25);
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado2);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado2 + " ");
              } else {
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= bit;//Adicionando o bit ao novoDado
                auxiliar <<= 1;//Desloca o valor uma posicao para a esquerda
              }

              if (bit == 1) {
                cont++;
              }

            }

          } else {//Caso nao tem uma sequencia de 5 Bits 1's
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
            this.computador.camadaEnlace.addText(dado + " ");
          }
        }

        int tail = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText(tail + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro

      } else if (i == (quadro.length-1)) {
        int header = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);

          if (cincoBits1) {
            int auxiliar = ManipuladorDeBit.deslocarBits(dado);
            int novoDado1 = 0;
            int novoDado2 = 0;

            //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
            int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
            //Para cada bit exibe 0 ou 1
            for (int b=1, cont=0; b<=8; b++) {
              //Utiliza displayMask para isolar o bit
              int bit = (auxiliar & displayMask) == 0 ? 0 : 1;

              if (cont == 5) {//Quando encontrar os cinco 1's seguidos
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= 0;//Adicionando o bit ao novoDado
                cont = 0;//Zerando o contador
              }

              if (b == 8) {//Caso chegou no ultimo bit
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado1);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado1 + " ");
                novoDado2 =  ManipuladorDeBit.adicionarBitNaPosicao(novoDado2,bit,25);
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado2);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado2 + " ");
              } else {
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= bit;//Adicionando o bit ao novoDado
                auxiliar <<= 1;//Desloca o valor uma posicao para a esquerda
              }

              if (bit == 1) {
                cont++;
              }

            }

          } else {//Caso nao tem uma sequencia de 5 Bits 1's
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
            this.computador.camadaEnlace.addText(dado + " ");
          }
        }

        int tail = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        // Painel.CAMADAS_TRANSMISSORAS.camadaEnlace(tail + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro
      
      }
    }

    return quadroEnquadrado;
  }

  /*********************************************
  * Metodo: enquadramentoViolacaoCamadaFisica
  * Funcao: Violar a Camada Fisica para que ela determine o inicio e o fim de um quadro
  * Parametros: quadro : int[]
  * Retorno: quadroEnquadrado : Quadro[]
  *********************************************/
  private Quadro[] enquadramentoViolacaoCamadaFisica(int... quadro) throws Exception {
    System.out.println("\n\t[Violacao da Camada Fisica]");
    this.computador.camadaEnlace.addText("\n\t[Violacao da Camada Fisica]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    final int byteFlag = 255;//00000000 00000000 00000000 11111111
    this.computador.camadaEnlace.addText("\n\t[Bits de Flag [1111111] = 255]\n\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    //Quantidade de Bytes de CargaUtil em cada Quadro = Cada Quadro tem no maximo 32 bits
    final int quantidadeDeCaracteres = 1;//8 de bits de carga util (IC)

    int quantidadeDeBytes = 0;//Armazenar a quantidade Bytes que tem no vetor de Inteiros
    for (int inteiro : quadro) {//Verificando o vetor de Inteiros
      quantidadeDeBytes += ManipuladorDeBit.quantidadeDeBytes(inteiro);//Adicionando a quantidade de Bytes
    }

    //Calculando o novo tamanho do vetor de Quadros
    int novoTamanho = quadro.length;
    Quadro[] quadroEnquadrado = new Quadro[novoTamanho];//Novo Vetor de Quadros
    int posQuadro = 0;//Posicao do Vetor de Quadros

    int novoQuadro = 0;//Novo Quadro
    Queue<Integer> cargaUtil = new LinkedList<>();//Fila de Carga Util


    //Pesquisando todo o vetor de Inteiros
    for (int i=0; i<quadro.length; i++) {
      int inteiro = quadro[i];
      int bytesInteiro = ManipuladorDeBit.quantidadeDeBytes(inteiro);

      //Pegando todos os Bytes do Inteiro e colocando na Fila de Carga Util
      for (int x=0; x<bytesInteiro; x++) {
        cargaUtil.add(ManipuladorDeBit.getPrimeiroByte(inteiro));
        inteiro <<= 8;
      }

      if (cargaUtil.size() >= quantidadeDeCaracteres) {
        int header = byteFlag;//Inforacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);

          if (cincoBits1) {
            int auxiliar = ManipuladorDeBit.deslocarBits(dado);
            int novoDado1 = 0;
            int novoDado2 = 0;

            //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
            int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
            //Para cada bit exibe 0 ou 1
            for (int b=1, cont=0; b<=8; b++) {
              //Utiliza displayMask para isolar o bit
              int bit = (auxiliar & displayMask) == 0 ? 0 : 1;

              if (cont == 5) {//Quando encontrar os cinco 1's seguidos
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= 0;//Adicionando o bit ao novoDado
                cont = 0;//Zerando o contador
              }

              if (b == 8) {//Caso chegou no ultimo bit
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado1);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado1 + " ");
                novoDado2 =  ManipuladorDeBit.adicionarBitNaPosicao(novoDado2,bit,25);
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado2);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado2 + " ");
              } else {
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= bit;//Adicionando o bit ao novoDado
                auxiliar <<= 1;//Desloca o valor uma posicao para a esquerda
              }

              if (bit == 1) {
                cont++;
              }

            }

          } else {//Caso nao tem uma sequencia de 5 Bits 1's
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
            this.computador.camadaEnlace.addText(dado + " ");
          }
        }

        int tail = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText(tail + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro

      } else if (i == (quadro.length-1)) {
        int header = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,header);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText("\n\tQuadro [ " + header + " ");

        for (int carac=0, tam=cargaUtil.size(); (carac<quantidadeDeCaracteres) && (carac<tam); carac++) {
          int dado = cargaUtil.poll();

          Boolean cincoBits1 = ManipuladorDeBit.cincoBitsSequenciais(dado,1);

          if (cincoBits1) {
            int auxiliar = ManipuladorDeBit.deslocarBits(dado);
            int novoDado1 = 0;
            int novoDado2 = 0;

            //Cria um inteiro com 1 no bit mais a esquerda e 0s em outros locais
            int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
            //Para cada bit exibe 0 ou 1
            for (int b=1, cont=0; b<=8; b++) {
              //Utiliza displayMask para isolar o bit
              int bit = (auxiliar & displayMask) == 0 ? 0 : 1;

              if (cont == 5) {//Quando encontrar os cinco 1's seguidos
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= 0;//Adicionando o bit ao novoDado
                cont = 0;//Zerando o contador
              }

              if (b == 8) {//Caso chegou no ultimo bit
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado1);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado1 + " ");
                novoDado2 =  ManipuladorDeBit.adicionarBitNaPosicao(novoDado2,bit,25);
                novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,novoDado2);//Adicionando Carga Util
                this.computador.camadaEnlace.addText(novoDado2 + " ");
              } else {
                novoDado1 <<= 1;//Deslocando 1 bit para a esquerda
                novoDado1 |= bit;//Adicionando o bit ao novoDado
                auxiliar <<= 1;//Desloca o valor uma posicao para a esquerda
              }

              if (bit == 1) {
                cont++;
              }

            }

          } else {//Caso nao tem uma sequencia de 5 Bits 1's
            novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,dado);//Adicionando Carga Util
            this.computador.camadaEnlace.addText(dado + " ");
          }
        }

        int tail = byteFlag;//Informacao de Controle

        novoQuadro = ManipuladorDeBit.inserirBits(novoQuadro,tail);//Adicionando a Informacao de Controle
        this.computador.camadaEnlace.addText(tail + " ]");
        Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

        quadroEnquadrado[posQuadro++] = new Quadro(novoQuadro);//Adicionando o Novo Quadro no Vetor
        ManipuladorDeBit.imprimirBits(novoQuadro);
        novoQuadro = 0;//Zerando todos os 32 bits do novoQuadro
      
      }
    }

    return quadroEnquadrado;
  }



  /*********************************************
  * Metodo: camadaEnlaceTransmissoraControleDeErro
  * Funcao: Aplica o Controle de Erro nos quadros
  * Parametros: quadros : Quadro[]
  * Retorno: quadro[]
  *********************************************/
  public Quadro[] camadaEnlaceTransmissoraControleDeErro(Quadro... quadros) throws Exception {
    System.out.println("\n\tCONTROLE DE ERRO");
    this.computador.camadaEnlace.addText("\nCONTROLE DE ERRO\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    Quadro[] quadroControleErro = null;

    //Recebendo o controle de erro escolhido pelo usuario
    int tipoDeControleErro = Painel.CONFIGURACOES.controleErro.getIndiceSelecionado();

    switch(tipoDeControleErro) {
      case 0:
        quadroControleErro = controleDeErroBitParidadePar(quadros);
        break;
      case 1:
        quadroControleErro = controleDeErroBitParidadeImpar(quadros);
        break;
      case 2:
        quadroControleErro = controleDeErroCRC(quadros);
        break;
      case 3:
        quadroControleErro = controleDeErroCodigoDeHamming(quadros);
        break;
    }

    this.computador.camadaEnlace.addText("\n");


    return quadroControleErro;
  }

  /*********************************************
  * Metodo: controleDeErroBitParidadePar
  * Funcao: Controle de Erro com algoritmo Bit de Paridade PAR
  * Parametros: quadros : Quadro[]
  * Retorno: quadro[]
  *********************************************/
  private Quadro[] controleDeErroBitParidadePar(Quadro[] quadros) throws Exception {
    System.out.println("\n\t[Bit de Paridade Par]");
    this.computador.camadaEnlace.addText("\n\t[Bit de Paridade Par]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (Quadro quadro : quadros) {
      int[] bitsQuadro = quadro.getBits();//Guardando os Bits do Quadro
      //Quantidade de Bytes do ultimo Inteiro do Vetor
      int quantBytesUltimoInteiro = ManipuladorDeBit.quantidadeDeBytes(bitsQuadro[bitsQuadro.length-1]);

      if (quantBytesUltimoInteiro < 4) {//Caso nao tiver os 4 bytes preenchidos
        bitsQuadro[bitsQuadro.length-1] <<= 8;//Desloca 8 bits para esquerda
      } else {//Cria um novo inteiro para armazenar a Informacao de Controle
        //Criando novo vetor de inteiros
        int[] novoQuadro = new int[bitsQuadro.length+1];
        for (int i=0; i<bitsQuadro.length; i++) {
          novoQuadro[i] = bitsQuadro[i];//Copiando cada Inteiro do Quadro
        }
        novoQuadro[bitsQuadro.length] = 0;//Atribuindo inteiro zero a ultima posicao
        bitsQuadro = novoQuadro;//Atribuindo novoQuadro ao bitsQuadro
      }
      
      //Recebendo a quantidade de Bits 1 do quadro
      int bits1 = ManipuladorDeBit.quantidadeBits1(quadro.getBits());
      this.computador.camadaEnlace.addText("\n\tAdicionando Bit [");
      if (bits1 % 2 == 0) {//Caso for PAR
        bitsQuadro[bitsQuadro.length-1] |= 0;//Adiciona [0]
        this.computador.camadaEnlace.addText("0]");
      } else {//Caso for IMPAR
        bitsQuadro[bitsQuadro.length-1] |= 1;//Adiciona [1]
        this.computador.camadaEnlace.addText("1]");
      }
      //Guarda os Bits dentro do Quadro novamente
      quadro.setBits(bitsQuadro);
      
      ManipuladorDeBit.imprimirBits(bitsQuadro[bitsQuadro.length-1]);

    }

    return quadros;
  }

  /*********************************************
  * Metodo: controleDeErroBitParidadeImpar
  * Funcao: Controle de Erro com algoritmo Bit de Paridade Impar
  * Parametros: quadros : Quadro[]
  * Retorno: quadro[]
  *********************************************/
  private Quadro[] controleDeErroBitParidadeImpar(Quadro[] quadros) throws Exception {
    System.out.println("\n\t[Bit de Paridade Impar]");
    this.computador.camadaEnlace.addText("\n\t[Bit de Paridade Impar]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (Quadro quadro : quadros) {
      int[] bitsQuadro = quadro.getBits();//Guardando os Bits do Quadro
      //Quantidade de Bytes do ultimo Inteiro do Vetor
      int quantBytesUltimoInteiro = ManipuladorDeBit.quantidadeDeBytes(bitsQuadro[bitsQuadro.length-1]);

      if (quantBytesUltimoInteiro < 4) {//Caso nao tiver os 4 bytes preenchidos
        bitsQuadro[bitsQuadro.length-1] <<= 8;//Desloca 8 bits para esquerda
      } else {//Cria um novo inteiro para armazenar a Informacao de Controle
        //Criando novo vetor de inteiros
        int[] novoQuadro = new int[bitsQuadro.length+1];
        for (int i=0; i<bitsQuadro.length; i++) {
          novoQuadro[i] = bitsQuadro[i];//Copiando cada Inteiro do Quadro
        }
        novoQuadro[bitsQuadro.length] = 0;//Atribuindo inteiro zero a ultima posicao
        bitsQuadro = novoQuadro;//Atribuindo novoQuadro ao bitsQuadro
      }
      
      //Recebendo a quantidade de Bits 1 do quadro
      int bits1 = ManipuladorDeBit.quantidadeBits1(quadro.getBits());
      this.computador.camadaEnlace.addText("\n\tAdicionando Bit [");
      if (bits1 % 2 == 0) {//Caso for PAR
        bitsQuadro[bitsQuadro.length-1] |= 1;//Adiciona [0]
        this.computador.camadaEnlace.addText("1]");
      } else {//Caso for IMPAR
        bitsQuadro[bitsQuadro.length-1] |= 0;//Adiciona [1]
        this.computador.camadaEnlace.addText("0]");
      }
      //Guarda os Bits dentro do Quadro novamente
      quadro.setBits(bitsQuadro);

    }

    return quadros;
  }

  /*********************************************
  * Metodo: controleDeErroCRC
  * Funcao: Controle de Erro com algoritmo CRC
  * Parametros: quadros : Quadro[]
  * Retorno: quadro[]
  *********************************************/
  private Quadro[] controleDeErroCRC(Quadro[] quadros) throws Exception {
    System.out.println("\n\t[CRC]");
    this.computador.camadaEnlace.addText("\n\t[CRC]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    final int[] polinomioCRC = {1,0,0,0,0,0,1,0,0,1,1,0,0,0,0,0,1,0,0,0,1,1,1,0,1,1,0,1,1,0,1,1,1};
    final int grauPolinomio = polinomioCRC.length-1;

    this.computador.camadaEnlace.addText("\n\tPolinomio CRC\n");
    this.computador.camadaEnlace.addText("100000100110000010001110110110111");


    for (Quadro quadro : quadros) {
      int[] bitsQuadro = quadro.bitsVetor();

      int tamanho = (polinomioCRC.length+bitsQuadro.length)-1; 

      int[] dividendo = new int[tamanho];
      int[] resto = new int[tamanho];

      for (int i=0; i<bitsQuadro.length; i++) {
        dividendo[i] = bitsQuadro[i];
        System.out.print(bitsQuadro[i]);
      }

      for (int i=0; i<dividendo.length; i++) {
        resto[i] = dividendo[i];
      }

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


      for (int i=0; i<dividendo.length; i++) {
        resto[i] = (dividendo[i] ^ resto[i]);
      }

      System.out.println();
      System.out.println("CRC : ");
      for(int i=0;i<resto.length;i++) {
        System.out.print(resto[i]);
      }

      //------------------------
      int tam = resto.length/32;
      if (resto.length%32 != 0) {
        tam++;
      }
      System.out.println("Novo tam " + tam);
      int[] vetor = new int[tam];
      int novoInteiro = 0;
      for (int i=1, pos=0; i<=resto.length; i++) {
        novoInteiro <<= 1;//Deslocando 1 bit para esquerda
        novoInteiro |= resto[i-1];
        if (i%32 == 0) {//Completou os 32 bits
          vetor[pos++] = novoInteiro;
          novoInteiro = 0;
        } else if (i == (resto.length) && novoInteiro != 0) {
          vetor[pos++] = novoInteiro;
        }
      }

      this.computador.camadaEnlace.addText("\n\tVetor Codificado");
      for (int i : vetor) {
        this.computador.camadaEnlace.addText("\n" + ManipuladorDeBit.imprimirBits(i));
      }

      quadro.setBits(vetor);

    }

    return quadros;
  }

  /*********************************************
  * Metodo: controleDeErroCodigoDeHamming
  * Funcao: Controle de Erro com algoritmo Codigo de Hamming
  * Parametros: quadros : Quadro[]
  * Retorno: quadro[]
  *********************************************/
  private Quadro[] controleDeErroCodigoDeHamming(Quadro[] quadros) throws Exception {
    System.out.println("\n\t[Codigo de Hamming]");
    this.computador.camadaEnlace.addText("\n\t[Codigo de Hamming]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (Quadro quadro : quadros) {//Percorrendo todo o vetor de quadros
      int[] bitsQuadro2 = quadro.getBits();
      //int[] bitsQuadro = {1,0,1,0,1,0,1};

      this.computador.camadaEnlace.addText("\n\tBits do Quadro");
      System.out.println("\n\tBits do Quadro");
      for (int i : bitsQuadro2) {
        System.out.print("\t");
        this.computador.camadaEnlace.addText("\n"+ManipuladorDeBit.imprimirBits(i));
      }

      bitsQuadro2 = ManipuladorDeBit.bitsParaInteiros(bitsQuadro2);

      
      String bitsMensagem = "";
      System.out.println("\n\n\tBits do Vetor");
      for (int i=0; i<bitsQuadro2.length; i++) {
        //System.out.print(bitsQuadro2[i]);
        int numero = bitsQuadro2[i];
        numero = ManipuladorDeBit.deslocarBits(numero);

        int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
        for (int bits=0; bits<8; bits++) {
          bitsMensagem += (numero & displayMask) == 0 ? '0' : '1';
          numero <<= 1;//Desloca 1 bits para esquerda
        }

      }
      System.out.println("\t"+bitsMensagem);


      System.out.println("\n\tADICIONANDO CADA BIT EM UMA POSISCAO DO VETOR");
      System.out.print("\t");
      int[] bitsQuadro = new int[bitsMensagem.length()];
      for (int i=0; i<bitsMensagem.length(); i++) {
        bitsQuadro[i] = Integer.parseInt(String.valueOf(bitsMensagem.charAt(i)));
        System.out.print(bitsQuadro[i]);
      }
      System.out.println("\n");



      System.out.println("\n\tCALCULANDO O NOVO TAMANO DO VETOR");
      int tamanhoAntigo = bitsQuadro.length;//Quantidade de Bits da mensagem
      int quantidadeBitsDeControle = 0;//Numero de bits de controle para adicionar na mensagem
      //CALCULANDO O NOVO TAMANHO
      for (int i=0; i<bitsQuadro.length;) {
        if (Math.pow(2,quantidadeBitsDeControle) == (i+quantidadeBitsDeControle+1)) {
          quantidadeBitsDeControle++;
        } else {
          i++;
        }
      }
      System.out.println(quantidadeBitsDeControle);
      System.out.println("\tNOVO TAMANHO " + (tamanhoAntigo+quantidadeBitsDeControle));
      int[] novoVetor = new int[tamanhoAntigo + quantidadeBitsDeControle];

      System.out.println("\n\tADICIONANDO OS BITS DE DADOS NO NOVO VETOR DA BASE 2");
      //ADICIONANDO OS BITS DE DADOS NO NOVO VETOR NAS POSICOES QUE NAO SAO COMPOSTA DA BASE 2
      for (int i=1, j=0, k=0; i<= novoVetor.length; i++) {
        if (Math.pow(2,j) == i) {
          j++;
        } else {
          novoVetor[k+j] = bitsQuadro[k++];
        }
      }
      System.out.print("\t");
      for (int i : novoVetor) {
       System.out.print(i);
      }
      System.out.println("\n");




      //ADICIONANDO OS BITS DE PARIDADE
      System.out.println("\tADICIONANDO OS BITS DE CONTROLE DE PARIDADE PAR");
      for (int i=0; i<quantidadeBitsDeControle; i++) {
        novoVetor[((int) Math.pow(2,i))-1] = obterParidadePar(novoVetor, i);
        System.out.println((((int) Math.pow(2,i))-1));
        System.out.println("\t"+obterParidadePar(novoVetor, i));
      }

      System.out.println("\tVETOR COM INFORMACOES DE CONTROLE");
      System.out.print("\t");
      for (int x : novoVetor) {
        System.out.print(x);
      }



      System.out.println("\n\n");


      //---------------------------------------------------------



      System.out.println("\tCALCULANDO O TAMANHO DO NOVO VETOR DE BITS");
      int tam = (novoVetor.length+1)/32;
      if (novoVetor.length%32 != 0) {
        tam++;
      }
      System.out.println("\tNovo tam " + tam);
      int[] vetor = new int[tam];
      int novoInteiro = 0;

      System.out.println("\tADICIONANDO BIT 1 PARA CONTROLE");
      int bitsAdicionados = 0;
      //Adicionando o bit de controle que fala onde incia os bits de Hamming
      novoInteiro <<= 1;//Deslocando 1 bit para esquerda
      novoInteiro |= 1;//Adicionando bit 1
      bitsAdicionados++;

      for (int pos=0, cont=0; cont<novoVetor.length; cont++) {
        novoInteiro <<= 1;//Deslocando 1 bit para esquerda
        novoInteiro |= novoVetor[cont];
        bitsAdicionados++;

        if (bitsAdicionados%32 == 0) {//Completou os 32 bits
          vetor[pos++] = novoInteiro;
          novoInteiro = 0;
          bitsAdicionados = 0;

          //Adicionando o bit de controle que fala onde incia os bits de Hamming
          novoInteiro <<= 1;//Deslocando 1 bit para esquerda
          novoInteiro |= 1;//Adicionando bit 1
          bitsAdicionados++;

        } else if (cont == (novoVetor.length-1) && novoInteiro != 0) {
          vetor[pos++] = novoInteiro;
        }
      }

      System.out.println("\n\tBITS PARA TRANSMITIR");
      System.out.print("\t");
      for (int i : vetor) {
        ManipuladorDeBit.imprimirBits(i);
      }
      quadro.setBits(vetor);

    }

    return quadros;
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
  * Funcao: Gerencia o controle dos quadros que serao enviados
  * Parametros: quadros : Quadro[]
  * Retorno: void
  *********************************************/
  private void camadaEnlaceTransmissoraControleDeFluxo(Quadro... quadros) throws Exception {
    System.out.println("\n\tCONTROLE DE FLUXO");
    this.computador.camadaEnlace.addText("\nCONTROLE DE FLUXO\n");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

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
  * Funcao: Enviar os quadros com uma transmissao Janela Deslizante de 1 bit
  * Parametros: quadros : Quadro
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizante1Bit(Quadro... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante 1 Bit]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante 1 Bit]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (int i=0; i<quadros.length; i++) {

      //Bloqueia para enviar o proximo quadro depois de receber o ACK
      MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
      //Bloqueia pra nao enviar outro Quadro
      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      
      //Painel.CONFIGURACOES.setDesabilitarSlider(true);//Desativando o slider de ERRO
      Painel.CONFIGURACOES.setDisabilitar(true);//Desativando a mudanca das opcoes
      this.computador.camadaEnlace.addText("\n\tENVIANDO QUADRO " + quadros[i].getId());
      
      Temporizador temporizador = new Temporizador(quadros[i], computador);

      Painel.TEMPORIZADORES.adicionarTemporizador(temporizador);
      chamarProximaCamada(temporizador.getBits());
    }

  }

  /*********************************************
  * Metodo: controleDeFluxoJanelaDeslizanteGoBackN
  * Funcao: Eviar os quadros com um controle de Janela Deslizante Go Back N
  * Parametros: quadros : Quadro[]
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizanteGoBackN(Quadro... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante Go Back N]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante Go Back N]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (int i=0; i<quadros.length; i++) {

      System.out.println("Enviar Quadro");
      System.out.println("SEMAPHORO_FLUXO: " + MeioDeComunicacao.SEMAPHORO_FLUXO.availablePermits());
      System.out.println("SEMAPHORO_QUADRO: " + MeioDeComunicacao.SEMAPHORO_QUADRO.availablePermits());

      MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      
      //Painel.CONFIGURACOES.setDesabilitarSlider(true);//Desativando o slider de ERRO
      Painel.CONFIGURACOES.setDisabilitar(true);//Desativando a mudanca das opcoes
      this.computador.camadaEnlace.addText("\n\tENVIANDO QUADRO " + quadros[i].getId());
      
      Temporizador temporizador = new Temporizador(quadros[i], computador);

      //computador

      Painel.TEMPORIZADORES.adicionarTemporizador(temporizador);
      chamarProximaCamada(temporizador.getBits());
    }

  }

  /*********************************************
  * Metodo: controleDeFluxoJanelaDeslizanteComRestricaoSeletiva
  * Funcao: Enviar os quadros fazendo um controle de janela deslizente seletica
  * Parametros: quadros : Quadro[]
  * Retorno: void
  *********************************************/
  private void controleDeFluxoJanelaDeslizanteComRestricaoSeletiva(Quadro... quadros) throws Exception {
    System.out.println("\n\t[Janela Deslizante com Restricao Seletiva]");
    this.computador.camadaEnlace.addText("\n\t[Janela Deslizante com Restricao Seletiva]");
    Thread.sleep(AplicacaoTransmissora.VELOCIDADE);

    for (int i=0; i<quadros.length; i++) {

      System.out.println("Enviar Quadro");
      System.out.println("SEMAPHORO_FLUXO: " + MeioDeComunicacao.SEMAPHORO_FLUXO.availablePermits());
      System.out.println("SEMAPHORO_QUADRO: " + MeioDeComunicacao.SEMAPHORO_QUADRO.availablePermits());

      MeioDeComunicacao.SEMAPHORO_FLUXO.acquire();
      MeioDeComunicacao.SEMAPHORO_QUADRO.acquire();
      
      //Painel.CONFIGURACOES.setDesabilitarSlider(true);//Desativando o slider de ERRO
      Painel.CONFIGURACOES.setDisabilitar(true);//Desativando a mudanca das opcoes
      this.computador.camadaEnlace.addText("\n\tENVIANDO QUADRO " + quadros[i].getId());
      
      Temporizador temporizador = new Temporizador(quadros[i], computador);

      Painel.TEMPORIZADORES.adicionarTemporizador(temporizador);
      chamarProximaCamada(temporizador.getBits());
    }

  }

}//Fim class