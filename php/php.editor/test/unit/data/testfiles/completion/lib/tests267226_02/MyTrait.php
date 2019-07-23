<?php

namespace app\traits;

trait MyTrait
{
    /**
     * Echoes the argument along with the function name.
     *
     * @param string $arg1 Some string.
     */
    public function myFunction($arg1)
    {
        echo __FUNCTION__ . " - $arg1\n";
    }
}
