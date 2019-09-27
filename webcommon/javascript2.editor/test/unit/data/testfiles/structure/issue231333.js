!(function() {
    function test() {
        return 10 + 3, "20", console.log("druhy"), {begin: 10, end: 20};
    }

    console.log(test());
})();   