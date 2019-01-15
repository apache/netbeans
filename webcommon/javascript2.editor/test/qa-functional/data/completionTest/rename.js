function Person(name) {
    this.realname = name;

    this.hello = function() {
        return this.realname;
    }
}

var pe = new Person("John");
pe.hello();
pe.realname = "Doe";

var control = {};
superGlobal = 1;

function drawResolvedFixedChart(object) {


    var data = [];
    var lastDate = new Date();

    for (var d in object) {
        var stamp = +object[d][0];
        lastDate = new Date(stamp);
        console.log(data[d]);

    }

    control = Util.set({
        'action': 'print',
        'dat': {
            'foo': {
                'dummy': {
                    'really': true,
                    value: superGlobal
                }
            }
        },
        'postprocess': {'start': {'start': new Date(lastDate.getTime() - 60 * 86400000)}}
    });
}

drawResolvedFixedChart({});

window.test = {
    name: {
        firstname: "John",
        lastName: "Smith"
    }
};

function Synergy() {
    //var synergy = this; 

    this.defaultCookiesExpiration = 7;

    this.modal = {
        modalBody: "#modal-body",
        show: function() {
            $(this.modal).modal('toggle');
        }
    };

}