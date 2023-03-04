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
 * The annotated element might be <code>null</code> under certain
 * <i>defined</i> circumstances. The necessity of the non-null check depends
 * on situations described in the javadoc (so the non-null check is
 * not required by default).
 * <p>
 * Consider {@link java.util.Map#get(java.lang.Object)} as an example of
 * such a method - depending on usage of the {@link java.util.Map}
 * <code>null</code> may be legal or forbidden.
 *
 * @author Andrei Badea, Petr Hejl
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
@javax.annotation.Nonnull(when=javax.annotation.meta.When.UNKNOWN)
@javax.annotation.meta.TypeQualifierNickname
public @interface NullUnknown {

}
