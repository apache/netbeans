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

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;

import org.netbeans.spi.viewmodel.*;

import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;



/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class BasicTest  extends NbTestCase {

    private String helpID = "A test help ID"; // NOI18N

    public BasicTest (String s) {
        super (s);
    }

    static OutlineTable createView(final Models.CompoundModel mcm) {
        final OutlineTable[] ttPtr = new OutlineTable[] { null };
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ttPtr[0] = (OutlineTable) Models.createView(mcm);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex.getTargetException());
        }
        assertNotNull(ttPtr[0]);
        return ttPtr[0];
    }

    public void testBasic () throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel cm = new CompoundModel ();
        l.add (cm);
        Models.CompoundModel mcm = Models.createCompoundModel(l, helpID);
        OutlineTable tt = createView(mcm);
        RequestProcessor rp = tt.currentTreeModelRoot.getRootNode().getRequestProcessor();
        cm.setRPUsed(rp);
        waitFinished (rp);
        Node n = tt.getExplorerManager ().
            getRootContext ();
        checkNode (n, "", rp);
        if (cm.exception != null)
            cm.exception.printStackTrace ();
        assertNull ("Threading problem", cm.exception);
        // TODO: Expansion test does not work - probably written in a bad way...
        //assertEquals ("nodeExpanded notification number", 3, cm.expandedTest.size ());
        //assertEquals ("nodeExpanded ", cm.toBeExpandedTest, cm.expandedTest);
        assertEquals(n.getValue("propertiesHelpID"), helpID);
    }
    
    private void checkNode (Node n, String name, RequestProcessor rp) {
        // init
        //assertEquals (null, n.getShortDescription ());
        Node[] ns = n.getChildren ().getNodes ();
        waitFinished (rp);
        
        ns = n.getChildren ().getNodes ();
        if (name.length () < 4) {
            assertEquals (name, 3, ns.length);
            checkNode (ns [0], name + "a", rp);
            checkNode (ns [1], name + "b", rp);
            checkNode (ns [2], name + "c", rp);
        } else
            assertEquals (ns.length, 0);
        
        if (name.length () > 0) {
            //assertEquals (name, n.getName ());
            n.getDisplayName ();
            String sd = n.getShortDescription ();
            n.getActions (false);
            waitFinished (rp);
            assertEquals (name, n.getDisplayName ());
            assertEquals (name + "WWW", sd);
            assertEquals (1, n.getActions (false).length);
        }
    }

    static void waitFinished (RequestProcessor rp) {
        rp.post (new Runnable () {
            public void run () {}
        }).waitFinished ();
    }
    
    
    public void testMnemonics() throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel cm = new CompoundModel ();
        l.add (cm);
        TestColumnModel tcm = new TestColumnModel();
        l.add(tcm);
        Models.CompoundModel mcm = Models.createCompoundModel(l);
        OutlineTable tt = createView(mcm);
        cm.setRPUsed(tt.currentTreeModelRoot.getRootNode().getRequestProcessor());
        Node.Property[] columns = tt.columns;
        assertEquals(2, columns.length);
        assertEquals(new Character('e'), columns[1].getValue("ColumnMnemonicCharTTV"));
    }
    
    public static class CompoundModel implements TreeModel, 
    NodeModel, NodeActionsProvider, TableModel, TreeExpansionModel {

    
        private final Set<ModelListener> listeners = new HashSet<ModelListener>();
        
        private Throwable exception;

        private Map callNumbers = new HashMap ();
        
        private RequestProcessor rp;

        void setRPUsed(RequestProcessor rp) {
            this.rp = rp;
        }

        protected synchronized void addCall (String methodName, Object node) {
            Map m = (Map) callNumbers.get (methodName);
            if (m == null)
                callNumbers.put (methodName, m = new HashMap ());
            if (m.containsKey (node)) {
                Object info = m.get(node);
                if (info instanceof Exception) {
                    System.err.println ("Second call of " + methodName + " method for the same node " + node);
                    System.err.println("First was at:");
                    ((Exception) info).printStackTrace();
                    System.err.println("Second is:");
                    Thread.dumpStack();
                    m.put (node, new Integer(2));
                } else {
                    int numCalls = ((Integer) info).intValue() + 1;
                    System.err.println (numCalls+". call of " + methodName + " method for the same node " + node);
                    Thread.dumpStack();
                    m.put (node, new Integer(numCalls));
                }
            } else {
                m.put (node, new Exception());
            }
        }

        void checkThread () {
            try {
                assertTrue ("The right thread", rp.isRequestProcessorThread ());
            } catch (Throwable t) {
                exception = t;
            }
            /*;
            Thread t = Thread.currentThread ();
            if ( t.getClass ().getName ().startsWith 
                    (RequestProcessor.class.getName ())
            ) exception = new Exception ();
             */
        }

        // TreeModel ...............................................................

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        public Object getRoot () {
            //addCall ("getRoot", null);
            return ROOT;
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         *
         * @return  children for given parent on given indexes
         */
        public Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException {
            addCall ("getChildren", parent);
            if (parent == ROOT)
                return new Object[] {"a", "b", "c"};
            if (parent instanceof String)
                return new Object[] {parent + "a", parent + "b", parent + "c"};
            throw new UnknownTypeException (parent);
        }

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public int getChildrenCount (Object node) throws UnknownTypeException {
            addCall ("getChildrenCount", node);
            if (node == ROOT)
                return 3;
            if (node instanceof String)
                return 3;
            throw new UnknownTypeException (node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         * @return  true if node is leaf
         */
        public boolean isLeaf (Object node) throws UnknownTypeException {
            addCall ("isLeaf", node);
            if (node == ROOT)
                return false;
            if (node instanceof String)
                return ((String) node).length () > 3;
            throw new UnknownTypeException (node);
        }


        // NodeModel ...........................................................

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) throws UnknownTypeException {
            addCall ("getDisplayName", node);
            //checkThread ();
            if (node instanceof String)
                return (String) node;
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            addCall ("getShortDescription", node);
            //checkThread (); Short description is called on AWT! How else we could display a tooltip?
            if (node == ROOT)
                return "";
            if (node instanceof String)
                return node + "WWW";
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            addCall ("getIconBase", node);
            //checkThread ();
            if (node instanceof String)
                return node + "XXX";
            throw new UnknownTypeException (node);
        }


        // NodeActionsProvider .....................................................

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) throws UnknownTypeException {
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) throws UnknownTypeException {
            //checkThread ();
            if (node == ROOT)
                return new Action [0];
            if (node instanceof String)
                return new Action[] {
                    new AbstractAction ((String) node) {
                        public void actionPerformed (ActionEvent ev) {
                            
                        }
                    },
                };
            throw new UnknownTypeException (node);
        }


        // ColumnsModel ............................................................

        /**
         * Returns sorted array of 
         * {@link org.netbeans.spi.viewmodel.ColumnModel}s.
         *
         * @return sorted array of ColumnModels
         */
        public ColumnModel[] getColumns () {
            return new ColumnModel [0];
        }


        // TableModel ..............................................................

        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            addCall ("getValueAt", node);
            checkThread ();
            if (node instanceof String) {
                if (columnID.equals ("1"))
                    return node + "1";
                if (columnID.equals ("2"))
                    return node + "2";
            }
            throw new UnknownTypeException (node);
        }

        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            addCall ("isReadOnly", node);
            checkThread ();
            if (node instanceof String) {
                if (columnID.equals ("1"))
                    return true;
                if (columnID.equals ("2"))
                    return true;
            }
            throw new UnknownTypeException (node);
        }

        public void setValueAt (Object node, String columnID, Object value) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }


        // TreeExpansionModel ......................................................

        private Set toBeExpandedTest = new HashSet ();
        private Set expandedTest = new HashSet ();
        {
            toBeExpandedTest.add (getRoot());
            toBeExpandedTest.add ("a");
            toBeExpandedTest.add ("ab");
            toBeExpandedTest.add ("abc");
        }
        
        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) throws UnknownTypeException {
            if (node instanceof String)
                return toBeExpandedTest.contains (node);
            throw new UnknownTypeException (node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            if (!toBeExpandedTest.contains (node)) {
                System.err.println("This node should not be expanded: " + node);
                Thread.dumpStack();
            }
            expandedTest.add (node);
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            System.err.println("nodeCollapsed " + node);
            Thread.dumpStack();
        }


        // listeners ...............................................................

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (listeners) {
                listeners.add (l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (listeners) {
                listeners.remove (l);
            }
        }
        
        public void fire () {
            List<ModelListener> v;
            synchronized (listeners) {
                v = new ArrayList<ModelListener>(listeners);
            }
            int i, k = v.size ();
            for (i = 0; i < k; i++)
                ((ModelListener) v.get (i)).modelChanged (null);
        }
        
        public void fire (ModelEvent event) {
            List<ModelListener> v;
            synchronized (listeners) {
                v = new ArrayList<ModelListener>(listeners);
            }
            int i, k = v.size ();
            for (i = 0; i < k; i++) {
                ((ModelListener) v.get (i)).modelChanged (event);
            }
        }
    }
    
    private static class TestColumnModel extends ColumnModel {
        public Class getType() {
            return String.class;
        }

        public String getDisplayName() {
            return "Test";
        }

        @Override
        public Character getDisplayedMnemonic() {
            return new Character('e');
        }

        public String getID() {
            return "xx";
        }

    }
}
