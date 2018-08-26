<?php

$testArray = array(
'subarray1' => array(
'field1' => 1,
'field2' => 2
),
'subarray2' => array(
'field3' => 3,
'field4' => 4
)
);

class ClassName {

var $test = array(
'one' => 10,
'two' => 5,
'three' => 2,
'nested' => array(
'one',
'two',
'three',
'four',
)
);

function bla() {
$this->getParser()->registerKeys(
array(
"test1" => "passed"
));
}

}
?>

