<?php
interface ifaceDeclaration {}
interface ifaceDeclaration2 extends ifaceDeclaration  {}
class clsDeclaration implements ifaceDeclaration {}
class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}
class clsDeclaration3 extends clsDeclaration {}
class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}
function formalParamFuncCall1(
    ifaceDeclaration $ifaceDeclarationVar,
    ifaceDeclaration2 $ifaceDeclaration2Var,
    ifaceDeclaration4 $ifaceDeclaration4Var,
    clsDeclaration  $clsDeclarationVar,
    clsDeclaration2 $clsDeclaration2Var,
    clsDeclaration3 $clsDeclaration3Var,
    clsDeclaration4 $clsDeclaration4Var

) {
    $ifaceDeclaration = 1;
    $ifaceDeclaration2 = 1;
    $ifaceDeclaration4 = 1;
    $clsDeclaration  = 1;
    $clsDeclaration2 = 1;
    $clsDeclaration3 = 1;
    $clsDeclaration4 = 1;
}
function ifaceDeclaration() {
    try {
    } catch (clsDeclaration $cex) {
        if ($cex instanceof clsDeclaration) {
            $cex = new clsDeclaration;
        }
        echo $cex;
    }
}
function ifaceDeclaration2() {}
function ifaceDeclaration4() {}
function clsDeclaration() {}
function clsDeclaration2() {}
function clsDeclaration3() {}
function clsDeclaration4() {}
?>
