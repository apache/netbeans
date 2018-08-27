<?php
$name = "test";
function foo() {
    global $name;
    echo $name;
}
?>