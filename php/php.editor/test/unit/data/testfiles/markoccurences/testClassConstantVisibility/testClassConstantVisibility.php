<?php

class ParentClass {

    const IMPLICIT_PUBLIC_PARENT_CONST = 0;
    public const PUBLIC_PARENT_CONST = 0;
    private const PRIVATE_PARENT_CONST = "private";
    protected const PROTECTED_PARENT_CONST = [0, 1];

    public function test() {
        ParentClass::IMPLICIT_PUBLIC_PARENT_CONST;
        self::PUBLIC_PARENT_CONST;
        static::PRIVATE_PARENT_CONST;
        ParentClass::PROTECTED_PARENT_CONST[0];
    }

}

class ChildClass extends ParentClass {

    public function childTest() {
        ChildClass::IMPLICIT_PUBLIC_PARENT_CONST;
        ChildClass::PUBLIC_PARENT_CONST;
        ChildClass::PROTECTED_PARENT_CONST[0];
    }

}

interface TestInterface {

    const IMPLICIT_PUBLIC_INTERFACE_CONST = 1;
    public const PUBLIC_INTERFACE_CONST = 0;

}

class TestInterfaceImpl implements TestInterface {

    public function test() {
        TestInterfaceImpl::IMPLICIT_PUBLIC_INTERFACE_CONST;
        TestInterfaceImpl::PUBLIC_INTERFACE_CONST;
    }

}

class ChildClass2 extends ParentClass implements TestInterface {

    public function child2Test() {
        ChildClass2::IMPLICIT_PUBLIC_PARENT_CONST;
        ChildClass2::PUBLIC_PARENT_CONST;
        ChildClass2::PROTECTED_PARENT_CONST[1];
        ChildClass2::IMPLICIT_PUBLIC_INTERFACE_CONST;
        ChildClass2::PUBLIC_INTERFACE_CONST;
    }

}

ParentClass::IMPLICIT_PUBLIC_PARENT_CONST; // global
ChildClass::IMPLICIT_PUBLIC_PARENT_CONST; // global
ChildClass2::IMPLICIT_PUBLIC_PARENT_CONST; // global
ParentClass::PUBLIC_PARENT_CONST; // global
ChildClass::PUBLIC_PARENT_CONST; // global
ChildClass2::PUBLIC_PARENT_CONST; // global
TestInterface::IMPLICIT_PUBLIC_INTERFACE_CONST; // global
TestInterfaceImpl::IMPLICIT_PUBLIC_INTERFACE_CONST; // global
TestInterface::PUBLIC_INTERFACE_CONST; // global
TestInterfaceImpl::PUBLIC_INTERFACE_CONST; // global
