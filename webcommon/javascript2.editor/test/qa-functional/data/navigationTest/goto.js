var pe = new Person("John");//17:9
pe.hello();//7:60
pe.realname = "Doe";//9:33
drawResolvedFixedChart(null);//15:228
window.test.name.firstname;//23:852
window.test.name.firstname;//15:836
window.test.name.firstname;//10:823
var a = 1;

with (a) {
var pe = new Person("John");//17:9
with (b) {
drawResolvedFixedChart(null);//15:228
}
window.test.name.firstname;//23:852
window.test.name.firstname;//15:836
window.test.name.firstname;//10:823
var a = 1;
}