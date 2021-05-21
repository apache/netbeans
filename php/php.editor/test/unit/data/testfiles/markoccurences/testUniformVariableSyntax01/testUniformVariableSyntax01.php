<?php

class UVS1 {
    const MAX = 99;
    const AVG = 50;
    static $MIN = "MIN";
    static $TOTAL = "TOTAL";
    /**
     * @var UVS1
     */
    static $INSTANCE;

    public $test = null;

    /**
     * @return self
     */
    public static function myStatic1() {
        return __CLASS__;
    }
    public static function myStatic2() { // UVS1
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

UVS3::myStatic3()::myStatic2();
UVS3::myStatic3()::myStatic2()::myStatic1()::MAX;
UVS3::myStatic3()::myStatic2()::myStatic1()::AVG;
UVS3::myStatic3()::myStatic2()::myStatic1()::$MIN;
UVS3::myStatic3()::myStatic2()::myStatic1()::$TOTAL;
UVS3::myStatic3()::myStatic2()::myStatic1()::$INSTANCE::myStatic1();
UVS3::myStatic3()::myStatic2()::myStatic1()::myStatic2();
UVS3::myStatic3()->myStatic2()::myStatic1()->myStatic2();
UVS3::myStatic3()->myStatic2()::myStatic1()->test;
