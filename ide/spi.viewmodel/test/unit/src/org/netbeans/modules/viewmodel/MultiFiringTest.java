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

package org.netbeans.modules.viewmodel;

import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin Entlicher
 */
public class MultiFiringTest extends NbTestCase implements ModelListener {

    private BasicTest.CompoundModel m;
    private volatile int changed = 0;

    public MultiFiringTest (String s) {
        super (s);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        changed = 0;
    }

    private void setUpBasicModel() {
        ArrayList l = new ArrayList ();
        m = new BasicTest.CompoundModel ();
        l.add (m);
        Models.CompoundModel cm = Models.createCompoundModel (l);
        //BasicTest.waitFinished (tt.currentTreeModelRoot.getRootNode().getRequestProcessor());
        //n = tt.getExplorerManager ().getRootContext ();
        cm.addModelListener(this);
    }

    private void setUpComplexModel() {
        ArrayList l = new ArrayList ();
        m = new BasicTest.CompoundModel ();
        l.add (m);
        l.add (new NodeModelFilter() {
            public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
                return original.getDisplayName(node);
            }
            public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
                return original.getIconBase(node);
            }
            public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
                return original.getShortDescription(node);
            }
            public void addModelListener(ModelListener l) {}
            public void removeModelListener(ModelListener l) {}
        });
        l.add (new TreeModelFilter() {
            public Object getRoot(TreeModel original) {
                return original.getRoot();
            }
            public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
                return original.getChildren(parent, from, to);
            }
            public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
                return Integer.MAX_VALUE;
            }
            public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
                return original.isLeaf(node);
            }
            public void addModelListener(ModelListener l) {}
            public void removeModelListener(ModelListener l) {}
        });
        l.add (new TableModelFilter() {
            public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
                return original.getValueAt(node, columnID);
            }
            public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
                return original.isReadOnly(node, columnID);
            }
            public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
            }
            public void addModelListener(ModelListener l) {}
            public void removeModelListener(ModelListener l) {}
        });
        Models.CompoundModel cm = Models.createCompoundModel (l);
        cm.addModelListener(this);
    }

    public void testBasicModelNodeSingleFiring() {
        setUpBasicModel();
        m.fire(new ModelEvent.NodeChanged(m, "a"));
        assertEquals("Firing occurred", 1, changed);
    }

    public void testBasicModelTableSingleFiring() {
        setUpBasicModel();
        m.fire(new ModelEvent.TableValueChanged(m, "a", null));
        assertEquals("Firing occurred", 1, changed);
    }

    public void testBasicModelTreeSingleFiring() {
        setUpBasicModel();
        m.fire(new ModelEvent.TreeChanged(m));
        assertEquals("Firing occurred", 1, changed);
    }

    public void testComplexModelNodeSingleFiring() {
        setUpComplexModel();
        m.fire(new ModelEvent.NodeChanged(m, "a"));
        assertEquals("Firing occurred", 1, changed);
    }

    public void testComplexModelTableSingleFiring() {
        setUpComplexModel();
        m.fire(new ModelEvent.TableValueChanged(m, "a", null));
        assertEquals("Firing occurred", 1, changed);
    }

    public void testComplexModelTreeSingleFiring() {
        setUpComplexModel();
        m.fire(new ModelEvent.TreeChanged(m));
        assertEquals("Firing occurred", 1, changed);
    }

    public void modelChanged(ModelEvent event) {
        changed++;
    }

}
