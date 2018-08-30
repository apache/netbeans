<?php

trait FirstTrait {
}

trait SecondTrait {
}

trait T {

    use FirstTrait; // test trait first
    use SecondTrait; // test trait second

}

trait U {

    public static $sf = "static field";
    use FirstTrait; // test after field

}

class A {

    use FirstTrait; // test class first
    use SecondTrait; // test class second

}

class B {

    const CONSTANT = "CONSTANT";
    use FirstTrait; // test after const

}
