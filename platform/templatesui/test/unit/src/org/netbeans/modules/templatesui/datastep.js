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

function assertDisplay(id, value, msg) {
    var e = document.getElementById(id);
    assertEquals(value, e.style.display, msg)
}

assertEquals(3, tck.steps(false).length, "There are three step directives");
assertEquals('init', tck.steps(false)[0], "First step id");
assertEquals('info', tck.steps(false)[1], "Second step id");
assertEquals('summary', tck.steps(false)[2], "3rd step id");

assertEquals(3, tck.steps(true).length, "There are three localized data-step headers");
assertEquals('Initial Page', tck.steps(true)[0], "First step has own display name");
assertEquals('info', tck.steps(true)[1], "Second display name is taken from id string");
assertEquals('summary', tck.steps(true)[2], "3rd display name fallbacks to id attribute");

assertEquals('init', tck.current(), "Current step is 1st one");
assertDisplay('s0', '', "Display characteristics of 1st panel not mangled");
assertDisplay('s1', 'none', "Invisible s1");
assertDisplay('s2', 'none', "Invisible s2");

tck.next();
assertEquals('info', tck.current(), "Moved to 2nd panel");
assertDisplay('s0', 'none', "Invisible s0");
assertDisplay('s1', '', "Display characteristics of 2nd panel not mangled");
assertDisplay('s2', 'none', "Invisible s2");

