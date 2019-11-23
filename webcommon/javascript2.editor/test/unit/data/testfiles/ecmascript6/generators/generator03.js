function Name (name) {
    this.name = name;
    
    this.used = function* () {
        yield "Pavel";
        yield* privateGen();
        yield "Honza";
        yield "Jitka";
    };
    
    function* privateGen() {
        var index = 1;
        while (index < 3) {
            yield index++;
        }
    }
}

Name.prototype.getName = function () {
    return this.name;
};

var n = new Name("Petr");
console.log(n.getName());
var t = n.used();
console.log(n.used().next());
console.log(t.next());
console.log(n.used().next());
console.log(t.next());
