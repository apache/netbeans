
(function($) {

    function Runner(name) {
        this.attempt = 1;
        this.name = name;
    }

    $.extend({"newPlugin": function(settings) {

            var publicObj = {};
            var _globals = {};
            publicObj.version = '1.0';

            var _helper = {
                "registerPlugin": function(name, object) {
                    var plugin;
                    var hooks = new Date();
                    hooks = $.extend(true, {}, object.hooks);
                    plugin = object.functions !== undefined ? object.functions : {};
                    _globals.plugins[name] = plugin;
                },

                "checkDependencies": function() {
                    var dependenciesPresent = true;
                    return dependenciesPresent;
                }
            };

            var defaultSettings = {
            };
            
            _globals.settings = {};

            _globals.dependencies = {
                "jquery1.6+": function() {
                  
                }
            };
            publicObj.test = {a:1, b:function(){}, c: new Date()};
            /**
             * 
             * @returns {Date}
             */
            publicObj.init = function() {
            };
            
            publicObj.foo = new Runner();

    
            publicObj.myFunction = function() {
                _helper.doHook('myFunction');
            };

            return publicObj;
        }
    });
})(jQuery);

//cc;61;;newPlugin;0

//cc;63;newPlugin().;init,myFunction,foo,version;

//cc;65;newPlugin().test.;a,b,c;

//cc;67;newPlugin().init().;getDay,UTC;

//cc;69;newPlugin().foo.;attempt,name;

//cc;71;newPlugin().test.c.;getDay,UTC;
