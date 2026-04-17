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
namespace DeprecatedAttribute\Types\InNamespace;

class ExampleClass {
}

#[Deprecated]
class DeprecatedClass {
}

#[Attr, Deprecated(since: "since", message: "message")]
interface DeprecatedInterface {
}

#[Attr]
#[Deprecated]
trait DeprecatedTrait {
}

#[Deprecated]
enum DeprecatedEnum {
}

$anon = new #[Deprecated] class() {
};

// FQN
#[\Deprecated, \Attr]
class DeprecatedClass2 {
}

#[\Deprecated]
interface DeprecatedInterface2 {
}

#[\Deprecated]
trait DeprecatedTrait2 {
}

#[\Deprecated]
#[\Attr]
enum DeprecatedEnum2 {
}

$anon = new #[\Deprecated] class() {
};

// PHPDoc
/**
 * @deprecated since version number
 */
class DeprecatedClass3 {
}

/**
 * @deprecated since version number
 */
interface DeprecatedInterface3 {
}

/**
 * @deprecated since version number
 */
trait DeprecatedTrait3 {
}

/**
 * @deprecated since version number
 */
enum DeprecatedEnum3 {
}
