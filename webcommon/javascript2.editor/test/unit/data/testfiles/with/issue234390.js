function Person234390(name) {
    this.realname = name;
    this.hello = function() {
        return this.realname;
    };
}

var pe234390 = new Person234390("John"); 
with (pe234390) {
    //hello(); // uncomment hello() call to make it pe.hello() work OK
    pe234390.hello();     
    pe234390.realname = "Doe";
} 