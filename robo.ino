#include <AFMotor.h>
//#include <DHT.h>
//#include <DHT_U.h>

//#define DHT11 11

class Robo {
  
  char comando;
  AF_DCMotor *rodaDD; //Dianteira Direita
  AF_DCMotor *rodaDE; //Dianteira Esquerda
  AF_DCMotor *rodaTD; //Traseira Direita
  AF_DCMotor *rodaTE; //Traseira Esquerda
  
  public: 
    Robo(char, int, int, int, int);
    void setComando(char _comando){
      comando = _comando;
    };
    char getComando(){
      return comando;
    }; 
    void acelerar(int velocidade){
      for(int i = 1; velocidade > i; i++){
        rodaDD->setSpeed(i);
        rodaDE->setSpeed(i);
        rodaTD->setSpeed(i);
        rodaTE->setSpeed(i);
      }
    };
    void desacelerar(int velocidade){
      for(int i = 1; velocidade > i; i++){
        int novaVelocidade = velocidade - i;
        rodaDD->setSpeed(novaVelocidade);
        rodaDE->setSpeed(novaVelocidade);
        rodaTD->setSpeed(novaVelocidade);
        rodaTE->setSpeed(novaVelocidade);
      }
    };
    void virarAEsquerda(int velocidade){
      acelerar(velocidade); 
      // rodas esquerdas param
      rodaDD->run(FORWARD);
      rodaDE->run(BACKWARD);
      rodaTD->run(FORWARD);
      rodaTE->run(BACKWARD);
      Serial.write("Virando a esquerda");
    }
    void virarADireita(int velocidade){
      acelerar(velocidade); 
      // rodas direitas param
      rodaDD->run(BACKWARD);
      rodaDE->run(FORWARD);
      rodaTD->run(BACKWARD);
      rodaTE->run(FORWARD);
      Serial.write("Virando a direita");
    }
    void pararRobo(){
      // para todas as rodas
      rodaDD->run(RELEASE);
      rodaDE->run(RELEASE);
      rodaTD->run(RELEASE);
      rodaTE->run(RELEASE);
      Serial.write("Parando robo");
    }
    void moverParaFrente(int velocidade){
      // aciona todas as rodas
      acelerar(velocidade); 
      
      rodaDD->run(FORWARD);
      rodaDE->run(FORWARD);
      rodaTD->run(FORWARD);
      rodaTE->run(FORWARD);
      Serial.write("Andando para frente");
    }
    void moverParaTras(int velocidade){
      // aciona todas as rodas
      acelerar(velocidade); 
      rodaDD->run(BACKWARD);
      rodaDE->run(BACKWARD);
      rodaTD->run(BACKWARD);
      rodaTE->run(BACKWARD);
      Serial.write("Andando para tras");
    }
}; 

Robo::Robo(char _comando, int _rodaDD, int _rodaDE, int _rodaTD, int _rodaTE) { 
  comando = _comando;
  rodaDD = new AF_DCMotor(_rodaDD);
  rodaDE = new AF_DCMotor(_rodaDE);
  rodaTD = new AF_DCMotor(_rodaTD);
  rodaTE = new AF_DCMotor(_rodaTE);
}

Robo *curtoCircuito = new Robo('a', 1, 2, 3, 4);

const unsigned LDR = A7;
const unsigned LED = 22;

void enviarDadosApp(){
  Serial1.println(Serial1.read());
}
char obterDadosBluetooth(){
  return Serial1.read();
}

void setup() 
{ 
  //Inicia a serial 
  Serial1.begin(9600); 
  Serial1.println("Digite os comandos AT :"); 
  //Inicia a serial configurada nas portas 10 e 11
  Serial.begin(9600); 
} 

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial1.available())  //verifica se tem dados disponíveis para leitura
  {
    
    //Serial.write(Serial1.read());   //reenvia para o computador o dado recebido
  }
  if (Serial.available())  //verifica se tem dados diponível para leitura
  {
    //Serial1.write(Serial.read());   //reenvia para o computador o dado recebido
  }

  curtoCircuito->setComando(Serial1.read());
  
  switch(toupper(curtoCircuito->getComando())){
   
   case 'A':
      curtoCircuito->virarAEsquerda(50);
      break;
   case 'B':
      curtoCircuito->virarADireita(50);
      break;
   case 'C':
      curtoCircuito->moverParaFrente(50);
      break;
   case 'D':
      curtoCircuito->moverParaTras(50);
      break;
   case 'E':
      curtoCircuito->pararRobo();
      break;
   case 'F':
     curtoCircuito->acelerar(50);
     break;
   case '0':
      curtoCircuito->desacelerar(50);
      break;
  }
 }
