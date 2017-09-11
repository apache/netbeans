/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.cookies;

import java.io.IOException;
import org.openide.nodes.Node;

/**
 * Cookie that should be provided by all nodes that are able
 * to create or return an "instance".
 * Generally this is used to register objects declaratively in XML layers.
 * The most commonly used implementation seems to be
 * <a href="@org-openide-loaders@/org/openide/loaders/InstanceDataObject.html">InstanceDataObject</a>.
 *
 * @author Jaroslav Tulach
 */
public interface InstanceCookie/*<T>*/ extends Node.Cookie {

    /**
     * The name of {@link #instanceClass}.
     * Should be the same as <code>instanceClass().getName()</code>
     * but may be able to avoid actually loading the class.
     * <p><strong>Generally this method should not be used.</strong>
     * @return the instance class name
     */
    public String instanceName();

    /**
     * The type that the instance is expected to be assignable to.
     * Can be used to test whether the instance is of an appropriate
     * class without actually creating it.
     * <p><strong>Generally this method should not be used.</strong>
     * To test whether the instance will be assignable to some type,
     * use {@link InstanceCookie.Of#instanceOf} instead.
     * To actually load instances, use {@link #instanceCreate}; if your
     * objects are not naturally singletons (e.g. public no-argument constructor),
     * the instances should rather be of some kind of <em>factory</em> you define.
     *
     * @return the type (or perhaps some interesting supertype) of the instance
     * @exception IOException if metadata about the instance could not be read, etc.
     * @exception ClassNotFoundException if the instance type could not be loaded
     */
    public Class<?/*T*/> instanceClass() throws IOException, ClassNotFoundException;

    /**
     * Create or obtain an instance. For example 
     * <a href="@org-openide-loaders@/org/openide/loaders/InstanceDataObject.html#instanceCreate()">InstanceDataObject</a>
     * (one of the most often used implementations of {@link InstanceCookie}) caches
     * previously returned instances.
     * 
     * @return an object assignable to {@link #instanceClass}
     * @throws IOException for the same reasons as {@link #instanceClass}, or an object could not be deserialized, etc.
     * @throws ClassNotFoundException for the same reasons as {@link #instanceClass}
    */
    public Object/*T*/ instanceCreate() throws IOException, ClassNotFoundException;

    /**
     * Enhanced cookie that can answer queries about the type of the
     * instance it creates. It does not add any additional ability except to
     * improve performance, because it is not necessary to load
     * the actual class of the object into memory.
     *
     * @since 1.4
     */
    public interface Of extends InstanceCookie {
        /**
         * Checks if the object created by this cookie is an
         * instance of the given type. The same as
         * <code>type.isAssignableFrom(instanceClass())</code>
         * But this can prevent the actual class from being
         * loaded into the Java VM.
         *
         * @param type the class type we want to check
         * @return true if this cookie will produce an instance of the given type
        */
        public boolean instanceOf(Class<?> type);
    }

}
