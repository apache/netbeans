<?php

function test(callable $callable) : callable {
    return $callable;
}

trait MyTrait {

    public function test($param) {
        echo __TRAIT__ . PHP_EOL;
    }

}
