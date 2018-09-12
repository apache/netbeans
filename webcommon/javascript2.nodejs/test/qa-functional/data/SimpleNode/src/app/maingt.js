
var o1 = require("../folder/literal");//gt;33;literal.js;1;1
var o2 = require("../folder/literalRef");
var kocka = require("fi");

o1.obj.conf.a;//gt;6;literal.js;17;5
o1.pokus.getDay();//gt;6;literal.js;16;5
o1.jejda.ale2;//gt;6;literal.js;12;5

o2.obj2.conf.a;//gt;6;literalRef.js;19;5
o2.pokus2.getSeconds();//gt;6;literalRef.js;18;5
o2.neco2;//gt;6;literalRef.js;20;5

var o3 = require("func");//gt;21;func.js;1;1

o3.inn.da;//gt;6;func.js;25;12
console.log(o3.inn.log.messages);//gt;17;func.js;25;12
console.log(o3.stNumber);//gt;18;func.js;26;12
console.log(o3.stDate.UTC());//gt;18;func.js;27;12
console.log(o3.stObj.foo);//gt;18;func.js;28;12

var p = new o3();
p.bar();//gt;5;func.js;8;10
p.ale2.f;//gt;5;func.js;4;10
new o3().bar();//gt;11;func.js;8;10
console.log(p.attempt);//gt;19;func.js;37;22
console.log(p.getAttempt().aa);//gt;19;func.js;38;22

var o4 = require("../folder/instance");
o4.firstName;//gt;8;instance.js;10;10
o4.dob;//gt;6;instance.js;11;10
o4.props.b.b1;//gt;6;instance.js;13;10
o4.walk();//gt;6;instance.js;20;10
o4.dateOfBirth();//gt;8;instance.js;27;10
o4.fakeOrigin.info();//gt;8;instance.js;46;18
o4.config.c1;//gt;8;instance.js;47;18


var o5 = require("../folder/instanceRef");
o5.rfirstName;//gt;8;instanceRef.js;10;10
o5.rdob;//gt;6;instanceRef.js;11;10
o5.rprops.b.b1;//gt;6;instanceRef.js;13;10
o5.rwalk();//gt;6;instanceRef.js;20;10
o5.rdateOfBirth();//gt;8;instanceRef.js;27;10
o5.rfakeOrigin.info();//gt;8;instanceRef.js;46;18
o5.rconfig.c1;//gt;8;instanceRef.js;47;18

var o6 = require("../exp/lit");
o6.myobj;//gt;7;lit.js;14;9
o6.myobj.jejda;//gt;12;lit.js;15;5
o6.naive();//gt;7;lit.js;11;9

var o7 = require("../exp/litref");
o7.mars;//gt;7;litref.js;22;9
o7.mars.jejda;//gt;12;litref.js;15;5



var o8 = require("../exp/ins");
o8.spell();//gt;7;ins.js;50;9
o8.mana;//gt;7;ins.js;51;9

var o9 = require("../exp/insref");
o9.witch.origin;//gt;13;insref.js;12;10

var o10 = require("../exp/fnc");
o10.foobar;//gt;7;fnc.js;41;9
o10.innerFnc;//gt;7;fnc.js;42;9
o10.foobar.stObj.foo;//gt;15;fnc.js;28;7

var p10 = new o10.foobar();
p10.bar();//gt;5;func.js;8;10
p10.ale2.f;//gt;5;func.js;4;10
new o10.foobar().bar();//gt;11;func.js;8;10
console.log(p10.attempt);//gt;19;func.js;37;22
console.log(p10.getAttempt().aa);//gt;19;func.js;38;22

var _1 = o1;
_1.obj;//gt;5;literal.js;17;5

var _2 = o2;
_2.pokus2;//gt;7;literalRef.js;18;5

var _3 = p;
_3.bar();//gt;5;func.js;8;10

var _4 = o4;
_4.fakeOrigin;//gt;11;instance.js;46;18

var _7 = o7;
o7.mars.jejda;//gt;6;litref.js;22;9

var _oo = o6;
_oo.myobj;//gt;7;lit.js;14;9

var _8 = o8;
_8.spell();//gt;7;ins.js;50;9

var _9 = o9;
_9.witch.origin;//gt;6;insref.js;54;9


var Auto = require('../complexModule').Car;//gt;41;complexModule.js;163;9
var sedan = new Auto();
sedan.ride();//gt;9;complexModule.js;155;15
sedan.speed;//gt;10;complexModule.js;150;10