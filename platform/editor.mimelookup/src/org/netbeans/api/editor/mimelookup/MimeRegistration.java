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
package org.netbeans.api.editor.mimelookup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**Register an implementation of a service to the mime lookup under given mime-type.
 * This annotation can be used either to annotate implementation class, in which case
 * it must have a public non-arg constructor, or a public static factory method returning
 * the service.
 *
 * @author Jan Lahoda
 * @since 1.19
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MimeRegistration {

    /**
     * The API service.
     */
    public Class<?> service();
    
    /**
     * Mime type to which should be the given provider registered.
     */
    public String mimeType();

    /**
     * Position of the provider in the list of providers.
     */
    public int position() default Integer.MAX_VALUE;

}
