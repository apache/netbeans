
function A() {
}

A.prototype.say = function() {
    return "ahoj";
}

A.prototype.help = function() {
    return "help from A";
}

var a = new A();

function B() {
}

B.prototype = new A();

B.prototype.help = function() {
    return "help from B";
}

B.prototype.helpAgain = function() {
    return "help Again from B";
}

var b = new B();

formatter.print("a.say(): " + a.help());
formatter.print("b.say(): " + b.help());

function C() {
    this.cry = function () {
        
    }
}

C.prototype = new B ();

var c = new C();

formatter.print("c.say(): " + c.help());
