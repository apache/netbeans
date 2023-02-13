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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.hints.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.model.Dependency;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.hints.ui.Bundle.*;
import org.netbeans.modules.maven.hints.ui.nodes.ArtifactNode;
import org.netbeans.modules.maven.hints.ui.nodes.VersionNode;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author  Anuradha G
 */
@Messages("BTN_Add=Add")
public class SearchDependencyUI extends javax.swing.JPanel implements ExplorerManager.Provider {
    private static final @StaticResource String WAIT = "org/netbeans/modules/maven/hints/wait.gif";
    private static final @StaticResource String EMPTY_IMAGE = "org/netbeans/modules/maven/hints/empty.png";

    private ExplorerManager explorerManager = new ExplorerManager();
    private JButton addButton = new JButton(BTN_Add());
    private BeanTreeView beanTreeView;
    private NBVersionInfo nbvi;
    private Tsk task = null;
    private static final RequestProcessor RP = new RequestProcessor(SearchDependencyUI.class.getName(),10);
    private Project project;
    
    private ResultsRootNode resultsRootNode;
    
    /** Creates new form SearchDependencyUI */
    public SearchDependencyUI(String clazz, Project mavProj) {
        initComponents();
        project = mavProj;
        beanTreeView = (BeanTreeView) treeView;
        beanTreeView.setPopupAllowed(false);
        beanTreeView.setRootVisible(false);
        beanTreeView.setDefaultActionAllowed(true);
        addButton.setEnabled(false);

        txtClassName.setText(clazz);
        txtClassName.selectAll();
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if (arg0.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {//NOI18N

                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                   
                    for (Node node : selectedNodes) {
                        if (node instanceof VersionNode) {

                            nbvi=((VersionNode) node).getNBVersionInfo();
        
                            
                            break;

                        }else if(node instanceof ArtifactNode){
                            ArtifactNode an=(ArtifactNode) node;
                            List<NBVersionInfo> infos = an.getVersionInfos();
                            nbvi = infos.isEmpty() ? null : infos.get(0);
                        }
                    }
                    if(nbvi!=null){
                     lblSelected.setText(nbvi.getGroupId()+" : "+nbvi.getArtifactId()
                             +" : "+nbvi.getVersion()+ " [ " + nbvi.getType() 
                             + (nbvi.getClassifier() != null ? ("," + nbvi.getClassifier()) : "")+" ]");
                    }else{
                     lblSelected.setText(null);
                    }
                    addButton.setEnabled(nbvi!=null);

                }
            }
        });
        resultsRootNode = new ResultsRootNode();
        explorerManager.setRootContext(resultsRootNode);
        createSearchTask();
        load();
        txtClassName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                load();
            }
            
        });
    }

    public NBVersionInfo getSelectedVersion() {
        
        return nbvi;
    }

    public JButton getAddButton() {
        return addButton;
    }

   private class Tsk implements Runnable, Cancellable {
        private volatile boolean cancelled = false;

        @Override
        public void run() {
                if (cancelled) return;
                
                final String[] search = new String[1];
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            lblSelected.setText(null);
                            search[0] = getClassSearchName();
//for debugging purposes only lblMatchingArtifacts.setText(search[0]);
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        resultsRootNode.setOneChild(getSearchingNode());
                    }
                });

                if (search[0].length() > 0) {
                    
                    try {
                        Result<NBVersionInfo> result = RepositoryQueries.findVersionsByClassResult(search[0], RepositoryPreferences.getInstance().getRepositoryInfos());
                        if (cancelled) return;
                        updateResult(result.getResults(), result.isPartial());
                        if (result.isPartial()) {
                            result.waitForSkipped();
                            if (cancelled) return;
                            updateResult(result.getResults(), false);
                        }
                    } catch (BooleanQuery.TooManyClauses exc) {
                        if (cancelled) return;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                resultsRootNode.setOneChild(getTooGeneralNode());
                            }
                        });
                    } catch (OutOfMemoryError oome) {
                        if (cancelled) return;
                        // running into OOME may still happen in Lucene despite the fact that
                        // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                        // (see #190265)
                        // in the bad circumstances theoretically any thread may encounter OOME
                        // but most probably this thread will be it
                        // trying to indicate the condition to the user here
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                resultsRootNode.setOneChild(getTooGeneralNode());
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resultsRootNode.setOneChild(getNoResultsNode());
                        }
                    });
                }
        }

        @Override
        public synchronized boolean cancel() {
            cancelled = true;
            return true;
        }
       
   }

    private Task createSearchTask() {
        if (task != null) {
            task.cancel();
        }
        task = new Tsk();
        return RP.create(task, true);
    }
    
    public final synchronized void load() {
        createSearchTask().schedule(500);
    }

    public String getClassSearchName() {
        return txtClassName.getText().trim();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblClassName = new javax.swing.JLabel();
        txtClassName = new javax.swing.JTextField();
        treeView = new BeanTreeView();
        lblMatchingArtifacts = new javax.swing.JLabel();
        lblSelected = new javax.swing.JLabel();

        lblClassName.setText(org.openide.util.NbBundle.getMessage(SearchDependencyUI.class, "LBL_Class_Name")); // NOI18N

        treeView.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionBackground")));

        lblMatchingArtifacts.setText(org.openide.util.NbBundle.getMessage(SearchDependencyUI.class, "LBL_Matching_artifacts")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSelected, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(treeView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(txtClassName, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(lblMatchingArtifacts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(lblClassName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblClassName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtClassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMatchingArtifacts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(treeView, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblClassName;
    private javax.swing.JLabel lblMatchingArtifacts;
    private javax.swing.JLabel lblSelected;
    private javax.swing.JScrollPane treeView;
    private javax.swing.JTextField txtClassName;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    private class ResultsRootNode extends AbstractNode {

        private ResultsRootChildren resultsChildren;

        public ResultsRootNode() {
            this(new InstanceContent());
        }

        private ResultsRootNode(InstanceContent content) {
            super (new ResultsRootChildren(), new AbstractLookup(content));
            content.add(this);
            this.resultsChildren = (ResultsRootChildren) getChildren();
        }

        public void setOneChild(Node n) {
            List<Node> ch = new ArrayList<Node>(1);
            ch.add(n);
            setNewChildren(ch);
        }
        
        public void setNewChildren(List<Node> ch) {
            resultsChildren.setNewChildren (ch);
        }
    }
    
    private class ResultsRootChildren extends Children.Keys<Node> {
        
        List<Node> myNodes;

        public ResultsRootChildren() {
            myNodes = Collections.EMPTY_LIST;
        }

        private void setNewChildren(List<Node> ch) {
            myNodes = ch;
            refreshList();
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        private void refreshList() {
            List<Node> keys = new ArrayList<>();
            for (Node node : myNodes) {
                keys.add(node);
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[] { key };
        }

    }

    private static Node noResultsNode, searchingNode, tooGeneralNode;
    
    @Messages("Node_Loading=Searching...")
    private static Node getSearchingNode() {
        if (searchingNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(WAIT); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Searching"); //NOI18N

            nd.setDisplayName(Node_Loading()); //NOI18N
            
            searchingNode = nd;
        }
        return new FilterNode (searchingNode, Children.LEAF);
    }

    @Messages("Node_Empty=No matching items")
    public static Node getNoResultsNode() {
        if (noResultsNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_IMAGE); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(Node_Empty());
            
            noResultsNode = nd;
        }
        return new FilterNode (noResultsNode, Children.LEAF);
    }

    @Messages("Node_TooGeneral=Too general query")
    private static Node getTooGeneralNode() {
        if (tooGeneralNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_IMAGE); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Too General"); //NOI18N

            nd.setDisplayName(Node_TooGeneral()); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode (tooGeneralNode, Children.LEAF);
    }
    
    
    void updateResult(List<NBVersionInfo> infos, final boolean partial) {
        final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

        for (NBVersionInfo ver : infos) {
            String key = ver.getGroupId() + " : " + ver.getArtifactId();
            List<NBVersionInfo> get = map.get(key);
            if (get == null) {
                get = new ArrayList<NBVersionInfo>();
                map.put(key, get);
            }
            get.add(ver);
        }
        final List<String> keyList = new ArrayList<String>(map.keySet());
        // sort specially using our comparator, see compare method
        Collections.sort(keyList, new HeuristicsComparator());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateResultNodes(keyList, map, partial);
            }
        });
    }
 
    private void updateResultNodes(List<String> keyList, Map<String, List<NBVersionInfo>> map, boolean partial) {

        if (keyList.size() > 0) { // some results available

            Map<String, Node> currentNodes = new HashMap<String, Node>();
            for (Node nd : resultsRootNode.getChildren().getNodes()) {
                currentNodes.put(nd.getName(), nd);
            }
            List<Node> newNodes = new ArrayList<Node>(keyList.size());

            for (String key : keyList) {
                Node nd;
                nd = currentNodes.get(key);
                if (null != nd) {
                    ((ArtifactNode)nd).setVersionInfos(map.get(key));
                } else {
                    nd = new ArtifactNode(key, map.get(key));
                }
                newNodes.add(nd);
            }
            
            // still searching?
            if (partial) {
                newNodes.add(getSearchingNode());
            }

            resultsRootNode.setNewChildren(newNodes);
        } else if (partial) { // still searching, no results yet
            resultsRootNode.setOneChild(getSearchingNode());
        } else { // finished searching with no results
            resultsRootNode.setOneChild(getNoResultsNode());
        }
    }
    
    //TODO
    // for netbeans projects, org.netbeans.api is the prefered item in the list
    // for web/ejb/ear projects, javax.* are probably preferred.
    // possibly items from groupids that are already present in the pom should also be
    // put up front.
    private class HeuristicsComparator implements Comparator<String> {
        private final Set<String> privilegedGroupIds = new HashSet<String>();
        
        private HeuristicsComparator() {
            String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
            if (NbMavenProject.TYPE_NBM.equalsIgnoreCase(packaging)) {
                privilegedGroupIds.add("org.netbeans.api"); //NOI18N
            }
            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(packaging) || 
                NbMavenProject.TYPE_EAR.equalsIgnoreCase(packaging) || 
                NbMavenProject.TYPE_EJB.equalsIgnoreCase(packaging)) {
                privilegedGroupIds.add("javax.activation");//NOI18N
                privilegedGroupIds.add("javax.ejb");//NOI18N
                privilegedGroupIds.add("javax.faces");//NOI18N
                privilegedGroupIds.add("javax.j2ee");//NOI18N
                privilegedGroupIds.add("javax.jdo");//NOI18N
                privilegedGroupIds.add("javax.jms");//NOI18N
                privilegedGroupIds.add("javax.mail");//NOI18N
                privilegedGroupIds.add("javax.management");//NOI18N
                privilegedGroupIds.add("javax.naming");//NOI18N
                privilegedGroupIds.add("javax.persistence");//NOI18N
                privilegedGroupIds.add("javax.portlet");//NOI18N
                privilegedGroupIds.add("javax.resource");//NOI18N
                privilegedGroupIds.add("javax.security");//NOI18N
                privilegedGroupIds.add("javax.servlet");//NOI18N
                privilegedGroupIds.add("javax.sql");//NOI18N
                privilegedGroupIds.add("javax.transaction");//NOI18N
                privilegedGroupIds.add("javax.xml");//NOI18N
            }
            //TODO add some more heuristics
            NbMavenProject mavenproject = project.getLookup().lookup(NbMavenProject.class);
            List<Dependency> deps = mavenproject.getMavenProject().getDependencies();
            for (Dependency d : deps) {
                privilegedGroupIds.add(d.getGroupId());
            }
        }

        @Override
        public int compare(String s1, String s2) {
            String[] split1 = s1.split(":");
            String[] split2 = s2.split(":");
            boolean b1 = privilegedGroupIds.contains(split1[0].trim());
            boolean b2 = privilegedGroupIds.contains(split2[0].trim());
            if (b1 && !b2) {
                return -1;
            }
            if (!b1 && b2) {
                return 1;
            }
            return s1.compareTo(s2);
        }
        
    }
            
}
