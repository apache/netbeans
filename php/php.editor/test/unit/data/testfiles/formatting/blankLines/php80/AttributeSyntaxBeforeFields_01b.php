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

// options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);

class AttributeSyntax {
    #[A(1)]
    public int|string $field1 = 1;
    #[A(1)] // comment
    public int $field2 = 1;


    /**
     * comment
     */
    #[A(1)]
    public   $field3 = 1;
    #[A(1)]
    /**
     * comment1
     */
    public  static $field4 = 1;

    /**
     * comment1
     */
    #[A(1)]
    /**
     * comment2
     */
      public   $field5;

    #[A(1)]
    // comment
    private int | bool $field6 = 1;
    // comment
    #[A]
    private int | bool $field7 = 1;

#[A(1)]
#[A(2)]

    private Test $field8;
}
