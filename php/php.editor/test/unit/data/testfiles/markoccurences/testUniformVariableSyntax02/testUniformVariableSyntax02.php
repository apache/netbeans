<?php

class UVS1 {
    /**
     * @return self
     */
    public static function myStatic1() {
        return new self;
    }
    public function my1() {
    }
}

class UVS2 {
    public function my2(): UVS1 {
        return new UVS1();
    }
}

class UVS3 {
    public static function myStatic3(): UVS2 {
        return new UVS2();
    }
}

UVS3::myStatic3()->my2()::myStatic1()->my1();
