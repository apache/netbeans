<?php

namespace Test\Sub;

class ParentClass {

    const IMPLICIT_PUBLIC_PARENT_CONST = [0, 1];
    public const PUBLIC_PARENT_CONST = 0;
    private const PRIVATE_PARENT_CONST = "private";
    protected const PROTECTED_PARENT_CONST = [0, 1];

}

class ChildClass extends ParentClass {
}

interface TestInterface {

    const IMPLICIT_PUBLIC_INTERFACE_CONST = 1;
    public const PUBLIC_INTERFACE_CONST = 0;

}

class TestInterfaceImpl implements TestInterface {
}

namespace Test\Sub2;

\Test\Sub\ParentClass::IMPLICIT_PUBLIC_PARENT_CONST[1];
\Test\Sub\ParentClass::PUBLIC_PARENT_CONST;
\Test\Sub\ChildClass::IMPLICIT_PUBLIC_PARENT_CONST[0];
\Test\Sub\ChildClass::PUBLIC_PARENT_CONST;
\Test\Sub\TestInterface::IMPLICIT_PUBLIC_INTERFACE_CONST;
\Test\Sub\TestInterface::PUBLIC_INTERFACE_CONST;
\Test\Sub\TestInterfaceImpl::IMPLICIT_PUBLIC_INTERFACE_CONST;
\Test\Sub\TestInterfaceImpl::PUBLIC_INTERFACE_CONST;
