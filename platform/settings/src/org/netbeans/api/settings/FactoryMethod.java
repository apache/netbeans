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
package org.netbeans.api.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.settings.Convertor;

/** Specifies an alternative factory method to use (rather than constructor)
 * to create the instance. Use in orchestration with 
 * {@link ConvertAsProperties} or on any class that is processed by serializing
 * {@link Convertor}s.
 * 
 * @since 1.40
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryMethod {
    /**
     * Name of factory method to use instead of default constructor. Sometimes, for
     * example when dealing with singletons, it may be desirable to control how
     * an instance of given class is created. In such case one can create a
     * factory method (takes no arguments and returns instance of desired type)
     * in the class annotated by {@link ConvertAsProperties} annotation.
     */
    String value();
}
