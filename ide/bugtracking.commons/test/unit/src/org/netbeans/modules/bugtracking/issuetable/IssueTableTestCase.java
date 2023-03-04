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

package org.netbeans.modules.bugtracking.issuetable;

import java.lang.reflect.Field;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.api.Query;

/**
 *
 * @author tomas
 */
public class IssueTableTestCase extends NbTestCase {
    public IssueTableTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testColumnsCount() throws Throwable {
        IssuetableTestFactory factory = IssuetableTestFactory.getInstance(this);
        final Query q = factory.createQuery();
        assertEquals(0,q.getIssues().size());

        final NodeTableModel model = getModel(q);       
        assertEquals(factory.getColumnsCountBeforeSave(), model.getColumnCount());
        final int[] columnCount = new int[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                IssuetableTestFactory.getInstance(IssueTableTestCase.this).setSaved(q);
                IssuetableTestFactory.getInstance(IssueTableTestCase.this).getTable(q).initColumns();                
            }
        });
        // awt things happening in .setSaved and .initColumns so wait until done
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                columnCount[0] = model.getColumnCount();
            }
        });
        assertEquals(factory.getColumnsCountAfterSave(), columnCount[0]);
    }

    private NodeTableModel getModel(Query q) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        IssueTable it = IssuetableTestFactory.getInstance(this).getTable(q);
        Field f = it.getClass().getDeclaredField("tableModel");
        f.setAccessible(true);
        return (NodeTableModel) f.get(it);
    }

}
