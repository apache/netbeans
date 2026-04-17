<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.
class Test1{}
class Test2{}
class Test3{}
class AsymmetricVisibility {
    public function __construct(
        public protected(set) readonly Test1|(Test2 &)
    ) {}
}
