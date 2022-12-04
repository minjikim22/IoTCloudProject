#include <Arduino.h>

#define OPEN 0
#define CLOSE 1

class Servomotor {
  private:
    int pin_S;
    int pin_R;
    int pin_G;
    byte state;

  public:
    Servomotor(int pin_S, int pin_R, int pin_G);
    void init();
    void barOpen();
    void barClose();
    byte getState();
};
