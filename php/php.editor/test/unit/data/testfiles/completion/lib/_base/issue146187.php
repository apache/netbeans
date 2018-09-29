<?php
class I146187Parent1 {
    function parent1() {}
}

class I146187Parent2 {
    function parent2() {}
}

class I146187ChildClass extends I146187Parent1{}
class I146187ChildClass extends I146187Parent2{}

{
    $tst = new I146187ChildClass;
    echo $tst->parent1();
}
?>