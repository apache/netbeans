<?php

function say(?string $msg, int $num) {
    if ($msg) {
        echo $msg . PHP_EOL;
    }
}
