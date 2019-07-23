<?php
namespace Test\Space {
    class Space {
        public function __construct ($arg){}
    }
    class SpaceUniverse {
        public function __construct($arg){}
    }
}

namespace {
class Test
{
    public function __construct ($arg)
    {}
}

new Test($arg);
new \Test\Space\Space // CC here

}
?>