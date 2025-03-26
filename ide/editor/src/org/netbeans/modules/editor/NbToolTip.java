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

package org.netbeans.modules.editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.PopupManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.text.Annotation;
import org.openide.text.Line;
import java.io.IOException;
import java.util.HashMap;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.AnnotationDesc;
import java.beans.PropertyChangeListener;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.EditorUI;
import org.netbeans.lib.editor.hyperlink.HyperlinkOperation;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
* ToolTip annotations reading and refreshing
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbToolTip extends FileChangeAdapter {

    // -J-Dorg.netbeans.modules.editor.NbToolTip.level=FINE
    private static final Logger LOG = Logger.getLogger(NbToolTip.class.getName());
    
    private static final HashMap<String,WeakReference<NbToolTip>> mime2tip = new HashMap<String,WeakReference<NbToolTip>>();
    
    private static final AtomicInteger lastRequestId = new AtomicInteger(0);
    
    private final String mimeType;
    
    private Annotation[] tipAnnotations;
    
    private static final RequestProcessor toolTipRP = new RequestProcessor("ToolTip-Evaluator", 1); // NOI18N
    private static volatile Reference<RequestProcessor.Task> lastToolTipTask = new WeakReference<RequestProcessor.Task>(null);
    
    static synchronized void buildToolTip(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target.getDocument());
        NbToolTip tip = getTip(mimeType);
        tip.buildTip(target);
    }
    
    private static int newRequestId() {
        return lastRequestId.incrementAndGet();
    }
    
    private static int getLastRequestId() {
        return lastRequestId.get();
    }
    
    
    private NbToolTip(String mimeType) {
        this.mimeType = mimeType;
    }
    
    private static NbToolTip getTip(String mimeType) {
        WeakReference<NbToolTip> nttWr = mime2tip.get(mimeType);
        NbToolTip tip = nttWr == null ? null : nttWr.get();
        if (tip == null) {
            tip = new NbToolTip(mimeType);
            mime2tip.put(mimeType, new WeakReference<NbToolTip>(tip));
        }
        return tip;
    }

    private Annotation[] getTipAnnotations() {
        Annotation[] annos;
        synchronized (NbToolTip.class) {
            annos = tipAnnotations;
        }
        
        if (annos == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Searching for tooltip annotations for mimeType = '" + mimeType + "'"); //NOI18N
            }
            Lookup l = Lookups.forPath("Editors/" + mimeType + "/ToolTips");  //NOI18N
            Collection<? extends Annotation> res = l.lookupAll(Annotation.class);
            if (res.contains(null)) {
                throw new IllegalStateException("Lookup returning null instance: " + l); // NOI18N
            }
            annos = res.toArray(new Annotation[0]);
            synchronized (NbToolTip.class) {
                tipAnnotations = annos;
            }
        }
        
        return annos;
    }
     
    private void buildTip(JTextComponent target) {
        EditorUI eui = Utilities.getEditorUI(target);
        ToolTipSupport tts = eui == null ? null : eui.getToolTipSupport();

        if (tts == null) {
            return; // no tooltip support, no tooltips
        }
        MouseEvent lastMouseEvent = tts.getLastMouseEvent();
        if (lastMouseEvent == null) {
            // no mouse event, shouldn't be invoked at all, see defect #215520
            return;
        }
        BaseDocument doc = Utilities.getDocument(target);
        Point p = lastMouseEvent.getPoint();
        doc.readLock();
        try {
            // Calls View.getTooltipText, which usually does nothing. CollapsedView.getTooltipText
            // however calls ToolTipSupport to show its own tooltip component and then
            // returns an empty string.
            String toolTipText = target.getUI().getToolTipText(target, tts.getLastMouseEvent().getPoint());
            if (toolTipText != null){
                return;
            }

            assert (tts.getLastMouseEvent().getSource() == target);
            // TODO create ViewHierarchy.getToolTip()
            org.netbeans.modules.editor.lib2.view.DocumentView docView =
                    org.netbeans.modules.editor.lib2.view.DocumentView.get(target);
            Shape alloc;
            if (docView != null && (alloc = docView.getAllocation()) != null) {
                JComponent toolTip = docView.getToolTip(p.getX(), p.getY(), alloc);
                if (toolTip != null) {
                    String tooltipType = (String) toolTip.getClientProperty("tooltip-type");
                    if ("fold-preview".equals(tooltipType)) {
                        tts.setToolTip(toolTip, PopupManager.ViewPortBounds, PopupManager.BelowPreferred, -1, 0);
    //                    tts.setToolTip(toolTip, PopupManager.ScrollBarBounds, PopupManager.Largest, 2, 0);
                    } else {
                        tts.setToolTip(toolTip);
                    }
                    return;
                }
            }
        } finally {
            doc.readUnlock();
        }
        
        Annotation[] annos = getTipAnnotations();
        if (doc != null) {
            DataObject dob = NbEditorUtilities.getDataObject(doc);
            if (dob != null && dob.isValid()) {
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                if (ec != null) {
                    StyledDocument openedDoc = ec.getDocument();
                    if (openedDoc != doc) { // doc has changed in meantime
                        return;
                    }

                    // partial fix of #33165 - read-locking of the document added
                    doc.readLock();
                    try {
                        int offset = getOffsetForPoint(p, target, doc);
                        if (offset >= 0) {
                            EditorKit kit = org.netbeans.editor.Utilities.getKit(target);
                            if (kit instanceof NbEditorKit) {
                                Object tooltipAttributeValue = null;
                                Line.Part lp = null;
                                Annotation [] tooltipAnnotations = null;
                                AnnotationDesc annoDesc = null;

                                // Get the highlighting layers stuff - first bottom highlights and then possibly top ones
                                boolean tops = false;
                                do {
                                    HighlightsContainer highlights = tops
                                            ? HighlightingManager.getInstance(target).getBottomHighlights()
                                            : HighlightingManager.getInstance(target).getTopHighlights();
                                    HighlightsSequence seq = highlights.getHighlights(offset, offset + 1);
                                    if (seq.moveNext()) {
                                        AttributeSet attrs = seq.getAttributes();
                                        if (attrs != null) {
                                            tooltipAttributeValue = attrs.getAttribute(EditorStyleConstants.Tooltip);
                                        }
                                    }
                                    tops = !tops;
                                } while (tooltipAttributeValue == null && tops);

                                if (annos != null) {
                                    // Get the annotations stuff
                                    int line = Utilities.getLineOffset(doc, offset);
                                    int col = offset - Utilities.getRowStartFromLineOffset(doc, line);
                                    Line.Set ls = ec.getLineSet();
                                    if (ls != null) {
                                        Line l = ls.getCurrent(line);
                                        if (l != null) {
                                            lp = l.createPart(col, 0);
                                            if (lp != null) {
                                                annoDesc = doc.getAnnotations().getActiveAnnotation(line);
                                                if (annoDesc != null && ((offset < annoDesc.getOffset() || offset >= annoDesc.getOffset() + annoDesc.getLength()))) {
                                                    annoDesc = null;
                                                }
                                                tooltipAnnotations = annos;
                                            }
                                        }
                                    }
                                }

                                if ((lp != null && tooltipAnnotations != null) || tooltipAttributeValue != null) {
                                    int requestId = newRequestId();
                                    RequestProcessor.Task lttt = lastToolTipTask.get();
                                    if (lttt != null) {
                                        lttt.cancel();
                                    }
                                    lastToolTipTask = new WeakReference<RequestProcessor.Task>(toolTipRP.post(new Request(
                                        annoDesc, tooltipAnnotations, lp, // annotations stuff
                                        offset, tooltipAttributeValue, // highlighting layers stuff
                                        tts, target, doc, (NbEditorKit) kit, requestId))); // request & tooltip support
                                }
                            }
                        }
                    } catch (BadLocationException ble) {
                        LOG.log(Level.FINE, null, ble);
                    } finally {
                        doc.readUnlock();
                    }
                }
            }
        }
    }
    
    private static int getOffsetForPoint(Point p, JTextComponent c, BaseDocument doc) throws BadLocationException {
        if (p.x >= 0 && p.y >= 0) {
            int offset = c.viewToModel(p);
            Rectangle r = c.modelToView(offset);
            EditorUI eui = Utilities.getEditorUI(c);

            // Check that p is on a line with text and not completely below text,
            // ie. behind EOF.
            int relY = p.y - r.y;
            if (eui != null && relY < eui.getLineHeight()) {
                // Check that p is on a line with text before its EOL.
                if (offset < Utilities.getRowEnd(doc, offset)) {
                    return offset;
                }
            }
        }
        
        return -1;
    }
        
    private static class Request implements Runnable, PropertyChangeListener, DocumentListener {
        
        private ToolTipSupport tts;
        
        private final Annotation[] annos;
        
        private final AnnotationDesc annoDesc;
        
        private final Line.Part linePart;
        
        private final JTextComponent component;
        
        private final AbstractDocument doc;
        
        private final NbEditorKit kit;
        
        private final int offset;
        
        private final Object tooltipAttributeValue;
        
        private final int requestId;
        
        private boolean documentModified;
        
        Request(
            AnnotationDesc annoDesc, Annotation[] annos, Line.Part lp,
            int offset, Object tooltipAttributeValue,
            ToolTipSupport tts, JTextComponent component, AbstractDocument doc, NbEditorKit kit, int requestId
        ) {
            this.annoDesc = annoDesc;
            this.annos = annos;
            this.linePart = lp;
            this.tts = tts;
            this.component = component;
            this.doc = doc;
            this.kit = kit;
            this.offset = offset;
            this.tooltipAttributeValue = tooltipAttributeValue;
            this.requestId = requestId;
        }
        
        public void run() {
            if (tts == null) return;
            
            if (tts == null || tts.getStatus() == ToolTipSupport.STATUS_HIDDEN) {
                return; // do nothing
            }
            if (!isRequestValid()) {
                return;
            }

            if (tts != null) tts.addPropertyChangeListener(this);

            final CharSequence tooltipText = resolveTooltipText();
            if (tooltipText != null && tooltipText.length() > 0 && isRequestValid()) {
                Utilities.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        final ToolTipSupport ftts = tts;
                        if (ftts != null) {
                            ftts.setToolTipText(tooltipText.toString());
                            if (tooltipText instanceof HyperlinkOperation.TooltipInfo) {
                                JComponent tt = ftts.getToolTip();
                                if (tt instanceof JEditorPane) {
                                    ((JEditorPane)tt).addHyperlinkListener(((HyperlinkOperation.TooltipInfo)tooltipText).getListener());
                                    ((JEditorPane)tt).setEditable(false);
                                }
                            }
                        }
                    }
                });
            } else { // Attempt to get tooltip from view under mouse

            }
        }

        private CharSequence resolveTooltipText() {
            kit.toolTipAnnotationsLock(doc);
            try {
                doc.readLock();
                try {

                    if (!isRequestValid()) {
                        return null;
                    }

                    // Read tooltip from annotations
                    String tooltipFromAnnotations = null;
                    {
                        if (annos != null) {
                            // Attach tooltip annotations
                            for (int i = 0; i < annos.length; i++) {
                                annos[i].attach(linePart);
                            }

                            if (annoDesc != null) {
                                tooltipFromAnnotations = annoDesc.getShortDescription();
                                annoDesc.addPropertyChangeListener(this);
                            } else {
                                for (int i = 0; i < annos.length; i++) {
                                    String desc = annos[i].getShortDescription();
                                    if (desc != null) {
                                        tooltipFromAnnotations = desc;
                                    }
                                    annos[i].addPropertyChangeListener(this);
                                }
                            }
                        }
                    }
                    
                    // Set tooltip text, if any
                    if (tooltipFromAnnotations != null) {
                        return tooltipFromAnnotations;
                    }
                } finally {
                    doc.readUnlock();
                }
            } finally {
                kit.toolTipAnnotationsUnlock(doc);
            }

            if (!isRequestValid()) {
                return null;
            }

            return getTooltipFromHighlightingLayers();
        }

        private CharSequence getTooltipFromHighlightingLayers() {
            // Read tooltip from highlighting layers attribute
            CharSequence tooltipFromHighlightingLayers = null;
            {
                if (tooltipAttributeValue != null) {
                    if (tooltipAttributeValue instanceof String) {
                        tooltipFromHighlightingLayers = (String) tooltipAttributeValue;
                    } else if (tooltipAttributeValue instanceof HighlightAttributeValue) {
                        @SuppressWarnings("unchecked") //NOI18N
                        CharSequence value = ((HighlightAttributeValue<CharSequence>) tooltipAttributeValue).getValue(
                            component, doc, EditorStyleConstants.Tooltip, offset, offset + 1);
                        tooltipFromHighlightingLayers = value;
                    } else {
                        LOG.fine("Invalid '" + EditorStyleConstants.Tooltip + "' attribute value " + tooltipAttributeValue); //NOI18N
                    }
                }
            }

            return tooltipFromHighlightingLayers;
        }
          
        private boolean isRequestValid() {
            return (getLastRequestId() == this.requestId)
                && !documentModified
                && isDocumentValid();
        }

        private boolean isDocumentValid() {
            DataObject dob = NbEditorUtilities.getDataObject(doc);
            if (dob != null) {
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                if (ec != null) {
                    StyledDocument openedDoc;
                    try {
                        openedDoc = ec.openDocument();
                    } catch (IOException e) {
                        openedDoc = null; // should return in next if stmt
                    }
                    
                    return (openedDoc == doc);
                }
            }
            return false;
        }

        private void dismiss() {
            if (tts !=null) tts.removePropertyChangeListener(this);
            tts = null; // signal that support no longer valid

            if (annoDesc != null) {
                annoDesc.removePropertyChangeListener(this);
            } else if (annos != null) {
                for (int i = 0; i < annos.length; i++) {
                    annos[i].removePropertyChangeListener(this);
                    annos[i].detach();
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (Annotation.PROP_SHORT_DESCRIPTION.equals(propName) || AnnotationDesc.PROP_SHORT_DESCRIPTION.equals(propName)) {
                final Object newValue = evt.getNewValue();
                toolTipRP.post(new Runnable() {
                    @Override public void run() {
                        final String tipText;
                        if (newValue != null) {
                            tipText = (String) newValue;
                        } else {
                            if (isRequestValid()) {
                                tipText = getTooltipFromHighlightingLayers().toString();
                                if (tipText == null || tipText.isEmpty()) {
                                    return ;
                                }
                            } else {
                                return ;
                            }
                        }
                        if (tipText != null) {
                            Utilities.runInEventDispatchThread( // ensure to run in AWT thread
                                new Runnable() {
                                    public void run() {
                                        final ToolTipSupport ftts = tts;
                                        if (ftts != null) {
                                            ftts.setToolTipText(tipText);
                                        }
                                    }
                                }
                            );
                        }
                    }
                });
            } else if (ToolTipSupport.PROP_STATUS.equals(propName)) {
                if (((Integer)evt.getNewValue()).intValue() == ToolTipSupport.STATUS_HIDDEN) {
                    dismiss();
                }
            }
        }
        
        public void insertUpdate(DocumentEvent evt) {
            documentModified = true;
        }
        
        public void removeUpdate(DocumentEvent evt) {
            documentModified = true;
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }

    } // End of Request class

}
