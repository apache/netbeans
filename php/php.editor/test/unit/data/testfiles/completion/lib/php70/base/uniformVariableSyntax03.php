<?php

class UVS1 {
    const MAX = 99;
    /** @var UVS1 */
    static $INSTANCE = UVS1::class;

    /**
     * @return self
     */
    public static function myStatic1() {
        return __CLASS__;
    }
}

class UVS2 {
    public static function myStatic2(): UVS1 {
        return new UVS1();
    }
}

class UVS3 {
    public static function myStatic3(): UVS2 {
        return new UVS2();
    }
}

UVS3::myStatic3()::myStatic2()::myStatic1()::MAX;
UVS3::myStatic3()::myStatic2()::$INSTANCE::myStatic1();
