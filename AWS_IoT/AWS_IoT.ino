//이 사물은 Location1으로 AWS에 등록하였다.
#include <ArduinoBearSSL.h>
#include <ArduinoECCX08.h>
#include <ArduinoMqttClient.h>
#include <WiFiNINA.h> // change to #include <WiFi101.h> for MKR1000

#include "arduino_secrets.h"
#include "Servomotor.h"

#include <ArduinoJson.h>


/////// Enter your sensitive data in arduino_secrets.h
const char ssid[]        = SECRET_SSID;
const char pass[]        = SECRET_PASS;
const char broker[]      = SECRET_BROKER;
const char* certificate  = SECRET_CERTIFICATE;

WiFiClient    wifiClient;            // Used for the TCP socket connection
BearSSLClient sslClient(wifiClient); // Used for SSL/TLS connection, integrates with ECC508
MqttClient    mqttClient(sslClient);

unsigned long lastMillis = 0;

//servomotor는 2번 pin
//RGB LED의 R은 4번, G는 5번 pin
Servomotor servo1(2,4,5);



void setup() {
  Serial.begin(115200);
  while (!Serial);
  
  if (!ECCX08.begin()) {
    Serial.println("No ECCX08 present!");
    while (1);
  }

  // Set a callback to get the current time
  // used to validate the servers certificate
  ArduinoBearSSL.onGetTime(getTime);

  // Set the ECCX08 slot to use for the private key
  // and the accompanying public certificate for it
  sslClient.setEccSlot(0, certificate);

  // Optional, set the client id used for MQTT,
  // each device that is connected to the broker
  // must have a unique client id. The MQTTClient will generate
  // a client id for you based on the millis() value if not set
  //
  // mqttClient.setId("clientId");

  // Set the message callback, this function is
  // called when the MQTTClient receives a message
  mqttClient.onMessage(onMessageReceived);

}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  if (!mqttClient.connected()) {
    // MQTT client is disconnected, connect
    connectMQTT();
  }

  // poll for new MQTT messages and send keep alives
  mqttClient.poll();
  
  // publish a message roughly every 5 seconds.
  if (millis() - lastMillis > 5000) {
    lastMillis = millis();
    char payload[512];
    getDeviceStatus(payload);
    sendMessage(payload);
  
  }
  
}

unsigned long getTime() {
  // get the current time from the WiFi module  
  return WiFi.getTime();
}

void connectWiFi() {
  Serial.print("Attempting to connect to SSID: ");
  Serial.print(ssid);
  Serial.print(" ");

  while (WiFi.begin(ssid, pass) != WL_CONNECTED) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the network");
  Serial.println();
}

void connectMQTT() {
  Serial.print("Attempting to MQTT broker: ");
  Serial.print(broker);
  Serial.println(" ");
  

  while (!mqttClient.connect(broker, 8883)) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the MQTT broker");
  Serial.println();

  // subscribe to a topic
  //이 사물은 Location1의 이름으로 디바이스 섀도우에 구독하였다.
  mqttClient.subscribe("$aws/things/Location1/shadow/update/delta");
}

void getDeviceStatus(char* payload) {
  //Read waterlevel
  float waterlevel = analogRead(A4);

  // Read servo status
  const char* bar = (servo1.getState() == OPEN)? "OPEN" : "CLOSE";

  // make payload for the device update topic ($aws/things/Location/shadow/update)
  sprintf(payload,"{\"state\":{\"reported\":{\"waterlevel\":\"%0.2f\",\"BAR\":\"%s\"}}}",waterlevel,bar);
}

void sendMessage(char* payload) {
  char TOPIC_NAME[]= "$aws/things/Location1/shadow/update";
  
  Serial.print("Publishing send message:");
  Serial.println(payload);
  mqttClient.beginMessage(TOPIC_NAME);
  mqttClient.print(payload);
  mqttClient.endMessage();
}


void onMessageReceived(int messageSize) {
  // we received a message, print out the topic and contents
  Serial.print("Received a message with topic '");
  Serial.print(mqttClient.messageTopic());
  Serial.print("', length ");
  Serial.print(messageSize);
  Serial.println(" bytes:");

  // store the message received to the buffer
  char buffer[512] ;
  int count=0;
  while (mqttClient.available()) {
     buffer[count++] = (char)mqttClient.read();
  }
  buffer[count]='\0'; // 버퍼의 마지막에 null 캐릭터 삽입
  Serial.println(buffer);
  Serial.println();

  DynamicJsonDocument doc(1024);
  deserializeJson(doc, buffer);
  JsonObject root = doc.as<JsonObject>();
  JsonObject state = root["state"];
  const char* bar = state["BAR"];
  Serial.println(bar);
  
  char payload[512];

  //CLOSE값이 들어오면 차단기를 내린다.
  //OPEN값이 들어오면 차단기를 올린다.
  if (strcmp(bar,"CLOSE")==0) {
    servo1.barClose(); 
    sprintf(payload,"{\"state\":{\"reported\":{\"BAR\":\"%s\"}}}","CLOSE");
    sendMessage(payload);
    
  } else if (strcmp(bar,"OPEN")==0) {
    servo1.barOpen();
    sprintf(payload,"{\"state\":{\"reported\":{\"BAR\":\"%s\"}}}","OPEN");
    sendMessage(payload);
    
  }
  
 }
