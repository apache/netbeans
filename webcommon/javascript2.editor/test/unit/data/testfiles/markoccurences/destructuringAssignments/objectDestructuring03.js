var a, b, c = 5;   
     
({c, a, b} = {b:1, a:c});

console.log(a); // 5
console.log(b); // 1
console.log(c); // undefined 