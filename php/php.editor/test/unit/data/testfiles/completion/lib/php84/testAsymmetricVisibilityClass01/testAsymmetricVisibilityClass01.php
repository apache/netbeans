<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

class Foo {}
class Bar {}

class AsymmetricVisibility {
    public(set) Foo $publicSet; // prop
    private(set) string|int $privateSet = 1; // prop
    protected(set) string|int $protectedSet1 = 1, $protectedSet2 = 1; // prop
    public protected(set) Bar $publicProtectedSet; // prop
    protected private(set) readonly int $protectedPrivateSet; // prop
    final protected private(set) int $finalProtectedPrivateSet; // prop
    final public private(set) readonly string $finalPublicPrivateSet; // prop
}

class AsymmetricVisibilityPromoted {
    public function __construct(
        public(set) Foo $publicSet, // constructor
        private(set) string|int $privateSet, // constructor
        protected(set) string|int $protectedSet, // constructor
        public protected(set) Bar $publicProtectedSet, // constructor
        protected private(set) readonly int $protectedPrivateSet, // constructor
        // cannot use final modifier
    ) {}
}
