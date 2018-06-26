/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug;

/**
 * An optional long property.
 * Use {@link #hasValue()} to test whether the property has the value or not.
 * 
 * @author Martin Entlicher
 */
public final class PropertyLong {
    
    private final Long l;
    
    /**
     * Create the long property.
     * @param l when <code>null</code>, then the property is undefined.
     */
    public PropertyLong(Long l) {
        this.l = l;
    }
    
    /**
     * Test whether the property has a value.
     * @return whether the property has a value.
     */
    public boolean hasValue() {
        return l != null;
    }
    
    /**
     * Get the property value. If the property does not have the value set,
     * it returns <code>0</code>.
     * @return the property value, or <code>0</code> when not set.
     */
    public long getValue() {
        if (l == null) {
            return 0;
        } else {
            return l;
        }
    }
    
    /**
     * Get the property value or the provided value when the property does not have one.
     * @param defaultValue The default value to return when the property is undefined.
     * @return 
     */
    public long getValueOr(long defaultValue) {
        if (l == null) {
            return defaultValue;
        } else {
            return l;
        }
    }
    
}
