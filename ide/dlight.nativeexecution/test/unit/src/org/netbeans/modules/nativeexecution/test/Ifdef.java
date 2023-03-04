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

package org.netbeans.modules.nativeexecution.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows switching tests on and off via .cndtestrc
 * If the given key in the given section exists,
 * then test method that is annotated with this annotation will be run,
 * otherwise it won't.
 *
 * All the above is true in the case you use NativeExecutionBaseTestSuite.addTest()
 * method for adding tests.
 * Otherwise the annotation is just ignored.
 *
 * @author Vladimir Kvashin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ifdef {
    /** Gets section of .cndtestrc that contains the given flag */
    String section();
    /** Gets flag key */
    String key();
}
