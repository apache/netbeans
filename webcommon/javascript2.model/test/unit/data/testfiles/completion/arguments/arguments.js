
var ArgumentsContext = {};

ArgumentsContext.testFunction = function testFunction(param1, param2) {
    formatter.println("Calling testFunction with " + arguments.length + " arguments.");
    formatter.addIndent(4);
    for (var i = 0; i < arguments.length; i++) {
        formatter.println("arguments[" + i + "]: " + arguments[i]);
    }
    formatter.removeIndent(4)
    formatter.println("End of call");
    formatter.println("");
    return 10;
}

ArgumentsContext.getFunction = function() {
    return new Function("p1", "p2", "formatter.println(\"function from get function executed\")");
}

formatter.println("Result of testFunction: " + ArgumentsContext.testFunction(1, 2).toString());
ArgumentsContext.testFunction(1, 2, 4, 5, 6, 7);
formatter.println("Declaration of " + ArgumentsContext.testFunction.name + " function has " 
        + ArgumentsContext.testFunction.length + " arguments.");
    


var createdFunction = ArgumentsContext.getFunction()
ArgumentsContext.getFunction();
createdFunction();
