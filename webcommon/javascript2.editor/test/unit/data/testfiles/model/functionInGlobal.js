
function printSomething() {
    formatter.println("printSomething executed");
}

var text = "hello from global";

this.printSomething();

function anotherFunction() {
    //this.printSomething();
    
    this.anotherInAnother  = function anotherInAnother() {
        formatter.println(text);
    }
}

this.anotherFunction();

var af = new anotherFunction();
af.anotherInAnother();
