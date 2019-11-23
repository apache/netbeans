<?php
class InvocationComment {
  public function foo() {return $this;}
  public static function bar() {}
}
$obj = new InvocationComment();
$obj/**/->foo();
$obj /**/->foo();
$obj/**/ ->foo();
$obj /* aa */ ->foo();
$obj/**/-> /**/foo();
$obj/**/-> /**/ foo();
$obj/**/->/**/ foo();
$obj/**/->/**/foo();
$obj/**/-> /* aa */foo();
$obj/**/-> /* aa */ foo();
$obj/**/->/* aa */foo();
$obj/**/->/* aa */ foo();
$obj/**/->
 /* aa */
 foo();
$obj/**/->
 /** aa */
 foo();
$obj/**/->
 /**/
 foo();
$obj/**/->
 // aa
 foo();
$obj/**/->
 // aa
foo();
$obj/**/->
// aa
foo();

InvocationComment/**/::bar();
InvocationComment /**/::bar();
InvocationComment/**/ ::bar();
InvocationComment /* aa */ ::bar();
InvocationComment/**/:: /**/bar();
InvocationComment/**/:: /**/ bar();
InvocationComment/**/::/**/bar();
InvocationComment/**/::/**/ bar();
InvocationComment/**/:: /* aa */bar();
InvocationComment/**/:: /* aa */ bar();
InvocationComment/**/::/* aa */bar();
InvocationComment/**/::/* aa */ bar();
InvocationComment/**/::
 /* aa */
 bar();
InvocationComment/**/::
 /** aa */
 bar();
InvocationComment/**/::
 /**/
 bar();
InvocationComment/**/::
 // aa
 bar();
InvocationComment/**/::
 // aa
bar();
InvocationComment/**/::
// aa
bar();
?>