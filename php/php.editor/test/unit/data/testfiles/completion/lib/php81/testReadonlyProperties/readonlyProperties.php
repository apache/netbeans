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
class ReadonlyProperties {

    // readonly properties
    public readonly int $publicReadonly;
    private readonly ?string $privateReadonly;
    protected readonly string|int $protectedReadonly;

    readonly public string $readonlyPublic;
    readonly private ?string $readonlyPrivate;
    readonly protected int|string $readonlyProtected;

    // promoted readonly properties
    public function __construct(
        public readonly int|string $promotedPublicReadonly = 0,
        private readonly array $promotedPrivateReadonly = [],
        protected readonly ?string $promotedProtectedReadonly = "test",
        readonly public int|string $promotedReadonlyPublic = 0,
        readonly private array $promotedReadonlyPrivate = [],
        readonly protected ?string $promotedReadonlyProtected = "test",
    ) {}
}
