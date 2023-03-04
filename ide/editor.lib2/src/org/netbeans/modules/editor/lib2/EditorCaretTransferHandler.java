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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Clipboard transfer handler for rectangular selection.
 * <br>
 * It overrides the original transfer handler during the rectangular selection.
 *
 * @author Miloslav Metelka
 */
public class EditorCaretTransferHandler extends TransferHandler {
    
    public static void install(JTextComponent c) {
        TransferHandler origHandler = c.getTransferHandler();
        if (!(origHandler instanceof EditorCaretTransferHandler)) {
            c.setTransferHandler(new EditorCaretTransferHandler(c.getTransferHandler()));
        }
    }
    
    public static void uninstall(JTextComponent c) {
        TransferHandler origHandler = c.getTransferHandler();
        if (origHandler instanceof EditorCaretTransferHandler) {
            c.setTransferHandler(((EditorCaretTransferHandler)origHandler).getDelegate());
        }
    }
    
    private static final DataFlavor RECTANGULAR_SELECTION_FLAVOR = new DataFlavor(RectangularSelectionData.class,
            NbBundle.getMessage(EditorCaretTransferHandler.class, "MSG_RectangularSelectionClipboardFlavor"));
    
    private static final DataFlavor MULTI_CARET_FLAVOR = new DataFlavor(MultiCaretData.class,
            NbBundle.getMessage(EditorCaretTransferHandler.class, "MSG_MultiCaretClipboardFlavor"));

    /** Boolean property defining whether selection is being rectangular in a particular text component. */
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N
    
    /** Boolean property defining whether paste is being requested to be in lines. */
    private static final String PASTE_LINES_PROPERTY = "clipboard-lines"; // NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.RectangularSelectionClipboardHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorCaretTransferHandler.class.getName());

    private final TransferHandler delegate;

