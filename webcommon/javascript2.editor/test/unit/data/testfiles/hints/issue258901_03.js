var o = {p: 42, q: true};
var {p, q} = o;   // <- false warning here

console.log(p); // 42
console.log(q); // true 
