<?php
interface ifaceModelTest1 {
    function methodIfaceModelTest1();
}
interface ifaceModelTest2 extends ifaceModelTest1 {
    function methodIfaceModelTest2();
}
abstract class clsModelTest1 implements ifaceModelTest1 {
    function methodClsModelTest1() {}
}
abstract class clsModelTest2 extends clsModelTest1 implements ifaceModelTest2 {
    function methodClsModelTest2() {}
}
?>
