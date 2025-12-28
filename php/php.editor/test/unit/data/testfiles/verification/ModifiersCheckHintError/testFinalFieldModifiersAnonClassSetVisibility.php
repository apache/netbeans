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

$anon = new class() {
    // final, set visibility
    final public(set) string $finalPublicSet = "final set visibility";
    public(set) final string $finalPublicSet2 = "final set visibility";

    //  PHP Fatal error:  Property with asymmetric visibility FinalFiedsTrait::$finalPublicSet3 must have type
    final public(set) $finalPublicSet3 = "final set visibility"; // error1

    final private(set) string $finalPrivateSet = "final set visibility";
    private(set) final string $finalPrivateSet2 = "final set visibility";
    final protected(set) string $finalProtectedSet = "final set visibility";
    protected(set) final string $finalProtectedSet2 = "final set visibility";
};
