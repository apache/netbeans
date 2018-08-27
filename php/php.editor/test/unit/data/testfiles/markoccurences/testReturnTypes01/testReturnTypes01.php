<?php

interface IfaceA {
    static function make(): IfaceA;
}
class ClsB implements IfaceA {
    static function make(): IfaceA {
        $IfaceA = 10;
        return new ClsB();
    }
    function test(): array {
    }
}

function create(IfaceA $a): ClsB {
}

function create2(ClsB $b): array {
}
