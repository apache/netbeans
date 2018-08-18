<?php
interface iface1 {}
interface iface2  {}
interface iface3 extends iface1 {}
interface iface4 extends iface1, iface2 {}
interface iface5 extends iface1, iface2, iface3 {}

class cls1 {
    public function __call($param1, $param2) {}
}
class cls2 extends cls1 implements iface1 {
    public function __construct() {}
}
class cls3 extends cls2 implements iface2 {
    /**
     * @return cls1
     */
    private function privmeth($param1) {}
    /**
     * @return cls2
     */
    protected  function protmeth($param1, $param2) {}
    /**
     * @return cls3
     */
    public function pubmeth($param1) {}
    /**
     * @return cls3|cls1
     */
    public static function pubstatmeth(cls1 $param1) {}
}
class cls4 extends cls3 implements iface3 {}

/**
 * @return cls1
 */
function fnca($param1, $param2) {
    global $varb;
    $vara = new cls3();
    $varb = new cls1();
    $varc = new cls4();
}
/**
 * @return cls2
 */
function fncb() {}
$vara = new cls1();
$varb = new cls1();
$vara = new cls2();

?>
