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

class OverridingFinalConstantParent
{
    final const IMPLICIT_PUBLIC_CONST = "parant";
    final public const PUBLIC_CONST = "parant";
    final protected const PROTECTED_CONST = "parant";
}

class OverridingFinalConstantChild extends OverridingFinalConstantParent
{
    const IMPLICIT_PUBLIC_CONST_CHILD = "child";
    public const PUBLIC_CONST_CHILD = "child";
    private const PRIVATE_CONST_CHILD = "child";
    protected const PROTECTED_CONST_CHILD = "child";

    // overriding
    const IMPLICIT_PUBLIC_CONST = "child";
    public const PUBLIC_CONST = "child";
    protected const PROTECTED_CONST = "child";
}
