function PC (){
    this.buf = 10;

   
    this.peek = function peek() { 
        var buf = this.buf;
        buf = 20;
        console.log(this.buf);
        console.log(buf);
    };
}

var a = new PC();
a.peek();
   