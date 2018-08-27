<?php

function generatorKV() {
    for ($i = 1; $i <= 3; $i++) {
        // do something
        yield $key => $value;
    }
}

?>