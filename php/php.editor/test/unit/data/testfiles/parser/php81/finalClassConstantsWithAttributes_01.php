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

class FinalPrivateConstant
{
    #[A("attribute")]
    final const IMPLICIT_PUBLIC_CONST = "foo";
    #[A("attribute")]
    final public const PUBLIC_CONST = "foo";
    #[A("attribute")]
    final private const PRIVATE_CONST = "foo";
    #[A("attribute")]
    final protected const PROTECTED_CONST = "foo";

    #[A("attribute")]
    public final const PUBLIC_CONST2 = "foo";
    #[A("attribute")]
    private final const PRIVATE_CONST2 = "foo";
    #[A("attribute")]
    protected final const PROTECTED_CONST2 = "foo";

    #[A("attribute")]
    final const IMPLICIT_PUBLIC_CONST2 = "foo", IMPLICIT_PUBLIC_CONST3 = "foo";
    #[A("attribute")]
    final public const PUBLIC_CONST3 = "foo", PUBLIC_CONST4 = "foo";
    #[A("attribute")]
    final private const PRIVATE_CONST3 = "foo", PRIVATE_CONST4 = "foo";
    #[A("attribute")]
    final protected const PROTECTED_CONST3 = "foo", PROTECTED_CONST4 = "foo";

    #[A("attribute")]
    public final const PUBLIC_CONST5 = "foo", PUBLIC_CONST6 = "foo";
    #[A("attribute")]
    private final const PRIVATE_CONST5 = "foo", PRIVATE_CONST6 = "foo";
    #[A("attribute")]
    protected final const PROTECTED_CONST5 = "foo", PROTECTED_CONST6 = "foo";
}

interface I
{
    #[A("attribute")]
    final const IMPLICIT_CONST = "i";
    #[A("attribute")]
    final public const PUBLIC_CONST1 = "i";
    #[A("attribute")]
    public final const PUBLIC_CONST2 = "i";
}
