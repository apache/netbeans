function Person(name) {
    this.realname = name;

    this.hello = function() {
        return this.realname;
    };
}

var pe = new Person("John"); 
with (pe) { //rename;8;man;9:var man =;10:(man) {,11:man.hello(),12:man.realname
    pe.hello(); //rename;11;greeting;11:pe.greeting();4:this.greeting;87:passanger.greeting
    pe.realname = "Doe";
}
var control = {};

superGlobal = 1;

function drawResolvedFixedChart(object) {


    var data = [];
    var lastDate = new Date();
    with(lastDate){
        lastDate = 1; //rename;13;dat;22:var dat;23:(dat);24:dat = 1;28:dat =;43:dat.getTime
    }
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

with (window) {
    superGlobal = 2; //rename;11;s_global;48:s_global = ;39:value: s_global;16:s_global = 1
    drawResolvedFixedChart({}); //rename;11;paint;18: function paint(;49:paint({})
}
window.test = {
    name: {
        firstname: "John",
        lastName: "Smith"
    }
};

function Synergy() {
    var synergy = this; 
    this.defaultCookiesExpiration = 7;

    synergy.modal = { //rename;16;dialog;62:synergy.dialog;74:(this.dialog);81:this.dialog.in
        modalBody: "#modal-body",
        show: function() { //rename;11;display;64:display: f;75:display()
            $(this.modal).modal('toggle');
        },
        innerObject: {
            innerName: 2,
            show: function() {
            }
        }
    };

    with (this.modal) {
        show();
        with(window.test.name){ //rename;29;n_ame;76:window.test.n_ame;52:n_ame: {
            lastName = "Doe"; //rename;18;surname;77:surname = ;54:surname: "Smith
        }
    }

    with (this.modal.innerObject) {
        show(); //rename;11;open;82:open();69:open: function(
    }

    var cc = new Car();
    with (cc) { //rename;12;_car;85:_car = ;86:(_car);88:_car.add
        passanger.hello(); //rename;12;psngr;87:psngr.hello;96:this.psngr
        cc.addPassanger(new Person("Jane")); //rename;31;Human;88:new Human;96:new Human;9:new Human;1:function Human(
    }
}


function Car() { //rename;12;Auto;93:function Auto(;85:new Auto(
    this.topSpeed = 200;
    this.hp = 150;
    this.passanger = new Person("Mike");
    this.addPassanger= function(){};
    this.go = function() {
    };
}