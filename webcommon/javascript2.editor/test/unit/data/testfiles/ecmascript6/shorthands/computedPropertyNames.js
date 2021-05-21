var prop = "foo";
var o = {
  [prop]: prop + " " + prop,
  ["b" + "ar"]: "there",
  prop : "test"
};

console.log(o.prop); // test
console.log(o.foo);  // foo foo 