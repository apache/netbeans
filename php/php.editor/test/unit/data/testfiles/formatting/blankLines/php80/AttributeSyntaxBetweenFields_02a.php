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

class AttributeSyntax {
    #[A(1)]
    public const CONSTANT1 = 1;
    #[A(1)] // comment
    public const CONSTANT2 = 2;


    /**
     * comment
     */
    #[A(1)]
    public const CONSTANT3 = 3;
    #[A(1)]
    /**
     * comment1
     */
    public const CONSTANT4 = 4;

    /**
     * comment1
     */
    #[A(1)]
    /**
     * comment2
     */
    public const CONSTANT5 = 5;

    #[A(1)]
    // comment
    public const CONSTANT6 = 6;
    // comment
    #[A]
    public const CONSTANT7 = 7;

#[A(1)]
#[A(2)]

    public const CONSTANT8 = 8;


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
    // comment
    // comment
    #[A]
    private int | bool $field9 = 1;

}
