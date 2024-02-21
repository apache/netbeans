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

package org.netbeans.modules.maven.codegen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.api.customizer.support.DelayedDocumentChangeListener;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.PluginIndexManager;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.spi.nodes.MavenNodeFactory;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author Dafe Simonek
 */
public class NewPluginPanel extends javax.swing.JPanel implements ChangeListener,
        Comparator<String> {
    private static final @StaticResource String EMPTY_ICON = "org/netbeans/modules/maven/codegen/empty.png";

    private static final Object LOCK = new Object();
    private static Node noResultsRoot;

    private String curTypedText;
    private final Color defSearchC;
    private String lastQueryText, inProgressText;
    private final QueryPanel queryPanel;
    private final DefaultListModel listModel;

    private NBVersionInfo selVi;
    private NotificationLineSupport support;

    /** Creates new form NewPluginPanel */
    public NewPluginPanel() {
        initComponents();

        defSearchC = tfQuery.getForeground();

        queryPanel = new QueryPanel(this);
        pluginsPanel.add(queryPanel, BorderLayout.CENTER);

        tfQuery.getDocument().addDocumentListener(
                DelayedDocumentChangeListener.create(
                tfQuery.getDocument(), this, 500));

        listModel = new DefaultListModel();
        goalsList.setModel(listModel);
        GoalRenderer gr = new GoalRenderer(goalsList);
        goalsList.setCellRenderer(gr);
        goalsList.addMouseListener(gr);
        goalsList.addKeyListener(gr);
    }

    public NBVersionInfo getPlugin () {
        return selVi;
    }

    public boolean isConfiguration () {
        return chkConfig.isSelected();
    }

    public List<String> getGoals () {
        List<String> goals = new ArrayList<String>();
        Enumeration<GoalEntry> e  = listModel.elements();
        GoalEntry ge;
        while (e.hasMoreElements()) {
            ge = e.nextElement();
            if (ge.isSelected) {
                goals.add(ge.name);
            }
        }
        return goals;
    }

    /** delayed change of query text */
    @Override
    public void stateChanged (ChangeEvent e) {
        Document doc = (Document)e.getSource();
        try {
            curTypedText = doc.getText(0, doc.getLength()).trim();
        } catch (BadLocationException ex) {
            // should never happen, nothing we can do probably
            return;
        }

        tfQuery.setForeground(defSearchC);
        if (curTypedText.length() < 3) {
            tfQuery.setForeground(Color.RED);
            //nls.setWarningMessage(NbBundle.getMessage(AddDependencyPanel.class, "MSG_QueryTooShort"));
        } else {
            tfQuery.setForeground(defSearchC);
            //nls.clearMessages();
            find(curTypedText);
        }
    }

    @NbBundle.Messages("LBL_Incomplete=Incomplete result, still processing indices...")
    void find(String queryText) {
        synchronized (LOCK) {
            if (inProgressText != null) {
                lastQueryText = queryText;
                return;
            }
            inProgressText = queryText;
            lastQueryText = null;
        }

        setSearchInProgressUI(true);

        final String q = queryText.trim();

        Task t = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                // prepare query
                List<QueryField> fields = new ArrayList<QueryField>();
                String[] splits = q.split(" "); //NOI118N

                //fStrings.add(QueryField.FIELD_GROUPID);
                //fStrings.add(QueryField.FIELD_NAME);

                for (String curText : splits) {
                    QueryField f = new QueryField();
                    f.setField(QueryField.FIELD_ARTIFACTID);
                    f.setOccur(QueryField.OCCUR_MUST);
                    f.setValue(curText);
                    fields.add(f);
                }
                // search only in plugins
                QueryField f = new QueryField();
                f.setField(QueryField.FIELD_PACKAGING);
                f.setValue("maven-plugin"); //NOI118N
                f.setOccur(QueryField.OCCUR_MUST);
                fields.add(f);
                
                final Result<NBVersionInfo> result = RepositoryQueries.findResult(fields, RepositoryPreferences.getInstance().getRepositoryInfos());
                final List<NBVersionInfo> infos = result.getResults();

                Node node = null;
                final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

                for (NBVersionInfo nbvi : infos) {
                    String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                    List<NBVersionInfo> get = map.get(key);
                    if (get == null) {
                        get = new ArrayList<NBVersionInfo>();
                        map.put(key, get);
                    }
                    get.add(nbvi);
                }
                final List<String> keyList = new ArrayList<String>(map.keySet());
                // sort specially using our comparator, see compare method
                keyList.sort(NewPluginPanel.this);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        queryPanel.getExplorerManager().setRootContext(createResultsNode(keyList, map));
                        if (result.isPartial()) {
                            support.setWarningMessage(Bundle.LBL_Incomplete());
                        } else {
                            support.clearMessages();
                        }
                        
                    }
                });
            }
        });

        t.addTaskListener(new TaskListener() {

            @Override
            public void taskFinished(Task task) {
                synchronized (LOCK) {
                    String localText = inProgressText;
                    inProgressText = null;
                    if (lastQueryText != null && !lastQueryText.equals(localText)) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (lastQueryText != null) {
                                    find(lastQueryText);
                                }
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setSearchInProgressUI(false);
                            }
                        });
                    }
                }
            }
        });
    }

    /** Impl of comparator, sorts artifacts asfabetically with exception
     * of items that contain current query string, which take precedence.
     */
    @Override
    public int compare(String s1, String s2) {

        int index1 = s1.indexOf(inProgressText);
        int index2 = s2.indexOf(inProgressText);

        if (index1 >= 0 || index2 >=0) {
            if (index1 < 0) {
                return 1;
            } else if (index2 < 0) {
                return -1;
            }
            return index1 - index2;
        } else {
            return s1.compareTo(s2);
        }
    }

    private Node createResultsNode(List<String> keyList, Map<String, List<NBVersionInfo>> map) {
        Node node;
        if (keyList.size() > 0) {
            Children.Array array = new Children.Array();
            node = new AbstractNode(array);

            for (String key : keyList) {
                array.add(new Node[]{MavenNodeFactory.createArtifactNode(key, map.get(key))});
            }
        } else {
            node = getNoResultsRoot();
        }
        return node;
    }

    private static Node getNoResultsRoot() {
        if (noResultsRoot == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(NewPluginPanel.class, "LBL_Node_Empty"));

            Children.Array array = new Children.Array();
            array.add(new Node[]{nd});
            noResultsRoot = new AbstractNode(array);
        }

        return noResultsRoot;
    }

    private static Border getNbScrollPaneBorder () {
        Border b = UIManager.getBorder("Nb.ScrollPane.border");
        if (b == null) {
            Color c = UIManager.getColor("controlShadow");
            b = new LineBorder(c != null ? c : Color.GRAY);
        }
        return b;
    }

    void setNotificationLineSupport(NotificationLineSupport createNotificationLineSupport) {
        this.support = createNotificationLineSupport;
    }

    private static class GoalRenderer extends JCheckBox
            implements ListCellRenderer, MouseListener, KeyListener {

        private JList parentList;

        public GoalRenderer (JList list) {
            this.parentList = list;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            GoalEntry ge = (GoalEntry)value;

            setText(ge.name);
            setSelected(ge.isSelected);
            setOpaque(isSelected);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int idx = parentList.locationToIndex(e.getPoint());
            if (idx == -1) {
                return;
            }
            Rectangle rect = parentList.getCellBounds(idx, idx);
            if (rect.contains(e.getPoint())) {
                doCheck();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                doCheck();
            }
        }

        private void doCheck() {
            int index = parentList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            GoalEntry ge = (GoalEntry) parentList.getModel().getElementAt(index);
            ge.isSelected = !ge.isSelected;
            parentList.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }


    }

    private static class QueryPanel extends JPanel implements ExplorerManager.Provider,
            PropertyChangeListener {

        private BeanTreeView btv;
        private ExplorerManager manager;
        private NewPluginPanel pluginPanel;

        /** Creates new form FindResultsPanel */
        private QueryPanel(NewPluginPanel plugPanel) {
            this.pluginPanel = plugPanel;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(false);
            manager = new ExplorerManager();
            manager.setRootContext(getNoResultsRoot());
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            manager.addPropertyChangeListener(this);
            pluginPanel.lblPlugins.setLabelFor(btv);
        }


        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        /** PropertyChangeListener impl, stores maven coordinates of selected artifact */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selNodes = manager.getSelectedNodes();
                pluginPanel.selVi = null;
                if (selNodes.length == 1) {
                    if (selNodes[0] instanceof MavenNodeFactory.VersionNode) {
                        pluginPanel.selVi = ((MavenNodeFactory.VersionNode)selNodes[0]).getNBVersionInfo();
                    } else if (selNodes[0] instanceof MavenNodeFactory.ArtifactNode) {
                        List<NBVersionInfo> infos = ((MavenNodeFactory.ArtifactNode)selNodes[0]).getVersionInfos();
                        if (infos.size() > 0) {
                            pluginPanel.selVi = infos.get(0);
                        }
                    }
                }
                pluginPanel.updateGoals();
            }
        }

    } // QueryPanel

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblQuery = new javax.swing.JLabel();
        tfQuery = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblPlugins = new javax.swing.JLabel();
        pluginsPanel = new javax.swing.JPanel();
        lblGoals = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        goalsList = new javax.swing.JList();
        chkConfig = new javax.swing.JCheckBox();

        lblQuery.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.lblQuery.text")); // NOI18N

        tfQuery.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.tfQuery.text")); // NOI18N
        tfQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfQueryActionPerformed(evt);
            }
        });

        lblHint.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        lblHint.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.lblHint.text")); // NOI18N

        lblPlugins.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.lblPlugins.text")); // NOI18N

        pluginsPanel.setBorder(getNbScrollPaneBorder());
        pluginsPanel.setLayout(new java.awt.BorderLayout());

        lblGoals.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.lblGoals.text")); // NOI18N

        jScrollPane1.setViewportView(goalsList);

        chkConfig.setSelected(true);
        chkConfig.setText(org.openide.util.NbBundle.getMessage(NewPluginPanel.class, "NewPluginPanel.chkConfig.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pluginsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblQuery)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHint)
                            .addComponent(tfQuery, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)))
                    .addComponent(lblPlugins)
                    .addComponent(lblGoals)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(chkConfig)
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuery)
                    .addComponent(tfQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPlugins)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pluginsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblGoals)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                    .addComponent(chkConfig))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setSearchInProgressUI(boolean b) {
        // TODO
    }

    private void updateGoals() {
        DefaultListModel<GoalEntry> m = (DefaultListModel<GoalEntry>) goalsList.getModel();
        m.clear();

        if (selVi != null) {
            Set<String> goals = null;
            try {
                goals = PluginIndexManager.getPluginGoals(selVi.getGroupId(),
                        selVi.getArtifactId(), selVi.getVersion());
            } catch (Exception ex) {
                // TODO - put err msg in dialog?
                Exceptions.printStackTrace(ex);
            }
            if (goals != null) {
                for (String goal : goals) {
                    m.addElement(new GoalEntry(goal));
                }
            }
        }
    }

    private static class GoalEntry {
        boolean isSelected = false;
        String name;

        public GoalEntry(String name) {
            this.name = name;
        }
    }

    private void tfQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfQueryActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_tfQueryActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkConfig;
    private javax.swing.JList goalsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblGoals;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblPlugins;
    private javax.swing.JLabel lblQuery;
    private javax.swing.JPanel pluginsPanel;
    private javax.swing.JTextField tfQuery;
    // End of variables declaration//GEN-END:variables

}
