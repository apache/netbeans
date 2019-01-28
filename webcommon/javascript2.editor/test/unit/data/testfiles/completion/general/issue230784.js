!(function() {

    function getNumber() {
        return 10;
    }

    var myObject = {
        property1: 10,
        property2: "ahoj",
        method1: function() {
            return 10;
        },
        method2: function() {
            return "ahoj";
        }
    };

    var myObject7 = new myObject().property1;
    var myObject8 = new myObject().property2;      
    var myObject9 = new myObject().property1.toLocaleString();
    var myObject10 = (new myObject()).property1.toLocaleString().fontsize().toPrecision();
})();