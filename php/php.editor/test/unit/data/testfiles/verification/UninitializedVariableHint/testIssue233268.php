<?php
function functionName($param) {
    return array_filter(function (Foo $foo) use ($param) {
        return $foo->bar($param);
    });
}
?>