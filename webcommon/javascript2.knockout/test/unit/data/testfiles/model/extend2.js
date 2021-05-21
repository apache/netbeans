wrap = {
    x: {
        a: "",
        b: "rrr",
        r: function() {

        }
    }
}

y = {
    c: "ccc"
}

ko.utils.extend(y, wrap.x);