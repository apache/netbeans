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
package org.netbeans.modules.versioning.ui.history;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 *
 * The toplevel Node in the HistoryView
 * 
 * @author Tomas Stupka
 *
 */
public class HistoryRootNode extends AbstractNode {
    
    static final String NODE_LOAD_NEXT  = "getmoreno"; // NOI18N
    static final String NODE_WAIT       = "waitnode";  // NOI18N
    static final String NODE_ROOT       = "rootnode";  // NOI18N
    
    static final Action[] NO_ACTION = new Action[0];
    
    private static DateFormat dateFormat = DateFormat.getDateInstance();
    
    private Map<Long, HistoryEntry> revisionEntries = new HashMap<Long, HistoryEntry>();
    
    private LoadNextNode loadNextNode;
    private WaitNode waitNode;
        
    private final String vcsName;
    private int vcsCount = 0;
    private final Action loadNextAction;
    private final Action[] actions;
        
    HistoryRootNode(String vcsName, Action loadNextAction, Action... actions) {
        super(new Children.SortedArray());
        this.vcsName = vcsName;
        this.loadNextAction = loadNextAction;
        this.actions = actions;
    }
    
    static boolean isLoadNext(Object n) {
        return n instanceof HistoryRootNode.LoadNextNode;
    }
    
    static boolean isWait(Object n) {
        return n instanceof HistoryRootNode.WaitNode;
    }
                    
    /**
     * @param removeThreshold all old entries newer than the theshold will be deleted
     */
    synchronized void addLHEntries (HistoryEntry[] entries, long removeThreshold) {
        addEntries(entries, false, removeThreshold);
    }
        
    /**
     * @param removeThreshold all old entries newer than the theshold will be deleted
     */
    synchronized void addVCSEntries (HistoryEntry[] entries, long removeThreshold) {
        addEntries(entries, true, removeThreshold);
    }
        
    synchronized HistoryEntry getPreviousEntry(HistoryEntry entry) {
        Node[] nodes = getChildren().getNodes();
        boolean hit = false;
        for (int i = nodes.length - 1; i >= 0; i--) {
            HistoryEntry he = nodes[i].getLookup().lookup(HistoryEntry.class);
            if(he != null) {
                if(!entry.isLocalHistory() && he.isLocalHistory()) {
                    continue;
                }
                if(hit) {
                    return he;
                }
                if(he == entry) {
                    hit = true;
                }
            }
        }
        return null;
    }
    
    private void addEntries (HistoryEntry[] entries, boolean vcs, long removeThreshold) {
        // add new
        List<Node> nodes = new LinkedList<Node>();
        removeOldEntries(entries, !vcs, removeThreshold);
        for (HistoryEntry e : entries) {
            if(!revisionEntries.containsKey(e.getDateTime().getTime())) {
                revisionEntries.put(e.getDateTime().getTime(), e);
                if(vcs) {
                    vcsCount++;
                }
                nodes.add(RevisionNode.create(e));
            } 
        }
        if(loadNextNode != null) {
            loadNextNode.refreshMessage();
        }
        getChildren().add(nodes.toArray(new Node[nodes.size()]));
    }

    private void removeOldEntries (HistoryEntry[] entries, boolean isLocal, long removeThreshold) {
        Set<Long> timestamps = new HashSet<Long>(entries.length);
        for (HistoryEntry e : entries) {
            timestamps.add(e.getDateTime().getTime());
        }
        List<Node> toRemove = new ArrayList<Node>();
        for (Node n : getChildren().getNodes()) {
            HistoryEntry e = n.getLookup().lookup(HistoryEntry.class);
            if (e != null && isLocal == e.isLocalHistory()) {
                long time = e.getDateTime().getTime();
                if (!timestamps.contains(time) && time > removeThreshold) {
                    // old entry would fit between just fetched revisions, but is not among of them
                    // probably was removed
                    History.LOG.log(Level.FINE, "Removing obsolete history entry: {0} {1}", //NOI18N
                            new Object[] { e.getRevisionShort(), e.getMessage() });
                    toRemove.add(n);
                    revisionEntries.remove(time);
                    if (!isLocal) {
                        vcsCount--;
                    }
                }
            }
        }
        getChildren().remove(toRemove.toArray(new Node[toRemove.size()]));
    }

    public synchronized void addWaitNode() {
        if(waitNode != null) {
            getChildren().remove(new Node[] { waitNode });
        }
        waitNode = new WaitNode();
        getChildren().add(new Node[] { waitNode });
    }
    
    public synchronized void removeWaitNode() {
        if(waitNode != null) {
            getChildren().remove(new Node[] { waitNode });
            waitNode = null;
        }
    }
    
    synchronized void loadingVCSStarted() {
        Children children = getChildren();
        if(loadNextNode != null) {
            children.remove(new Node[] { loadNextNode });
        }
        addWaitNode();
    }

