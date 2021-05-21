var A = {}

A.f1 = function f1 (cont) {
        console.log("running  static A.f1");
        if (cont) {
            f1(false);
        } 
}; 
      
A.f1(true);

var b = A;
b.f1();