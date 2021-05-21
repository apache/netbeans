<?php

class ClassConstantVisibility {

    const IMPLICIT_PUBLIC_CONST = 0;
    public const PUBLIC_CONST = 1;
    private const PRIVATE_CONST = 2;
    protected const PROTECTED_CONST = 3;
    private const FOO = 1, BAR = [1, 2];

}

interface InterfaceConstantVisibility {

    const IMPLICIT_PUBLIC_CONST = 0;
    public const PUBLIC_CONST = 1;

}
