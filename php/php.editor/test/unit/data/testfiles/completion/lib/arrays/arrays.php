<?php

class ArraysCc {
    /**
     * desc
     *
     * @var ArraysCc[][int][string]
     */
    public $field;
    
    function method() {
    }
}

/**
 *
 * @return ArraysCc[] foo
 */ 
function arrayFunctionName() {
    return array(new ArraysCc());
}

/**
 *
 * @return ArraysCc[] foo
 */
function arrayFunctionTyped(): array {
    return array(new ArraysCc());
}

(new ArraysCc)->field[0]->field;

$a = array(new ArraysCc());
$a[0]->field;

arrayFunctionName()[0]->field;

$b = arrayFunctionName();
$b[0]->field;

arrayFunctionTyped()[0]->field;

$c = arrayFunctionTyped();
$c[0]->field;

?>