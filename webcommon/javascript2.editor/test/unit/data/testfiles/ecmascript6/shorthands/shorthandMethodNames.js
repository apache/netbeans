var property = 10;
var o = {
    method() {
        return "method"
    },
    get property() { 
        return property; // global property
    },
    set property(value) {
        property = value;  // global property
    },
    * generator() {}
};   

console.log(o.property);
o.property = 20;
console.log(o.property);
console.log(property);
console.log(o.method());