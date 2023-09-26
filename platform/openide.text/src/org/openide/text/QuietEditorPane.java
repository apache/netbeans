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

package org.openide.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.im.InputContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.ExternalDropHandler;
import org.openide.windows.TopComponent;

/** performance trick - 18% of time saved during open of an editor
*
* @author Ales Novak
*/
final class QuietEditorPane extends JEditorPane {

    private static final Logger LOG = Logger.getLogger(QuietEditorPane.class.getName());
    
    static DataFlavor constructActiveEditorDropFlavor() {
        try {
            return new DataFlavor("text/active_editor_flavor;class=org.openide.text.ActiveEditorDrop", // NOI18N
                    "Active Editor Flavor", // XXX missing I18N!
                    QuietEditorPane.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    static final int FIRE = 0x1;
    static final int PAINT = 0x2;
    static final int ALL = FIRE | PAINT;

    // #21120. Caret was null while serializing CloneableEditor.

    /** Saves last position of caret when, doing it's UI reinstallation. */
    private int lastPosition = -1;

    /** is firing of events enabled? */
    int working = 0; // #132669, see CloneableEditor.DoInitialize.initDocument (line 424)
    
    /** determines scroll unit */
    private int fontHeight;
    private int charWidth;

    /**
     * consturctor sets the initial values for horizontal
     * and vertical scroll units.
     * also listenes for font changes.
     */
    public QuietEditorPane() {
        setFontHeightWidth(getFont());
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        AccessibleContext ctx = super.getAccessibleContext();
        if (ctx != null) {
            // Lazily set the accessible name and desc
            // since JEditorPane.AccessibleJEditorPane resp. AccessibleJTextComponent
            // attaches document listener which prevents cloned JEPs from being GCed.
            ctx.setAccessibleName(
                    NbBundle.getMessage(CloneableEditor.class, "ACS_CloneableEditor_QuietEditorPane", this.getName())
            );
            ctx.setAccessibleDescription(
                    NbBundle.getMessage(
                            CloneableEditor.class, "ACSD_CloneableEditor_QuietEditorPane",
                            this.getName()
                    )
            );
        }
        return ctx;
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        setFontHeightWidth(getFont());
    }


    private void setFontHeightWidth(Font font) {
        FontMetrics metrics=getFontMetrics(font);
        fontHeight=metrics.getHeight();
        charWidth=metrics.charWidth('m');
    }

    /**
     * fix for #38139. 
     * returns height of a line for vertical scroll unit
     * or width of a widest char for a horizontal scroll unit
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
            case SwingConstants.VERTICAL:
                return fontHeight;
            case SwingConstants.HORIZONTAL:
                return charWidth;
            default:
                throw new IllegalArgumentException("Invalid orientation: " +orientation);
        }
    }
    
    @Override
    public void setDocument(Document doc) {
        super.setDocument(doc);
        
        // Setting DelegatingTransferHandler, where CallbackTransferable will
        // be handled in importData method. 
        // For more details, please refer issue #53439        
        if (doc != null){
            TransferHandler thn = getTransferHandler();
            if( !(thn instanceof DelegatingTransferHandler) ) {
                DelegatingTransferHandler dth = new DelegatingTransferHandler(thn);
                setTransferHandler(dth);
            }

            DropTarget currDt = getDropTarget();
            if( !(currDt instanceof DelegatingDropTarget ) ) {
                DropTarget dt = new DelegatingDropTarget( currDt );
                setDropTarget( dt );
            }
        }
    }
    
    public void setWorking(int x) {
        working = x;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("QEP@" + Integer.toHexString(System.identityHashCode(this)) //NOI18N
                + " firing is " + ((working & FIRE) == 0 ? "OFF" : "ON")); //NOI18N
        }
    }

    // #143368 - no caret during DnD in HTML files. It was caused by swalowing some
    // property change events that are fired when setting transferables, etc.
    // The list of 'expensive' properties is just what I think is expensive. It's not based
    // on any measurements. Use -Dtryme.args="-J-Dorg.netbeans.QuietEditorPane.level=FINE" to
    // see in the log file what property chenges are swallowed.
    // If making changes to the list make sure to check on #132669.
    private static final Set<String> EXPENSIVE_PROPERTIES = new HashSet<String>(Arrays.asList(
            "document", //NOI18N
            "editorKit", //NOI18N
            "keymap" //NOI18N
    ));

    public @Override void firePropertyChange(String s, Object val1, Object val2) {
        if ((working & FIRE) != 0 || s == null || !EXPENSIVE_PROPERTIES.contains(s)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("QEP@" + Integer.toHexString(System.identityHashCode(this)) //NOI18N
                    + " firing '" + s + "' change event;" //NOI18N
                    + " firing is " + ((working & FIRE) == 0 ? "OFF" : "ON")); //NOI18N
            }
            super.firePropertyChange(s, val1, val2);

        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("QEP@" + Integer.toHexString(System.identityHashCode(this)) //NOI18N
                + " suppressed '" + s + "' change event;" //NOI18N
                + " firing is OFF"); //NOI18N
        }
    }

    /** Overrides superclass method, to keep old caret position.
     * While is reinstallation of UI in progress, there
     * is a gap between the uninstallUI
     * and intstallUI when caret set to <code>null</code>. */
    @Override
    public void setCaret(Caret caret) {
        if (caret == null) {
            Caret oldCaret = getCaret();

            if (oldCaret != null) {
                lastPosition = oldCaret.getDot();
            }
        }

        super.setCaret(caret);
    }

    /** Gets the last caret position, for the case the serialization
     * is done during the time of pane UI reinstallation. */
    int getLastPosition() {
        return lastPosition;
    }

    @Override
    public void revalidate() {
        if ((working & PAINT) != 0) {
            super.revalidate();
        }
    }

    @Override
    public void repaint() {
        if ((working & PAINT) != 0) {
            super.repaint();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        final InputContext currentInputContext = getInputContext();

        Runnable w = new Runnable() {
            @Override
            public void run() {
                try {
                    //#191248: the current component is held through InputContext.{inputMethodWindowContext,previousInputMethod}
                    //and/or CompositionAreaHandler - trying to clear:
                    Class<?> inputContext = Class.forName("sun.awt.im.InputContext", false, QuietEditorPane.class.getClassLoader());
                    Field inputMethodWindowContext = inputContext.getDeclaredField("inputMethodWindowContext");
                    Field previousInputMethod = inputContext.getDeclaredField("previousInputMethod");

                    inputMethodWindowContext.setAccessible(true);
                    previousInputMethod.setAccessible(true);

                    if (currentInputContext == inputMethodWindowContext.get(null)) {
                        inputMethodWindowContext.set(null, null);
                        previousInputMethod.set(null, null);
                    }

                    Class<?> inputMethodContext = Class.forName("sun.awt.im.InputMethodContext", false, QuietEditorPane.class.getClassLoader());
                    Method getCompositionAreaHandler = inputMethodContext.getDeclaredMethod("getCompositionAreaHandler", boolean.class);

                    getCompositionAreaHandler.setAccessible(true);

                    getCompositionAreaHandler.invoke(currentInputContext, false);
                } catch (NoSuchFieldException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (InvocationTargetException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (NoSuchMethodException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (IllegalAccessException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (SecurityException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.FINE, null, ex);
                } catch (RuntimeException ex) {
                    if ("java.lang.reflect.InaccessibleObjectException".equals(ex.getClass().getName())) {
                        LOG.log(Level.FINE, null, ex); // On JDK9 the patch is not currently working until a better solution is found
                    } else {
                        throw ex; // Re-throw all other runtime exceptions
                    }
                    
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            w.run();
        } else {
            SwingUtilities.invokeLater(w);
        }
    }

/* Commented out since virtual height is implemented in the editor (in GapDocumentView)
    @Override
    public Dimension getPreferredSize() {
        //Avoid always typing at the bottom of the screen by
        //adding some virtual height, so the last line of the file
        //can always be scrolled up to the middle of the viewport
        Dimension result = super.getPreferredSize();
        JViewport port = (JViewport) SwingUtilities.getAncestorOfClass(
                JViewport.class, this);
        if (port != null) {
            int viewHeight = port.getExtentSize().height;
            if (result.height > viewHeight) {
                result.height += viewHeight / 2;
            }
        }
        return result;
    }
*/

    /**
     * Delegating TransferHandler.
     * The main purpose is hooking on importData method where CallbackTransferable
     * is handled. For more details, please refer issue #53439
     */    
    private class DelegatingTransferHandler extends TransferHandler{
        
        TransferHandler delegator;
        
        public DelegatingTransferHandler(TransferHandler delegator){
            this.delegator = delegator;
        }
        
        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            delegator.exportAsDrag(comp, e, action);
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            delegator.exportToClipboard(comp, clip, action);
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            return delegator.importData(comp, t);
        }

        @Override
        public boolean importData(TransferSupport t) {
            try {
                if (t.isDataFlavorSupported(ActiveEditorDrop.FLAVOR)){
                    Object obj = t.getTransferable().getTransferData(ActiveEditorDrop.FLAVOR);
                    final JComponent comp = (JComponent) t.getComponent();
                    if (obj instanceof ActiveEditorDrop && comp instanceof JTextComponent){
                        boolean success = false;
                        try {
                            success = ((ActiveEditorDrop)obj).handleTransfer((JTextComponent)comp);
                        }
                        finally {
                            requestFocus(comp);
                        }
                        return success;
                    }
                }
            } catch (Exception exc){
                exc.printStackTrace();
            }
            return delegator.importData(t);
        }

        private void requestFocus(JComponent comp) {
            Container container = SwingUtilities.getAncestorOfClass(TopComponent.class, comp);
            if (container != null) {
                ((TopComponent)container).requestActive();
            }
            else {
                Component f = comp;
                do {
                    f = f.getParent();
                    if (f instanceof Frame) {
                        break;
                    }
                } while (f != null);
                if (f != null) {
                    f.requestFocus();
                }
                comp.requestFocus();
            }
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return delegator.canImport(comp, transferFlavors);
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            DataFlavor[] transferFlavors = support.getDataFlavors();
            for (int i=0; i<transferFlavors.length; i++){
                if (transferFlavors[i] == ActiveEditorDrop.FLAVOR){
                    return true;
                }
            }
            return delegator.canImport(support);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return delegator.getSourceActions(c);
        }

        @Override
        public Icon getVisualRepresentation(Transferable t) {
            return delegator.getVisualRepresentation(t);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "exportDone",  // NOI18N
                    new Class<?>[] {javax.swing.JComponent.class, Transferable.class, int.class});
                method.setAccessible(true);
                method.invoke(delegator, new Object[] {source, data, new Integer(action)});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        protected Transferable createTransferable(JComponent comp) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "createTransferable", // NOI18N
                    new Class<?>[] {javax.swing.JComponent.class});
                method.setAccessible(true);
                return (Transferable)method.invoke(delegator, new Object[] {comp});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return null;
	}
    }
    
     private class DelegatingDropTarget extends DropTarget implements UIResource {

        private final DropTarget orig;
        private boolean isDragging = false;

        public DelegatingDropTarget(DropTarget orig) {
            this.orig = orig;
        }

        @Override
        public void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
            //#131830: It is to avoid NPE on JDK 1.5
            orig.removeDropTargetListener(dtl);
            orig.addDropTargetListener(dtl);
        }

        @Override
        public void removeDropTargetListener(DropTargetListener dtl) {
            orig.removeDropTargetListener(dtl);
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            Collection<? extends ExternalDropHandler> handlers = Lookup.getDefault().lookupAll(ExternalDropHandler.class);
            for (ExternalDropHandler handler : handlers) {
                if (handler.canDrop(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    isDragging = false;
                    return;
                }
            }

            orig.dragEnter(dtde);
            isDragging = true;
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            if (isDragging) {
                orig.dragExit(dte);
            }
            isDragging = false;
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            Collection<? extends ExternalDropHandler> handlers = Lookup.getDefault().lookupAll(ExternalDropHandler.class);
            for (ExternalDropHandler handler : handlers) {
                if (handler.canDrop(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    isDragging = false;
                    return;
                }
            }

            orig.dragOver(dtde);
            isDragging = true;

        }

        @Override
        public void drop(DropTargetDropEvent e) {
            Collection<? extends ExternalDropHandler> handlers = Lookup.getDefault().lookupAll(ExternalDropHandler.class);
            for (ExternalDropHandler handler : handlers) {
                if (handler.canDrop(e)) {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    boolean dropped = handler.handleDrop(e);
                    if(!dropped) {
                        continue; //try next ExternalDropHandler
                    }
                    e.dropComplete(true);
                    isDragging = false;
                    return;
                }
            }
            orig.drop(e);
            isDragging = false;
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            if (isDragging) {
                orig.dropActionChanged(dtde);
            }
        }
    }
}
