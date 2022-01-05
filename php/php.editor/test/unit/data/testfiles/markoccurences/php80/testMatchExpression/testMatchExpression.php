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
        return match ($state) {
            MatchExpression::START => self::$start,
            MatchExpression::SUSPEND => $this->suspend,
            MatchExpression::STOP => $this->stopState(),
            default =>  MatchExpression::default() . MatchExpression::match . $this->match(),
        };
    }

    public function stopState(): string {
        return "stop state";
    }

    public static function default(): string {
        return "default";
    }

    public function match(): string {
        return "Context Sensitive Lexer" . PHP_EOL;
    }
}

$instance = new MatchExpression();
var_dump($instance->run());
echo $instance->match();

echo match (true) {
    true => "true",
    false => "false",
    default => MatchExpression::default(),
} . PHP_EOL;
