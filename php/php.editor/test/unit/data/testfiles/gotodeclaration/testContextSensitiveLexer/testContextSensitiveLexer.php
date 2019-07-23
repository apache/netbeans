<?php
interface MyInterface
{
    const interface = "interface";

    public function implements();
}

class ParentClass implements MyInterface
{
    const CONST = "CONST";

    public static function new($new) {
        self::interface;
        return new static();
    }

    public function implements() {
    }
}

trait MyTrait
{
    public function trait($a) {
    }
}

class ChildClass extends ParentClass
{
    use MyTrait;

    const GOTO = [1, 2], IF = 2;
    const ECHO = "ECHO", FOR = 1;

    public function foreach($test) {
        self::GOTO[0];
        static::ECHO;
        parent::CONST;
        ChildClass::IF;
        $this->implements();
    }

    public static function for() {
    }
}

MyInterface::interface;

$parent = new ParentClass();
$parent::CONST;
ParentClass::CONST;

$child = new ChildClass();
$child::GOTO[0];
ChildClass::FOR;
ChildClass::for();
ChildClass::new("test");
$child->foreach("test");
$child->trait("trait");
