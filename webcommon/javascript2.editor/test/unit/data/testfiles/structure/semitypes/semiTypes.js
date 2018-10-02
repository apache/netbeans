!(function () {
    
    function getInfo () {
        return "info";
    }
    
    var MyObject = {
        property01 : 10,
        property02 : "Martin",
        method01 : function (){
            return  this.property01;
        },
        method02 : function () {
            return this.property02;
        }
    };
    // simple types
    var prom01 = "ahoj";
    var prom02 = 10;
    var prom03 = prom01;
    
    var prom04 = new MyObject().method01();
    var prom05 = (new MyObject()).method02();
    
    var prom06 = getInfo();
    var prom07 = getInfo().fontsize();
    var prom08 = prom01.fontsize();
    var prom09 = prom08.toLocaleString();
    
    var prom10 = getInfo().fontcolor().small().fontsize().toPrecision(4);
    
    var prom11 = new MyObject();
    var prom12 = prom11.property01.isFrozen("ll");
})();