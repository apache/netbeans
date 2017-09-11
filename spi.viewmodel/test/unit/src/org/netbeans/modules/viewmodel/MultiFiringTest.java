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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
