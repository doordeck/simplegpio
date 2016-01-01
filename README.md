SimpleGPIO
==========
[![Build Status](https://travis-ci.org/doordeck/simplegpio.svg?branch=master)](https://travis-ci.org/doordeck/simplegpio)
[![Coverage Status](https://coveralls.io/repos/doordeck/simplegpio/badge.svg?branch=master&service=github)](https://coveralls.io/github/doordeck/simplegpio?branch=master)

A simple library for reading input and writing output to GPIO pins on Linux platforms using Sysfs.

## Usage
SimpleGPIO is split into three components
- **simplegpio-core** - Core components including a fallback input poller based on a naieve pure Java implementation
- **simplegpio-module-libbulldog** - An enhanced input poller using JNI and libbulldog; this version offers most stability and performance but least portability - the libbulldog library should be [downloaded](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22bulldog-linux-native-raspberrypi%22) and named 'libbulldog-linux.so'
- **simplegpio-module-epoll** - A performant and slightly more portable input poller based on calls to epoll using JNA

## Example

```
    InputPollerFactory inputPollerFactory = InputPollerFactoryLocator.locate();
    PinFactory pinFactory = new PinFactory(inputPollerFactory);
    Pin pin = pinFactory.getPin(71); // P8_48 on a Beaglebone Black 
    DigitalInput in = pin.as(DigitalInput.class);
    in.addInterruptListener(new InterruptListener() {
              @Override
              public void interruptRequest(InterruptEventArgs args) {
                  System.out.println("Input! " + args.getEdge());
              }
          });
```

## Building
SimpleGPIO uses maven.
```
mvn clean install
```

## Credits
SimpleGPIO is based on a fork of [silverspoon.io](http://silverspoon.io)'s [libbulldog](https://github.com/px3/bulldog) with JNA epoll support from [jcommon-process](https://github.com/jcommon/process).

## License
SimpleGPIO is licensed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).