<?php
/**
 * @property Mixin $Mixin Description
 */
class FieldAccess
{
    /**
     * @var C1
     */
    public $C1;
    public function __construct()
    {
    }
}

$fieldAccess = new FieldAccess();
$fieldAccess->C1->publicMethodC1(); // CC
$fieldAccess->Mixin->publicMethodC1(); // CC

$fieldAccess->C1::publicStaticMethodC1(); // CC
$fieldAccess->Mixin::publicStaticMethodC1(); // CC
