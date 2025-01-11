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

class DeprecatedClassConstant {
    #[Attr]
    public const string PUBLIC_CONSTANT = "public constant";
    private const string PRIVATE_CONSTANT = "private constant";
    protected const string PROTECTED_CONSTANT = "protected constant";
    #[Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT = "public constant";
    #[Deprecated]
    #[Attr]
    private const string PRIVATE_DEPRECATED_CONSTANT = "private constant";
    #[Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT = "protected constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT2 = "public constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT3 = "public constant", PUBLIC_DEPRECATED_CONSTANT4 = "public constant";
    #[Attr]
    #[\Deprecated]
    private const string PRIVATE_DEPRECATED_CONSTANT2 = "private constant";
    #[\Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT2 = "protected constant";
}

interface DeprecatedInterfaceConstant {
    public const string PUBLIC_CONSTANT = "public constant";
    #[Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT = "public constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT2 = "public constant";
}

trait DeprecatedTraitConstant {
    #[Attr]
    public const string PUBLIC_CONSTANT = "public constant";
    private const string PRIVATE_CONSTANT = "private constant";
    protected const string PROTECTED_CONSTANT = "protected constant";
    #[Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT = "public constant";
    #[Deprecated]
    #[Attr]
    private const string PRIVATE_DEPRECATED_CONSTANT = "private constant";
    #[Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT = "protected constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT2 = "public constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT3 = "public constant", PUBLIC_DEPRECATED_CONSTANT4 = "public constant";
    #[Attr]
    #[\Deprecated]
    private const string PRIVATE_DEPRECATED_CONSTANT2 = "private constant";
    #[\Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT2 = "protected constant";
}

enum DeprecatedEnumConstant {
    public const string PUBLIC_CONSTANT = "public constant";
    private const string PRIVATE_CONSTANT = "private constant";
    protected const string PROTECTED_CONSTANT = "protected constant";
    #[Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT = "public constant";
    #[Deprecated, Attr()]
    private const string PRIVATE_DEPRECATED_CONSTANT = "private constant";
    #[Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT = "protected constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT2 = "public constant";
    #[\Deprecated]
    private const string PRIVATE_DEPRECATED_CONSTANT2 = "private constant";
    #[\Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT2 = "protected constant";
}

$anon = new class() {
    public const string PUBLIC_CONSTANT = "public constant";
    private const string PRIVATE_CONSTANT = "private constant";
    protected const string PROTECTED_CONSTANT = "protected constant";
    #[Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT = "public constant";
    #[Deprecated]
    private const string PRIVATE_DEPRECATED_CONSTANT = "private constant";
    #[Deprecated]
    protected const string PROTECTED_DEPRECATED_CONSTANT = "protected constant";
    #[\Deprecated]
    public const string PUBLIC_DEPRECATED_CONSTANT2 = "public constant";
    #[\Attri, \Deprecated]
    private const string PRIVATE_DEPRECATED_CONSTANT2 = "private constant";
    #[\Deprecated(since: "1.5", message: "deprecated test")]
    protected const string PROTECTED_DEPRECATED_CONSTANT2 = "protected constant";
};
