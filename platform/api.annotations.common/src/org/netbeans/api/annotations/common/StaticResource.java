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

package org.netbeans.api.annotations.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for a constant representing a static resource.
 * The annotated field must be a compile-time {@code String} constant whose value denotes a resource path.
 * For example, the resource might be an icon path intended for {@code ImageUtilities.loadImage}.
 * The primary purpose of the annotation is for its processor, which will signal a compile-time error
 * if the resource does not in fact exist - ensuring that at least this usage will not be accidentally
 * broken by moving, renaming, or deleting the resource.
 * @since 1.13
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface StaticResource {

    /**
     * If true, permit the resource to be in the classpath.
     * By default, it may only be in the sourcepath.
     * @return true to search in classpath
     */
    boolean searchClasspath() default false;

    /**
     * If true, consider the resource path to be relative to the current package.
     * ({@code ../} sequences are permitted.)
     * By default, it must be an absolute path (not starting with {@code /}).
     * @return true to consider resource path to be relative to current package
     */
    boolean relative() default false;

}
