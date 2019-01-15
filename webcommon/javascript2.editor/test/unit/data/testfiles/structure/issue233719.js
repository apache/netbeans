
(function() {
    window.prom01 = "nazdar";
    console.log(prom01);
    window['prom02'] = "cau";
    window['prom03'] = 111;
    console.log(prom02);
    
    
    var man = {};
    man.prop1 = 10;
    man['prop2'] = 20;
    man['prop3'] = "ajo";
    console.log(man.prop1);
    console.log(man.prop2);
}());