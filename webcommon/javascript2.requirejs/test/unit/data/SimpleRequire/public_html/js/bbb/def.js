define(['jquery', "piwik", "app/function", "app/newFunction", "app/objectLiteral", "app/stdModule"],
        function($, pi, func, newFunc, obj, stdModule) {


        func().a();
        func().b.c;
        
        newFunc.anatomy;
        newFunc.anatomy.eyes();
        newFunc.anatomy.heads.leftOnes;
        newFunc.getStuff().c().x;
        newFunc.getWannabeDate();
        newFunc.birth;
        
        obj.getLiteral().propY.a;
        obj.test.ale.aa;
        obj.name;
        
        stdModule.dummy2.heads;
        stdModule.getSomeDate();
        stdModule.logIt.a;
        }); 