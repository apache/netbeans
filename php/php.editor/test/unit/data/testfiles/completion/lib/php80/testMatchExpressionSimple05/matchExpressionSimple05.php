<?php
// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

$matchTest = "match test";
const MATCH_TEST_CONST = "match test const";
function matchTest() {
    return "match test";
}

$result = match ($matchTest) {
    "match test" => 
};

$result = match ($matchTest) {
    "match test" => matchTest(),
};

$result = match ($matchTest) {
    "match test" => matchT
};
