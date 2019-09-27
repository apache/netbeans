requirejs.config({
    baseUrl: 'js/lib',
    paths: {
        app: '../app',
        piknic: '../picnic',
        ojs: 'oj/min/'
    }
});
 
requirejs(['moduleLib1', 
        'js/lib/moduleLib2.js', 
        'app/moduleApp1',
        'proto/localization',
        'piknic'], 
    function(lib1, lib2, test, loc, pik) {
    console.log(lib1.const1);
    console.log(lib2.const4);
    console.log(test.second);
    console.log(pik.date1);
    console.log(loc.message1);
});