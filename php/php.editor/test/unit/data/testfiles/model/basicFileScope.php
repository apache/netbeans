<?php
class ClsWithStaticMembers {
    public static $myStatic;
    public static function staticTest() {
        self::$myStatic;
    }
}

interface MySuperIFace {
    public function supermeth($name);
}

interface MyIFace extends MySuperIFace {
    public function meth($name);
}

class MySuperClass implements MyIFace {
    public function meth($name){}
}

class MyClass extends MySuperClass {
    public $myFld;

    /**
     * @return MySuperClass|MyIFace
     */
    public function meth($name) {
        function myfnc4() {}
        return new MySuperClass();
    }
    public static function statmeth($name) {}
}

class MyException extends Exception {
}

$a = new MyClass();
$b = $a;
$a = new MySuperClass();

function myfnc(MyClass $param) {
    global $a;
    $c = $a;
    $d =$c;
    try {
    } catch(MyException $exc) {
    }
    return null;
}

function myfnc2() {
    function myfnc3() {}
}
myfnc();
myfnc();

?>
