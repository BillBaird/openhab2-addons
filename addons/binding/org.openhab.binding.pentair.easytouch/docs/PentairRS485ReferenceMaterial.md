### Pentair RS485 references material:


####As always with free stuff: Use at your own risk. If you don't understand what it's doing, don't run it! Absolutely no warranties guaranteed.

* [MD CheatSheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
* github OpenHAB repo: [https://github.com/openhab](https://github.com/openhab)
* Eclipse Smarthome: [https://projects.eclipse.org/projects/technology.smarthome/developer](https://projects.eclipse.org/projects/technology.smarthome/developer)
* OpenHAB docs: [http://docs.openhab.org/](http://docs.openhab.org/)

* Greenfield project querying and controlling a Pentair VS pool pump over RS485 from Arduino: [https://github.com/eriedl/pavsp_rs485_examples](https://github.com/eriedl/pavsp_rs485_examples)
    * readme.txt: [https://github.com/eriedl/pavsp_rs485_examples/tree/master/pab014share](https://github.com/eriedl/pavsp_rs485_examples/tree/master/pab014share)
    * Constants: [https://github.com/eriedl/pavsp_rs485_examples/blob/master/rs485_master/rs485_master.h](https://github.com/eriedl/pavsp_rs485_examples/blob/master/rs485_master/rs485_master.h)
    * [examples](https://github.com/eriedl/pavsp_rs485_examples/blob/master/rs485_master/RS485_Master.ino)


     * Every device on the bus has an address:
     *      0x0f - is the broadcast address, it is used by the more sophisticated controllers as <dst>
     *          in their system status broadcasts most likely to keep queries for system status low.
     *      0x1x - main controllers (IntelliComII, IntelliTouch, EasyTouch ...)
     *      0x2x - remote controllers
     *      0x6x - pumps, 0x60 is pump 1
     *
     * Let's use 0x20, the first address in the remote controller space

     * Build the queue of instructions we need to send to the pump to make the current command work.
     * For example, if we want to run Program 1, we need to
     * 1. Put the pump into remote control
     * 1.1 Confirm remote control
     * 2. Set program(s), write memory or set mode
     * 2.1 Confirm commands that are in the queue
     * 3. repeat 2. until everything is set
     * 4. Put the pump into local control mode again
     *
     * TODO: Keep track of remote/local control setting.

* Michael Rousse's pab014share sources: [http://cocoontech.com/forums/files/file/173-pab014sharezip/](http://cocoontech.com/forums/files/file/173-pab014sharezip/)
    * [http://cocoontech.com/forums/topic/13548-intelliflow-pump-rs485-protocol/page-4](http://cocoontech.com/forums/topic/13548-intelliflow-pump-rs485-protocol/page-4)
* Jason Young's blog post: [http://www.sdyoung.com/home/pool-status/how-i-control-the-pool/](http://www.sdyoung.com/home/pool-status/how-i-control-the-pool/)
    * and [http://www.sdyoung.com/home/decoding-the-pentair-easytouch-rs-485-protocol/](http://www.sdyoung.com/home/decoding-the-pentair-easytouch-rs-485-protocol/)
* Mark (aka rocco): [http://cocoontech.com/forums/user/465-rocco/](http://cocoontech.com/forums/user/465-rocco/)
* tagyoureit's Homebridge/node.js project: [https://github.com/tagyoureit/nodejs-Pentair](https://github.com/tagyoureit/nodejs-Pentair)

#### Other off-topic interesting Pool control / sensors
* [http://www.desert-home.com/p/swimming-pool.html](http://www.desert-home.com/p/swimming-pool.html)