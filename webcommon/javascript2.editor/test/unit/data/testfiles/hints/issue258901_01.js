var foo = ["one", "two", "three"];

var [one, two, three ] = foo; //<- false warning here d
console.log(one); // "one" 
console.log(two); // "two"
console.log(three); // "three"  // <- false warning here, if it's the last eof