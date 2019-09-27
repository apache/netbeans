var a = "foo",
    b = 42,
    c = {
        first: "1th",
        second: "2th"
    };

var o = {a, b, c};

console.log(o.a);           // foo
console.log(o.b);           // 42
console.log(o.c);           // { first: '1th', second: '2th' }
console.log(o.c.first);     // 1th
console.log(o.c.second);    // 2th

o.c.third = "3th";
console.log(o.c.third);     // 3th
console.log(c.third);       // 3th 

o.c.sayHello = function () {
    return "Hello";
}

console.log(c.sayHello());  // Hello