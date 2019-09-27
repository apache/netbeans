<?php
namespace Core;

class User {

    public $uBar;

    /**
     * Some method in \Core\User, wich has a \Data\User as an argument.
     * @param \Data\User $var1
     */
    public function TheMethod($var1) {
        $var1->foo();
        //And here I simply create a new object of \Data\User class
        $var2 = new \Data\User();
        $var2->foo();

    }
}
?>