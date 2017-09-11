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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 *
 * @author  S. Aubrecht
 */
public final class TreeTableViewTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableViewTest.class);
    }

    private MyNodeTableModel testModel;
    
    public TreeTableViewTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * When TreeTableView is removed from component hierarchy its model should detach property change listeners from Nodes to prevent
     * memory leaks. When the TTV is added back to the component hierarchy, the listeners must be re-attached.
     */
    public void testRemoveAddNotify() throws InterruptedException {
        MyNode[] childrenNodes = new MyNode[3];
        for( int i=0; i<childrenNodes.length; i++ )
            childrenNodes[i] = new MyNode();
        Children.Array children = new Children.Array();
        children.add( childrenNodes );
        Node rootNode = new MyNode( children );
        
        TTVComponent testComponent = new TTVComponent( rootNode );
        
        //make sure addNotify is called on the TreeTableView
        testComponent.pack();
        for( int i=0; i<childrenNodes.length; i++ )
            childrenNodes[i].forcePropertyChangeEvent();
        assertEquals("NodeTableModel must be notified that propery values changed", childrenNodes.length, testModel.tableCellUpdateCounter );
        testModel.tableCellUpdateCounter = 0;

        //make sure removeNotify is called
        testComponent.dispose();
        for( int i=0; i<childrenNodes.length; i++ )
            childrenNodes[i].forcePropertyChangeEvent();
        assertEquals("NodeTableModel must detach listeners when the TTV is removed from component hierarchy", 0, testModel.tableCellUpdateCounter );
        
        //make sure addNotify is called on the TreeTableView
        testComponent.pack();
        for( int i=0; i<childrenNodes.length; i++ )
            childrenNodes[i].forcePropertyChangeEvent();
        assertEquals("TTV must re-attach listeners when added back to the component hierarchy", childrenNodes.length, testModel.tableCellUpdateCounter );
    }
    
    final class TTVComponent extends JFrame implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager();

        TreeTableView view;
        NodeTableModel nodeTableModel;

        private TTVComponent( Node rootNode ) {
            getRootPane().setLayout( new BorderLayout() );
            manager.setRootContext( rootNode );
            Node[] nodes = rootNode.getChildren().getNodes();
            testModel = new MyNodeTableModel();
            nodeTableModel = testModel;
            nodeTableModel.setNodes(nodes);

            Node.Property[] props = nodes[0].getPropertySets()[0].getProperties();

            nodeTableModel.setProperties(props);
            view = new TreeTableView(nodeTableModel);
            view.setProperties(props);

            view.setRootVisible( false );

            //Here we add the TTV to the topcomponent:
            getRootPane().add(view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    class MyNode extends AbstractNode {

        /** Creates a new instance of MyNode */
        public MyNode() {
            super( Children.LEAF );
        }

        public MyNode( Children children ) {
            super( children );
        }

        @Override
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet();
                s.put(ss);
            }
            ss.put( new DummyProperty() );
            return s;
        }
        
        void forcePropertyChangeEvent() {
            firePropertyChange("unitTestPropName", null, new Object());
        }
        
        class DummyProperty extends Property<Object> {

            public DummyProperty() {
                super( Object.class );
                setName("unitTestPropName");
            }
            
            public boolean canRead() {
                return true;
            }

            public Object getValue() throws IllegalAccessException,
                                            InvocationTargetException {
                return getValue("unitTestPropName");
            }

            public boolean canWrite() {
                return true;
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException,
                                                    InvocationTargetException {
                setValue("unitTestPropName", val);
            }
        }
            
    }
    
    class MyNodeTableModel extends NodeTableModel {

        int tableCellUpdateCounter = 0;
        @Override
        public void fireTableCellUpdated(int row, int column) {
            tableCellUpdateCounter++;
            super.fireTableCellUpdated(row, column);
        }
    }
}
