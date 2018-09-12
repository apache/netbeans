(function($) {
    "use strict";


    $.widget("myWidget", {
        options: {
            runner: new Runner(),
            editable: new Date(),
            url: null,
            test: {
                a: 1,
                b: function() {
                }

            }
        },
        _create: function()
        {

        },
        _setOption: function(key, value)
        {
            this.options[key] = value;
            this._change();
        },
        _change: function()
        {

        }
    });
    
    
    function Runner(name){
        this.attempt = 1;
        this.name = name;
    }
    
    
})(jQuery);


//cc;43;;myWidget;0

//cc;45;myWidget.;_change,_create,_setOption,options;

//cc;47;myWidget.options.;editable,url,runner,test;

//cc;49;myWidget.options.test.;b,a;

//cc;51;myWidget.options.editable.;getDay,UTC;

//cc;53;myWidget.options.runner.;attempt,name;
