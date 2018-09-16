var {p: foo, q: bar, w: abc} = {p: 5, q:7};     // <- false warning here
          
console.log(foo); // 5   
console.log(bar); // 7  
console.log(abc); // undefined