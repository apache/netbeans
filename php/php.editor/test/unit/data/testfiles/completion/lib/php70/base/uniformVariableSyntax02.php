<?php

class Numbers {
    const MAX = 100;
    public function hello() {
    }
}

class Cls {
    /**
     * @return Numbers[]
     */
    public function getNumbers() {
        return [new Numbers()];
    }
}

function fnc(): Cls {
    return new Cls();
}

fnc()->getNumbers()[0]::MAX;
