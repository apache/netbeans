var Dog = {

    get years() {return this.old;},
    set years(count){this.old = count + 1;},
    getColor : function() {return this.color}
            
}

var o = {
    a: 7,
    get b() {return this.a + 1;},
    set c(x) {this.a = x / 2;}
};


formatter.println('a: ' + o.a);
formatter.println('b: ' + o.b);
formatter.println("c is set to 50");
o.c = 50;
formatter.println('a: ' + o.k);
Dog.years = 10;
formatter.println("The dos is old " + Dog.years + " years.");
formatter.println("Dog.old: " + Dog.old);
