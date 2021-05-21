function test(name) {
    window.console.log(1);
}

var foo = {};
foo.bar = test;
foo.bar();


function Cube() {
}

Cube.prototype.foo = test;


var cube = new Cube();
cube.foo();