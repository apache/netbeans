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

// anonymous classes

echo new class {
    const CONSTANT = 'constant';
}::CONSTANT;

echo new class {
    const CONSTANT = 'constant';
}::{'CONSTANT'};

echo new class {
    public $field = 'field';
}->field;

echo new class {
    public static $staticField = 'static field';
}::$staticField;

new class {
    public function method() {}
}->method();

$anon = new #[Attr()] class {
    public function method() {}
}->method();

new class {
    public static function staticMethod() {}
}::staticMethod();

new class {
    public function __invoke() {}
}();

$anon = new #[Attr(1, 2)] class {
    public function __invoke() {}
}();

new class () implements ArrayAccess {
}['key'];

$anon = new class () implements ArrayAccess {
    private int $field = 1;
}['key'];

isset(new class ($a, 1, "test") implements ArrayAccess {
}['key']);
