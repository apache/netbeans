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

trait ConstTrait {
    const CONST_01 = 1;
    const CONST_01_A = 1, CONST_01_B = 1;
    const int CONST_02 = 1;
    const int CONST_02_A = 1, CONST_02_B = 1;
    public const PUBLIC_CONST = "pubic const";
    public const PUBLIC_CONST_A = "pubic const", PUBLIC_CONST_B = "pubic const";
    private const PRIVATE_CONST = 1;
    private const PRIVATE_CONST_A = 1, PRIVATE_CONST_B = 1;
    protected const PROTECTED_CONST = 1;
    protected const PROTECTED_CONST_A = 1, PROTECTED_CONST_B = 1;
    public const string PUBLIC_CONST_TYPE = "pubic const";
    public const string PUBLIC_CONST_TYPE_A = "pubic const", PUBLIC_CONST_TYPE_B = "pubic const";
    private const int PRIVATE_CONST_TYPE = 1;
    private const int PRIVATE_CONST_TYPE_A = 1, PRIVATE_CONST_TYPE_B = 1;
    protected const int PROTECTED_CONST_TYPE = 1;
    protected const int PROTECTED_CONST_TYPE_A = 1, PROTECTED_CONST_TYPE_B = 1;
    final const FINAL_IPUBLIC_CONST = 1; // OK as of PHP 8.1 class
    final public const FINAL_PUBLIC_CONST = 1; // OK as of PHP 8.1 class
    public final const FINAL_PUBLIC_CONST2 = 1; // OK as of PHP 8.1 class
    final protected const FINAL_PROTECTED_CONST = 1; // OK as of PHP 8.1 class
    protected final const FINAL_PROTECTED_CONST2 = 1; // OK as of PHP 8.1 class
    final const int FINAL_IPUBLIC_CONST_TYPE = 1; // OK as of PHP 8.1 class
    final public const int FINAL_PUBLIC_CONST_TYPE = 1; // OK as of PHP 8.1 class
    public final const int FINAL_PUBLIC_CONST_TYPE2 = 1; // OK as of PHP 8.1 class
    final protected const int FINAL_PROTECTED_CONST_TYPE = 1; // OK as of PHP 8.1 class
    protected final const int FINAL_PROTECTED_CONST_TYPE2 = 1; // OK as of PHP 8.1 class

    #[Attr("attribute")]
    const ATTR_CONST_01 = 1;
    #[Attr("attribute")]
    const ATTR_CONST_01_A = 1, ATTR_CONST_01_B = 1;
    #[Attr("attribute")]
    const int ATTR_CONST_02 = 1;
    #[Attr("attribute")]
    const int ATTR_CONST_02_A = 1, ATTR_CONST_02_B = 1;
    #[Attr("attribute")]
    public const ATTR_PUBLIC_CONST = "pubic const";
    #[Attr("attribute")]
    public const ATTR_PUBLIC_CONST_A = "pubic const", ATTR_PUBLIC_CONST_B = "pubic const";
    #[Attr("attribute")]
    private const ATTR_PRIVATE_CONST = 1;
    #[Attr("attribute")]
    private const ATTR_PRIVATE_CONST_A = 1, ATTR_PRIVATE_CONST_B = 1;
    #[Attr("attribute")]
    protected const ATTR_PROTECTED_CONST = 1;
    #[Attr("attribute")]
    protected const ATTR_PROTECTED_CONST_A = 1, ATTR_PROTECTED_CONST_B = 1;
    #[Attr("attribute")]
    public const string ATTR_PUBLIC_CONST_TYPE = "pubic const";
    #[Attr("attribute")]
    public const string ATTR_PUBLIC_CONST_TYPE_A = "pubic const", ATTR_PUBLIC_CONST_TYPE_B = "pubic const";
    #[Attr("attribute")]
    private const int ATTR_PRIVATE_CONST_TYPE = 1;
    #[Attr("attribute")]
    private const int ATTR_PRIVATE_CONST_TYPE_A = 1, ATTR_PRIVATE_CONST_TYPE_B = 1;
    #[Attr("attribute")]
    protected const int ATTR_PROTECTED_CONST_TYPE = 1;
    #[Attr("attribute")]
    protected const int ATTR_PROTECTED_CONST_TYPE_A = 1, ATTR_PROTECTED_CONST_TYPE_B = 1;
    #[Attr("attribute")]
    final const ATTR_FINAL_IPUBLIC_CONST = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    final public const ATTR_FINAL_PUBLIC_CONST = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    public final const ATTR_FINAL_PUBLIC_CONST2 = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    final protected const ATTR_FINAL_PROTECTED_CONST = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    protected final const ATTR_FINAL_PROTECTED_CONST2 = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    final const int ATTR_FINAL_IPUBLIC_CONST_TYPE = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    final public const int ATTR_FINAL_PUBLIC_CONST_TYPE = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    public final const int ATTR_FINAL_PUBLIC_CONST_TYPE2 = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    final protected const int ATTR_FINAL_PROTECTED_CONST_TYPE = 1; // OK as of PHP 8.1 class
    #[Attr("attribute")]
    protected final const int ATTR_FINAL_PROTECTED_CONST_TYPE2 = 1; // OK as of PHP 8.1 class
}
