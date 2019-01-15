<?php

class ClassA extends ClassB implements InterfaceA, InterfaceB, InterfaceC {
public $number = 1;
private $letters = array ( "A",
        "B",
        "C",
        "D" );
public function method($text, $number) {
if ( $text == NULL ) {
    $text = "a";
}
else if ($number == 0
        && $text == "NetBeans" ) {
    $text = "empty";
}
else {
    $number++;
}

switch($number) {
case 1:
    return method("text", 22);
case 2:
    return 20;
default:
    return -1;
}
}
}
?>
