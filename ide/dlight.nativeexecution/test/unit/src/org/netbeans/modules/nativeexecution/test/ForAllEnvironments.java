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
 * Annotate a test method with this annotation in the case you want it to be invoked
 * for each execution environment specified in .cndtestrc
 *
 * To put it more precise:  
 * 
 * In the case your method is annotated with this annotation,
 * NativeExecutionBaseTestSuite.addTest will create an instance of your class
 * (and run the test method) for each environment specified in given section.
 *
 * The constructor with (String, ExecutionEnvironment)
 * signature will be invoked in this case.
 *
 * (Note that such test method should be public, have void return type and no parameters)
 *
 * In the case test method it is not annotated, 
 * constructor with a single String parameter (test name) will be used,
 * only one instance of the class per test method will be created,
 * and test method will be run only once.
 *
 * All the above is true in the case you use NativeExecutionBaseTestSuite.addTest()
 * method for adding tests.
 * Otherwise the annotation is just ignored.
 *
 * @author Vladimir Kvashin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForAllEnvironments {
    /**
     * In the case section is empty,
     * default is set via suite constructor
     */
    String section() default "";
}
