(function () {
var MyContext = {};

MyContext.createTextWrapper = function () {
    var begin = "wrap ";
    var end = " wrap";
    
    function CreateNeco () {
        this.nazdar = function () {
            formatter.println("nazdar");
            return "nazdar";
        }
    }
    var neco = new CreateNeco();

    var wrapper = {};
    wrapper.print = function (text) {
        var result = neco.nazdar();
        formatter.println(begin + text +  end);
        return this;
    }
    return wrapper;
} 

var myWrapper = MyContext.createTextWrapper();
myWrapper.print("ahoj");

}());

