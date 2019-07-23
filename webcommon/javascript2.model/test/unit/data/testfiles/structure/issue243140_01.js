var myModule = function()
{
    return {
        foo: function()
        {
            console.log("foo");
        },
        bar: "bar",
        address: {
            street: "Naskove",
            city: "Prague",
            zip: "15000"
        }
    };  
};

myModule().bar;
myModule().foo();

var yourModule = myModule();
yourModule.bar;
yourModule.foo();