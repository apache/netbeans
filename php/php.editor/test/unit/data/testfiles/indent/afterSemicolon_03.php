<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.
class Test {
    public function test(): void {
        $anon = new class () {
            public function test(): void {
                if (true) {
                    
                }
                while (true) {
                    
                }
                foreach ($array as $key => $value) {
                    if (true) {
                        
                    } else {
                    }
                }
            }
        };^
    }
}
