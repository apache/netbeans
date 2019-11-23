<?php

class ParentClass {

    const IMPLICIT_PUBLIC_PARENT_CONST = 0;
    public const PUBLIC_PARENT_CONST = 0;
    private const PRIVATE_PARENT_CONST = 0;
    protected const PROTECTED_PARENT_CONST = 0;

    public function test() {
        ParentClass::IMPLICIT_PUBLIC_PARENT_CONST; // CC in class
        self::IMPLICIT_PUBLIC_PARENT_CONST; // CC in class
        static::PRIVATE_PARENT_CONST; // CC in class
    }

}

class ChildClass extends ParentClass {

    const IMPLICIT_PUBLIC_CHILD_CONST = 0;
    public const PUBLIC_CHILD_CONST = 0;
    private const PRIVATE_CHILD_CONST = 0;
    protected const PROTECTED_CHILD_CONST = 0;

    public function childTest() {
        ChildClass::IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex
        self::IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex
        static::IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex
    }

}

interface TestInterface {

    const IMPLICIT_INTERFACE_PUBLIC_CONST = 0;
    public const PUBLIC_INTERFACE_CONST = 1;

}

class TestInterfaceImpl implements TestInterface {

    const IMPLICIT_PUBLIC_INTERFACE_IMPL_CONST = [1, 2];
    public const PUBLIC_INTERFACE_IMPL_CONST = 0;
    private const PRIVATE_INTERFACE_IMPL_CONST = 0;
    protected const PROTECTED_INTERFACE_IMPL_CONST = 0;

    public function test() {
        TestInterfaceImpl::IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl
        self::IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl
        static::IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl
    }

}

class ChildClass2 extends ParentClass implements TestInterface {

    const IMPLICIT_PUBLIC_CHILD2_CONST = 0;
    public const PUBLIC_CHILD2_CONST = 0;
    private const PRIVATE_CHILD2_CONST = 0;
    protected const PROTECTED_CHILD2_CONST = 0;

    public function child2Test() {
        ChildClass2::IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl
        self::IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl
        static::IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl
    }

}

ParentClass::IMPLICIT_PUBLIC_PARENT_CONST; // CC global
ChildClass::IMPLICIT_PUBLIC_CHILD_CONST; // CC global
TestInterfaceImpl::IMPLICIT_PUBLIC_INTERFACE_IMPL_CONST; // CC global
ChildClass2::PUBLIC_CHILD2_CONST; // CC global

$p = new ParentClass();
$p->test();
$c1 = new ChildClass();
$c1->childTest();
$c2 = new ChildClass2();
$c2->child2Test();
$t = new TestInterfaceImpl();
$t->test();
