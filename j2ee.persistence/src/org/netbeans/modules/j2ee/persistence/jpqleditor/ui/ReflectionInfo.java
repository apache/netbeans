/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.jpqleditor.ui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class ReflectionInfo implements Comparable<ReflectionInfo> {

    private final Integer index;

    private final String propertyName;

    private ReflectionInfo(Integer index, String propertyName) {
        this.index = index;
        this.propertyName = propertyName;
    }

    public Integer getIndex() {
        return index;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return "{" + index + ", " + propertyName + '}';
    }

    @Override
    public int compareTo(ReflectionInfo o) {
        if (index != o.index) {
            if (index == null) {
                return -1;
            }
            if (o.index == null) {
                return 1;
            }
            int result = index.compareTo(o.index);
            if (result != 0) {
                return result;
            }
        }

        if (propertyName != o.propertyName) {
            if (propertyName == null) {
                return -1;
            }
            if (o.propertyName == null) {
                return 1;
            }
            return propertyName.compareTo(o.propertyName);
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.index);
        hash = 79 * hash + Objects.hashCode(this.propertyName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReflectionInfo other = (ReflectionInfo) obj;
        if (!Objects.equals(this.propertyName, other.propertyName)) {
            return false;
        }
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        return true;
    }

    public static List<ReflectionInfo> prepare(List<? extends Object> data) throws IntrospectionException {
        // Array mode is followed, if each data item is an array and has same dimension
        Integer rowLength = null;
        for (Object row : data) {
            if (row instanceof Object[]) {
                if (rowLength != null) {
                    // Arraylength differs
                    if (rowLength != ((Object[]) row).length) {
                        rowLength = null;
                        break;
                    }
                } else {
                    // Initial row length
                    rowLength = ((Object[]) row).length;
                }
            } else {
                // Non array
                rowLength = null;
                break;
            }
        }

        TreeSet<ReflectionInfo> resultPrecursor = new TreeSet<>();
        for (Object row : data) {
            if (rowLength != null) {
                Object[] rowArray = (Object[]) row;
                for (int i = 0; i < rowLength; i++) {
                    resultPrecursor.addAll(fromObject(i, rowArray[i]));
                }
            } else {
                resultPrecursor.addAll(fromObject(null, row));
            }

        }

        return new ArrayList<>(resultPrecursor);
    }

    private static List<ReflectionInfo> fromObject(Integer index, Object obj) throws IntrospectionException {
        if (obj == null || obj.getClass().getName().startsWith("java.lang") // NOI18N
                || obj.getClass().getName().startsWith("java.math")) { // NOI18N
            // Let the default handle this
            return Collections.singletonList(new ReflectionInfo(index, null));
        } else {
            BeanInfo bi = Introspector.getBeanInfo(obj.getClass(), Object.class);
            List<ReflectionInfo> result = new ArrayList<>();
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                result.add(new ReflectionInfo(index, pd.getName()));
            }
            if (result.isEmpty()) {
                return Collections.singletonList(new ReflectionInfo(index, null));
            } else {
                return result;
            }
        }
    }
}
