var obj = new function(){
    this.propertyA = "A";

    this.methodA = function(){

    };

    this.methodB = methodB;   // <- the methodB is marked as global
    function methodB(){

    }
}