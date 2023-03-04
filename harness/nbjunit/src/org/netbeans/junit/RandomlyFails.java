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

package org.netbeans.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this test can fail randomly.
 * <p>When used on a method in a class extending {@link NbTestCase},
 * the implicit suite will exclude this test in case the system property
 * <code>ignore.random.failures</code> is set to <code>true</code>.
 * When used on a class passed to {@link NbTestSuite#NbTestSuite(Class)} or {@link NbTestSuite#addTestSuite(Class)},
 * the suite will be empty if the system property is set;
 * the same if it is used on a class extending {@link NbTestCase}.
 * <p>Test runs which must be reliable should define the system property.
 * (E.g. for NetBeans modules: <code>ant -Dtest-unit-sys-prop.ignore.random.failures=true test</code>)
 * Developers running tests interactively should not.
 * 
 * JUnit {@literal @}Ignore annotation is supported.
 * {@literal @}Ignore annotation is recommended to mark tests that are broken instead this annotation.
 * 
 * @since 1.51
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface RandomlyFails {}
