function Person(name){
    this.realname = name;

    this.hi = function(){
        return this.realname;
    }
}

var pe = new Person("John");
pe.hi();
