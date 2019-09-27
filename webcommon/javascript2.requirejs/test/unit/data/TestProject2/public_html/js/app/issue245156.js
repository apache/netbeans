define(function(require, exports, module) {
    var module1 = require("app/moduleApp1");
    var module2 = require("lib/moduleLib1");
    module2.const1;
    module1.first;
    return function() {
    };
}
);