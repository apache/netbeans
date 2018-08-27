<?php
//START

class Top {
    function __construct() {
    }
}
class ParentCls extends Top {
    function __construct($bar = 1) {
    }
}
class Inter extends ParentCls {
    public function __construct() {
        parent::__construct(1,2);
    }

}
class MyCls extends Inter {
    function __construct() {
        parent::__construct();
    }
}

class Top2 {
    public function __construct($foo, $bar = 2) {
    }
}
class Ok1 extends Top2 {
    public function __construct() {
        parent::__construct(1);
    }
}
class Ok2 extends Top2 {
    public function __construct() {
        parent::__construct(1, 2);
    }
}
class NoOk1 extends Top2 {
    public function __construct() {
        parent::__construct();
    }
}
class NoOk2 extends Top2 {
    public function __construct() {
        parent::__construct(1, 2, 3);
    }
}

//END
?>