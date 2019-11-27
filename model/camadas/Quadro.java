/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 17/03/18
* Ultima alteracao: 20/05/18
* Nome: Quadro
* Funcao: Armazenar os Bits que compoe um Quadro
***********************************************************************/

package model.camadas;

import util.ManipuladorDeBit;
import java.util.List;
import java.util.ArrayList;

public class Quadro {
  private int id;//Identificador do Quadro
  private int[] bits;//Bits do Quadro
  private int tamanhoBits;//Tamanho do Quadro em bits
  private int quantidadeBitsSignificativos;
  private boolean enviado = false;

  /*********************************************
  * Metodo: Temporizador - Construtor
  * Funcao: Criar objetos da Classe Temporizador
  * Parametros: quadro : Quadro
  *********************************************/
  public Quadro(int... bits) {
    this.bits = bits;
    for (int dado : this.bits) {
      tamanhoBits += ManipuladorDeBit.quantidadeDeBits(dado);
      quantidadeBitsSignificativos += Integer.toBinaryString(dado).length();
    }
  }

  /*********************************************
  * Metodo: setBits
  * Funcao: Atribui os bits desse Quadro
  * Parametros: bits : int[]
  * Retorno: void
  *********************************************/
  public void setBits(int... bits) {
    this.bits = bits;
    for (int dado : bits) {
      tamanhoBits += ManipuladorDeBit.quantidadeDeBits(dado);
      quantidadeBitsSignificativos += Integer.toBinaryString(dado).length();
    }
  }

  /*********************************************
  * Metodo: getBits
  * Funcao: Retorna os Bits do Quadro
  * Parametros: void
  * Retorno: bits : int[]
  *********************************************/
  public int[] getBits() {
    return bits;
  }

  /*********************************************
  * Metodo: getBitsSignificativos
  * Funcao: Retorna os bits significativos do quadro
  * Parametros: void
  * Retorno: int
  *********************************************/
  public int getBitsSignificativos() {
    return quantidadeBitsSignificativos;
  }

  /*********************************************
  * Metodo: bitsVetor
  * Funcao: Retorna os Bits como vetor
  * Parametros: void
  * Retorno: vetorDeBits : int[]
  *********************************************/
  public int[] bitsVetor() {
    int[] vetorDeBits = new int[32*bits.length];
    int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
    int pos = 0;
    for (int inteiro : bits) {
      int tam = ManipuladorDeBit.quantidadeDeBits(inteiro);
      inteiro = ManipuladorDeBit.deslocarBits(inteiro);
      for (int i=1; i<=tam; i++) {
        int bit = (inteiro & displayMask) == 0 ? 0 : 1;
        vetorDeBits[pos++] = bit;
        inteiro <<= 1;//Desloca o valor uma posicao para a esquerda
      }
    }
    return vetorDeBits;
  }

  /*********************************************
  * Metodo: 
  * Funcao: 
  * Parametros: 
  * Retorno: 
  *********************************************/
  public int[] bitsVetor3() {
    List<Integer> listaDeBits = new ArrayList<>();
    int displayMask = 1 << 31;//10000000 00000000 00000000 00000000
    int pos = 0;
    for (int inteiro : bits) {

      int tam = ManipuladorDeBit.quantidadeDeBits(inteiro);
      inteiro = ManipuladorDeBit.deslocarBits(inteiro);

      //-------------
      while (ManipuladorDeBit.pegarBitNaPosicao(inteiro,0) != 1) {
        inteiro <<= 1;//Deslocando 1 bit
        tam--;
      }
      //---------------

      for (int i=1; i<=tam; i++) {
        int bit = (inteiro & displayMask) == 0 ? 0 : 1;
        //vetorDeBits[pos++] = bit;
        listaDeBits.add(bit);
        inteiro <<= 1;//Desloca o valor uma posicao para a esquerda
      }
    }

    int[] vetorDeBits = new int[listaDeBits.size()];
    for (int i=0; i<listaDeBits.size(); i++) {
      vetorDeBits[i] = listaDeBits.get(i);
    }


    return vetorDeBits;
  }

  /*********************************************
  * Metodo: setId
  * Funcao: Atribuir o Id do Quadro
  * Parametros: id : int
  * Retorno: void
  *********************************************/
  public void setId(int id) {
    if (this.id == 0) {
      this.id = id;
      int quantDeBytes = ManipuladorDeBit.quantidadeDeBytes(bits[bits.length-1]);

      if (quantDeBytes < 4) {
        bits[bits.length-1] <<= 8;//Deslocando 8 bits para esquerda
        bits[bits.length-1] |= id;
      } else {
        int[] bitsAux = new int[bits.length+1];
        for (int i=0; i<bits.length; i++) {
          bitsAux[i] = bits[i];
        }
        bitsAux[bits.length] = id;
        this.bits = bitsAux;
      }
    }
  }

  /*********************************************
  * Metodo: getId
  * Funcao: Retorna os Id do Quadro
  * Parametros: void
  * Retorno: id : int
  *********************************************/
  public int getId() {
    return id;
  }

}//Fim class