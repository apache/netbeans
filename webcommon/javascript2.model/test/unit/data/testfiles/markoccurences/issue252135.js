var count = 1;
var aa = function bb () {           // <- place caret in aa word
    console.log("running aa");
    count++;
    if (count < 5) {
        bb();
    }
};

aa();