    public EditorCaretTransferHandler(TransferHandler delegate) {
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
                    new RectangularTransferable(
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

        } else if (c instanceof JTextComponent &&
                ((JTextComponent)c).getCaret() instanceof EditorCaret &&
                ((EditorCaret)((JTextComponent)c).getCaret()).getCarets().size() > 1)
        {
            EditorCaret editorCaret = (EditorCaret) ((JTextComponent)c).getCaret();
            final JTextComponent tc = (JTextComponent) c;
            StringBuilder stringSelectionBuffer;
            String[] lines = null;
            AbstractDocument doc = (AbstractDocument) tc.getDocument();
            doc.readLock();
            try {
                CharSequence docText = DocumentUtilities.getText(doc);
                stringSelectionBuffer = new StringBuilder(100);
                List<CaretInfo> carets = editorCaret.getSortedCarets();
                lines = new String[carets.size()];
                boolean newline = false;
                for (int i = 0; i < carets.size(); i++) {
                    CaretInfo caret = carets.get(i);
                    if(!caret.isSelection()) continue;
                    int startOffset = caret.getSelectionStart();
                    int endOffset = caret.getSelectionEnd();
                    CharSequence lineSel = docText.subSequence(startOffset, endOffset);
                    if (newline) {
                        stringSelectionBuffer.append('\n');
                    } else {
                        newline = true;
                    }
                    stringSelectionBuffer.append(lineSel);
                    lines[i] = lineSel.toString();
                }
            } finally {
                doc.readUnlock();
            }

            clip.setContents(
                    new MultiCaretTransferable(
                        new StringSelection(stringSelectionBuffer.toString()),
                        new MultiCaretData(lines)),
                    null);

            if (action == TransferHandler.MOVE) {
                List<CaretInfo> carets = editorCaret.getSortedCarets();
                for (CaretInfo caret : carets) {
                    if (caret.isSelection()) {
                        // remove selection
                        final int dot = caret.getDot();
                        final int mark = caret.getMark();
                        try {
                            if (RectangularSelectionUtils.isRectangularSelection(tc)) {
                                RectangularSelectionUtils.removeSelection(tc);
                                RectangularSelectionCaretAccessor.get().setRectangularSelectionToDotAndMark(editorCaret);
                            } else {
                                doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                            }
                        } catch (BadLocationException ble) {
                            LOG.log(Level.FINE, null, ble);
                        }
                    }
                }
            }
            return;
        } else { // No rectangular selection or multiple carets
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
            if (RectangularSelectionUtils.isRectangularSelection(tc)) {
                if (t.isDataFlavorSupported(RECTANGULAR_SELECTION_FLAVOR)) {
                    boolean result = false;
                    try {
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
                        result = true;
                    } catch (UnsupportedFlavorException | IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return result;
                } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
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
            } else {
                Caret caret = ((JTextComponent) c).getCaret();
                if (caret instanceof EditorCaret && ((EditorCaret) caret).getSortedCarets().size() > 1) {
                    final EditorCaret editorCaret = (EditorCaret) caret;
                    boolean result = false;
                    MultiCaretData multiCaretData = null;
                    if (t.isDataFlavorSupported(MULTI_CARET_FLAVOR)) {
                        try {
                            multiCaretData = (MultiCaretData) t.getTransferData(MULTI_CARET_FLAVOR);
                        } catch (UnsupportedFlavorException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    boolean pasteLines = false;
                    if(Boolean.TRUE.equals(((JTextComponent) c).getClientProperty(PASTE_LINES_PROPERTY))) {
                        if(multiCaretData != null) {
                            pasteLines = true;
                        } else {
                        try {
                            String content = (String) t.getTransferData(DataFlavor.stringFlavor); // There should be string flavor
                            if(content != null && content.length() > 0) {
                                multiCaretData = new MultiCaretData(content.split("\n", editorCaret.getCarets().size()));
                                pasteLines = true;
                            }
                        } catch (UnsupportedFlavorException | IOException ex) {
                            LOG.info("Problem getting stringFlavor for paste as Lines."); //NOI18N
                        }
                        }
                    }
                    if (multiCaretData != null && (pasteLines ||
                            multiCaretData.strings().length == editorCaret.getCarets().size())) {
                        final MultiCaretData content = multiCaretData;
                        final Document doc = ((JTextComponent) c).getDocument();
                        DocUtils.runAtomicAsUser(doc, new Runnable() {
                            @Override
                            public void run() {
                                List<CaretInfo> sortedCarets = editorCaret.getSortedCarets();
                                for (int i = 0; i < sortedCarets.size(); i++) {
                                    CaretInfo ci = sortedCarets.get(i);
                                    try {
                                        int startOffset = ci.getSelectionStart();
                                        int endOffset = ci.getSelectionEnd();
                                        if (startOffset != endOffset) {
                                            doc.remove(startOffset, endOffset - startOffset);
                                        }
                                        if (content.strings().length > i && content.strings()[i] != null && content.strings()[i].length() > 0) {
                                            doc.insertString(startOffset, content.strings()[i], null);
                                        }
                                    } catch (BadLocationException ex) {
                                        //ignore ?
                                    }
                                }
                            }
                        });
                    } else {
                        try {
                            final String content = (String) t.getTransferData(DataFlavor.stringFlavor); // There should be string flavor
                            final Document doc = ((JTextComponent) c).getDocument();
                            DocUtils.runAtomicAsUser(doc, new Runnable() {
                                @Override
                                public void run() {
                                    for (CaretInfo ci : editorCaret.getSortedCarets()) {
                                        try {
                                            int startOffset = ci.getSelectionStart();
                                            int endOffset = ci.getSelectionEnd();
                                            if (startOffset != endOffset) {
                                                doc.remove(startOffset, endOffset - startOffset);
                                            }
                                            if (content != null && content.length() > 0) {
                                                doc.insertString(startOffset, content, null);
                                            }
                                        } catch (BadLocationException ex) {
                                            //ignore ?
                                        }
                                    }
                                }
                            });
                            result = true;
                        } catch (UnsupportedFlavorException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return result;
                }
            }
        }
        return delegate.importData(support); // Regular importData()
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        return delegate.importData(comp, t);
    }

    private static List<String> splitByLines(String s) {
        StringTokenizer splitByLines = new StringTokenizer(s, "\n", false);
        List<String> lines = new ArrayList<String>();
        while (splitByLines.hasMoreTokens()) {
            lines.add(splitByLines.nextToken());
        }
        return lines;
    }

    private static final class RectangularTransferable implements Transferable {

        private final Transferable delegate;

        private final RectangularSelectionData rectangularSelectionData;

        private DataFlavor[] transferDataFlavorsCache;

        public RectangularTransferable(Transferable delegate, RectangularSelectionData rectangularSelectionData) {
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
    
    private static final class MultiCaretTransferable implements Transferable {

        private final Transferable delegate;

        private final MultiCaretData multiCaretData;

        private DataFlavor[] transferDataFlavorsCache;

        public MultiCaretTransferable(Transferable delegate, MultiCaretData multiCaretData) {
            this.delegate = delegate;
            this.multiCaretData = multiCaretData;
        }

        @Override
        public synchronized DataFlavor[] getTransferDataFlavors() {
            if (transferDataFlavorsCache != null) {
                return transferDataFlavorsCache;
            }
            DataFlavor[] flavors = delegate.getTransferDataFlavors();
            DataFlavor[] result = Arrays.copyOf(flavors, flavors.length + 1);
            result[flavors.length] = MULTI_CARET_FLAVOR;

            return transferDataFlavorsCache = result;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return MULTI_CARET_FLAVOR.equals(flavor) || delegate.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (MULTI_CARET_FLAVOR.equals(flavor)) {
                return multiCaretData;
            }
            return delegate.getTransferData(flavor);
        }
    }
    
    public static final class MultiCaretData {
        
        private final String[] strings;
        
        public MultiCaretData(String[] strings) {
            this.strings = strings;
        }
        
        /**
         * Strings containing rectangular selection (on particular selected line).
         */
        public String[] strings() {
            return strings;
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
