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

package org.netbeans.spi.sendopts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Adds human readable description to {@code @}{@link Arg} annotation.
 * Can be attached to public non-static fields that are also annotated 
 * by {@code @}{@link Arg}.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 2.20
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    /** 
     * Specific display name for the option. If not specified, it is inferred
     * from the name of the option and the type of its parameters. Specify 
     * with using of {@link org.openide.util.NbBundle.Messages}. 
     * @return specific display name
     */
    String displayName() default "";
    /** Explanation of the purpose of the option.
     * Specify by using {@link org.openide.util.NbBundle.Messages}. 
     * @return explanation of the purpose of the option
     */
    String shortDescription();
}
