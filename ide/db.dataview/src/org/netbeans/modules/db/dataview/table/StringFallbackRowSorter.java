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
package org.netbeans.modules.db.dataview.table;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Comparator;
import javax.swing.table.TableRowSorter;
import org.netbeans.modules.db.dataview.util.LobHelper;

/**
 * RowSorter that falls back to comparing values by their string representation
 * if normal comparison fails.
 *
 * The Sorter is necessary to prevent exceptions when columns in
 * ResultSetJXTable are sorted and different types are present (for example
 * string and date)
 */
public class StringFallbackRowSorter extends TableRowSorter<ResultSetTableModel> {
    public StringFallbackRowSorter(ResultSetTableModel model) {
        super(model);
    }

    @Override
    public Comparator<?> getComparator(int column) {
        Comparator superComparator = super.getComparator(0);
        Class klass = getModel().getColumnClass(column);
        if (Blob.class.isAssignableFrom(klass)) {
            superComparator = LobHelper.getBlobComparator();
        } else if (Clob.class.isAssignableFrom(klass)) {
            superComparator = LobHelper.getClobComparator();
        }
        return new StringFallBackComparator(superComparator);
}

    @Override
    protected boolean useToString(int column) {
        Class klass = getModel().getColumnClass(column);
        if (Blob.class.isAssignableFrom(klass)
                || Clob.class.isAssignableFrom(klass)) {
            return false;
        }
        return super.useToString(column);
    }
}

class StringFallBackComparator implements Comparator<Object> {

    private Comparator<?> delegate;

    public StringFallBackComparator(Comparator<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object t, Object t1) {
        try {
            return ((Comparator<Object>) delegate).compare(t, t1);
        } catch (Exception ex) {
            String s1 = t != null ? t.toString() : "";                  //NOI18N
            String s2 = t1 != null ? t1.toString() : "";                //NOI18N
            return s1.compareTo(s2);
        }
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }
}
