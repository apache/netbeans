"use strict";

class Keybord {
    
    * keys() {
        yield 'A';
        yield 'B';
    }
}

var keyboard = new Keybord;

console.log(keyboard.keys().next());