it("case3: wrapped object", function() {
    var obj = (function() {
        return {    
            f2: function() {
                return this.f1(); // ctr+click does not work on f1
            },
            f1: function() {
                return 'f1';
            }
        };
    })();
    expect(obj.f2()).toEqual('f1'); // here it works
});