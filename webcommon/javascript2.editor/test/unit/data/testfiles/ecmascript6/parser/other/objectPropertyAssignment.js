var target = {}
var mix1 = {property1: 10, property2: "Hello"};
var mix2 = {property1: 7,
    method1: function () {
        return "Was run";
    }
};

Object.assign(target, mix1);
console.log(target.property1);
console.log(target.property2);

Object.assign(target, mix2);
console.log(target.property1);
console.log(target.property2);
console.log(target.method1());

mix1.property1 = 22;
console.log(target.property1);

mix2.property1 = 33;
console.log(target.property1);

target.property1 = 11;
console.log(target.property1);
console.log(mix1.property1);
console.log(mix2.property1);

 