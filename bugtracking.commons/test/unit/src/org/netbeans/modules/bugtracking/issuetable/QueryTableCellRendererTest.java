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

package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import javax.swing.JTable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.TestIssueProvider;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.TestStatusProvider;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider.Status;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class QueryTableCellRendererTest {

    public QueryTableCellRendererTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetCellStyle() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        JTable table = new JTable();
        RendererRepository rendererRepository = new RendererRepository();
        RendererQuery rendererQuery = new RendererQuery(rendererRepository);

        MessageFormat issueNewFormat       = getFormat("issueNewFormat", UIUtils.getTaskNewColor());           // NOI18N
        MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat", UIUtils.getTaskModifiedColor()); // NOI18N

        Color newHighlightColor            = new Color(0x00b400);
        Color modifiedHighlightColor       = new Color(0x0000ff);
        
        RepositoryImpl repository = TestKit.getRepository(rendererRepository);
        QueryImpl query = TestKit.getQuery(repository, rendererQuery);
        
        IssueTable issueTable = new IssueTable(
                repository.getId(),
                rendererQuery.getDisplayName(), 
                rendererQuery.getController(),
                new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")}, 
                rendererQuery.isSaved());

        
        // issue seen, not selected
        RendererIssue rendererIssue = new RendererIssue(rendererRepository, "");
        IssueProperty property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        rendererQuery.containsIssue = true;
        boolean selected = false;
        setIssueValues(rendererRepository, rendererIssue, Status.SEEN, true);
        TableCellStyle defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        TableCellStyle result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // issue seen, selected
        rendererQuery.containsIssue = true;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        selected = true;
        setIssueValues(rendererRepository, rendererIssue, Status.SEEN, true);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // modified issue, not selected
        rendererQuery.containsIssue = true;
        selected = false;
        rendererIssue = new RendererIssue(rendererRepository, "changed");
        property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        setIssueValues(rendererRepository, rendererIssue, Status.INCOMING_MODIFIED, false);
        result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueModifiedFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000ff\">Modified</font> - this task is modified - changed</html>", result.getTooltip());


        // modified issue, selected
        rendererQuery.containsIssue = true;
        selected = true;
        rendererIssue = new RendererIssue(rendererRepository, "changed");
        property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        setIssueValues(rendererRepository, rendererIssue, Status.INCOMING_MODIFIED, false);
        result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(modifiedHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000ff\">Modified</font> - this task is modified - changed</html>", result.getTooltip());

        // new issue, not selected
        rendererQuery.containsIssue = true;
        selected = false;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        setIssueValues(rendererRepository, rendererIssue, Status.INCOMING_NEW, false);
        result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueNewFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font> - this task is new</html>", result.getTooltip());


        // new issue, selected
        rendererQuery.containsIssue = true;
        selected = true;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository).createProperty();
        setIssueValues(rendererRepository, rendererIssue, Status.INCOMING_NEW, false);
        result = QueryTableCellRenderer.getCellStyle(table, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(newHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font> - this task is new</html>", result.getTooltip());

    }


    /**
     * Test of getDefaultCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetDefaultCellStyle() {
        JTable table = new JTable();
        RendererRepository rendererRepository = new RendererRepository();
        RendererIssue issue = new RendererIssue(rendererRepository, "");
        RendererQuery query = new RendererQuery(rendererRepository);
        IssueProperty property = new RendererNode(issue, "some value", rendererRepository).createProperty();

        IssueTable issueTable = new IssueTable(
                TestKit.getRepository(rendererRepository).getId(),
                query.getDisplayName(), 
                query.getController(),
                new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")}, 
                query.isSaved());
        
        TableCellStyle result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, true, 0);
        assertEquals(table.getSelectionBackground(), result.getBackground()); // keep table selection colors
        assertEquals(Color.WHITE, result.getForeground());
        assertNull(result.getFormat());
        assertNull(result.getTooltip());

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 0);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color unevenBackground = result.getBackground();

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 1);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color evenBackground = result.getBackground();

        assertNotSame(evenBackground, unevenBackground);
        assertTrue(evenBackground.equals(Color.WHITE) || unevenBackground.equals(Color.WHITE));
        assertTrue(evenBackground.equals(new Color(0xf3f6fd)) || unevenBackground.equals(new Color(0xf3f6fd)));
    }

    private static MessageFormat getFormat (String key, Color c) {
        String format = NbBundle.getMessage(IssueTable.class, key,
                new Object[] { UIUtils.getColorString(c), "{0}" }); //NOI18N
        return new MessageFormat(format);
    }

    private class RendererQuery extends TestQuery {
        private boolean containsIssue;
        private RendererRepository repository;

        public RendererQuery(RendererRepository repository) {
            this.repository = repository;
        }

        @Override
        public boolean isSaved() {            
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Renderer Query";
        }

        @Override
        public String getTooltip() {
            return "Renderer Query";
        }

        public TestRepository getRepository() {
            return repository;
        }

        public boolean contains(String id) {
            return containsIssue;
        }
    }

    private class TestChangesProvider implements IssueNode.ChangesProvider<TestIssue>  {
        @Override
        public String getRecentChanges(TestIssue issue) {
            return ((RendererIssue) issue).getRecentChanges();
        }
    }
    
    private class RendererNode<TestIssue> extends IssueNode {

        Object propertyValue;
        public RendererNode(RendererIssue issue, String value, RendererRepository rendererRepository) {
            super("testconnector", issue.repo.info.getID(), issue, new TestIssueProvider(), new TestStatusProvider(), new TestChangesProvider());
            propertyValue = value;
        }
        RendererIssueProperty createProperty() {
            return new RendererIssueProperty(null, null, null, null, this);
        }
        @Override
        protected Property<?>[] getProperties() {
            return new Property[0];
        }
        class RendererIssueProperty extends IssueProperty {
            public RendererIssueProperty(String arg0, Class name, String type, String displayName, Object value) {
                super(arg0, name, type, displayName);
            }
            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return propertyValue;
            }
        }
    }

    private static class RendererIssue extends TestIssue {
        private static int id = 0;
        private String recentChanges;
        private final RendererRepository repo;
        private IssueStatusProvider.Status status;
        private IssueStatusProvider.Status prevStatus;
        public RendererIssue(RendererRepository repo, String recentChanges) {
            id++;
            this.recentChanges = recentChanges;
            this.repo = repo;
        }

        @Override
        public String getDisplayName() {
            return "Renderer Issue";
        }

        @Override
        public String getTooltip() {
            return "Renderer Issue";
        }

        @Override
        public String getID() {
            return id + "";
        }

        public String getRecentChanges() {
            return recentChanges;
        }
        
        public void setStatus(IssueStatusProvider.Status status) {
            this.status = status;
        }
        
        @Override
        public IssueStatusProvider.Status getStatus() {
            return status;
        }

        @Override
        public void setSeen(boolean seen) {
            if(seen) {
                prevStatus = status;
                status = Status.SEEN;
            } else if(prevStatus != null) {
                status = prevStatus;
            }
        }
    }

    private class RendererRepository extends TestRepository {
        private final RepositoryInfo info;
        public RendererRepository() {
            info = new RepositoryInfo("testrepo", "testconnector", null, null, null, null, null, null, null);
        }
        
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    };

    private void setIssueValues(RendererRepository repository, RendererIssue rendererIssue, Status status, boolean seen) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        rendererIssue.setStatus(status);
        rendererIssue.setSeen(seen);
    }
    
}
