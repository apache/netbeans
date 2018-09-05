<?php

function functionName($param) {
    if (true) {
        return;
    } else {
        return;
    }
}

class Foo {

    public function foo($xx) {
        return $fail;

        $lambda = function() use ($xx) {
            if (true) {
                return;
            } else {
                return;
            }
        };

        return $fail;
    }

    function functionName($xx) {
        $lambda = function() use ($xx) {
            if (true) {
                return;
            } else {
                return;
            }
        };
        return $ok;
    }

}

?>