<?php
class Test148856Class{
    function function_name() {}
}

/**
 * @return Test148856Class
 */
function test148856Func($a){

}

test148856Func((1 + 1) * test148856Func(1))->function_name();
?>