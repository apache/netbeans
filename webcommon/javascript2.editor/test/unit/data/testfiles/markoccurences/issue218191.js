var Core = Core || {};

Core.needModules(function() {
    "use strict";

    var REGEXP = /[+]?\d{1,20}$/; // REGEXP marked as unused

    Core.Test = {
        test: function(value) {
            var fieldType; // fieldType marked as unused

            alert(REGEXP.test(value));

            switch (fieldType) {
                case "test":
                    alert(REGEXP.test(value));
                    break;
            }
        }
    };   
});
