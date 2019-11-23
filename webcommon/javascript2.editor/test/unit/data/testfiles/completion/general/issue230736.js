!(function () {
    
    function getNumber() {
        return 10;
    }
    
    var myObject = {
        method1: function() { return 10;},
        method2: function() { return "ahoj";}
    };
    
    var myObject0 = new myObject();
    var myObject1 = new myObject().method1();
    var myObject2 = new myObject().method2();
    var myObject3 = new myObject().method2().toUpperCase().fontsize().toPrecision(3);
    var myObject4 = (new myObject()).method1();
    var myObject5 = (new myObject()).method2();
    var myObject6 = (new myObject()).method2().toUpperCase().fontsize().toPrecision(3);
                   
})();