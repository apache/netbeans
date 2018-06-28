<?php

class Numbers {
    const MAX = 100;
}

class Cls {
    public function getNumbers(): Numbers {
        return new Numbers();
    }
}

function fnc(): Cls {
    return new Cls();
}

fnc()->getNumbers()::MAX;
