#include <AFMotor.h>
//#include <DHT.h>
//#include <DHT_U.h>

//#define DHT11 11

class Robo {
  
  char comando;
  int velocidade;
  AF_DCMotor *rodaDD; //Dianteira Direita
  AF_DCMotor *rodaDE; //Dianteira Esquerda
  AF_DCMotor *rodaTD; //Traseira Direita
  AF_DCMotor *rodaTE; //Traseira Esquerda
  
  public: 
    Robo(char, int, int, int, int, int);
    void setComando(char _comando){
      this->comando = _comando;
    };
    char getComando(){
      return toupper(comando);
    };
    void setVelocidade(int _velocidade){
      this->velocidade = _velocidade;
    };
    int getVelocidade(){
      return velocidade;  
    }; 
    void definirVelocidade(){
      for(int i = 1; this->velocidade > i; i++){
        rodaDD->setSpeed(i);
        rodaDE->setSpeed(i);
        rodaTD->setSpeed(i);
        rodaTE->setSpeed(i);
      }
    };
    void virarAEsquerda(){
      definirVelocidade(); 
      // rodas esquerdas param
      rodaDD->run(FORWARD);
      rodaDE->run(BACKWARD);
      rodaTD->run(FORWARD);
      rodaTE->run(BACKWARD);
      Serial.write("Virando a esquerda");
    }
    void virarADireita(){
      definirVelocidade(); 
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
    void moverParaFrente(){
      // aciona todas as rodas
      definirVelocidade(); 
      
      rodaDD->run(FORWARD);
      rodaDE->run(FORWARD);
      rodaTD->run(FORWARD);
      rodaTE->run(FORWARD);
      Serial.write("Andando para frente");
    }
    void moverParaTras(){
      // aciona todas as rodas
      definirVelocidade(); 
      rodaDD->run(BACKWARD);
      rodaDE->run(BACKWARD);
      rodaTD->run(BACKWARD);
      rodaTE->run(BACKWARD);
      Serial.write("Andando para tras");
    }
}; 

Robo::Robo(char _comando, int _velocidade, int _rodaDD, int _rodaDE, int _rodaTD, int _rodaTE) { 
  comando = _comando;
  velocidade = _velocidade;
  rodaDD = new AF_DCMotor(_rodaDD);
  rodaDE = new AF_DCMotor(_rodaDE);
  rodaTD = new AF_DCMotor(_rodaTD);
  rodaTE = new AF_DCMotor(_rodaTE);
}

Robo *curtoCircuito = new Robo('a', 255, 1, 2, 3, 4);

long previousMillis = 0;
 
long interval = 1000;

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

  unsigned long currentMillis = millis();
  
  char data;
  if(Serial1.available()){
    data = Serial1.read();
    curtoCircuito->setComando(data);
  }
  
    switch(curtoCircuito->getComando()){
     
     case 'A':
        if(currentMillis - previousMillis > interval) {
          curtoCircuito->virarAEsquerda();
          previousMillis = currentMillis;   
        }
        break;
     case 'B':
        if(currentMillis - previousMillis > interval) {
          curtoCircuito->virarADireita();
          previousMillis = currentMillis;   
        }
        
        break;
     case 'C':
        if(currentMillis - previousMillis > interval) {
          curtoCircuito->moverParaFrente();
          previousMillis = currentMillis;   
        }
        
        break;
     case 'D':
        if(currentMillis - previousMillis > interval) {
          curtoCircuito->moverParaTras();
          previousMillis = currentMillis;   
        }
        
        break;
     case 'E':
        if(currentMillis - previousMillis > interval) {
          curtoCircuito->pararRobo();
          previousMillis = currentMillis;   
        }
        break;
    }
}
