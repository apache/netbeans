<?php

class VarType
{
    const CONSTANT = "CONSTANT";
    public $publicField = "publicField";

    public function test()
    {
    }
}

/** @var VarType $varType */
$varType = getVarType();
$varType->test(); // CC

/** @var VarType $value */
foreach ($array as $value) {
    $value->test(); // CC
}
