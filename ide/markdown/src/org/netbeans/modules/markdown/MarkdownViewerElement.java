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
package org.netbeans.modules.markdown;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.markdown.ui.preview.MarkdownEditorKit;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;

/**
 *
 * @author lkishalmi
 */
@MultiViewElement.Registration(
        displayName = "#LBL_MarkdownViewer",
        iconBase = "org/netbeans/modules/markdown/markdown.png",
        mimeType = "text/x-markdown",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "MarkdownViewer",
        position = 2000
)
@Messages("LBL_MarkdownViewer=Preview")
public class MarkdownViewerElement implements MultiViewElement {

    private static final Logger LOG = Logger.getLogger(MarkdownViewerElement.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(MarkdownViewerElement.class);
    private static final int UPDATE_DELAY = 500;

    private final MarkdownDataObject dataObject;
    private transient JToolBar toolbar;

    private transient JComponent component;
    private transient JEditorPane viewer;
    private transient MultiViewElementCallback callback;

    static final DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(
                    AnchorLinkExtension.create(),
                    TablesExtension.create(),
                    TaskListExtension.create()
            ))
            .set(HtmlRenderer.INDENT_SIZE, 2)
            .set(HtmlRenderer.RENDER_HEADER_ID, true)
            .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "")
            // JEditorPane search for the name attribute
            .set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true)
            .set(AnchorLinkExtension.ANCHORLINKS_SET_ID, false)
            .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "")
            .set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "")
            // Make the table generation SWING Friendly
            .set(TablesExtension.COLUMN_SPANS, false)
            .set(TablesExtension.MIN_HEADER_ROWS, 1)
            .set(TablesExtension.MAX_HEADER_ROWS, 1)
            .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
            .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
            .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
            .toImmutable();

    final Parser parser = Parser.builder(OPTIONS).build();
    final HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();

    final DocumentListener dl = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updater.schedule(UPDATE_DELAY);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updater.schedule(UPDATE_DELAY);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updater.schedule(UPDATE_DELAY);
        }
    };

    private final Task updater = RP.create(MarkdownViewerElement.this::updateView);
    private StyledDocument source;

    public MarkdownViewerElement(Lookup lookup) {
        dataObject = lookup.lookup(MarkdownDataObject.class);
    }

    @Override
    public JComponent getVisualRepresentation() {
        if (component == null) {
            viewer = new JEditorPane();
            viewer.setEditorKit(new MarkdownEditorKit());
            viewer.setEditable(false);
            viewer.addHyperlinkListener(this::linkHandler);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(viewer), BorderLayout.CENTER);

            component = panel;
        }
        return component;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new JToolBar();
        }
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return callback.createDefaultActions();
    }

    @Override
    public Lookup getLookup() {
        return dataObject.getLookup();
    }

    @Override
    public void componentOpened() {
        FileObject fo = dataObject.getPrimaryFile();
        callback.updateTitle(fo.getNameExt());
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
        EditorCookie ec = getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            RP.post(() -> {
                try {
                    source = ec.openDocument();
                    source.addDocumentListener(dl);
                    updateView();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }

    @Override
    public void componentHidden() {
        if (source != null) {
            source.removeDocumentListener(dl);
        }
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Messages("TXT_MarkdownViewerElement_Error=<html>Something happened during markdown parsing.")
    public void updateView() {
        if ((source != null) && (viewer != null)) {
            try {
                Rectangle vis = viewer.getVisibleRect();

                String content = source.getText(0, source.getLength());
                String html = renderer.render(parser.parse(content));

                Reader htmlReader = new StringReader(html);
                HTMLEditorKit kit = (HTMLEditorKit) viewer.getEditorKit();

                HTMLDocument doc = (HTMLDocument) viewer.getDocument();

                // Would be better to create some diff and update the changed elemets
                doc.remove(0, doc.getLength());
                kit.read(htmlReader, doc, 0);

                viewer.scrollRectToVisible(vis);

            } catch (IOException ex) {
                viewer.setText(Bundle.TXT_MarkdownViewerElement_Error());
                LOG.log(Level.WARNING, "Could not parse markdown!", ex); //NOI18N
            } catch (BadLocationException ex) {
                viewer.setText(Bundle.TXT_MarkdownViewerElement_Error());
            }
        }
    }

    private void linkHandler(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String dsc = evt.getDescription();
            if ((evt.getURL() == null) && (dsc != null)) {
                String parts[] = dsc.split("#");
                if (parts.length == 1) {
                    //Probably a link to relative file
                    FileObject parent = dataObject.getPrimaryFile().getParent();
                    FileObject fo = parent.getFileObject(parts[0]);
                    if (fo != null) {
                        DataObject dat;
                        try {
                            dat = DataObject.find(fo);
                            OpenCookie open = dat.getLookup().lookup(OpenCookie.class);
                            open.open();
                        } catch (DataObjectNotFoundException ex) {
                            // Got unlucky do nothing
                        }
                    }
                } else {
                    if (parts[0].isEmpty() && !parts[1].isEmpty()) {
                        viewer.scrollToReference(parts[1]);
                    }
                }
            } else if (evt.getURL() != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(evt.getURL());
            }
        }
    }
}
