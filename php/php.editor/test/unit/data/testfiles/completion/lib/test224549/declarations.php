<?php

trait Trt {
    /**
     * @return OmgCls
     */
    function trtFnc() {}
}

class OmgCls {
    function clsFnc() {}
}

class Super {
    use Trt;
}

?>