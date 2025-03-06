<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.
class ExampleClass {
    private(set) int $property {
        get {
            if (true) {
                echo "test if";
            } else {
                echo "test else"
            }
        }
        set {}
    } // comment^
}
