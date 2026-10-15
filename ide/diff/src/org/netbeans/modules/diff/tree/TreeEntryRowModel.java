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

package org.netbeans.modules.diff.tree;

import org.netbeans.swing.outline.RowModel;

public class TreeEntryRowModel implements RowModel {

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueFor(Object node, int column) {
        TreeEntry te = (TreeEntry) node;
        switch(column) {
            case -1 -> {
                return te;
            }
            case 0 -> {
                if (!te.isFilesIdentical()) {
                    if (te.getFile1() == null) {
                        return "[+]";
                    } else if (te.getFile2() == null) {
                        return "[-]";
                    } else {
                        return "[M]";
                    }
                } else {
                    return "[ ]";
                }
            }
        }
        return null;
    }

    @Override
    public Class getColumnClass(int column) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    @Override
    public void setValueFor(Object node, int column, Object value) {
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0 -> {
                return "Modified";
            }
        }
        return null;
    }

}
