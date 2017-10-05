#include <AFMotor.h>
#include <DHT.h>

#define DHTPIN A11    
#define DHTTYPE DHT11
#define LED 22

DHT dht(DHTPIN, DHTTYPE);

float umidade;
float temperatura;
int ldrValue;

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
      this->comando = toupper(_comando);
    };
    char getComando(){
      return comando;
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

Robo *curtoCircuito = new Robo('z', 255, 1, 2, 3, 4);

long previousMillis = 0;
 
long interval = 1000;

void setup() 
{ 
  //Inicia a serial do bluetooth
  Serial1.begin(9600);
   
  //Inicia a serial 
  Serial.begin(9600);

  //Inicia o dht
  dht.begin();
  
  //Definindo pinMode do LED
  pinMode(LED, OUTPUT);
} 
void lerDht(){
  delay(1000);

  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  umidade = dht.readHumidity();
  // Read temperature as Celsius (the default)
  temperatura = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)

  // Check if any reads failed and exit early (to try again).
  if (isnan(umidade) || isnan(temperatura)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  Serial.print("Umidade: ");
  Serial.print(umidade);
  Serial.print(" %\t");
  Serial.print("Temperatura: ");
  Serial.print(temperatura);
  Serial.print(" *C ");
}
void acionarLed(){
  digitalWrite(LED, HIGH);   
  delay(1000);                       
  digitalWrite(LED, LOW);    
  delay(1000);   
}
void readLdr(){
  // read the input on analog pin 0:
  ldrValue = analogRead(A10);
  // print out the value you read:
  Serial.println("LDR:");
  Serial.println(ldrValue);
  delay(1);        // delay in between reads for stability  
}

void enviarDadosDhtLdr(){
  String dado = "";
  dado.concat("T");
  dado.concat(temperatura);
  dado.concat("U") ;
  dado.concat(umidade);
  dado.concat("L");
  dado.concat(ldrValue);
  for(int indice = 0; dado.length() < indice; indice++){
    Serial1.write(dado.charAt(indice));
  }
}

void loop() {
  //Lendo umidade, temperatura e luminosidade
  lerDht();
  
  //Acionando led
  acionarLed();

  //Lendo Ldr
  readLdr();
  
  unsigned long currentMillis = millis();
  enviarDadosDhtLdr();
  if(Serial1.available()){
    curtoCircuito->setComando(Serial1.read());
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
  
}
