<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

namespace DeprecatedTest1;

function test(): void {}

namespace DeprecatedTest2;

use Deprecated;

#[Deprecated]
function deprecated(): void {
}

#[Attr1, Attr2()]
#[Deprecated]
function deprecated02(): void {
}

#[Attr1, Attr2()]
#[Deprecated]
#[Attr3]
function deprecated03(): void {
}

#[Attr1, Attr2()]
function deprecated04(): void {
}

/**
 * @deprecated since 2.0
 * @return void
 */
#[Deprecated]
function deprecatedWithPhpDoc01(): void {
}

/**
 * @deprecated since 2.0
 * @return void
 */
#[Attr]
function deprecatedWithPhpDoc02(): void {
}

#[Deprecated("2.0.0", "use newFunction() instead")]
function deprecatedWithParam01(): void {
}

#[Deprecated(since: "2.0.0", message: "use newFunction() instead")]
function deprecatedWithParam02(): void {
}

#[\Deprecated]
function deprecatedFQN(): void {
}

#[\Attr1, \Attr2()]
#[\Deprecated]
function deprecatedFQN02(): void {
}

#[\Attr1, \Attr2()]
#[\Deprecated]
#[\Attr3]
function deprecatedFQN03(): void {
}

#[\Attr1, \Attr2()]
function deprecatedFQN04(): void {
}

/**
 * @deprecated since 2.0
 * @return void
 */
#[\Deprecated]
function deprecatedFQNWithPhpDoc01(): void {
}

/**
 * @deprecated since 2.0
 * @return void
 */
#[\Attr]
function deprecatedFQNWithPhpDoc02(): void {
}

#[\Deprecated("2.0.0", "use newFunction() instead")]
function deprecatedFQNWithParam01(): void {
}

#[\Deprecated(since: "2.0.0", message: "use newFunction() instead")]
function deprecatedFQNWithParam02(): void {
}
