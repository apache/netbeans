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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.editor.util.random;


/**
 * Provider of properties extended by {@link RandomTestContainer} and some of its subclasses.
 *
 * @author Miloslav Metelka
 */
public abstract class PropertyProvider {

    /**
     * Get value of property or return null.
     * @param key key.
     * @return value or null.
     */
    public abstract Object getPropertyOrNull(Object key);

    /**
     * Put new value of a property or instance value.
     * @param key key (or a class for instance values).
     * @param value new value.
     */
    public abstract void putProperty(Object key, Object value);

    /**
     * Get non-null value of property.
     * @param key key.
     * @return non-null value.
     * @throws IllegalStateException in case the property is null.
     */
    public final Object getProperty(Object key) {
        Object value = getPropertyOrNull(key);
        if (value == null) {
            throw new IllegalStateException("No value for property " + key); // NOI18N
        }
        return value;
    }

    /**
     * Get value of property or a default value.
     * @param key key.
     * @return value or default value if it would be null.
     */
    public final <V> V getProperty(Object key, V defaultValue) {
        @SuppressWarnings("unchecked")
        V value = (V) getPropertyOrNull(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Get instance value of property or null.
     * @param cls class which acts as key to {@link #getPropertyOrNull(Object)}.
     * @return instance value or null.
     */
    public final <C> C getInstanceOrNull(Class<C> cls) {
        @SuppressWarnings("unchecked")
        C instance = (C) getPropertyOrNull(cls);
        return instance;
    }

    /**
     * Get non-null instance value of property.
     * @param cls class which acts as key to {@link #getPropertyOrNull(Object)}.
     * @return non-null value.
     * @throws IllegalStateException in case the property is null.
     */
    public final <C> C getInstance(Class<C> cls) {
        C instance = getInstanceOrNull(cls);
        if (instance == null) {
            throw new IllegalStateException("No value for instance of class " + cls); // NOI18N
        }
        return instance;
    }

    public final boolean isLogOp() {
        return (Boolean.TRUE.equals(getPropertyOrNull(RandomTestContainer.LOG_OP)));
    }

}
