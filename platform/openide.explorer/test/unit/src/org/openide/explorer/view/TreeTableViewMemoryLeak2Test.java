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

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Matteo Di Giovinazzo <digiovinazzo@streamsim.com>
 */
public class TreeTableViewMemoryLeak2Test extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableViewMemoryLeak2Test.class);
    }

    private Node root;
    private Node child;
    private TreeTableView ttv;

    public TreeTableViewMemoryLeak2Test(String name) {
        super(name);
    }
    
    

    public void testMain() {
        Property<String> fakeProp = new ReadWriteImpl("selected", String.class, "Selected", "");
        child = new AbstractNode(Children.LEAF) {

            {
                setName("child1");
            }

            @Override
            protected Sheet createSheet() {
                Sheet sheet = super.createSheet();

                Sheet.Set set = Sheet.createPropertiesSet();

                set.put(new PropertySupport.ReadWrite<String>("selected", String.class, "Selected", "") {

                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return "child";
                    }

                    @Override
                    public void setValue(String newValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    }
                });


                sheet.put(set);
                return sheet;
            }
        };


        {
            Children.Array children = new Children.Array();
            //children.add(new Node[]{child1, child2});
            children.add(new Node[]{child});
            root = new AbstractNode(children) {

                {
                    setName("root");
                }
            };
        }

        fakeProp.setValue("ComparableColumnTTV", Boolean.TRUE);
        fakeProp.setValue("TreeColumnTTV", Boolean.TRUE); // COMMENT TO MAKE THE TEST WORK


        try {
            SwingUtilities.invokeAndWait(new MyRunnable(fakeProp));
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        Reference<Node> child1Ref = new WeakReference<Node>(child);
        Reference<Node> rootRef = new WeakReference<Node>(root);
        Reference<Property> propRef = new WeakReference<Property>(fakeProp);
        child = null;
        root = null;
        fakeProp = null;

        assertGC("child in TTV must be GCed", child1Ref, Collections.singleton(ttv));
        assertGC("child in general must be GCed", child1Ref);
        assertGC("root in TTV must be GCed", rootRef, Collections.singleton(ttv));
        assertGC("root in general must be GCed", rootRef);
        assertGC("prop0 in TTV must be GCed", propRef, Collections.singleton(ttv));
        assertGC("prop0 in general must be GCed", propRef);
    }

    private class MyRunnable implements Runnable {
        private Property fakeProp;

        public MyRunnable(Property fp) {
            this.fakeProp = fp;
        }

        @Override
        public void run() {

            // create panel (implementing ExplorerManager.Provider) with TTV
            MyPanel panel = new MyPanel();
            panel.setLayout(new GridLayout(1, 2));
            ttv = new TreeTableView();
            ttv.setProperties(new Property[]{fakeProp});
            fakeProp = null;
            panel.add(ttv);

            // set root and keep the same root
            panel.setExplorerManagerRoot(root);

            ttv.expandNode(root);

            //cleare property and root
            ttv.setProperties(new Property[0]);
            panel.setExplorerManagerRoot(Node.EMPTY);
        }
    }

    private static class MyPanel extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager;

        public MyPanel() {
            super();
            manager = new ExplorerManager();
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private void setExplorerManagerRoot(Node root) {
            manager.setRootContext(root);
        }
    }

    private static class ReadWriteImpl extends ReadWrite<String> {

        public ReadWriteImpl(String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
