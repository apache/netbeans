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

namespace app\classes;

use app\traits\MyTrait as AliasedTrait;

class MyClassAlias
{
    use AliasedTrait;
}

namespace app;

use app\classes\MyClassAlias;

$myClassAlias = new MyClassAlias();
$myClassAlias->myFunction('hello aliased');
