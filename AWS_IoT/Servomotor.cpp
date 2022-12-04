#include "Servomotor.h"
#include <Servo.h>

Servo servo;

Servomotor::Servomotor(int pin_S, int pin_R, int pin_G) {
  // Use 'this->' to make the difference between the
  // 'pin' attribute of the class and the 
  // 'pin_S'는 서보모터, 'pin_R'은 LED RED, 'pin_G'는 LED GREEN
  this->pin_S = pin_S;
  this->pin_R = pin_R;
  this->pin_G = pin_G;
  init();
}
void Servomotor::init() {
  servo.attach(pin_S);
  pinMode(pin_R,OUTPUT);
  pinMode(pin_G,OUTPUT);
  barOpen();
  state = OPEN;
}
void Servomotor::barOpen() { //OPEN 차단기의 각도를 0도로 옮긴다. 초록색LED로 통과해도 된다고 알려준다.
  servo.write(0);
  digitalWrite(pin_R, LOW);
  digitalWrite(pin_G, HIGH);
  state = OPEN;
}
void Servomotor::barClose() { //CLOSE 차단기의 각도를 90도로 옮긴다. 빨간색LED로 경고를 준다.
  servo.write(90);
  digitalWrite(pin_R, HIGH);
  digitalWrite(pin_G, LOW);
  state = CLOSE;
}

byte Servomotor::getState() {
  return state;
}
