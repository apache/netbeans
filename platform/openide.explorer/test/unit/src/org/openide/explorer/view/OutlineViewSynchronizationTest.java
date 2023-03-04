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
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Tests fix of Jira issue [NETBEANS-4857]. There should be no additional
 * property change events during outline view tree synchronization.
 *
 * @author Michael Kuettner
 */
public final class OutlineViewSynchronizationTest extends NbTestCase {

    public OutlineViewSynchronizationTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testSingleOutlineSelection() throws InterruptedException, IllegalAccessException, InvocationTargetException, PropertyVetoException {

        MultipleOutlinesPanel outlinesPanel = new MultipleOutlinesPanel(1);
        outlinesPanel.addNotify();

        Node[] nodes = outlinesPanel.getExplorerManager().getRootContext().getChildren().getNodes();

        LoggingPropertyChangeListener evtLog = new LoggingPropertyChangeListener();
        ExplorerManager manager = outlinesPanel.getExplorerManager();
        manager.addPropertyChangeListener(evtLog);

        // select a single node
        manager.setSelectedNodes(new Node[]{nodes[0]});

        assertEquals(1, evtLog.getEventCount());
        assertEquals("[] -> [0]", evtLog.getEvent(0));
        assertEquals(1, outlinesPanel.getOutline(0).getSelectedRowCount());

        // remove already collected events
        evtLog.clearEvents();

        // select all nodes
        manager.setSelectedNodes(nodes);

        assertEquals(1, evtLog.getEventCount());
        assertEquals("[0] -> [0,1,2,3,4,5,6,7,8,9]", evtLog.getEvent(0));
        assertEquals(10, outlinesPanel.getOutline(0).getSelectedRowCount());
    }

    public void testMultipleOutlinesSelectionSynchronization() throws InterruptedException, IllegalAccessException, InvocationTargetException, PropertyVetoException {

        MultipleOutlinesPanel outlinesPanel = new MultipleOutlinesPanel(4);
        outlinesPanel.addNotify();

        Node[] nodes = outlinesPanel.getExplorerManager().getRootContext().getChildren().getNodes();

        LoggingPropertyChangeListener evtLog = new LoggingPropertyChangeListener();
        ExplorerManager manager = outlinesPanel.getExplorerManager();
        manager.addPropertyChangeListener(evtLog);

        // select a single node
        manager.setSelectedNodes(new Node[]{nodes[0]});

        assertEquals(1, evtLog.getEventCount());
        assertEquals("[] -> [0]", evtLog.getEvent(0));
        assertEquals(1, outlinesPanel.getOutline(0).getSelectedRowCount());
        assertEquals(1, outlinesPanel.getOutline(1).getSelectedRowCount());
        assertEquals(1, outlinesPanel.getOutline(2).getSelectedRowCount());
        assertEquals(1, outlinesPanel.getOutline(3).getSelectedRowCount());

        // remove already collected events
        evtLog.clearEvents();

        // select all nodes
        manager.setSelectedNodes(nodes);

        assertEquals(1, evtLog.getEventCount());
        assertEquals("[0] -> [0,1,2,3,4,5,6,7,8,9]", evtLog.getEvent(0));
        assertEquals(10, outlinesPanel.getOutline(0).getSelectedRowCount());
        assertEquals(10, outlinesPanel.getOutline(1).getSelectedRowCount());
        assertEquals(10, outlinesPanel.getOutline(2).getSelectedRowCount());
        assertEquals(10, outlinesPanel.getOutline(3).getSelectedRowCount());
    }

    /**
     * A panel that provides an {@link ExplorerManager} and contains multiple
     * panels with outlines.
     */
    public static class MultipleOutlinesPanel extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager();

        public MultipleOutlinesPanel(int numberOfOutlineViews) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            for (int i = 0; i < numberOfOutlineViews; i++) {
                add(new OutlinePanel());
            }
            Node rootNode = new AbstractNode(Children.create(new MultipleTreesNodeFactory(), false));
            manager.setRootContext(rootNode);
        }

        public Outline getOutline(int index) {
            OutlinePanel panel = (OutlinePanel) getComponent(index);
            return panel.getOutlineView().getOutline();
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    /**
     * A panel that contains a single {@link OutlineView}.
     */
    private static class OutlinePanel extends JPanel {

        private OutlineView outlineView;

        public OutlinePanel() {
            setLayout(new BorderLayout());

            outlineView = new OutlineView("tree");
            outlineView.getOutline().setRootVisible(false);
            // activate multiple interval selection
            outlineView.getOutline().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            outlineView.getOutline().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            add(outlineView, BorderLayout.CENTER);
        }

        public OutlineView getOutlineView() {
            return outlineView;
        }
    }

    /**
     * A simple node factory that creates some String nodes.
     */
    private static class MultipleTreesNodeFactory extends ChildFactory<String> {

        @Override
        protected boolean createKeys(final List<String> values) {
            for (int i = 0; i < 10; i++) {
                values.add(Integer.toString(i));
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(final String key) {
            return new MultipeTreesNode(key);
        }
    }

    /**
     * A simple node for a String.
     */
    private static class MultipeTreesNode extends AbstractNode {

        public MultipeTreesNode(final String value) {
            super(Children.LEAF, new AbstractLookup(new InstanceContent()));
            setName(value);
        }
    }

    /**
     * PropertyChangeListener implementation that keeps a string representation
     * of each ExplorerManager.PROP_SELECTED_NODES event.
     */
    private static class LoggingPropertyChangeListener implements PropertyChangeListener {

        private List<String> events = new ArrayList<String>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] oldNodes = (Node[]) evt.getOldValue();
                Node[] newNodes = (Node[]) evt.getNewValue();
                events.add(nodesToString(oldNodes) + " -> " + nodesToString(newNodes));
            }
        }

        private String nodesToString(final Node[] nodes) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < nodes.length; i++) {
                builder.append(nodes[i].getName());
                if (i != nodes.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        public int getEventCount() {
            return events.size();
        }

        public String getEvent(int index) {
            return events.get(index);
        }

        public void clearEvents() {
            events.clear();
        }
    }
}
