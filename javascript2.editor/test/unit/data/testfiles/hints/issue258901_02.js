function f() {
  return [1, 2, 3];
}

var [a, , b] = f(); // <- false warning here
console.log(a); // 1
console.log(b); // 3

[,,,] = f(); // <- false warning here
