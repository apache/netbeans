<?php
namespace First;

use Second\Delegate;
use Second\UnusedDeprecated;
use Second as S;

class ClassName implements S\Iface {
    use \Second\Trt;
    /**
     * @var \Second\Delegate
     */
    private $delegate;

    /**
     * @param S\Delegate  $param
     */
    function info($param) {
        Delegate::FOO;
        \Second\Delegate::FOO;
        S\Delegate::FOO;
        Delegate::staticFunction($param);
    }

}

namespace Second;

/** @deprecated */
class Delegate {
}

/** @deprecated */
class UnusedDeprecated {
}

/** @deprecated */
interface Iface {}

/** @deprecated */
trait Trt {}

?>