    synchronized void loadingVCSFinished(Date dateFrom) {
        Children children = getChildren();
        removeWaitNode();         
        if(loadNextNode != null) {
            children.remove(new Node[] { loadNextNode });
        }
        if(dateFrom != null && !HistorySettings.getInstance().getLoadAll()) {
            loadNextNode = new LoadNextNode(dateFrom);
            children.add(new Node[] {loadNextNode});
        }
    }
    
    @Override
    public String getName() {
        return NODE_ROOT; 
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(HistoryRootNode.class, "LBL_LocalHistory_Column_Version"); // NOI18N
    }            
        
    @Override
    public Action[] getActions(boolean context) {
        return NO_ACTION;
    }
        
    synchronized void refreshLoadNextName() {
        if(loadNextNode != null) {
            loadNextNode.nameChanged();
        }
    }

    class LoadNextNode extends AbstractNode implements Comparable<Node> {

        LoadNextNode(Date dateFrom) {
            super(new Children.SortedArray());
            
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = Sheet.createPropertiesSet();
            ps.put(new BaseProperty(RevisionNode.PROPERTY_NAME_VERSION)); 
            ps.put(new BaseProperty(RevisionNode.PROPERTY_NAME_USER)); 
            ps.put(new MessageProperty(dateFrom)); 
            sheet.put(ps);
            setSheet(sheet);        
        }

        @Override
        public Action getPreferredAction() {
            return loadNextAction;
        }

        @Override
        public Action[] getActions(boolean context) {
            if(!HistorySettings.getInstance().getLoadAll()) {
                return actions;
            }
            return new Action[0];
        }
        
        @Override
        public String getDisplayName() {
            return (String) loadNextAction.getValue(Action.NAME);  
        }

        @Override
        public int compareTo(Node n) {
            return n instanceof WaitNode ? 0 : 1;
        }

        @Override
        public String getName() {
            return NODE_LOAD_NEXT;
        }

        private void refreshMessage() {
            firePropertyChange(RevisionNode.PROPERTY_NAME_LABEL, null, null);
        }      

        private void nameChanged() {
            fireDisplayNameChange(null, null);
        }

        class MessageProperty extends BaseProperty {
            private final Date dateFrom;
            public MessageProperty(Date dateFrom) {
                super(RevisionNode.PROPERTY_NAME_LABEL, TableEntry.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc")); // NOI18N
                this.dateFrom = dateFrom;
            }
            @Override
            public String getDisplayValue() {
                if(dateFrom != null) {
                    String entries = NbBundle.getMessage(HistoryRootNode.class, vcsCount == 1 ? "LBL_EntryCountOne" : "LBL_EntryCountMore", vcsCount); //NOI18N
                    return NbBundle.getMessage(HistoryRootNode.class, "LBL_ShowingVCSRevisions", vcsName, dateFormat.format(dateFrom), entries); // NOI18N
                } else {
                    return NbBundle.getMessage(HistoryRootNode.class, "LBL_ShowingAllVCSRevisions", vcsName); // NOI18N
                }
            }
            @Override
            public String toString() {
                return getDisplayValue();
            }
        }
    }

    private class WaitNode extends AbstractNode implements Comparable<Node> {
        public WaitNode() {
            super(Children.LEAF);
            setDisplayName(NbBundle.getMessage(HistoryRootNode.class, "LBL_LoadingPleaseWait"));        // NOI18N
            setIconBaseWithExtension("org/netbeans/modules/versioning/ui/resources/icons/wait.gif");    // NOI18N
            
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = Sheet.createPropertiesSet();
            ps.put(new BaseProperty(RevisionNode.PROPERTY_NAME_VERSION)); 
            ps.put(new BaseProperty(RevisionNode.PROPERTY_NAME_USER)); 
            ps.put(new BaseProperty(RevisionNode.PROPERTY_NAME_LABEL)); 
            sheet.put(ps);
            setSheet(sheet);                    
        }
        
        @Override
        public int compareTo(Node n) {
            return n instanceof LoadNextNode ? 0 : 1;
        }   
        
        @Override
        public String getName() {
            return NODE_WAIT;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
        
    }    
    
    private class BaseProperty extends PropertySupport.ReadOnly<TableEntry> {
        private final TableEntry te;

        public BaseProperty(String name, Class<TableEntry> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
            te = new TableEntry() {
                @Override
                public String getDisplayValue() {
                    return BaseProperty.this.getDisplayValue();
                }
                @Override
                public String getTooltip() {
                    return BaseProperty.this.getDisplayValue();
                }
                @Override
                public Integer order() {
                    return -1;
                }                    
            };  
        }

        public BaseProperty(String name) {
            this(name, TableEntry.class, "", "");
        }
        String getDisplayValue() {
            return "";
        }
        @Override
        public TableEntry getValue() throws IllegalAccessException, InvocationTargetException {
            return te;
        }
    }    
}

