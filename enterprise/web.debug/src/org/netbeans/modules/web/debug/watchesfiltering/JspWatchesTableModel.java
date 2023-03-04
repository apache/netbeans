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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.debugger.ui.Constants;

/**
 * Table model for JSP EL watches.
 *
 * @author Maros Sandor
 */
public class JspWatchesTableModel implements TableModel {

    public Object getValueAt (Object row, String columnID) throws UnknownTypeException {
        if (!(row instanceof JspElWatch)) throw new UnknownTypeException(row);
        JspElWatch watch = (JspElWatch) row;
        if (columnID.equals(Constants.WATCH_TO_STRING_COLUMN_ID)) {
            return watch.getValue();
        } else if (columnID.equals (Constants.WATCH_TYPE_COLUMN_ID)) {
            return watch.getType();
        } else if (columnID.equals (Constants.WATCH_VALUE_COLUMN_ID)) {
            String e = watch.getExceptionDescription ();
            if (e != null) return "> " + e + " <";
            return watch.getValue();
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws UnknownTypeException {
        if (!(row instanceof JspElWatch)) throw new UnknownTypeException(row);
        return true;
    }
    
    public void setValueAt (Object row, String columnID, Object value) throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}
