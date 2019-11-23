var watch = function(scope, attr, name, defaultVal) {
    if (attr) {
        scope.$watch(attr,function(val, oldVal, scope){
            scope[name] = val;
        });
    } else {
        scope[name] = defaultVal;
    }
};
