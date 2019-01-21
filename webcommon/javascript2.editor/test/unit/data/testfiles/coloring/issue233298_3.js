module.exports = function test() {
  var test1 = Foo.test2;
  Foo.test2 = function(err, test3) { return test3; };
  Foo.test2 = test1;
};

