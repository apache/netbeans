<?php

trait AA {
    public function bar(){}
}

trait BB {
    public function bar(){}
}

trait CC {
    public function bar(){}
}

trait DD {
    public function bar(){}
}

class Foo {
    use AA, BB, CC, DD {
        CC::bar insteadof AA, BB;
		DD::bar as foo;
    }

}

$foo = new Foo();
$foo->bar();
?>