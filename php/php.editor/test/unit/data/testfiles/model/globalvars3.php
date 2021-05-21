<?php
class MyClass {
    public function my() {}
}
class YourClass {
    public function your() {}
}
$varA = new MyClass();
if (true) {
    $varA = new YourClass();
}
$varA->your();
function fnc(YourClass $varA) {
    $varB = $varA;
}
?>
