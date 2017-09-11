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

package org.netbeans.modules.bugzilla.query;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.IssuetableTestFactory;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class IssueTableTest extends IssuetableTestFactory implements QueryConstants, TestConstants {

    private Map<String, BugzillaQuery> queries = new HashMap<>();
    
    public IssueTableTest(Test test) {
        super(test);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(org.netbeans.modules.bugtracking.issuetable.IssueTableTestCase.class);
        return new IssueTableTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", System.getProperty("java.io.tmpdir"));
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.delay", "please!");
        
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    @Override
    public Query createQuery() {
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        String queryName = QUERY_NAME + System.currentTimeMillis();
        final BugzillaQuery bugzillaQuery = new BugzillaQuery(queryName, repo, p, false, false, true); // false = not saved
        assertEquals(0,bugzillaQuery.getIssues().size());
        Query query = TestUtil.getQuery(bugzillaQuery);
        queries.put(queryName, bugzillaQuery);
        return query;
    }
    
    @Override
    public void setSaved(Query q) {
        BugzillaQuery bugzillaQuery = queries.get(q.getDisplayName());
        bugzillaQuery.getController().save(q.getDisplayName());
    }
    
    @Override
    public IssueTable getTable(Query q) {
        try {
            BugzillaQuery bugzillaQuery = queries.get(q.getDisplayName());
            QueryController c = bugzillaQuery.getController();
            return c.getIssueTable();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getColumnsCountBeforeSave() {
        return 7;
    }

    @Override
    public int getColumnsCountAfterSave() {
        return 9;
    }



}
