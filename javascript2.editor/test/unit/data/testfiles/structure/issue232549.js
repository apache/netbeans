function Animal(name) {
    this.name = name;
    this.toString = this.getName = function () {
        return this.name;  
    };
}