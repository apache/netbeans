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
