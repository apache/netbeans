/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 8.14
 * @see ServiceProvider#path() 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface NamedServiceDefinition {
    /** Type, or array of types that the registered type
     * has to implement. The annotated type needs to register at least
     * one of the enumerated classes.
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
     */
    public String path();
    /** Name of attribute that specifies position. By default the system tries
     * to find <code>int position()</code> attribute in the defined annotation
     * and use it to specify the order of registrations. In case a different
     * attribute should be used to specify the position, one can be provide its
     * name by specifying non-default here. Should the position be ignored,
     * specify empty string.
     * 
     * @param name of attribute in the annotated annotation to use for defining
     *   position of the registration. The attribute should return int value.
     */
    public String position() default "-";
}
