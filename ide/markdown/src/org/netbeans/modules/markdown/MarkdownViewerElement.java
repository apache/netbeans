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

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
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
        position = 1100
)
@Messages("LBL_MarkdownViewer=Preview")
public class MarkdownViewerElement implements MultiViewElement {

    private static final Logger LOG = Logger.getLogger(MarkdownViewerElement.class.getName());

    private final MarkdownDataObject dataObject;
    private transient JToolBar toolbar;

    private transient JComponent component;
    private transient JEditorPane viewer;

    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileChanged(FileEvent fe) {
            updateView();
        }
    
    };

    public MarkdownViewerElement(Lookup lookup) {
        dataObject = lookup.lookup(MarkdownDataObject.class);
    }

    @Override
    public JComponent getVisualRepresentation() {
        if (component == null) {
            viewer = new JEditorPane();
            viewer.setContentType("text/html");
            viewer.setEditable(false);
            component = new JScrollPane(viewer);
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
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return dataObject.getLookup();
    }

    @Override
    public void componentOpened() {
        dataObject.getPrimaryFile().addFileChangeListener(fcl);
        updateView();
    }

    @Override
    public void componentClosed() {
        dataObject.getPrimaryFile().removeFileChangeListener(fcl);
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
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
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Messages("TXT_MarkdownViewerElement_Error=<html>Something happened during markdown parsing.")
    private void updateView() {
        FileObject fo = dataObject.getPrimaryFile();
        if ((fo != null) && (viewer != null)) {
            MutableDataSet options = new MutableDataSet();
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            try {
                String html = renderer.render(parser.parse(fo.asText("UTF-8")));
                viewer.setText(html);
            } catch (IOException ex) {
                viewer.setText(Bundle.TXT_MarkdownViewerElement_Error());
                LOG.log(Level.WARNING, "Could not parse markdown!", ex);
            }
        }
    }
}
