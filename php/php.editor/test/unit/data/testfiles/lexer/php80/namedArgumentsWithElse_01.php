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
$test = 0;
if ($test === 0):
    echo "0";
elseif ($test === 5):
    echo "5";
else :
    echo "else";
endif;

#[A(else: "else")]
class TestClass {

    public function test(): void {
        if ($test === 0):
            echo "0";
        elseif ($test === 5):
            echo "5";
        else :
            if ($test === 100):
                echo "100";
            else:
                testElse(if: "if", elseif: "elseif", else: $test, endif: "endif");
                testElse(elseif: "elseif", else: "else", endif: "endif", if: "if");
                testElse(else: "else", endif: "endif", if: "if", elseif: "elseif",);
                testElse(endif: "endif", if: "if", elseif: "elseif",else: "else",);
                echo "else";
            endif;
        endif;
    }
}
