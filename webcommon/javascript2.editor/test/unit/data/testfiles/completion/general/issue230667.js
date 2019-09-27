!(function () {
    
    var myObject = {
        method1: function() { return 10;},
        method2: function() { return "ahoj";}
    };
    
    var testik1 = new Number(6).toPrecision(5);
    var testik2 = (new Number(6)).toPrecision(4);
    var testik3 = ( new Number(6) ).toPrecision(4).toString().small();
    var testik4 = ( new Number("dfadfs") /* fdasfdassa*/ ).toPrecision(4);
    
    var myObject1 = new myObject().method1().toPrecision(5).toLocaleString().big();
    var myObject2 = new myObject().method2().small();
    
})();

