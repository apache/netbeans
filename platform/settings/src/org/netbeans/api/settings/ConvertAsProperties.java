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
import java.util.Properties;

/** Annotation to attach to object that is wishing to support conversion from
 * and to {@link Properties}. More info about the format and protocol
 * <a href="../../spi/settings/package-summary.html">is available</a> in separate document,
 * here is the shortest possible howto:
 * <pre>
 * <code>@</code>ConvertAsProperties(dtd="-//Your Org//Your Setting//EN")
 * <font class="type">public class</font> YourObject {
 *   <font class="type">public</font> YourObject() {} // public constructor is a must
 *   <font class="type">void</font> <font class="function-name">readProperties</font>(java.util.<font class="type">Properties</font> <font class="variable-name">p</font>) {
 *     // do the read
 *   }
 *   <font class="type">void</font> <font class="function-name">writeProperties</font>(java.util.<font class="type">Properties</font> <font class="variable-name">p</font>) {
 *     // handle the store
 *   }
 * }
 * </pre>
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 1.18
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ConvertAsProperties {
    /** constant to return from {@link #ignoreChanges()} to signal that all
     * property changes shall be ignored.
     */
    public static final String IGNORE_ALL_CHANGES = "all";

    /** Public ID of the XML file that results in creation of the
     * annotated type and to which the annotated type can be converted.
     * @return public ID of the file's DTD
     */
    String dtd();
    /** Shall every change in the object result in save? Or shall the
     * object just be marked as dirty?
     */
    boolean autostore() default true;
    /** An array of properties that are ignored without marking the object
     * as dirty or saving it.
     * @return array of property names or {@link #IGNORE_ALL_CHANGES} to ignore all properties
     */
    String[] ignoreChanges() default {};
}
