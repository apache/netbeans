function Person(n,race){ 
    this.clothing="nothing/naked";
}

var gk=new Person("Gavin","caucasian"); 
gk.beCool=function(){    
        this.clothing="tinfoil";
    };