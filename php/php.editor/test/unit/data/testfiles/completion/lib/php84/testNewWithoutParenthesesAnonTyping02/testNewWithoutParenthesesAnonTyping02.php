<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

echo new class () {
    const IMPLICIT_PUBLIC_CONST = "implicit public const";
    public const string PUBLIC_CONST = "public const";
    protected const string PROTECTED_CONST = "protected const";
    private const string PRIVATE_CONST = "private const";

    public int $publicField = 1;
    protected int $protectedField = 2;
    private int $privateField = 3;

    public static string $publicStaticField = "public static field";
    protected static string $protectedStaticField = "protected static field";
    private static string $privateStaticField = "private static field";

    public function publicMethod(): string {
        return "public method";
    }

    protected function protectedMethod(): string {
        return "protected method";
    }

    private function privateMethod(): string {
        return "private method";
    }

    public static function publicStaticMethod(): string {
        return "public static method";
    }

    protected static function protectedStaticMethod(): string {
        return "protected static method";
    }

    private static function privateStaticMethod(): string {
        return "private static method";
    }
}->;
