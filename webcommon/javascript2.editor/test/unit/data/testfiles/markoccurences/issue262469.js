(function () {
    var service = () => {console.log("running");};
    
    var test = function (fn) {
        fn();
    }
    test(service);
}());