<?php

$func1 =    function      ($arg)   use  ($param) {
    echo "$param\n";
};


$func2 =    function($arg)   use($param) {
    echo "$param\n";
};

?>
