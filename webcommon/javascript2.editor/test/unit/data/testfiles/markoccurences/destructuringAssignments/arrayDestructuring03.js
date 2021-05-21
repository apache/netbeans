function f() {
  return [1, 2];
}

var a, b; 
[a, b] = f(); 
console.log(a); // 1
console.log(b); // 2