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
package org.openide.util.lookup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to simplify creation and add robustness to usage of
 * various named service registration annotations. For example
 * the {@code @}<a href="@org-openide-util@/org/openide/util/URLStreamHandlerRegistration.html">
 * URLStreamHandlerRegistration</a> annotation uses the {@link NamedServiceDefinition}
 * as: <pre>
 * {@code @NamedServiceDefinition(path="URLStreamHandler/@protocol()", serviceType=URLStreamHandler.class)}
 * </pre>
 * The above instructs the annotation processor that handles {@link NamedServiceDefinition}s
 * to verify the annotated type is subclass of <code>URLStreamHandler</code> and
 * if so, register it into <code>URLStreamHandler/@protocol</code> where the
 * value of <code>@protocol()</code> is replaced by the value of annotation's 
 * <a href="@org-openide-util@/org/openide/util/URLStreamHandlerRegistration.html#protocol()">
 * protocol attribute</a>. The registration can later be found by using
 * {@link Lookups#forPath(java.lang.String) Lookups.forPath("URLStreamHandler/ftp")}
 * (in case the protocol was ftp).
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 8.14
 * @see ServiceProvider#path() 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface NamedServiceDefinition {
    /** Type, or array of types that the registered type
     * has to implement. The annotated type needs to register at least
     * one of the enumerated classes.
     * @return set of type
     */
    public Class<?>[] serviceType();
    /** Path to register the annotation to, so it can later be found by
     * using {@link Lookups#forPath(java.lang.String) Lookups.forPath(theSamePath)}.
     * The path may reference attributes of the annotated annotation by prefixing
     * them with {@code @}. To reuse attribute named <code>location</code> one
     * can for example use <code>"how/to/get/to/@location()s/please"</code>
     * These attributes must be of type <code>String</code>
     * or array of <code>String</code>s (then one registration is performed
     * per each string in the array).
     * @return path to register annotation
     */
    public String path();
    /** Name of attribute that specifies position. By default the system tries
     * to find <code>int position()</code> attribute in the defined annotation
     * and use it to specify the order of registrations. In case a different
     * attribute should be used to specify the position, one can be provide its
     * name by specifying non-default here. Should the position be ignored,
     * specify empty string.
     * 
     * name of attribute in the annotated annotation to use for defining
     * position of the registration. The attribute should return int value.
     * @return position
     */
    public String position() default "-";
}
