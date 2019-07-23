function Foo(param1) {
    this.n = 1;
}

with (window) {
    var o = new Foo();
}