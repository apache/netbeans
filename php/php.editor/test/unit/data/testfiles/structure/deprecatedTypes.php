<?php
namespace Hhh {
use \Omm\DeprType;

class ClassName {

    /**
     * @var \Omm\DeprType
     */
    private $foo;

    /**
     * @param \Omm\DeprType $param
     * @return \Omm\DeprType
     */
    function barBaz(\Omm\DeprType $param) {
        $this->foo;
    }

}
}
namespace Omm {
/** @deprecated */
class DeprType {
}
}