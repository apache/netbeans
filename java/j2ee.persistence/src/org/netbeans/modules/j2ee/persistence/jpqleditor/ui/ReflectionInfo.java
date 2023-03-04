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
