function MyClass() {
    var self = this; // in MyClass
    self.myField = {};
    this.myField = "something else";
    this.myField.doFirst();
    self.myField.doAgain();
}

MyClass.prototype.myMethod = function () {
    var self = this;    // in MyMethod
    self.myField.doSomething(); // click on myField
    this.myField = "foo"; // click on myField
    self.myField.doAgain(); // in myMethod
}   