/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.bugtracking.issuetable;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.openide.nodes.*;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * The node that is rendered in the IssuesTable. It gets values to display from an
 * Issue which serves as the 'data' 'visual' node.
 * 
 * @author Tomas Stupka
 * @param <I>
 */
public abstract class IssueNode<I> extends AbstractNode {

    /**
     * Seen property id
     */
    public static final String LABEL_NAME_SEEN = "issue.seen";                        // NOI18N
    /**
     * Recetn Changes property id
     */
    public static final String LABEL_RECENT_CHANGES = "issue.recent_changes";         // NOI18N

    public static final String LABEL_NAME_SUMMARY          = "issue.summary";     // NOI18N
    private final String repositoryID;
    private final String connectorID;
    private Repository repository;

    public interface ChangesProvider<I> {
        public String getRecentChanges(I i);
    }
        
    private IssueImpl issueImpl;

    private String htmlDisplayName;
    private Action preferedAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Repository repo = getRepository();
            Util.openIssue(repo, issueImpl.getID());
        }
    };
    private final ChangesProvider<I> changesProvider;

    /**
     * Creates a {@link IssueNode}
     * 
     * @param connectorID
     * @param repositoryID
     * @param i
     * @param issueProvider
     * @param statusProvider
     * @param changesProvider
     */
    public IssueNode(String connectorID, String repositoryID, I i, IssueProvider<I> issueProvider, IssueStatusProvider<?, I> statusProvider, ChangesProvider<I> changesProvider) {
        super(Children.LEAF);
        this.issueImpl = new IssueImpl(i, issueProvider, statusProvider);
        this.repositoryID = repositoryID;
        this.connectorID = connectorID;
        this.changesProvider = changesProvider;
        initProperties();
        refreshHtmlDisplayName();
        issueImpl.addPropertyChangeListener(WeakListeners.propertyChange(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(IssueNode.this.issueImpl.i != evt.getSource()) {
                    return;
                }
                if(evt.getPropertyName().equals(IssueStatusProvider.EVENT_STATUS_CHANGED)) {
                    fireSeenValueChanged();
                } else if(IssueProvider.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                    fireDataChanged();
                }
            }
        }, this));
    }
    
    public I getIssueData() {
        return issueImpl.i;
    }
    
    /**
     * Returns the properties to be shown in the Issue Table according to the ColumnDescriptors returned by
     * {@link Query#getColumnDescriptors() }
     *
     * @return properties
     */
    protected abstract Node.Property<?>[] getProperties();

    @Override
    public Action getPreferredAction() {
        return preferedAction;
    }

    public boolean wasSeen() {
        return issueImpl.getStatus() == IssueStatusProvider.Status.SEEN;
    }

    IssueStatusProvider.Status getStatus() {
        return issueImpl.getStatus();
    }

    void setSeen(boolean b) {
        issueImpl.setSeen(b);
    }

    String getSummary() {
        return issueImpl.getSummary();
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        Node.Property<?>[] properties = getProperties();
        for (Property<?> property : properties) {
            ps.put(property);
        }
        ps.put(new RecentChangesProperty());
        ps.put(new SeenProperty());
        sheet.put(ps);
        setSheet(sheet);    
    }

    private void refreshHtmlDisplayName() {
        htmlDisplayName = issueImpl.getDisplayName();
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    void fireSeenValueChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Property[] properties = getProperties();
                for (Property p : properties) {
                    if(p instanceof IssueNode.IssueProperty) {
                        String pName = ((IssueProperty)p).getName();
                        firePropertyChange(pName, null, null);
                    }
                }
            }
        });
    }

    protected void fireDataChanged() {
        // table sortes isn't thread safe
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Property[] properties = getProperties();
                for (Property p : properties) {
                    if(p instanceof IssueNode.IssueProperty) {
                        String pName = ((IssueProperty)p).getName();
                        firePropertyChange(pName, null, null);
                    }
                }
            }
        });
    }

    /**
     * An IssueNode Property
     */
    public abstract class IssueProperty<T> extends org.openide.nodes.PropertySupport.ReadOnly implements Comparable<IssueNode<I>.IssueProperty<T>> {
        protected IssueProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (IllegalAccessException | InvocationTargetException e) {
                IssueTable.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
        public I getIssueData() {
            return IssueNode.this.issueImpl.i;
        }

        @Override
        public int compareTo(IssueNode<I>.IssueProperty<T> o) {
            return toString().compareTo(o.toString());
        }
        
        @Override
        public abstract T getValue() throws IllegalAccessException, InvocationTargetException;        

        IssueStatusProvider.Status getStatus() {
            return IssueNode.this.getStatus();
        }
        
        String getRecentChanges() {
            String changes = changesProvider.getRecentChanges(getIssueData());
            if(changes == null) {
                changes = ""; // NOI18N
            } else {
                changes = changes.trim();
            }
            IssueStatusProvider.Status status = IssueNode.this.issueImpl.getStatus();
            if(changes.equals("") && status == IssueStatusProvider.Status.INCOMING_MODIFIED) { // NOI18N
                changes = NbBundle.getMessage(IssueNode.class, "LBL_IssueModified"); // NOI18N
            }
            return changes;
        }

        private String getSummary() {
            return IssueNode.this.getSummary();
        }
    }
    
    // XXX the same for id
    // XXX CTL_Issue_Summary_Title also defined in bugzilla nad jira!!!
    public class SummaryProperty extends IssueProperty<String> {
        public SummaryProperty() {
            super(LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return IssueNode.this.getSummary();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = IssueNode.this.getSummary();
            String s2 = p.getSummary();
            return s1.compareTo(s2);
        }
    }

    /**
     * Represents the Seen value in a IssueNode
     */
    public class SeenProperty extends IssueProperty<Boolean> {
        public SeenProperty() {
            super(LABEL_NAME_SEEN,
                  Boolean.class,
                  "", // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Seen_Desc")); // NOI18N
        }
        @Override
        public Boolean getValue() {
            return issueImpl.getStatus() == IssueStatusProvider.Status.SEEN;
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Boolean b1 = IssueNode.this.wasSeen();
            Boolean b2 = p.getStatus() == IssueStatusProvider.Status.SEEN;
            return b1.compareTo(b2);
        }

    }

    /**
     * Represents the Seen value in a IssueNode
     */
    public class RecentChangesProperty extends IssueNode<I>.IssueProperty<String> {
        public RecentChangesProperty() {
            super(LABEL_RECENT_CHANGES,
                  String.class,
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Recent"), // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Recent_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return changesProvider.getRecentChanges(IssueNode.this.issueImpl.i);
        }
        @Override
        public int compareTo(IssueNode<I>.IssueProperty<String> p) {
            if(p == null) return 1;
            if(p.getClass().isAssignableFrom(RecentChangesProperty.class)) {
                String recentChanges1 = changesProvider.getRecentChanges(IssueNode.this.issueImpl.i);
                String recentChanges2 = changesProvider.getRecentChanges(p.getIssueData());
                return recentChanges1.compareToIgnoreCase(recentChanges2);
            }
            return 1;
        }
    }

    private Repository getRepository() {
        if(repository == null) {
            repository = RepositoryManager.getInstance().getRepository(connectorID, repositoryID);
        }
        return repository;
    }
        
    private class IssueImpl {
        private final I i;
        private final IssueProvider<I> provider;
        private final IssueStatusProvider<?, I> statusProvider;
        
        public IssueImpl(I i, IssueProvider<I> provider, IssueStatusProvider<?, I> statusProvider) {
            this.i = i;
            this.provider = provider;
            this.statusProvider = statusProvider;
        }

        private void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
            provider.addPropertyChangeListener(i, propertyChangeListener);
        }

        private IssueStatusProvider.Status getStatus() {
            return statusProvider.getStatus(i);
        }

        private String getDisplayName() {
            return provider.getDisplayName(i);
        }

        private String getSummary() {
            return provider.getSummary(i);
        }

        private void setSeen(boolean b) {
            statusProvider.setSeenIncoming(i, b);
        }

        private String getID() {
            return provider.getID(i);
        }
    }
}
