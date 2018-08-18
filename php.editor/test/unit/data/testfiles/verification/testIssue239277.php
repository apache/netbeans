<?php
namespace z;
use app\model\Foo;
use app\model\Bat;
class Bar {
    public $nic;
    function __construct() {
        $this->nic = new Foo();
        Foo::ahoj();
        Bat::$barz;
    }
}
?>