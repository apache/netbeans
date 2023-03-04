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
package org.netbeans.modules.xml.text.breadcrumbs;

import java.awt.Image;
import java.beans.BeanInfo;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import static org.netbeans.modules.xml.text.structure.XMLConstants.*;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Monitors a single {@link JTextComponent} and tracks its caret. Maintains a weak cache
 * of BreadcrumbElemens for indiviudal DocumentElements, mainly to preserve identity. 
 * The Breadcrumbs Controller checks that the parent really owns child, so completely
 * lightweight BreadcrumbElements are not possible.
 * <p/>
 * Individual BreadcrumbElements are attached to DocumentElements and monitor their
 * structure changes - invalidate their children so next query to children will create an
 * updated model.
 * 
 * @author sdedic
 */
final class BreadcrumbProvider implements CaretListener {
    /**
     * The associated text editor
     */
    private final JTextComponent   pane;
    
    /**
     * Identity cache of BreadcrumbProviders based on DocumentElement model
     */
    // @GuardedBy(this)
    private Map<DocumentElement, Reference<XE>>    nodes = new WeakHashMap<>();
    
    public BreadcrumbProvider(JTextComponent pane) {
        this.pane = pane;
        pane.addCaretListener(this);
    }
    
    void release() {
        pane.removeCaretListener(this);
        synchronized (this) {
            nodes.clear();
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        update();
    }
    
    private synchronized XE createElement(DocumentElement el, XE parent) {
        Reference<XE> e = nodes.get(el);
        XE node;
        if (e != null) {
            node = e.get();
            if (node != null) {
                return node;
            }
        }
        node = new XE(el, parent);
        nodes.put(el, new WeakReference(node));
        return node;
    }
    
    /**
     * Icons resource paths; taken from navigator implementation.
     */
    private static final String TAG_16 = "org/netbeans/modules/xml/text/navigator/resources/tag.png"; // NOI18N
    private static final String PI_16 = "org/netbeans/modules/xml/text/navigator/resources/xml_declaration.png"; // NOI18N
    private static final String DOCTYPE_16 = "org/netbeans/modules/xml/text/navigator/resources/doc_type.png"; // NOI18N
    private static final String CDATA_16 = "org/netbeans/modules/xml/text/navigator/resources/cdata.png"; // NOI18N
    
    void update() {
        if (!pane.isVisible()) {
            return;
        }
        int pos = pane.getCaret().getDot();
        Document doc = pane.getDocument();
        
        DocumentModel mdl;
        try {
            mdl = DocumentModel.getDocumentModel(doc);
        } catch (DocumentModelException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        DocumentElement el = mdl.getLeafElementForOffset(pos);
        OUT: while (el != null) {
            switch (el.getType()) {
                case XML_TAG:
                case XML_EMPTY_TAG:
                case XML_PI:
                case XML_CDATA:
                case XML_DOCTYPE:
                    break OUT;
                default:
                    el = el.getParentElement();
            }
        }        
        if (el == null) {
            return;
        }
        // create elements from the root up to the element:
        LinkedList<DocumentElement> path = new LinkedList<>();
        while (el != null) {
            path.addFirst(el);
            el = el.getParentElement();
        }
        XE node = null;
        
        for (DocumentElement x : path) {
            node = createElement(x, node);
        }
        BreadcrumbsController.setBreadcrumbs(doc, node);
    }
    
    @NbBundle.Messages({
        "LABEL_CDATA=<i>CDATA</i>",
        "LABEL_DOCTYPE=<i>DOCTYPE</i>"
    })
    private class XE implements BreadcrumbsElement, DocumentElementListener, OpenCookie {
        private final DocumentElement docEl;
        private final XE parent;

        public XE(DocumentElement docEl, XE parent) {
            this.docEl = docEl;
            this.parent = parent;
            docEl.addDocumentElementListener(WeakListeners.create(DocumentElementListener.class, this, docEl));
        }

        @Override
        public void elementAdded(DocumentElementEvent e) {
            invalidate();
        }

        @Override
        public void elementRemoved(DocumentElementEvent e) {
            invalidate();
        }

        @Override
        public void childrenReordered(DocumentElementEvent e) {
            invalidate();
        }

        @Override
        public void contentChanged(DocumentElementEvent e) {
            invalidate();
        }

        @Override
        public void attributesChanged(DocumentElementEvent e) {
            invalidate();
        }
        
        @Override
        public String getHtmlDisplayName() {
            switch (docEl.getType()) {
                case XML_TAG:
                case XML_EMPTY_TAG:
                    return docEl.getName();
                case XML_PI:
                    return docEl.getName();
                case XML_CDATA:
                    return Bundle.LABEL_CDATA();
                case XML_DOCTYPE:
                    return Bundle.LABEL_DOCTYPE();
                default:
                    // unsupported nodes
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public Image getIcon(int type) {
            if (type != BeanInfo.ICON_COLOR_16x16) {
                return null;
            }
            String resource;
            
            switch (docEl.getType()) {
                case XML_TAG:
                case XML_EMPTY_TAG:
                    resource = TAG_16; 
                    break;
                case XML_PI:
                    resource = PI_16;
                    break;
                case XML_CDATA:
                    resource = CDATA_16;
                    break;
                case XML_DOCTYPE:
                    resource = DOCTYPE_16;
                    break;
                default:
                    // unsupported nodes
                    throw new IllegalArgumentException();
            }
            return ImageUtilities.loadImage(resource, true);
        }

        @Override
        public Image getOpenedIcon(int type) {
            if (type != BeanInfo.ICON_COLOR_16x16) {
                return null;
            }
            return getIcon(type);
        }
        
        private volatile List<BreadcrumbsElement> children;
        
        private void invalidate() {
            this.children = null;
        }

        @Override
        public void open() {
            int offset = docEl.getStartOffset();
            if (pane.getDocument().getLength() >= offset) {
                pane.getCaret().setDot(offset);
            }
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            if (children != null) {
                return children;
            }
            List<BreadcrumbsElement> children = new ArrayList<>();
            for (DocumentElement ch : docEl.getChildren()) {
                switch (ch.getType()) {
                    case XML_TAG:
                    case XML_EMPTY_TAG:
                    case XML_PI:
                    case XML_CDATA:
                    case XML_DOCTYPE:
                        children.add(createElement(ch, this));
                        break;
                    default:
                        continue;
                }
            }
            synchronized (this) {
                if (this.children == null) {
                    this.children = children;
                }
            }
            return children;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public BreadcrumbsElement getParent() {
            return parent;
        }
        
    }
}
