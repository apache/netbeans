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
class Session {

    public ?User $user;

    public function __construct(?User $user) {
        $this->user = $user;
    }

    public function getUser(): ?User {
        return $this->user;
    }

}

class User {

    private ?Address $address;
    private string $name;
    public int $id = 1;
    public static string $test = "test";
    protected $protectedField;

    public function __construct(string $name) {
        $this->address = new Address();
        $this->name = $name;
    }

    private function privateMethod() {
    }

    protected function protectedMethod() {
    }

    public function getAddress(): ?Address {
        return $this?->address;
    }

    public static function test(): string {
        return self::$test;
    }

    public static function create(string $name): User {
        return new User($name);
    }

}

class Address {

    public const ID = "Adress";
    public Country $country;

    public function __construct() {
        $this->country = new Country();
    }
}

class Country {
}

$session = new Session(new User("test"));
$country = $session?->user?->getAddress()?->country;
$country = $session?->user::$test;
$country = $session?->user::test();
$country = $session?->user->id;
$country = $session?->user?->getAddress()::ID;

$country = User::create("test")?->getAddress()?->country;
$country = $session->getUser()::create("test")?->getAddress()->country;

$country = (new User("test"))?->getAddress()->country;
