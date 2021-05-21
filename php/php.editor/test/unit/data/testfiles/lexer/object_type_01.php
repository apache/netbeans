<?php

function parameterType(object $object1, object $object2) {
    echo get_class($object1) . PHP_EOL;
    echo get_class($object2) . PHP_EOL;
}

function returnType(): object {
    $stdClass = new stdClass();
    return $stdClass;
}
