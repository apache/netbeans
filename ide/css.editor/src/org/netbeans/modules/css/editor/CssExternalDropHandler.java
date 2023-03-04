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
package org.netbeans.modules.css.editor;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.ExternalDropHandler;

/**
 *
 * @author marek
 */
@ServiceProvider(service = ExternalDropHandler.class, position = 750)
public class CssExternalDropHandler extends ExternalDropHandler {

    private static final Logger LOG = Logger.getLogger(CssExternalDropHandler.class.getName());

    private static DataFlavor uriListDataFlavor;

    private boolean containsLanguageAtOffset(final Document document, final int offset) {
        if (offset == -1) {
            return false;
        }
        final AtomicBoolean result = new AtomicBoolean();
        document.render(new Runnable() {

            @Override
            public void run() {
                TokenHierarchy<Document> th = TokenHierarchy.get(document);
                List<TokenSequence<?>> embeddedTokenSequences = th.embeddedTokenSequences(offset, true); //backward bias
                if (!embeddedTokenSequences.isEmpty()) {
                    TokenSequence<?> leaf = embeddedTokenSequences.get(embeddedTokenSequences.size() - 1);
                    if (leaf.language() == CssTokenId.language()) {
                        result.set(true);
                    }
                }
                embeddedTokenSequences = th.embeddedTokenSequences(offset, false); //fw bias
                if (!embeddedTokenSequences.isEmpty()) {
                    TokenSequence<?> leaf = embeddedTokenSequences.get(embeddedTokenSequences.size() - 1);
                    if (leaf.language() == CssTokenId.language()) {
                        result.set(true);
                    }
                }

            }

        });
        return result.get();
    }

    private int getLineEndOffset(JEditorPane pane, Point location) {
        int offset = pane.getUI().viewToModel(pane, location);
        try {
            return Utilities.getRowEnd((BaseDocument) pane.getDocument(), offset);
        } catch (BadLocationException ex) {
            //highly unlikely to happen
            Exceptions.printStackTrace(ex);
            return offset;
        }
    }

    @Override
    public boolean canDrop(DropTargetDragEvent e) {
        //check if the JEditorPane contains html document
        JEditorPane pane = findPane(e.getDropTargetContext().getComponent());
        if (pane == null) {
            return false;
        }
        int offset = getLineEndOffset(pane, e.getLocation());
        if (!containsLanguageAtOffset(pane.getDocument(), offset)) {
            return false;
        } else {
            //update the caret as the user drags the object
            //needs to be done explicitly here as QuietEditorPane doesn't call
            //the original Swings DropTarget which does this
            pane.setCaretPosition(offset);

            pane.requestFocusInWindow(); //pity we need to call this all the time when dragging, but  ExternalDropHandler don't handle dragEnter event

            return canDrop(e.getCurrentDataFlavors());
        }

    }

    @Override
    public boolean canDrop(DropTargetDropEvent e) {
        //check if the JEditorPane contains html document
        JEditorPane pane = findPane(e.getDropTargetContext().getComponent());
        if (pane == null) {
            return false;
        }
        int offset = getLineEndOffset(pane, e.getLocation());
        if (!containsLanguageAtOffset(pane.getDocument(), offset)) {
            return false;
        }

        //check if the dropped target is supported
        return canDrop(e.getCurrentDataFlavors());
    }

    private boolean canDrop(DataFlavor[] flavors) {
        for (int i = 0; null != flavors && i < flavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(flavors[i])
                    || getUriListDataFlavor().equals(flavors[i])) {

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleDrop(DropTargetDropEvent e) {
        Transferable t = e.getTransferable();
        if (null == t) {
            return false;
        }
        List<File> fileList = getFileList(t);
        if ((fileList == null) || fileList.isEmpty()) {
            return false;
        }

        //handle just the first file
        File file = fileList.get(0);
        FileObject target = FileUtil.toFileObject(file);
        if (file.isDirectory()) {
            return true; //as we previously claimed we canDrop() it so we need to say we've handled it even if did nothing.
        }

        JEditorPane pane = findPane(e.getDropTargetContext().getComponent());
        if (pane == null) {
            return false;
        }

        final BaseDocument document = (BaseDocument) pane.getDocument();
        FileObject current = DataLoadersBridge.getDefault().getFileObject(document);
        String relativePath = WebUtils.getRelativePath(current, target);

        final StringBuilder sb = new StringBuilder();

        //hardcoded support for common file types
        String mimeType = target.getMIMEType();
        switch (mimeType) { //NOI18N -- whole switch content
            case "text/css":
            case "text/less":
            case "text/scss":
                sb.append("@import \"").append(relativePath).append("\";");
                break;
            default:
                LOG.log(Level.INFO, "Dropping of files with mimetype {0} is not supported -  what would you like to generate? Let me know in the issue 219985 please. Thank you!", mimeType);
                return true;
        }

        //check if the line is white, and if not, insert a new line before the text
        final int offset = getLineEndOffset(pane, e.getLocation());

        final Indent indent = Indent.get(document);
        indent.lock();
        try {

            document.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        int ofs = offset;
                        if (!Utilities.isRowWhite(document, ofs)) {
                            document.insertString(ofs, "\n", null);
                            ofs++;
                        }
                        document.insertString(ofs, sb.toString(), null);

                        //reformat the line
                        final int from = Utilities.getRowStart(document, ofs);
                        final int to = Utilities.getRowEnd(document, ofs);

                        indent.reindent(from, to);

                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            });

        } finally {
            indent.unlock();
        }

        return true;
    }

    private JEditorPane findPane(Component component) {
        while (component != null) {
            if (component instanceof JEditorPane) {
                return (JEditorPane) component;
            }
            component = component.getParent();
        }
        return null;
    }

    //copied from org.netbeans.modules.openfile.DefaultExternalDropHandler
    private List<File> getFileList(Transferable t) {
        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                //windows & mac
                try {
                    return (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (InvalidDnDOperationException ex) { // #212390
                    LOG.log(Level.FINE, null, ex);
                }
            }
            if (t.isDataFlavorSupported(getUriListDataFlavor())) {
                //linux
                String uriList = (String) t.getTransferData(getUriListDataFlavor());
                return textURIListToFileList(uriList);
            }
        } catch (UnsupportedFlavorException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            // Ignore. Can be just "Owner timed out" from sun.awt.X11.XSelection.getData.
            LOG.log(Level.FINE, null, ex);
        }
        return null;
    }

    //copied from org.netbeans.modules.openfile.DefaultExternalDropHandler
    private DataFlavor getUriListDataFlavor() {
        if (null == uriListDataFlavor) {
            try {
                uriListDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            } catch (ClassNotFoundException cnfE) {
                //cannot happen
                throw new AssertionError(cnfE);
            }
        }
        return uriListDataFlavor;
    }

    //copied from org.netbeans.modules.openfile.DefaultExternalDropHandler
    private List<File> textURIListToFileList(String data) {
        List<File> list = new ArrayList<>(1);
        for (StringTokenizer st = new StringTokenizer(data, "\r\n\u0000");
                st.hasMoreTokens();) {
            String s = st.nextToken();
            if (s.startsWith("#")) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = org.openide.util.Utilities.toFile(uri);
                list.add(file);
            } catch (URISyntaxException | IllegalArgumentException e) {
                // malformed URI
            }
        }
        return list;
    }

}
