/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
