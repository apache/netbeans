function ChartP3(container) {

    function buildData(records) {
        var indexOfBeta = getIndexOfLatestBeta();
        window.console.log(indexOfBeta);
    }

    function getIndexOfLatestBeta() { // problem here
        
        return 1;
    }
    buildData()
}

var TestObject = {
    myMethod1 : function () {
        return this.myMethod2();
    },
    myMethod2 : function () {
        return "method2";
    },
    myMethod3 : function() {
        return myMethod2();
    }
};

TestObject.__proto__.myMethod2 = function () {
    return "myMethod2 from prototype";
}

console.log(TestObject.myMethod1());
console.log(TestObject.myMethod3());