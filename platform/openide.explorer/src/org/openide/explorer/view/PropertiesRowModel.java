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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.WeakHashMap;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author David Strupl
 */
class PropertiesRowModel implements RowModel {
   
    private Node.Property[] prop = new Node.Property[0];
    private Outline outline;
    private WeakHashMap<Node, PropertyChangeListener> nodesListenersCache = new WeakHashMap<Node, PropertyChangeListener> ();
    private String [] names = new String [prop.length];
    private String [] descs = new String [prop.length];

    
    /** listener on node properties changes, recreates displayed data */
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        propertyChange(evt);
                    }
                });
                return;
            }
            //fireTableDataChanged();
            int row = rowForNode((Node)evt.getSource());
            if (row == -1) {
                return;
            }

            int column = columnForProperty(evt.getPropertyName());
            if (column == -1) {
                outline.tableChanged(new TableModelEvent(outline.getModel(), row, row,
                                TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
            } else {
                outline.tableChanged(new TableModelEvent(outline.getModel(), row, row,
                                column+1, TableModelEvent.UPDATE));
            }
        }
    };

    OutlineTooltipUpdater otu = new OutlineTooltipUpdater();
    
    NodeListener nl = new NodeListener() {

        @Override
        public void childrenAdded(NodeMemberEvent ev) {}

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {}

        @Override
        public void childrenReordered(NodeReorderEvent ev) {}

        @Override
        public void nodeDestroyed(NodeEvent ev) {}

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        propertyChange(evt);
                    }
                });
                return;
            }
            if (Node.PROP_SHORT_DESCRIPTION.equals(evt.getPropertyName())) {
                int row = rowForNode((Node)evt.getSource());
                otu.fireToolTipChanged(outline, row);
            }
            pcl.propertyChange(evt);
        }
    };

    private static class OutlineTooltipUpdater implements MouseMotionListener, MouseListener {

        private MouseEvent lastMouseMovedEvent = null;
        
        public void fireToolTipChanged(final Outline outline, final int row) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (lastMouseMovedEvent != null) {
                        int r = outline.rowAtPoint(lastMouseMovedEvent.getPoint());
                        if (r == row) {
                            ToolTipManager.sharedInstance().mouseMoved(lastMouseMovedEvent);
                        }
                    }
                }
            });
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            lastMouseMovedEvent = null;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastMouseMovedEvent = e;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            lastMouseMovedEvent = null;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastMouseMovedEvent = null;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lastMouseMovedEvent = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            lastMouseMovedEvent = null;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            lastMouseMovedEvent = null;
        }
        
    }
    
    /** Creates a new instance of PropertiesRowModel */
    public PropertiesRowModel() {
        
    }
    
    public void setOutline(Outline outline) {
        if (this.outline != null) {
            this.outline.removeMouseListener(otu);
            this.outline.removeMouseMotionListener(otu);
        }
        this.outline = outline;
        outline.addMouseListener(otu);
        outline.addMouseMotionListener(otu);
    }
    
    private int rowForNode(Node n) {
        TreeNode tn = Visualizer.findVisualizer(n);
        if (tn != null) {
            ArrayList<TreeNode> al = new ArrayList<TreeNode> ();
            while (tn != null) {
                al.add(tn);
                tn = tn.getParent();
            }
            Collections.reverse(al);
            TreePath tp = new TreePath(al.toArray());
            int row = outline.getLayoutCache().getRowForPath(tp);
            return row;
        }
        return -1;
    }

    @Override
    public Class getColumnClass(int column) {
        return Node.Property.class;
    }

    @Override
    public int getColumnCount() {
        return prop.length;
    }

    @Override
    public String getColumnName(int column) {
        assert column < prop.length : column + " must be bellow " + prop.length;
        if (names [column] == null) {
            String n = prop[column].getDisplayName ();
            JLabel l = new JLabel ();
            Mnemonics.setLocalizedText (l, n);
            names [column] = l.getText ();
        }
        return names [column];
    }

    public String getShortDescription (int column) {
        assert column < prop.length : column + " must be bellow " + prop.length;
        if (descs [column] == null) {
            String n = prop[column].getShortDescription ();
            JLabel l = new JLabel ();
            Mnemonics.setLocalizedText (l, n);
            descs [column] = l.getText ();
        }
        return descs [column];
    }
    
    public void setShortDescription(int column, String descr) {
        assert column < prop.length : column + " must be bellow " + prop.length;
        prop[column].setShortDescription(descr);
        descs[column] = null;
    }

    public String getRawColumnName (int column) {
        return prop[column].getDisplayName();
    }

    @Override
    public Object getValueFor(Object node, int column) {
        Node n = Visualizer.findNode(node);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + node + " of class " + node.getClass().getName());
        }
        PropertyChangeListener cacheEntry = nodesListenersCache.get (n);
        if (cacheEntry == null) {
            PropertyChangeListener p = WeakListeners.propertyChange(pcl, n);
            nodesListenersCache.put(n, p);
            n.addPropertyChangeListener(p);
            NodeListener l = WeakListeners.create(NodeListener.class, nl, n);
            n.addNodeListener(l);
        }
        Node.Property theRealProperty = getPropertyFor(n, prop[column]);
        return theRealProperty;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        Node n = Visualizer.findNode(node);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + node + " of class " + node.getClass().getName());
        }
        Node.Property theRealProperty = getPropertyFor(n, prop[column]);
        if (theRealProperty != null) {
            return theRealProperty.canWrite();
        } else {
            return false;
        }
    }
    
    protected Node.Property getPropertyFor(Node node, Node.Property prop) {
        Node.PropertySet[] propSets = node.getPropertySets();

        for (int i = 0; i < propSets.length; i++) {
            Node.Property[] props = propSets[i].getProperties();

            for (int j = 0; j < props.length; j++) {
                if (prop.equals(props[j])) {
                    return props[j];
                }
            }
        }

        return null;
    }

    private boolean ignoreSetValue;

    void setIgnoreSetValue(boolean ignoreSetValue) {
        this.ignoreSetValue = ignoreSetValue;
    }

    @Override
    public void setValueFor(Object node, int column, Object value) {
        // Intentionally ignore this method when the cell editor components are
        // PropertyPanels that will propagate the change into the target
        // property object - no need to do anything in this method.
        //System.err.println("PropertiesRowModel.setValueFor("+node+", "+column+", "+value+") ignored = "+ignoreSetValue);
        if (ignoreSetValue) {
            return ;
        }
        // Otherwise someone is explicitly trying to set the value:
        Node n = Visualizer.findNode(node);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + node + " of class " + node.getClass().getName());
        }
        Node.Property theRealProperty = getPropertyFor(n, prop[column]);
        try {
            theRealProperty.setValue(value);
        } catch (IllegalAccessException ex) {
            processThrowable(ex, theRealProperty.getDisplayName(), value);
        } catch (IllegalArgumentException ex) {
            processThrowable(ex, theRealProperty.getDisplayName(), value);
        } catch (InvocationTargetException ex) {
            processThrowable(ex.getTargetException(), theRealProperty.getDisplayName(), value);
        }
    }
    
    /** Processes <code>Throwable</code> thrown from <code>setAsText</code>
     * or <code>setValue</code> call on <code>editor</code>. Helper method.
     * Almost copied from PropUtils */
    private static void processThrowable(Throwable throwable, String title, Object newValue) {
        //Copied from old PropertyPanel impl
        if (throwable instanceof ThreadDeath) {
            throw (ThreadDeath) throwable;
        }

        String locMsg = Exceptions.findLocalizedMessage(throwable);

        if (locMsg != null
            && (throwable.getLocalizedMessage() != throwable.getMessage())) { //XXX See issue 34569

            String msg = NbBundle.getMessage(
                    PropertiesRowModel.class, "FMT_ErrorSettingValue", newValue, title); //NOI18N
            Exceptions.attachLocalizedMessage(throwable, msg);
        } else if (throwable instanceof NumberFormatException) {
            //Handle NFE's from the core sun.beans property editors w/o raising stack traces
            Exceptions.attachLocalizedMessage(throwable, NbBundle.getMessage(PropertiesRowModel.class, "FMT_BAD_NUMBER_FORMAT", newValue));
        }

        String msg = Exceptions.findLocalizedMessage(throwable);
        if (msg == null) {
            msg = NbBundle.getMessage(
                    PropertiesRowModel.class, "FMT_ErrorSettingValue", newValue, title); //NOI18N

        }
        NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(d);
    }

    public void setProperties(Node.Property[] newProperties) {
        prop = newProperties;
        names = new String [prop.length];
        descs = new String [prop.length];
    }
    
    /**
     * Of the parameter is of type Node.Property this methods
     * calls getValue on the property and returns the value.
     * If the parameter is something else <code>null</code>
     * is returned.
     */
    public static Object getValueFromProperty(Object property) {
        if (property instanceof Node.Property) {
            Node.Property prop = (Node.Property)property;
            try {
                return prop.getValue();
            } catch (Exception x) {
                ErrorManager.getDefault().getInstance(
                    PropertiesRowModel.class.getName()).notify(
                        ErrorManager.INFORMATIONAL, x);
            }
        }
        return null;
    }
    /**
     * Search the properties for given property name.
     * The returned value is the index of property: you
     * have to add 1 to make it the column index because the
     * column with index 0 is reserved for the tree!
     */
    private int columnForProperty(String propName) {
        for (int i = 0; i < prop.length; i++) {
            if (prop[i].getName().equals(propName))
                return i;
        }
        return -1;
    }

    Object getPropertyValue(String propName, int column) {
        return prop[column].getValue(propName);
    }

    final Node.Property[] getProperties() {
        return prop;
    }

    /**
     * Changes the value of the boolean property.
     */
    public static void toggleBooleanProperty(Node.Property<Boolean> p) {
        if (p.getValueType() == Boolean.class || p.getValueType() == Boolean.TYPE) {
            if (!p.canWrite()) {
                return;
            }
            try {
                Boolean val = p.getValue ();
                if (Boolean.FALSE.equals(val)) {
                    p.setValue(Boolean.TRUE);
                } else {
                    //This covers null multi-selections too
                    p.setValue(Boolean.FALSE);
                }
            } catch (Exception e1) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e1);
            }
        }
    }    
}
