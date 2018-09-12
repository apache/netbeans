var MyOb = {
    fn1: function (data) {
        var xxx = data;

        this.xxx = xxx;    // <- here is the second xxx marked as global
    }
};
  
console.log(MyOb.xxx);
MyOb.fn1("sranda");
console.log(MyOb.xxx);