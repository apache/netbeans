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

assertEquals(3, tck.steps(false).length, "There are three values in step property");
assertEquals('init', tck.steps(false)[0], "First data-step");
assertEquals('info', tck.steps(false)[1], "Second data-step");
assertEquals('summary', tck.steps(false)[2], "3rd data-step");

assertEquals(3, tck.steps(true).length, "There are three localized data-step headers");
assertEquals('Initial Page', tck.steps(true)[0], "First data-step display name");
assertEquals('info', tck.steps(true)[1], "taken as string value");
assertEquals('summary', tck.steps(true)[2], "fallback to id attribute");

assertEquals('init', tck.current(), "Current step is 1st one");
tck.next();
assertEquals('info', tck.current(), "Moved to 2nd panel");

tck.next();
assertEquals('info', tck.current(), "Remains on second panel");

tck.data().message('Some msg');
tck.next();
assertEquals('summary', tck.current(), "Moved to 3rd panel");
