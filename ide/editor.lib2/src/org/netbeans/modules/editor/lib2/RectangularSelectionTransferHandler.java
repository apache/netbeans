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
package org.netbeans.modules.editor.lib2;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Clipboard transfer handler for rectangular selection.
 * <br>
 * It overrides the original transfer handler during the rectangular selection.
 *
 * @author Miloslav Metelka
 * @deprecated replaced by {@link EditorCaretTransferHandler}, kept in place for BaseCaret
 */
@Deprecated
public class RectangularSelectionTransferHandler extends TransferHandler {
    
    public static void install(JTextComponent c) {
        TransferHandler origHandler = c.getTransferHandler();
        if (!(origHandler instanceof RectangularSelectionTransferHandler)) {
            c.setTransferHandler(new RectangularSelectionTransferHandler(c.getTransferHandler()));
        }
    }
    
    public static void uninstall(JTextComponent c) {
        TransferHandler origHandler = c.getTransferHandler();
        if (origHandler instanceof RectangularSelectionTransferHandler) {
            c.setTransferHandler(((RectangularSelectionTransferHandler)origHandler).getDelegate());
        }
    }
    
    private static final DataFlavor RECTANGULAR_SELECTION_FLAVOR = new DataFlavor(RectangularSelectionData.class,
            NbBundle.getMessage(RectangularSelectionTransferHandler.class, "MSG_RectangularSelectionClipboardFlavor"));

    /** Boolean property defining whether selection is being rectangular in a particular text component. */
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.RectangularSelectionClipboardHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(RectangularSelectionTransferHandler.class.getName());


    private final TransferHandler delegate;

    public RectangularSelectionTransferHandler(TransferHandler delegate) {
        this.delegate = delegate;
    }
    
