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

/*
 *
 */
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;


import org.netbeans.junit.NbTestCase;

import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Array;

/**
 * Tests for class ListView
 */
public class ListViewTopComponentTest extends NbTestCase {
    static {
        System.setProperty("sun.awt.datatransfer.timeout", "0");
    }
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ListViewTopComponentTest.class);
    }

    private static final int NO_OF_NODES = 3;

    
    public ListViewTopComponentTest(String name) {
        super(name);
    }

    /**
     * 1. selects a node in a ListView
     * 2. removes the node
     * 3. Shift-Click another node by java.awt.Robot
     * /
    XXX failed in NB-Core-Build #3039, fails for me locally too -jglick
    public void testNodeSelectionByRobot() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {
            //Thread.sleep(500);
            
            // Waiting for until the view is updated.
            // This should not be necessary [HREBEJK]
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
       
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + children[i].getName ()+ ".");
            }
            
            //Thread.sleep(500);
            c.remove(new Node[] { children[i] });
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            }
            
            if (lv.isShowing()) {
                Robot r = null;
                
                try {
                    r = new Robot();
                } catch (AWTException ae) {
                    fail ("Caught AWTException: " + ae.getMessage ());
                }
                
                r.keyPress(KeyEvent.VK_SHIFT);
                r.mouseMove(lv.getLocationOnScreen().x + 10,lv.getLocationOnScreen().y + 10);
                r.mousePress(InputEvent.BUTTON1_MASK);
                r.keyRelease(KeyEvent.VK_SHIFT);
                r.mouseRelease(InputEvent.BUTTON1_MASK);
            } else {
                fail();
            }
        }
    }
     */
    
    /**
     * Removes selected node by calling destroy
     */
    public void testDestroySelectedNodes() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {     
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + children[i].getName ()+ ".");
            }
            //Thread.sleep(500);
            try {
                children[i].destroy();
            } catch (IOException ioe) {
                fail ("Caught the IOException when destroy the node " + children[i].getName ()+ ".");
            }
        }
    }
    
    /**
     * Removes selected node by calling Children.Array.remove
     */
    public void testRemoveAndAddNodes() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        try {
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            p.getExplorerManager().setSelectedNodes(new Node[] {children[0]} );
        } catch (PropertyVetoException  pve) {
            fail ("Caught the PropertyVetoException when set selected node " + children[0].getName ()+ ".");
        }
        
        for (int i = 0; i < NO_OF_NODES; i++) {
            c.remove(new Node [] { children[i] } );
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
            //Thread.sleep(350);
        }
        assertEquals(NO_OF_NODES, c.getNodesCount());
    }
    
    /**
     * Creates two nodes. Selects one and tries to remove it
     * and replace with the other one (several times).
     */
    public void testNodeAddingAndRemoving() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();

        final Node c1 = new AbstractNode(Children.LEAF);
        c1.setDisplayName("First");
        c1.setName("First");
        c.add(new Node[] { c1 });
        Node c2 = new AbstractNode(Children.LEAF);
        c2.setDisplayName("Second");
        c2.setName("Second");
        //Thread.sleep(500);

        for (int i = 0; i < 5; i++) {
            c.remove(new Node[] { c1 });
            c.add(new Node[] { c2 });
            
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {c2} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + c2.getName ()+ ".");
            }
            
            c.remove(new Node[] { c2 });
            c.add(new Node[] { c1 });
            
            //Thread.sleep(350);
        }
    }
    
    private static class PListView extends ListView {
        JList getJList() {
            return list;
        }
    }

    
    private class EmptyRunnable extends Object implements Runnable {

	public void run() {
	}

    }
    

}
