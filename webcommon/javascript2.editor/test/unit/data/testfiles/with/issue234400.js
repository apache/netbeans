function Foo234400(param1) {
    this.name = param1;
    this.start = function() {
        window.console.log("jedem");
    };
}

with (window) {
    var o234400 = new Foo234400();
    o234400.name = 10;    
}
