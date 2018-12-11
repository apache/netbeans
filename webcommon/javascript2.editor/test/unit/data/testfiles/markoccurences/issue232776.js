var testWith01 = {
    prop01: "ahoj",
    prop02: 20,
    method01: function() {

    }
}

with (testWith01) {
    prop01 = prop01 + prop02;
    method01();
}