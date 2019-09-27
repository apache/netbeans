function f1(cont) {
    if (cont) {
        f1(false); // f1 is called again
    }
    console.log("running f1 outer");
}

f1(true);