<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Simple class for our unit tests.
 */
class Calculator {

    /**
     * @assert (0, 0) == 0
     * @assert (0, 1) == 1
     * @assert (1, 0) == 1
     * @assert (1, 1) == 2
     */
    public function plus($a, $b) {
        return $a + $b;
    }

    /**
     * @assert (0, 0) == 0
     * @assert (0, 1) == -1
     * @assert (1, 0) == 1
     * @assert (1, 1) == 0
     */
    public function minus($a, $b) {
        return $a - $b;
    }

    /**
     * @assert (0, 0) == 0
     * @assert (0, 1) == 0
     * @assert (1, 0) == 0
     * @assert (1, 1) == 1
     * @assert (3, 2) == 6
     */
    public function multiply($a, $b) {
        return $a * $b;
    }

    /**
     * @assert (0, 1) == 0
     * @assert (1, 1) == 1
     * @assert (6, 2) == 3
     */
    public function divide($a, $b) {
        if ($b == 0) {
            throw new InvalidArgumentException('Cannot divide by zero');
        }
        return $a / $b;
    }

    public function modulo($a, $b) {
        return $a % $b;
    }

}
