<?php
class Bar {
    function sayHello() {
        echo "Hello!";
    }
}

/* @var $arrayOfBars \Bar[] */
$arrayOfBars = array(0 => new Bar());

$arrayOfBars[$arrayOfBarArrayIndexes[0]]->sayHello();
$arrayOfBars[getArrayOfIndexes()[0]]->sayHello();

?>