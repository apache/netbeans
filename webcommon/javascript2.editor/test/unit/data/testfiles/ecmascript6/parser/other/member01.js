var bug = "ahoj";
var a = {ahoj: 1};
a[bug].test = "test";
a[bug] = 10;
console.log(a[bug]);