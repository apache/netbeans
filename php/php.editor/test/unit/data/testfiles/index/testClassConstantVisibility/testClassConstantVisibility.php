<?php

class ClassConstantVisibility {

    const IMPLICIT_PUBLIC_CONST = 0;
    public const PUBLIC_CONST = 1;
    private const PRIVATE_CONST = 2;
    protected const PROTECTED_CONST = 3;
    private const PRIVATE_FOO = 1, PRIVATE_BAR = [1, 2];

}

interface InterfaceConstantVisibility {

    const IMPLICIT_PUBLIC_INTERFACE_CONST = 0;
    public const PUBLIC_INTERFACE_CONST = 1;

}
