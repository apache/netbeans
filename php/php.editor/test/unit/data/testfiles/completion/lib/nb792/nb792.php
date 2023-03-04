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

function something1($arg):a {}

function something2($arg = ''):a {}

function something3($arg = "test"):a {}

function something4($arg = array("test")):a {}

function something5($arg = ["test"]):a {}

function something6($arg = 10):a {}

function something7($arg = -10):a {}

function something8($arg = -1.0):a {}

function something9($arg = true):a {}

function something10($arg = false):a {}

function something11($arg = null):a {}
