/**
 * pluginWithoutElementName to be used without an element, so $.pluginWithoutElementName();
 * based on boilerplate version 1.0
 **/
(function($) {
  "use strict"; //ECMA5 strict modus

  $.extend({"pluginWithoutElementName": function(settings) {

      /* define vars
       */

      /* this object will be exposed to other objects */
      var publicObj = {};

      //the version number of the plugin
      publicObj.version = '1.0'

      /* this object holds functions used by the plugin boilerplate */
      var _helper = {
        /**
         * Call hooks, additinal parameters will be passed on to registered plugins
         * @param {string} name
         */
        "doHook": function(name) {
          var i;
          var pluginFunctionArgs = [];

          /* call function */
          if (_globals.plugins !== undefined) {
            /* remove first two arguments */
            for (i = 1; i < arguments.length; i++) {
              pluginFunctionArgs.push(arguments[i]);
            }

            $.each(_globals.plugins, function(pluginWithoutElementName, extPlugin) {
              if (extPlugin.__hooks !== undefined && extPlugin.__hooks[name] !== undefined) {
                extPlugin.__hooks[name].apply(publicObj, pluginFunctionArgs);
              }
            });
          }
        },
        /**
         * Registers a plugin
         * @param {string} name Name of plugin, must be unique
         * @param {object} object An object {("functions": {},) (, "hooks: {})}
         */
        "registerPlugin": function(name, object) {

          var plugin;
          var hooks;

          /* reorder plugin */
          hooks = $.extend(true, {}, object.hooks);
          plugin = object.functions !== undefined ? object.functions : {};
          plugin.__hooks = hooks;

          /* add plugin */
          _globals.plugins[name] = plugin;
        },
        /**
         * Calls a plugin function, all additional arguments will be passed on
         * @param {string} pluginWithoutElementName
         * @param {string} pluginFunctionName
         */
        "callPluginFunction": function(pluginWithoutElementName, pluginFunctionName) {
          var i;

          /* remove first two arguments */
          var pluginFunctionArgs = [];
          for (i = 2; i < arguments.length; i++) {
            pluginFunctionArgs.push(arguments[i]);
          }

          /* call function */
          _globals.plugins[pluginWithoutElementName][pluginFunctionName].apply(null, pluginFunctionArgs);
        },
        /**
         * Checks dependencies based on the _globals.dependencies object
         * @returns {boolean}
         */
        "checkDependencies": function() {
          var dependenciesPresent = true;
          for (var libName in _globals.dependencies) {
            var callback = _globals.dependencies[libName];
            if (callback.call() === false) {
              console.error('jquery.pluginWithoutElementName: Library ' + libName + ' not found! This may give unexpected results or errors.')
              dependenciesPresent = false;
            }
          }

          return dependenciesPresent;
        }
      };

      /* this object holds all global variables */
      var _globals = {};

      /* handle settings */
      var defaultSettings = {
      };

      _globals.settings = {};

      if ($.isPlainObject(settings) === true) {
        _globals.settings = $.extend(true, {}, defaultSettings, settings);
      } else {
        _globals.settings = defaultSettings;
      }

      /* this object contains a number of functions to test for dependencies,
       * functies should return TRUE if the library/browser/etc is present
       */
      _globals.dependencies = {
        /* check for jQuery 1.6+ to be present */
        "jquery1.6+": function() {
          var jqv, jqv_main, jqv_sub;
          if (window.jQuery) {
            jqv = jQuery().jquery.split('.');
            jqv_main = parseInt(jqv[0], 10);
            jqv_sub = parseInt(jqv[1], 10);
            if (jqv_main > 1 || (jqv_main === 1 && jqv_sub >= 6)) {
              return true;
            } else {
              return false;
            }
          }
        }
      };
      _helper.checkDependencies();

      //this object holds all plugins
      _globals.plugins = {};

      /**
       * Init function
       **/
      publicObj.init = function() {
      };

      /**
       * Public function
       */
      publicObj.myFunction = function() {
        _helper.doHook('myFunction');
      };

      /**
       * Registers a plugin
       * @param {string} name Name of plugin, must be unique
       * @param {object} object An object {("functions": {},) (, "hooks: {}) (, "targetpluginWithoutElementNames": [])}
       */
      publicObj.registerPlugin = function(name, object) {
        _helper.registerPlugin(name, object);
      };

      /**
       * Calls a plugin function, all additional arguments will be passed on
       * @param {string} pluginWithoutElementName
       * @param {string} pluginFunctionName
       */
      publicObj.callPluginFunction = function(pluginWithoutElementName, pluginFunctionName) {
        /* call function */
        _helper.callPluginFunction.apply(null, arguments);
      };

      /**
       * Private function
       **/
      function myFunction(myParam) {
        //call hook
        _helper.doHook('onMyFunctionCalled', myParam);
      }

      /* initialize pluginWithoutElementName
       */
      $(document).trigger('pluginWithoutElementName.beforeInit', publicObj, settings); //trigger event on document
      publicObj.init();
      $(document).trigger('pluginWithoutElementName.init', publicObj); //trigger event on document

      return publicObj;
    }
  });
})(jQuery);