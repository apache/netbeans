(function () {
    var context;
    context.Parser = function Parser() {



        var MyError = function () {
            this.prop = "prot"
        }
         
        MyError.prototype.constructor = MyError;
    }
    
    context.Parser2 = Parser;
       
}());