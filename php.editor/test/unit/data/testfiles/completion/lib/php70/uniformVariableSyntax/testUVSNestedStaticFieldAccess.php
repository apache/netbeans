<?php

class UVS1
{
    const MAX = 99; // UVS1

    /**
     * @var UVS2
     */
    static $INSTANCE2;

    public function test() {
    }
}

class UVS2
{
    const MAX = 100; // UVS2

    /**
     * @var UVS3
     */
    static $INSTANCE3;

    public function test() {
    }
}

class UVS3
{
    const MAX = 101; // UVS3

    /**
     * @var UVS1
     */
    static $INSTANCE1;

    public function test() {
    }
}

function test() { // func
    return new UVS3();
}

UVS1::$INSTANCE2::$INSTANCE3::MAX;
test()::$INSTANCE1::$INSTANCE2::MAX;
