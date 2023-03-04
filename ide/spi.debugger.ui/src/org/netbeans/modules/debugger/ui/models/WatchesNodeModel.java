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

package org.netbeans.modules.debugger.ui.models;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.WatchesReader;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.DnDNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel implements ExtendedNodeModel, DnDNodeModel, CheckNodeModel {

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/watch_16.png";

    private Vector listeners = new Vector ();

    private static Watch getWatch(Object o) {
        if (o instanceof Watch) {
            return (Watch) o;
        //} else if (o instanceof Watch.Provider) {
        //    return ((Watch.Provider) o).getWatch();
        } else try {
            if (o.getClass().getMethod("getWatch") != null) {
                return (Watch) o.getClass().getMethod("getWatch").invoke(o);
            } else {
                return null;
            }
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle(WatchesNodeModel.class).getString("CTL_WatchesModel_Column_Name_Name");
        if (o instanceof WatchesTreeModel.EmptyWatch) {
            return "<_html><font color=\"#808080\">&lt;" + // [TODO] <_html> tag used as workaround, see TreeModelNode.setName()
                        NbBundle.getBundle (WatchesNodeModel.class).getString("CTL_WatchesModel_Empty_Watch_Hint") +
                        "&gt;</font></html>";
        }
        Watch w = getWatch(o);
        if (w != null) {
            return w.getExpression ();
        }
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return TreeModel.ROOT;
        if (o instanceof WatchesTreeModel.EmptyWatch) {
            return NbBundle.getMessage(WatchesNodeModel.class, "TTP_NewWatch");
        }
        Watch w = getWatch(o);
        if (w != null) {
            return w.getExpression () + NbBundle.getBundle(WatchesNodeModel.class).getString("CTL_WatchesModel_Column_NameNoContext_Desc");
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT)
            return WATCH;
        if (node instanceof WatchesTreeModel.EmptyWatch) {
            return null;
        }
        if (getWatch(node) != null)
            return WATCH;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    public boolean canRename(Object node) throws UnknownTypeException {
        return (node instanceof WatchesTreeModel.EmptyWatch) || getWatch(node) != null;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return getWatch(node) != null;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return getWatch(node) != null;
    }

    public Transferable clipboardCopy(Object node) throws IOException,
                                                          UnknownTypeException {
        return new StringSelection(getWatch(node).getExpression());
    }

    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        return new StringSelection(getWatch(node).getExpression());
    }

    public PasteType[] getPasteTypes(final Object node, final Transferable t) throws UnknownTypeException {
        if (node != TreeModel.ROOT && getWatch(node) == null) {
            return null;
        }
        DataFlavor[] flavors = t.getTransferDataFlavors();
        final DataFlavor textFlavor = DataFlavor.selectBestTextFlavor(flavors);
        if (textFlavor != null) {
            return new PasteType[] { new PasteType() {

                public Transferable paste() {
                    try {
                        java.io.Reader r = textFlavor.getReaderForText(t);
                        java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(1000);
                        r.read(cb);
                        cb.flip();
                        Watch w = getWatch(node);
                        if (w != null) {
                            w.setExpression(cb.toString());
                            //fireModelChange(new ModelEvent.NodeChanged(WatchesNodeModel.this, node));
                        } else {
                            // Root => add a new watch
                            DebuggerManager.getDebuggerManager().createWatch(cb.toString());
                        }
                    } catch (Exception ex) {}
                    return null;
                }
            } };
        } else {
            return null;
        }
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        if (node instanceof Watch) {
            ((Watch)node).setExpression(name);
            return;
        }
        if (node instanceof WatchesTreeModel.EmptyWatch) {
            ((WatchesTreeModel.EmptyWatch)node).setExpression(name);
            return;
        }
        throw new UnknownTypeException(node);
    }

    public int getAllowedDragActions() {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    public int getAllowedDropActions() {
        return DnDConstants.ACTION_COPY;
    }

    public int getAllowedDropActions(Transferable t) {
        if (t != null && t.isDataFlavorSupported(new DataFlavor(Watch.class, null))) {
            return DnDConstants.ACTION_COPY_OR_MOVE;
        } else {
            return DnDConstants.ACTION_COPY;
        }
    }

    public Transferable drag(Object node) throws IOException,
                                                 UnknownTypeException {
        Watch w = getWatch(node);
        if (w != null) {
            return new WatchSelection(w);
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public PasteType getDropType(final Object node, final Transferable t, int action,
                                 final int index) throws UnknownTypeException {
        //System.err.println("\n\ngetDropType("+node+", "+t+", "+action+", "+index+")");
        DataFlavor[] flavors = t.getTransferDataFlavors();
        final DataFlavor textFlavor = DataFlavor.selectBestTextFlavor(flavors);
        //System.err.println("Text Flavor = "+textFlavor);
        if (textFlavor != null) {
            return new PasteType() {

                public Transferable paste() {
                    String watchExpression;
                    try {
                        java.io.Reader r = textFlavor.getReaderForText(t);
                        java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(1000);
                        r.read(cb);
                        cb.flip();
                        watchExpression = cb.toString();
                    } catch (Exception ex) {
                        return t;
                    }
                    Watch w = getWatch(node);
                    if (w != null) {
                        w.setExpression(watchExpression);
                        //fireModelChange(new ModelEvent.NodeChanged(WatchesNodeModel.this, node));
                    } else {
                        // Root => add a new watch
                        if (index < 0) {
                            DebuggerManager.getDebuggerManager().createWatch(watchExpression);
                        } else {
                            DebuggerManager.getDebuggerManager().createWatch(
                                    Math.min(index, DebuggerManager.getDebuggerManager().getWatches().length),
                                    watchExpression);
                        }
                    }
                    return t;
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public boolean isCheckable(Object node) throws UnknownTypeException {
        return getWatch(node) != null;
    }

    @Override
    public boolean isCheckEnabled(Object node) throws UnknownTypeException {
        return getWatch(node) != null;
    }

    @Override
    public Boolean isSelected(Object node) throws UnknownTypeException {
        Watch w = getWatch(node);
        if (w != null) {
            return w.isEnabled();
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
        Watch w = getWatch(node);
        if (w != null) {
            w.setEnabled(selected);
        } else {
            throw new UnknownTypeException(node);
        }
    }

    private static  class WatchSelection implements Transferable, ClipboardOwner {

        private static final int STRING = 0;
        private static final int PLAIN_TEXT = 1;
        private static final int WATCH = 2;

        private static final DataFlavor[] flavors = {
            DataFlavor.stringFlavor,
            DataFlavor.plainTextFlavor, // deprecated
            new DataFlavor(Watch.class, "Watch")
        };

        private Watch watch;
        private String str;

        /**
         * Creates a <code>Transferable</code> capable of transferring
         * the specified <code>Watch</code>.
         */
        public WatchSelection(Watch watch) {
            this.watch = watch;
            this.str = watch.getExpression();
        }

        /**
         * Returns an array of flavors in which this <code>Transferable</code>
         * can provide the data.
         */
        public DataFlavor[] getTransferDataFlavors() {
            // returning flavors itself would allow client code to modify
            // our internal behavior
            return (DataFlavor[])flavors.clone();
        }

        /**
         * Returns whether the requested flavor is supported by this
         * <code>Transferable</code>.
         *
         * @throws NullPointerException if flavor is <code>null</code>
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavor.equals(flavors[i])) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the <code>Transferable</code>'s data in the requested
         * <code>DataFlavor</code> if possible.
         * @param flavor the requested flavor for the data
         * @return the data in the requested flavor, as outlined above
         * @throws UnsupportedFlavorException if the requested data flavor is
         *         not supported.
         * @throws IOException if an IOException occurs while retrieving the data.
         * @throws NullPointerException if flavor is <code>null</code>
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
                                                                IOException {
            if (flavor.equals(flavors[STRING])) {
                return (Object)str;
            } else if (flavor.equals(flavors[PLAIN_TEXT])) {
                return new StringReader(str == null ? "" : str);
            } else if (flavor.equals(flavors[WATCH])) {
                return watch;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }
    }

}
