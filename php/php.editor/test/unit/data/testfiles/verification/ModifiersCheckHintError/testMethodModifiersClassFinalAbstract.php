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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

abstract class InvalidMethodClass {
    // errors
    // PHP Fatal error:  Cannot use the final modifier on an abstract method
    final abstract function finalAbstractClassMethod(): void {}
//  abstract function finalAbstractClassMethod(): void {}
//  final function finalAbstractClassMethod(): void {}

    final abstract public function finalAbstractPublicClassMethod(): void {}
//  abstract public function finalAbstractPublicClassMethod(): void {}
//  final public function finalAbstractPublicClassMethod(): void {}

    final abstract protected function finalAbstractProtectedClassMethod(): void {}
//  abstract protected function finalAbstractProtectedClassMethod(): void {}
//  final protected function finalAbstractProtectedClassMethod(): void {}

    final abstract private function finalAbstractPrivateClassMethod(): void {}
//  abstract private function finalAbstractPrivateClassMethod(): void {}
//  final private function finalAbstractPrivateClassMethod(): void {}
}
