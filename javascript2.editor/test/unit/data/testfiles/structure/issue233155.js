var obj = new function(){
    this.propertyA = "A";

    this.methodA = function(){
        /*do something; but this method won't appear in the code hint popup.*/
    };

    this.methodB = methodB;
    function methodB(){
        /*do something; this method will appear in the code hint popup*/
    }
}