    TransferHandler getDelegate() {
        return delegate;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return delegate.canImport(support);
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return delegate.canImport(comp, transferFlavors);
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        try {
            java.lang.reflect.Method method = delegate.getClass().getDeclaredMethod(
                    "createTransferable", // NOI18N
                    new Class[]{javax.swing.JComponent.class});
            method.setAccessible(true);

            return (Transferable) method.invoke(delegate, new Object[]{c});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        delegate.exportAsDrag(comp, e, action);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        try {
            java.lang.reflect.Method method = delegate.getClass().getDeclaredMethod(
                    "exportDone", // NOI18N
                    new Class[]{javax.swing.JComponent.class, Transferable.class, int.class});
            method.setAccessible(true);
            method.invoke(delegate, new Object[]{source, data, Integer.valueOf(action)});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void exportToClipboard(JComponent c, Clipboard clip, int action) throws IllegalStateException {
        List<Position> regions;
        if (c instanceof JTextComponent &&
                (Boolean.TRUE.equals(c.getClientProperty(RECTANGULAR_SELECTION_PROPERTY))) &&
                (regions = RectangularSelectionUtils.regionsCopy(c)) != null)
        {
            final JTextComponent tc = (JTextComponent) c;
            String[] data;
            StringBuilder stringSelectionBuffer;
            AbstractDocument doc = (AbstractDocument) tc.getDocument();
            doc.readLock();
            try {
                // Cannot delegate to overriden transfer handler - at least not the JTextComponent.DefaultTransferHandler
                // because it would:
                // for COPY action whole selection would be copied which is wrong
                // for MOVE selection it would in addition remove <dot,mark> portion of the document.
                // Therefore handle string selection here explicitly.
                CharSequence docText = DocumentUtilities.getText(doc);
                stringSelectionBuffer = new StringBuilder(100);
                int size = regions.size();
                data = new String[size >>> 1];
                for (int i = 0; i < size; i++) {
                    Position startPos = regions.get(i++);
                    Position endPos = regions.get(i);
                    CharSequence lineSel = docText.subSequence(startPos.getOffset(), endPos.getOffset());
                    int halfI = (i >>> 1);
                    if (halfI != 0) {
                        stringSelectionBuffer.append('\n');
                    }
                    stringSelectionBuffer.append(lineSel);
                    data[halfI] = lineSel.toString();
                }
            } finally {
                doc.readUnlock();
            }

            clip.setContents(
                    new WrappedTransferable(
                        new StringSelection(stringSelectionBuffer.toString()),
                        new RectangularSelectionData(data)),
                    null);

            if (action == TransferHandler.MOVE) {
                try {
                    RectangularSelectionUtils.removeSelection(doc, regions);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return;

        } else { // No rectangular selection
            delegate.exportToClipboard(c, clip, action);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return delegate.getSourceActions(c);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return delegate.getVisualRepresentation(t);
    }

    @Override
    public boolean importData(TransferSupport support) {
        Component c = support.getComponent();
        Transferable t = support.getTransferable();
        if (c instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) c;
            if (t.isDataFlavorSupported(RECTANGULAR_SELECTION_FLAVOR) && c instanceof JTextComponent) {
                boolean result = false;
                try {
                    if (Boolean.TRUE.equals(tc.getClientProperty(RECTANGULAR_SELECTION_PROPERTY))) {
                        final RectangularSelectionData data = (RectangularSelectionData) t.getTransferData(RECTANGULAR_SELECTION_FLAVOR);
                        final List<Position> regions = RectangularSelectionUtils.regionsCopy(tc);
                        final Document doc = tc.getDocument();
                        DocUtils.runAtomicAsUser(doc, new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    RectangularSelectionUtils.removeSelection(doc, regions);
                                    String[] strings = data.strings();
                                    for (int i = 0; i < strings.length; i++) {
                                        int doubleI = (i << 1);
                                        if (doubleI >= regions.size()) {
                                            break;
                                        }
                                        Position linePos = regions.get(doubleI);
                                        doc.insertString(linePos.getOffset(), strings[i], null);
                                    }
                                } catch (BadLocationException ex) {
                                    // Ignore
                                }
                            }

                        });

                    } else { // Regular selection
                        String s = (String) t.getTransferData(DataFlavor.stringFlavor); // There should be string flavor
                        if (s != null) {
                            tc.replaceSelection("");
                        }
                    }
                    result = true;

                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                return result;

            } else if (RectangularSelectionUtils.isRectangularSelection(tc)) {
                if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        // Paste individual lines into rectangular selection
                        String s = (String) t.getTransferData(DataFlavor.stringFlavor); // There should be string flavor
                        final List<String> dataLines = splitByLines(s);
                        final List<Position> regions = RectangularSelectionUtils.regionsCopy(tc);
                        final Document doc = tc.getDocument();
                        final int dataLinesSize = dataLines.size();
                        final int regionsSize = regions.size();
                        if (dataLinesSize > 0 && regionsSize > 0) { // Otherwise do nothing
                            DocUtils.runAtomicAsUser(doc, new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        RectangularSelectionUtils.removeSelection(doc, regions);
                                        int dataLineIndex = 0;
                                        for (int i = 0; i < regionsSize; i += 2) {
                                            Position linePos = regions.get(i);
                                            doc.insertString(linePos.getOffset(),
                                                    dataLines.get(dataLineIndex++), null);
                                            if (dataLineIndex >= dataLinesSize) {
                                                dataLineIndex = 0;
                                            }
                                        }
                                    } catch (BadLocationException ex) {
                                        // Ignore
                                    }
                                }
                            });
                            return true;
                        } else {
                            return false;
                        }
                    } catch (UnsupportedFlavorException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return delegate.importData(support); // Regular importData()
    }

    private static List<String> splitByLines(String s) {
        StringTokenizer splitByLines = new StringTokenizer(s, "\n", false);
        List<String> lines = new ArrayList<String>();
        while (splitByLines.hasMoreTokens()) {
            lines.add(splitByLines.nextToken());
        }
        return lines;
    }

    private static final class WrappedTransferable implements Transferable {

        private final Transferable delegate;

        private final RectangularSelectionData rectangularSelectionData;

        private DataFlavor[] transferDataFlavorsCache;

        public WrappedTransferable(Transferable delegate, RectangularSelectionData rectangularSelectionData) {
            this.delegate = delegate;
            this.rectangularSelectionData = rectangularSelectionData;
        }

        @Override
        public synchronized DataFlavor[] getTransferDataFlavors() {
            if (transferDataFlavorsCache != null) {
                return transferDataFlavorsCache;
            }
            DataFlavor[] flavors = delegate.getTransferDataFlavors();
            DataFlavor[] result = Arrays.copyOf(flavors, flavors.length + 1);
            result[flavors.length] = RECTANGULAR_SELECTION_FLAVOR;

            return transferDataFlavorsCache = result;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return RECTANGULAR_SELECTION_FLAVOR.equals(flavor) || delegate.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (RECTANGULAR_SELECTION_FLAVOR.equals(flavor)) {
                return rectangularSelectionData;
            }
            return delegate.getTransferData(flavor);
        }
    }

    public static final class RectangularSelectionData {

        /**
         * Strings containing rectangular selection (on particular selected line).
         */
        private final String[] strings;
        
        public RectangularSelectionData(String[] strings) {
            this.strings = strings;
        }
        
        public String[] strings() {
            return strings;
        }

    }

}
