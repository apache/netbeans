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

class DuplicateTypes {

    private First|First $union;
    private First&First $intersection;
    private First|Second|First|Second $twoUnions;
    private First&Second&First&Second $twoIntersections;

    private bool|true|bool $unionBoolAndTrue;
    private bool|false|bool $unionBoolAndFalse;

    private bool|true|First|First $unionAfterBoolAndTrue;
    private bool|false|First|First $unionAfterBoolAndFalse;
    private true|bool|First|First $unionAfterTrueAndBool;
    private false|bool|First|First $unionAfterFalseAndBool;

    public function returnUnion(): First|First {
    }

    public function returnIntersection(): First&First {
    }

    public function returnTwoUnions(): First|Second|First|Second {
    }

    public function returnTwoIntersections(): First&Second&First&Second {
    }

    public function returnUnionBoolAndTrue(): bool|true|bool {
    }

    public function returnUnionBoolAndFalse(): bool|false|bool {
    }

    public function returnUnionAfterBoolAndTrue(): bool|true|First|First {
    }

    public function returnUnionAfterBoolAndFalse(): bool|false|First|First {
    }

    public function returnUnionAfterTrueAndBool(): true|bool|First|First {
    }

    public function returnUnionAfterFalseAndBool(): false|bool|First|First {
    }

    public function parameterUnion(First|First $union) {
    }

    public function parameterIntersection(First&First $intersection) {
    }

    public function parameterTwoUnions(First|Second|First|Second $twoUnions) {
    }

    public function parameterTwoIntersections(First&Second&First&Second $twoIntersections) {
    }
    
    public function parameterUnionBoolAndTrue(bool|true|bool $union) {
    }

    public function parameterUnionBoolAndFalse(bool|false|bool $union) {
    }

    public function parameterUnionAfterBoolAndTrue(bool|true|First|First $union) {
    }

    public function parameterUnionAfterBoolAndFalse(bool|false|First|First $union) {
    }

    public function parameterUnionAfterTrueAndBool(true|bool|First|First $union) {
    }

    public function parameterUnionAfterFalseAndBool(false|bool|First|First $union) {
    }
}

class First {}

class Second {}
