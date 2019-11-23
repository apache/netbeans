var MyClass =  function (){
              
       this.method1 = function (param) {
           return ("MyClass.method1 executed with param: " + param );
       }

       var method2 = function() {
           return "MyClass.method2 executed";
       }
       this.method2 = method2;
    }
    
    var myClass = new MyClass();

formatter.println(myClass.method1("sranda"));
formatter.println(myClass.method2());