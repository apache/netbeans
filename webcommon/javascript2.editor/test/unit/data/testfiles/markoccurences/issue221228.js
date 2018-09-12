function Greetings() {
    function hi() { // private function
        formatter.println(msg); // uses private var
    }

    var msg = "private"; // private variable
    
    this.pozdrav = function () {
        hi();           // uses private function
    };
}

Greetings.prototype.sayAhoj = function () {
    
};
       
     
var a221228 = new Greetings();
a221228.hi();                     // the function is not accessible here
a221228.pozdrav();                // rename hi here
a221228.msg = "Hi public";        // creates new property of object a/
formatter.println(a221228.msg);
a221228.sayAhoj();

var b221228 = new Greetings();
formatter.println(b221228.msg);
b221228.msg = "from b";           // create new property of object b
b221228.hi();
b221228.sayAhoj();
b221228.pozdrav();
formatter.println(b221228.msg);   
formatter.println(a221228.msg);
