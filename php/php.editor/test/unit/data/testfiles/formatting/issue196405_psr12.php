<?php

class foo {

    public function test() {
        // just the function call
        $this->foobar(
            1, 2
        );
        // fuction call AND assignment, this changes on reformat!
        $x = $this->foobar(
                1, 2
        );
    }

}

$y = foo (1,
"some text",
"another text"
);