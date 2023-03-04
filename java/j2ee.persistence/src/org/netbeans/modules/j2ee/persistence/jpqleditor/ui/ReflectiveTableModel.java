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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import static org.netbeans.modules.j2ee.persistence.jpqleditor.ui.Bundle.*;
import org.openide.util.NbBundle;

public class ReflectiveTableModel extends AbstractTableModel {

    private static final Logger LOG = Logger.getLogger(ReflectiveTableModel.class.getName());

    private final List<ReflectionInfo> reflectionInfo;

    private final List<Object> data;

    public ReflectiveTableModel(List<ReflectionInfo> reflectionInfo, List<Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null");  //NOI18N
        }
        this.reflectionInfo = reflectionInfo;
        this.data = data;
    }

    @Override
    @NbBundle.Messages({
        "LBL_QueryResultDefaultColumnName=Column",
        "#{0} - column index",
        "LBL_Index=[{0}]",
        "#{0} - column index",
        "#{1} - property name",
        "LBL_IndexProperty=[{0}].{1}"
    })
    public String getColumnName(int columnIndex) {
        ReflectionInfo ri = reflectionInfo.get(columnIndex);
        if (ri.getIndex() == null && ri.getPropertyName() == null) {
            return LBL_QueryResultDefaultColumnName();
        } else if (ri.getIndex() != null && ri.getPropertyName() == null) {
            return LBL_Index(ri.getIndex());
        } else if (ri.getIndex() == null && ri.getPropertyName() != null) {
            return ri.getPropertyName();
        } else {
            return LBL_IndexProperty(ri.getIndex(), ri.getPropertyName());
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return reflectionInfo.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object dataItem = data.get(rowIndex);
        if (dataItem == null) {
            return null;
        }
        ReflectionInfo ri = reflectionInfo.get(columnIndex);
        if (ri.getIndex() != null) {
            dataItem = ((Object[]) dataItem)[ri.getIndex()];
        }
        if (dataItem == null || ri.getPropertyName() == null) {
            return dataItem;
        }
        Class klass = dataItem.getClass();
        String property = ri.getPropertyName();
        String getterString = "get" // NOI18N
                + property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1);
        String isString = "is" // NOI18N
                + property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1);
        try {
            Method getter = klass.getMethod(getterString);
            return getter.invoke(dataItem);
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.WARNING, "Failed to reflect", ex); //NOI18N
            return null;
        }
        try {
            Method getter = klass.getMethod(isString);
            return getter.invoke(dataItem);
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.WARNING, "Failed to reflect", ex);  //NOI18N
            return null;
        }
        return null;
    }
}
