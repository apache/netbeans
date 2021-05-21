// the w property is not colored, because  it doesn't correspond with the right properties
var {p: foo, q: bar, w: abc} = {p: 5, q:7};     
          
console.log(foo); // 5   
console.log(bar); // 7  
console.log(abc); // undefined