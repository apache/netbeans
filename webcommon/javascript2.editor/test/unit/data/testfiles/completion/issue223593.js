(function() {
    var obj = new function() {
        this.propertyA = "A";

        this.methodA = function() {

        };

        function methodB() {

        }
    }
    obj.propertyC = "B";
})();