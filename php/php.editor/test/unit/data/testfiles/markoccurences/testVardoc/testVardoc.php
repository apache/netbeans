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
$varType->test();

/* @var  $varType2   VarType  */
$varType2 = getVarType();
$varType2->test();

/** @var    VarType    $value */
foreach ($array as $value) {
    $value->test();
}
