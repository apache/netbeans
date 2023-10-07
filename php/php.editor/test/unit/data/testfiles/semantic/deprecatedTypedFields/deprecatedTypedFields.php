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

class TypedFields {

    /** @deprecated */
    public int $typedField;

    /** @deprecated */
    public int $multiField1, $multiField2;

    /** @deprecated */
    public ?string $nullableTypeField;

    /**
     * @deprecated
     */
    public string|TypedFields $unionTypeField;

    /**
     * @deprecated
     */
    public Foo&Bar $intersectionTypeField;

    /**
     * @deprecated
     */
    public (Foo&Bar)|Baz $dnfTypeField;

    /** @deprecated */
    public static int $typedStaticField;

    /** @deprecated */
    public static int $multiStaticField1, $multiStaticField2;

    /** @deprecated */
    public static ?string $nullableTypeStaticField;

    /**
     * @deprecated
     */
    public static string|TypedFields $unionTypeStaticField;

    /**
     * @deprecated
     */
    public static Foo&Bar $intersectionTypeStaticField;

    /**
     * @deprecated
     */
    public static (Foo&Bar)|Baz $dnfTypeStaticField;
}

trait TypedFieldsTrait {

    /** @deprecated */
    public int $typedField;

    /** @deprecated */
    public int $multiField1, $multiField2;

    /** @deprecated */
    public ?string $nullableTypeField;

    /**
     * @deprecated
     */
    public string|TypedFields $unionTypeField;

    /**
     * @deprecated
     */
    public Foo&Bar $intersectionTypeField;

    /**
     * @deprecated
     */
    public (Foo&Bar)|Baz $dnfTypeField;

    /** @deprecated */
    public static int $typedStaticField;

    /** @deprecated */
    public static int $multiStaticField1, $multiStaticField2;

    /** @deprecated */
    public static ?string $nullableTypeStaticField;

    /**
     * @deprecated
     */
    public static string|TypedFields $unionTypeStaticField;

    /**
     * @deprecated
     */
    public static Foo&Bar $intersectionTypeStaticField;

    /**
     * @deprecated
     */
    public static (Foo&Bar)|Baz $dnfTypeStaticField;
}
