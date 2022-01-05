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

$x = 2;
match($x){
1 => '1',
2 => '2',
default => 10,
};

$result = match($x){
1 => '1',
2 => '2',
default => 10,
};

var_dump($result);

$x = 1;
$result = match($x)
{
1 => '1',
2 => '2',
default => 10
};

var_dump($result);

$x = 5;
$result = match ($x) {
 1, => '1',
2, => '2',
default, => 10,
};

var_dump($result);

$result = match ($x) {
1, => '1', // comment
2, => '2', // comment
default, => 10
};

var_dump($result);

$x = 0;
$result = match(true) {
$x < 0 => 'negative',
$x === 0 => 'zero',
$x > 0 => 'positive',
default => print "default",
};

var_dump($result);

// empty body
$result = match(true) {
};

var_dump($result);

$x = 3;
$result = match ($x) {
1, 2 => '1, 2',
3, 4, 5, => '3, 4, 5',
default => 10,
};

var_dump($result);

// nested match
$x = 1;
$y = 2;
$result = match ($x) {
1 => match ($y) {
  2 => 2,
  default => 3,
},
default => 10,
};

$result = match ($x)
{
1 => match($y)
{
2 => 2,
default => 3,
},
default => 10,
};

echo $result . PHP_EOL;

echo match (true) {
    true => "true",
    false => "false",
} . PHP_EOL;

class MatchExpression
{
public const START = "start";
public const SUSPEND = "suspend";
public const STOP = "stop";
private const match = "match"; // context sensitive lexer

private static $start = "start state";
private $suspend = "suspend state";

  public function run(): string {
 $state = self::STOP;
 return match($state) {
  MatchExpression::START => self::$start,
MatchExpression::SUSPEND => $this->suspend,
MatchExpression::STOP => $this->stopState(),
default => MatchExpression::default(),
  };
}

    public function stopState(): string {
        return "stop state";
    }

    public static function default(): string {
        return "default";
    }

    public function match(): void {
        echo "Context Sensitive Lexer" . PHP_EOL;
    }
}

$instance = new MatchExpression();
var_dump($instance->run());
$instance->match();

function test($param1, $param2): void {
    echo $param2 . PHP_EOL;
}

test("test", match(true) {
true => "true",
false => 'false',
});
