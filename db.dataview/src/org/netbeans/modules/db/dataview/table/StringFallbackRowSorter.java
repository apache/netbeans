/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Comparator;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.sort.TableSortController;
import org.netbeans.modules.db.dataview.util.LobHelper;

/**
 * RowSorter that falls back to comparing values by their string representation
 * if normal comparison fails.
 *
 * The Sorter is necessary to prevent exceptions when columns in
 * ResultSetJXTable are sorted and different types are present (for example
 * string and date)
 */
public class StringFallbackRowSorter extends TableSortController<TableModel> {
    public StringFallbackRowSorter(TableModel model) {
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