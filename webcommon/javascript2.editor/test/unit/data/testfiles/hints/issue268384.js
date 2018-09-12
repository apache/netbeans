"use strict";

class A {
    print() {
        console.log("A print");
    }
};

class B extends A {
    print() {
        super.print();
        console.log("B print");
    }
};
var a = new B();
a.print();