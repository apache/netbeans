function additionalInfo(n) {

    var container = {};
    container.name = n;
    container.course = 1;

    function finalize(microitem) {
        var a = {};
        a.url = container.name;
    }

    return container;
}
