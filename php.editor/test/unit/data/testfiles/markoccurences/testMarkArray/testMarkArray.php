<?php
$name = "whatever";
class TestArray148261 {
    private static $static_array = array('', 'thousand ', 'million ', 'billion ');
    private $field_array = array('', 'thousand ', 'million ', 'billion ');
    function test() {
        $idx = 1;
        $result .= self::$static_array[$idx++];
        $result .= $this->field_array[$idx++];
        $instance_array = array('', 'thousand ', 'million ', 'billion ');
        $instance_array[$idx];
        $result .= self::$static_array[$instance_array[$idx]];
        $result .= $this->field_array[$instance_array[$idx]];
        $result .= $this->field_array[$instance_array[$GLOBALS['name']]];
    }
}
function test2() {
    $idx2 = 1;
    $instance_array2 = array('', 'thousand ', 'million ', 'billion ');
    $instance_array2[$idx2];
}

$idx3 = 1;
$instance_array3 = array('', 'thousand ', 'million ', 'billion ');
$instance_array3[$idx3];
?>
