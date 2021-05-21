function SubClass() {
    this.parent = "MyAwesome";
    this.init = function() {
    };
}

Ext.define('My.awesome.Class', {
    child: new SubClass(),
    someProperty: 'something',
    someMethod: function(s) {
        alert(s + this.someProperty);
    }


});
var obj = new My.awesome.Class();
//cc;18;obj.;child,someProperty,someMethod;0

//cc;20;obj.child.;parent,init;0

//cc;22;My.;awesome;0

//cc;24;My.awesome.;Class;0


var obj = {};
Ext.apply(obj, {
    test: 'test',
    day: new Date(),
    config: {
        id: 'view',
        attempt: {
            one:1,
            two: 'preview'
        }
    }
});

//cc;40;obj.;test,config;0

//cc;42;obj.config.;id,attempt;0

//cc;44;obj.config.attempt.;one,two;0

//cc;46;obj.day.;getDay,getDate;0
