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

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.AttributedCharacterIterator;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.Utilities;
import org.openide.text.NbDocument;
import org.openide.text.AttributedCharacters;
import javax.swing.text.Position;
import org.openide.text.Annotation;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import javax.swing.JToolBar;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.editor.impl.ComplexValueSettingsFactory;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.lib.BaseDocument_PropertyHandler;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.view.PrintUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends GuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
NbDocument.Printable, NbDocument.CustomEditor, NbDocument.CustomToolbar, NbDocument.Annotatable {

    /** Indent engine for the given kitClass. */
    public static final String INDENT_ENGINE = "indentEngine"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("NbEditorDocument", 1, false, false); //NOI18N

    /** Map of [Annotation, AnnotationDesc] */
    private final HashMap annoMap = new HashMap(20);

    /**
     * Creates a new document.
     * 
     * @deprecated Use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    @Deprecated
    public NbEditorDocument(Class kitClass) {
        super(kitClass);
        init();
    }
    
    /**
     * Creates a new document.
     * 
     * @param mimeType The mime type for the new document.
     * 
     * @since 1.18
     */
    public NbEditorDocument(String mimeType) {
        super(mimeType);
        init();
    }
    
    private void init() {
        setNormalStyleName(NbDocument.NORMAL_STYLE_NAME);
        

        // Fill in the indentEngine property
        putProperty(INDENT_ENGINE, new BaseDocument.PropertyEvaluator() {
            public Object getValue() {
                MimePath mimePath = MimePath.parse((String) getProperty(MIME_TYPE_PROP));
                return ComplexValueSettingsFactory.getIndentEngine(mimePath);
            }
        });

        putProperty(SimpleValueNames.TEXT_LINE_WRAP, new BaseDocument_PropertyHandler() {
            public @Override Object getValue() {
                return CodeStylePreferences.get(NbEditorDocument.this).getPreferences().get(SimpleValueNames.TEXT_LINE_WRAP, "none"); //NOI18N
            }

            public @Override Object setValue(Object value) {
                // ignore, just let the document fire the property change event
                return null;
            }
        });
        putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, new BaseDocument_PropertyHandler() {
            public @Override Object getValue() {
                return CodeStylePreferences.get(NbEditorDocument.this).getPreferences().getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, EditorPreferencesDefaults.defaultTextLimitWidth); //NOI18N
            }

            public @Override Object setValue(Object value) {
                // ignore, just let the document fire the property change event
                return null;
            }
        });
    }

    public @Override int getShiftWidth() {
        return IndentUtils.indentLevelSize(this);
    }

    public @Override int getTabSize() {
        return IndentUtils.tabSize(this);
    }

    public @Override void setCharacterAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        if (s != null) {
            Object val = s.getAttribute(NbDocument.GUARDED);
            if (val instanceof Boolean) {
                if (((Boolean)val).booleanValue() == true) { // want make guarded
                    super.setCharacterAttributes(offset, length, guardedSet, replace);
                } else { // want make unguarded
                    super.setCharacterAttributes(offset, length, unguardedSet, replace);
                }
            } else { // not special values, just pass
                super.setCharacterAttributes(offset, length, s, replace);
            }
        }
    }

    @Override
    public java.text.AttributedCharacterIterator[] createPrintIterators() {
        List<AttributedCharacterIterator> lineList = PrintUtils.printDocument(this, true, 0, getLength() + 1);
        AttributedCharacterIterator[] lines = new AttributedCharacterIterator[lineList.size()];
        lineList.toArray(lines);
        return lines;
    }

    public Component createEditor(JEditorPane j) {
        EditorUI editorUI = Utilities.getEditorUI(j);
        if (editorUI == null) { // Editor kit not installed yet??
            javax.swing.plaf.TextUI ui = j.getUI();
            javax.swing.text.EditorKit kit = j.getEditorKit();
            throw new IllegalStateException("NbEditorDocument.createEditor(): ui=" + ui + // NOI18N
                    ", kit=" + kit + ", pane=" + j); // NOI18N
        }
        return editorUI.getExtComponent();
    }

    public JToolBar createToolbar(JEditorPane j) {
        final EditorUI ui = Utilities.getEditorUI(j);
        return ui != null ? ui.getToolBarComponent() : null;
    }
    
    /** Add annotation to the document. For annotation of whole line
     * the length parameter can be ignored (specify value -1).
     * @param startPos position which represent begining 
     * of the annotated text
     * @param length length of the annotated text. If -1 is specified 
     * the whole line will be annotated
     * @param annotation annotation which is attached to this text */
    @Override
    public void addAnnotation(Position startPos, int length, Annotation annotation) {
        readLock(); // Ensure read-locking (if not aqcquired by caller)
        try {
            // Make sure the annotation's position is inside the document
            int docLen = getLength();
            int offset = startPos.getOffset();
            assert (offset >= 0) : "offset=" + offset + " < 0"; // NOI18N
            if (offset > docLen) {
                try {
                    startPos = createPosition(offset);
                } catch (BadLocationException e) {
                    throw new IllegalStateException("Cannot create position at offset=" + offset + // NOI18N
                            ", docLen=" + docLen, e); // NOI18N
                }
            }
            
            AnnotationDescDelegate a;
            synchronized(annoMap) {
                a = (AnnotationDescDelegate)annoMap.get(annotation);
                if (a != null) { // already added before
                    throw new IllegalStateException("Annotation already added: " + a); // NOI18N
                }
                if (annotation.getAnnotationType() != null) {
                    a = new AnnotationDescDelegate(this, startPos, length, annotation);
                    annoMap.put(annotation, a);
                    getAnnotations().addAnnotation(a);
                }
            }
        } finally {
            readUnlock();
        }
    }

    /** Removal of added annotation.
     * @param annotation annotation which is going to be removed */
    public void removeAnnotation(Annotation annotation) {
        if (annotation == null) { // issue 14803
            throw new IllegalStateException("Trying to remove null annotation."); // NOI18N
        }
        // partial fix of #33165 - read-locking of the document added
        readLock();
        try {
            if (annotation.getAnnotationType() != null) {
                final AnnotationDescDelegate a;
                synchronized (annoMap) {
                    a = (AnnotationDescDelegate)annoMap.remove(annotation);
                    if (a == null) { // not added yet
                        throw new IllegalStateException("Annotation not added: " + annotation.getAnnotationType() + annotation.getShortDescription());
                    }
                }
                a.detachListeners();
                getAnnotations().removeAnnotation(a);
            }
        } finally {
            readUnlock();
        }
    }
    
    Map getAnnoMap(){
        return annoMap;
    }

    protected @Override Dictionary createDocumentProperties(Dictionary origDocumentProperties) {
        return new LazyPropertyMap(origDocumentProperties) {
            public @Override Object put(Object key, Object value) {
                Object origValue = super.put(key, value);
                if (Document.StreamDescriptionProperty.equals(key)) {
                    assert value != null;
                    
                    if (origValue == null) {
                        // XXX: workaround for #137528, touches project settings                        
                        RP.post(new Runnable() {
                            public void run() {
                                IndentUtils.indentLevelSize(NbEditorDocument.this);
                            }
                        });
                    } else {
                        // this property should only ever be set once. even if it
                        // is set more times it must never be set to a different value
                        assert origValue.equals(value);
                    }
                }
                
                return origValue;
            }
        };
    }

    /** Implementation of AnnotationDesc, which delegate to Annotation instance
     * defined in org.openide.text package.
     */
    static class AnnotationDescDelegate extends AnnotationDesc implements Lookup.Provider {
        
        private Annotation delegate;
        private PropertyChangeListener l;
        private Position pos;
        private BaseDocument doc;

        private int lastKnownOffset = -1;
        private int lastKnownLine = -1;
        
        AnnotationDescDelegate(BaseDocument doc, Position pos, int length, Annotation anno) {
            super(pos.getOffset(),length);
            this.pos = pos;
            this.delegate = anno;
            this.doc = doc;
            
            // update AnnotationDesc.type member
            updateAnnotationType();
            
            // forward property changes to AnnotationDesc property changes
            l = new PropertyChangeListener() {
                public void propertyChange (PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == null || Annotation.PROP_SHORT_DESCRIPTION.equals(evt.getPropertyName())) {
                        firePropertyChange(AnnotationDesc.PROP_SHORT_DESCRIPTION, null, null);
                    }
                    if (evt.getPropertyName() == null || Annotation.PROP_MOVE_TO_FRONT.equals(evt.getPropertyName())) {
                        firePropertyChange(AnnotationDesc.PROP_MOVE_TO_FRONT, null, null);
                    }
                    if (evt.getPropertyName() == null || Annotation.PROP_ANNOTATION_TYPE.equals(evt.getPropertyName())) {
                        updateAnnotationType();
                        firePropertyChange(AnnotationDesc.PROP_ANNOTATION_TYPE, null, null);
                    }
                }
            };
            delegate.addPropertyChangeListener(l);
        }

        public String getAnnotationType() {
            return delegate.getAnnotationType();
        }
        
        public String getShortDescription() {
            return delegate.getShortDescription();
        }
        
        void detachListeners() {
            delegate.removePropertyChangeListener(l);
        }

        public int getOffset() {
            return pos.getOffset();
        }
        
        public int getLine() {
            int offset = pos.getOffset();

            if (lastKnownOffset != -1 && lastKnownLine != -1) {
                if (lastKnownOffset == offset) {
                    return lastKnownLine;
                }
            }

            try {
                lastKnownLine = LineDocumentUtils.getLineIndex(doc, offset);
                lastKnownOffset = offset;
            } catch (BadLocationException e) {
                lastKnownOffset = -1;
                lastKnownLine = 0;
            }

            return lastKnownLine;
        }

        public Lookup getLookup() {
            return Lookups.singleton(delegate);
        }
        
    }
    
    class NbPrintContainer extends AttributedCharacters implements PrintContainer {

        ArrayList acl = new ArrayList();

        AttributedCharacters a;

        NbPrintContainer() {
            a = new AttributedCharacters();
        }

        public void add(char[] chars, Font font, Color foreColor, Color backColor) {
            a.append(chars, font, foreColor);
        }

        public void eol() {
            acl.add(a);
            a = new AttributedCharacters();
        }

        public boolean initEmptyLines() {
            return true;
        }

        public AttributedCharacterIterator[] getIterators() {
            int cnt = acl.size();
            AttributedCharacterIterator[] acis = new AttributedCharacterIterator[cnt];
            for (int i = 0; i < cnt; i++) {
                AttributedCharacters ac = (AttributedCharacters)acl.get(i);
                acis[i] = ac.iterator();
            }
            return acis;
        }

    }

}
