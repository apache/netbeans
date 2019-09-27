var Utils = {
    name : "test",
    values : function* () {
        yield "first";
        yield "second";
        yield "third";
    }
}

console.log(Utils.values().next().done);
console.log(Utils.values().next().value);
console.log(Utils.values().next().value);
