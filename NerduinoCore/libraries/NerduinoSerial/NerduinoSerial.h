#ifndef NerduinoSerial_h
#define NerduinoSerial_h

#include <inttypes.h>

// commands
#define LCD_CLEARDISPLAY 0x01

class NerduinoSerial
{
public:
  NerduinoSerial();

  void init();
    
  void begin();

  void clear();
  void home();

private:

  //uint8_t _numlines,_currline;
};

#endif
