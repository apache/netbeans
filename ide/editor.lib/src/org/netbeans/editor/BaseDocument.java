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

package org.netbeans.editor;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.document.CustomUndoDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.modules.editor.document.implspi.CharClassifier;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.lib.BaseDocument_PropertyHandler;
import org.netbeans.modules.editor.lib.BeforeSaveTasks;
import org.netbeans.modules.editor.lib.EditorPackageAccessor;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.lib.WcwdithUtil;
import org.netbeans.modules.editor.lib.drawing.DrawEngine;
import org.netbeans.modules.editor.lib.drawing.DrawGraphics;
import org.netbeans.modules.editor.lib.impl.MarkVector;
import org.netbeans.modules.editor.lib.impl.MultiMark;
import org.netbeans.modules.editor.lib2.CaretUndo;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib2.document.ContentEdit;
import org.netbeans.modules.editor.lib2.document.EditorDocumentContent;
import org.netbeans.modules.editor.lib2.document.EditorDocumentHandler;
import org.netbeans.modules.editor.lib2.document.EditorDocumentServices;
import org.netbeans.modules.editor.lib2.document.LineRootElement;
import org.netbeans.modules.editor.lib2.document.ListUndoableEdit;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.modules.editor.lib2.document.DocumentPostModificationUtils;
import org.netbeans.modules.editor.lib2.document.ReadWriteBuffer;
import org.netbeans.modules.editor.lib2.document.ReadWriteUtils;
import org.netbeans.modules.editor.lib2.document.StableCompoundEdit;
import org.netbeans.modules.editor.lib2.document.UndoRedoDocumentEventResolver;
import org.netbeans.spi.editor.document.UndoableEditWrapper;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
* Document implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

@SuppressWarnings("ClassWithMultipleLoggers")
public class BaseDocument extends AbstractDocument implements AtomicLockDocument, LineDocument, CustomUndoDocument {

    static {
        EditorPackageAccessor.register(new Accessor());
        EditorDocumentHandler.setEditorDocumentServices(BaseDocument.class, BaseDocumentServices.INSTANCE);
        UndoRedoDocumentEventResolver.register(new UndoRedoDocumentEventResolver() {
            @Override
            public boolean isUndoRedo(DocumentEvent evt) {
                if (evt instanceof BaseDocumentEvent) {
                    BaseDocumentEvent bevt = (BaseDocumentEvent) evt;
                    return bevt.isInUndo() || bevt.isInRedo();
                }
                return false;
            }
        });
    }

    // -J-Dorg.netbeans.editor.BaseDocument.level=FINE
    private static final Logger LOG = Logger.getLogger(BaseDocument.class.getName());

    // -J-Dorg.netbeans.editor.BaseDocument-listener.level=FINE
    private static final Logger LOG_LISTENER = Logger.getLogger(BaseDocument.class.getName() + "-listener");

    // -J-Dorg.netbeans.editor.BaseDocument.EDT.level=FINE - check that insert/remove only in EDT
//    private static final Logger LOG_EDT = Logger.getLogger(BaseDocument.class.getName() + "-EDT");
    
    /**
     * Mime type of the document. This property can be used for determining
     * mime type of a document.
     *
     * @since 1.26
     */
    public static final String MIME_TYPE_PROP = "mimeType"; // NOI18N

    /** Registry identification property */
    public static final String ID_PROP = "id"; // NOI18N

    /** This document's version. It's accessed by DocumentUtilities.getDocumentVersion(). */
    private static final String VERSION_PROP = "version"; //NOI18N

    /** Timestamp when this document was last modified. It's accessed by DocumentUtilities.getDocumentVersion(). */
    private static final String LAST_MODIFICATION_TIMESTAMP_PROP = "last-modification-timestamp"; //NOI18N

    /** Line separator property for reading files in */
    public static final String READ_LINE_SEPARATOR_PROP = DefaultEditorKit.EndOfLineStringProperty;

    /** Line separator property for writing content into files. If not set
     * the writing defaults to the READ_LINE_SEPARATOR_PROP.
     */
    public static final String WRITE_LINE_SEPARATOR_PROP = "write-line-separator"; // NOI18N

    /** File name property */
    public static final String FILE_NAME_PROP = "file-name"; // NOI18N

    /** Wrap search mark property */
    public static final String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"; // NOI18N

    /** Undo manager property. This can be used to implement undo
    * in a simple way. Default undo and redo actions try to get this
    * property and perform undo and redo through it.
    */
    public static final String UNDO_MANAGER_PROP = "undo-manager"; // NOI18N

    /** Kit class property. This can become useful for getting
    * the settings that logicaly belonging to the document.
    */
    public static final String KIT_CLASS_PROP = "kit-class"; // NOI18N

    /** String forward finder property */
    public static final String STRING_FINDER_PROP = "string-finder"; // NOI18N

    /** String backward finder property */
    public static final String STRING_BWD_FINDER_PROP = "string-bwd-finder"; // NOI18N

    /** Highlight search finder property. */
    public static final String BLOCKS_FINDER_PROP = "blocks-finder"; // NOI18N

    /**
     * Maximum line width encountered during the initial read operation.
     * This is filled by Analyzer and used by UI to set the correct initial width
     * of the component.
     * Values: java.lang.Integer
     * @deprecated property no longer populated; deprecated without replacement.
     */
    @Deprecated
    public static final String LINE_LIMIT_PROP = "line-limit"; // NOI18N

    /**
     * If set, determines the document's editability. Note that even though the 
     * editable property may be set to true, the document may be still uneditable for
     * other reasons. The document should not permit any edits if the editable
     * property is set to false.
     */
    /* public */ static final String EDITABLE_PROP = "editable"; // NOI18N

    /**
     * Size of the line batch. Line batch can be used at various places
     * especially when processing lines by syntax scanner.
     * @deprecated property no longer populated; deprecated without replacement.
     */
    @Deprecated
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N

    /** Line separator is marked by CR (Macintosh) */
    public static final String  LS_CR = "\r"; // NOI18N

    /** Line separator is marked by LF (Unix) */
    public static final String  LS_LF = "\n"; // NOI18N

    /** Line separator is marked by CR and LF (Windows) */
    public static final String  LS_CRLF = "\r\n"; // NOI18N

    /** Name of the formatter setting. */
    public static final String FORMATTER = "formatter"; // NOI18N

    /**
     * Document Boolean property defined by openide.text.CloneableEditorSupport to determine
     * whether document implementation fires VetoableChangeListener prior doing actual write-locking
     * followed by document modification or an atomic section.
     */
    private static final String SUPPORTS_MODIFICATION_LISTENER_PROP = "supportsModificationListener"; // NOI18N

    /**
     * Document property into which openide.text.CloneableEditorSupport
     * puts VetoableChangeListener instance that the document fires prior doing actual write-locking
     * followed by document modification or an atomic section.
     */
    private static final String MODIFICATION_LISTENER_PROP = "modificationListener"; // NOI18N
    /**
     * How many modifications under atomic lock are necessary for disabling of lexer's token hierarchy.
     */
    private static final int DEACTIVATE_LEXER_THRESHOLD = 30;

    private static final Object annotationsLock = new Object();

    /** Debug the stack of calling of the insert/remove */
    private static final boolean debugStack = Boolean.getBoolean("netbeans.debug.editor.document.stack"); // NOI18N
    /** Debug the document insert/remove but do not output text inserted/removed */
    private static final boolean debugNoText = Boolean.getBoolean("netbeans.debug.editor.document.notext"); // NOI18N

    /** Debug the StreamDescriptionProperty during read() */
    private static final boolean debugRead = Boolean.getBoolean("netbeans.debug.editor.document.read"); // NOI18N

    /** How many times atomic writer requested writing */
    private int atomicDepth;
    
    /**
     * If VetoableChangeListener for "modificationListener" property fired
     * veto which means that any document's modification is prohibited then this variable will become false.
     * Each insertString() or remove() will generate BadLocationException.
     * If no veto is fired or no listener is present then this variable is true.
     */
    boolean modifiable = true;

    /* Was the document initialized by reading? */
    protected boolean inited;

    /* Was the document modified by doing inert/remove */
    protected boolean modified;

    /** Default element - lazily inited */
    protected Element defaultRootElem;

    private SyntaxSupport syntaxSupport;

    /** Reset merging next created undoable edit to the last one. */
    boolean undoMergeReset;

    /** Kit class stored here */
    private final Class deprecatedKitClass;
    private String mimeType;

    /** Undo event for atomic events is fired after the successful
    * atomic operation is finished. The changes are stored in this variable
    * during the atomic operation. If the operation is broken, these edits
    * are used to restore previous state.
    */
    private AtomicCompoundEdit atomicEdits;

    private Acceptor identifierAcceptor;

    private Acceptor whitespaceAcceptor;

//    private final ArrayList<Syntax> syntaxList = new ArrayList<Syntax>();

    /** Root element of line elements representation */
    LineRootElement lineRootElement;

    /** Last document event to be undone. The field is filled
     * by the lastly done modification undoable edit.
     * BaseDocumentEvent.canUndo() checks this flag.
     */
    UndoableEdit lastModifyUndoEdit; // #8692 check last modify undo edit

    /** List of annotations for this document. */
    private Annotations annotations;

    /* Bug #6258 during composing I18N text using input method the Undoable Edit
     * actions must be disabled so only the final push (and not all intermediate
     * ones) of the I18N word will be stored as undoable edit.
     */
     private boolean composedText = false;

    /** Atomic lock event instance shared by all the atomic lock firings done for this document */
    private AtomicLockEvent atomicLockEventInstance = new AtomicLockEvent(this);

    private FixLineSyntaxState fixLineSyntaxState;

    private Object[] atomicLockListenerList;
    
    private int postModificationDepth;

    private DocumentListener postModificationDocumentListener;

    private ListenerList<DocumentListener> postModificationDocumentListenerList = new ListenerList<DocumentListener>();

    private ListenerList<DocumentListener> updateDocumentListenerList = new ListenerList<DocumentListener>();

    private Position lastPositionEditedByTyping = null;

    /** Size of one indentation level. If this variable is null (value
     * is not set in Settings, then the default algorithm will be used.
     */
    private int shiftWidth = -1;
    private int tabSize;
    
    private CharSequence text;
    
    private UndoableEdit removeUpdateLineUndo;

    private Collection<? extends UndoableEditWrapper> undoEditWrappers;

    private DocumentFilter.FilterBypass filterBypass;
    
    private int runExclusiveDepth;

    private Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt == null ? null : evt.getKey();
            if (key == null || SimpleValueNames.TAB_SIZE.equals(key)) {
                tabSize = prefs.getInt(SimpleValueNames.TAB_SIZE, EditorPreferencesDefaults.defaultTabSize);
            }

            if (key == null || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(key)) {
                shiftWidth = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
            }

            if (key == null || SimpleValueNames.SPACES_PER_TAB.equals(key)) {
                if (shiftWidth == -1) {
                    shiftWidth = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
                }
            }

            if (key == null || EditorPreferencesKeys.READ_BUFFER_SIZE.equals(key)) {
                int readBufferSize = prefs.getInt(EditorPreferencesKeys.READ_BUFFER_SIZE, -1);
                if (readBufferSize <= 0) {
                    readBufferSize = EditorPreferencesDefaults.defaultReadBufferSize;
                }
                putProperty(EditorPreferencesKeys.READ_BUFFER_SIZE, Integer.valueOf(readBufferSize));
            }

            if (key == null || EditorPreferencesKeys.WRITE_BUFFER_SIZE.equals(key)) {
                int writeBufferSize = prefs.getInt(EditorPreferencesKeys.WRITE_BUFFER_SIZE, -1);
                if (writeBufferSize <= 0) {
                    writeBufferSize = EditorPreferencesDefaults.defaultWriteBufferSize;
                }
                putProperty(EditorPreferencesKeys.WRITE_BUFFER_SIZE, Integer.valueOf(writeBufferSize));
            }

            if (key == null || EditorPreferencesKeys.MARK_DISTANCE.equals(key)) {
                int markDistance = prefs.getInt(EditorPreferencesKeys.MARK_DISTANCE, -1);
                if (markDistance <= 0) {
                    markDistance = EditorPreferencesDefaults.defaultMarkDistance;
                }
                putProperty(EditorPreferencesKeys.MARK_DISTANCE, Integer.valueOf(markDistance));
            }

            if (key == null || EditorPreferencesKeys.MAX_MARK_DISTANCE.equals(key)) {
                int maxMarkDistance = prefs.getInt(EditorPreferencesKeys.MAX_MARK_DISTANCE, -1);
                if (maxMarkDistance <= 0) {
                    maxMarkDistance = EditorPreferencesDefaults.defaultMaxMarkDistance;
                }
                putProperty(EditorPreferencesKeys.MAX_MARK_DISTANCE, Integer.valueOf(maxMarkDistance));
            }

            if (key == null || EditorPreferencesKeys.MIN_MARK_DISTANCE.equals(key)) {
                int minMarkDistance = prefs.getInt(EditorPreferencesKeys.MIN_MARK_DISTANCE, -1);
                if (minMarkDistance <=0 ) {
                    minMarkDistance = EditorPreferencesDefaults.defaultMinMarkDistance;
                }
                putProperty(EditorPreferencesKeys.MIN_MARK_DISTANCE, Integer.valueOf(minMarkDistance));
            }

            if (key == null || EditorPreferencesKeys.READ_MARK_DISTANCE.equals(key)) {
                int readMarkDistance = prefs.getInt(EditorPreferencesKeys.READ_MARK_DISTANCE, -1);
                if (readMarkDistance <= 0) {
                    readMarkDistance = EditorPreferencesDefaults.defaultReadMarkDistance;
                }
                putProperty(EditorPreferencesKeys.READ_MARK_DISTANCE, Integer.valueOf(readMarkDistance));
            }

            if (key == null || EditorPreferencesKeys.SYNTAX_UPDATE_BATCH_SIZE.equals(key)) {
                int syntaxUpdateBatchSize = prefs.getInt(EditorPreferencesKeys.SYNTAX_UPDATE_BATCH_SIZE, -1);
                if (syntaxUpdateBatchSize <= 0) {
                    syntaxUpdateBatchSize = 7 * (Integer) getProperty(EditorPreferencesKeys.MARK_DISTANCE);
                }
                putProperty(EditorPreferencesKeys.SYNTAX_UPDATE_BATCH_SIZE, Integer.valueOf(syntaxUpdateBatchSize));
            }

            if (key == null || LINE_BATCH_SIZE.equals(key)) {
                int lineBatchSize = prefs.getInt(LINE_BATCH_SIZE, -1);
                if (lineBatchSize <= 0) {
                    lineBatchSize = EditorPreferencesDefaults.defaultLineBatchSize;
                }
                putProperty(LINE_BATCH_SIZE, Integer.valueOf(lineBatchSize));
            }

            if (key == null || EditorPreferencesKeys.IDENTIFIER_ACCEPTOR.equals(key)) {
                identifierAcceptor = (Acceptor) SettingsConversions.callFactory(prefs, MimePath.parse(mimeType), EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, AcceptorFactory.LETTER_DIGIT);
            }

            if (key == null || EditorPreferencesKeys.WHITESPACE_ACCEPTOR.equals(key)) {
                whitespaceAcceptor = (Acceptor) SettingsConversions.callFactory(prefs, MimePath.parse(mimeType), EditorPreferencesKeys.WHITESPACE_ACCEPTOR, AcceptorFactory.WHITESPACE);
            }

            boolean stopOnEOL = prefs.getBoolean(EditorPreferencesKeys.WORD_MOVE_NEWLINE_STOP, EditorPreferencesDefaults.defaultWordMoveNewlineStop);

            if (key == null || EditorPreferencesKeys.NEXT_WORD_FINDER.equals(key)) {
                Finder finder = (Finder) SettingsConversions.callFactory(prefs, MimePath.parse(mimeType), EditorPreferencesKeys.NEXT_WORD_FINDER, null);
                putProperty(EditorPreferencesKeys.NEXT_WORD_FINDER, finder != null ? finder : new FinderFactory.NextWordFwdFinder(BaseDocument.this, stopOnEOL, false));
            }

            if (key == null || EditorPreferencesKeys.PREVIOUS_WORD_FINDER.equals(key)) {
                Finder finder = (Finder) SettingsConversions.callFactory(prefs, MimePath.parse(mimeType), EditorPreferencesKeys.PREVIOUS_WORD_FINDER, null);
                putProperty(EditorPreferencesKeys.PREVIOUS_WORD_FINDER, finder != null ? finder : new FinderFactory.PreviousWordBwdFinder(BaseDocument.this, stopOnEOL, false));
            }

            SettingsConversions.callSettingsChange(BaseDocument.this);
        }
    };
    private PreferenceChangeListener weakPrefsListener;

    /**
     * Creates a new document.
     *
     * @param kitClass class used to initialize this document with proper settings
     *   category based on the editor kit for which this document is created
     * @param addToRegistry If <code>true</code> the document will be listed in the
     *   <code>EditorRegistry</code>. In most situations <code>true</code> is the
     *   correct. However, if you are absolutely sure that document should not be
     *   listed in the registry, then set this parameter to <code>false</code>.
     *
     * @deprecated Use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    @Deprecated
    public BaseDocument(Class kitClass, boolean addToRegistry) {
        super(new EditorDocumentContent());

        if (LOG.isLoggable(Level.FINE) || LOG.isLoggable(Level.WARNING)) {
            String msg = "Using deprecated document construction for " + //NOI18N
                getClass().getName() + ", " + //NOI18N
                "see http://www.netbeans.org/nonav/issues/show_bug.cgi?id=114747. " + //NOI18N
                "Use -J-Dorg.netbeans.editor.BaseDocument.level=500 to see the stacktrace."; //NOI18N

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, null, new Throwable(msg)); //NOI18N
            } else {
                LOG.warning(msg); //NOI18N
            }
        }

        this.deprecatedKitClass = kitClass;
        this.mimeType = BaseKit.getKit(kitClass).getContentType();
        init(addToRegistry);
    }

    /**
     * Creates a new document.
     *
     * @param addToRegistry If <code>true</code> the document will be listed in the
     *   <code>EditorRegistry</code>. In most situations <code>true</code> is the
     *   correct. However, if you are absolutely sure that document should not be
     *   listed in the registry, then set this parameter to <code>false</code>.
     * @param mimeType The mime type for the document.
     *
     * @since 1.26
     */
    public BaseDocument(boolean addToRegistry, String mimeType) {
        super(new EditorDocumentContent());
        this.deprecatedKitClass = null;
        this.mimeType = mimeType;
        init(addToRegistry);
    }

    private void init(boolean addToRegistry) {
//        System.out.println("~~~ " + s2s(this) + " created for '" + mimeType + "'");
//
        setDocumentProperties(createDocumentProperties(getDocumentProperties()));
        super.addDocumentListener(org.netbeans.lib.editor.util.swing.DocumentUtilities.initPriorityListening(this));

        text = ((EditorDocumentContent)getContent()).getText();
        putProperty(CharSequence.class, text);
        putProperty(GapStart.class, new GapStart() {
            @Override
            public int getGapStart() {
                return ((EditorDocumentContent)getContent()).getCharContentGapStart();
            }
        });
        putProperty(SUPPORTS_MODIFICATION_LISTENER_PROP, Boolean.TRUE); // NOI18N
        putProperty(MIME_TYPE_PROP, new MimeTypePropertyEvaluator(this));
        putProperty(VERSION_PROP, new AtomicLong());
        putProperty(LAST_MODIFICATION_TIMESTAMP_PROP, new AtomicLong());
        putProperty(SimpleValueNames.TAB_SIZE, new BaseDocument_PropertyHandler() {
            public @Override Object setValue(Object value) {
                return null;
            }

            public @Override Object getValue() {
                return getTabSize();
            }
        });
        putProperty(PropertyChangeSupport.class, new PropertyChangeSupport(this));

        lineRootElement = new LineRootElement(this);

        // Line separators default to platform ones
        putProperty(READ_LINE_SEPARATOR_PROP, ReadWriteUtils.getSystemLineSeparator());

        // Initialize preferences and document properties
        prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        prefsListener.preferenceChange(null);
//        System.out.println("~~~ init: '" + mimeType + "' -> " + s2s(prefs));

        // Additional initialization of the document through the kit
        EditorKit kit = getEditorKit();
        if (kit instanceof BaseKit) {
            ((BaseKit) kit).initDocument(this);
        }

        ModRootElement modElementRoot = new ModRootElement(this);
        this.addUpdateDocumentListener(modElementRoot);
        modElementRoot.setEnabled(true);

        BeforeSaveTasks.get(this); // Ensure that "beforeSaveRunnable" gets initialized

        undoEditWrappers = MimeLookup.getLookup(mimeType).lookupAll(UndoableEditWrapper.class);
        if (undoEditWrappers != null && undoEditWrappers.isEmpty()) {
            undoEditWrappers = null;
        }

        if (weakPrefsListener == null) {
            // the listening could have already been initialized from setMimeType(), which
            // is called by some kits from initDocument()
            weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
            prefs.addPreferenceChangeListener(weakPrefsListener);
//            System.out.println("~~~ init: " + s2s(prefs) + " adding " + s2s(weakPrefsListener));
        }
    }
    
    public CharSeq getText() {
        return new CharSeq() {
            @Override
            public int length() {
                return text.length();
            }
            @Override
            public char charAt(int index) {
                return text.charAt(index);
            }
        };
    }

    Syntax getFreeSyntax() {
        EditorKit kit = getEditorKit();
        if (kit instanceof BaseKit) {
            return ((BaseKit) kit).createSyntax(this);
        } else {
            return new BaseKit.DefaultSyntax();
        }
//        synchronized (syntaxList) {
//            int cnt = syntaxList.size();
//            if (cnt > 0) {
//                return syntaxList.remove(cnt - 1);
//            } else {
//                EditorKit kit = getEditorKit();
//                if (kit instanceof BaseKit) {
//                    return ((BaseKit) kit).createSyntax(this);
//                } else {
//                    return new BaseKit.DefaultSyntax();
//                }
//            }
//        }
    }

    void releaseSyntax(Syntax syntax) {
//        synchronized (syntaxList) {
//            syntaxList.add(syntax);
//        }
    }

// XXX: formatting cleanup
//    /**
//     * @deprecated Please use Editor Indentation API instead, for details see
//     *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
//     */
//    public Formatter getLegacyFormatter() {
//        if (formatter == null) {
//            formatter = (Formatter) SettingsConversions.callFactory(prefs, MimePath.parse(mimeType), FORMATTER, null);
//            if (formatter == null) {
//                formatter = Formatter.getFormatter(mimeType);
//            }
//        }
//        return formatter;
//    }
//
//    /**
//     * Gets the formatter for this document.
//     *
//     * @deprecated Please use Editor Indentation API instead, for details see
//     *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
//     */
//    public Formatter getFormatter() {
//        Formatter f = getLegacyFormatter();
//        FormatterOverride fp = Lookup.getDefault().lookup(FormatterOverride.class);
//        return (fp != null) ? fp.getFormatter(this, f) : f;
//    }

    /**
     * @deprecated Please use Lexer instead, for details see
     *   <a href="@org-netbeans-modules-lexer@/overview-summary.html">Lexer</a>.
     */
    @Deprecated
    public SyntaxSupport getSyntaxSupport() {
        if (syntaxSupport == null) {
            EditorKit kit = getEditorKit();
            if (kit instanceof BaseKit) {
                syntaxSupport = ((BaseKit) kit).createSyntaxSupport(this);
            } else {
                syntaxSupport = new SyntaxSupport(this);
            }
        }
        return syntaxSupport;
    }

    /** Perform any generic text processing. The advantage of this method
    * is that it allows the text to processed in line batches. The initial
    * size of the batch is given by the SettingsNames.LINE_BATCH_SIZE.
    * The TextBatchProcessor.processTextBatch() method is called for every
    * text batch. If the method returns true, it means the processing should
    * continue with the next batch of text which will have double line count
    * compared to the previous one. This guarantees there will be not too many
    * batches so the processing should be more efficient.
    * @param tbp text batch processor to be used to process the text batches
    * @param startPos starting position of the processing.
    * @param endPos ending position of the processing. This can be -1 to signal
    *   the end of document. If the endPos is lower than startPos then the batches
    *   are created in the backward direction.
    * @return the returned value from the last tpb.processTextBatch() call.
    *   The -1 will be returned for (startPos == endPos).
    */
    public int processText(TextBatchProcessor tbp, int startPos, int endPos)
    throws BadLocationException {
        if (endPos == -1) {
            endPos = getLength();
        }
        int batchLineCnt = ((Integer)getProperty(LINE_BATCH_SIZE)).intValue();
        int batchStart = startPos;
        int ret = -1;
        if (startPos < endPos) { // batching in forward direction
            while (ret < 0 && batchStart < endPos) {
                int batchEnd = Math.min(Utilities.getRowStart(this, batchStart, batchLineCnt), endPos);
                if (batchEnd == -1) { // getRowStart() returned -1
                    batchEnd = endPos;
                }
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        } else {
            while (ret < 0 && batchStart > endPos) {
                int batchEnd = Math.max(Utilities.getRowStart(this, batchStart, -batchLineCnt), endPos);
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        }
        return ret;
    }


    public boolean isIdentifierPart(char ch) {
        return identifierAcceptor.accept(ch);
    }

    public boolean isWhitespace(char ch) {
        return whitespaceAcceptor.accept(ch);
    }
    
    /**
     * When called within runnable of {@link #runAtomic(java.lang.Runnable) }
     * this method returns true if the document can be mutated or false
     * if any attempt of inserting/removing text would throw {@link GuardedException}.
     *
     * @return true if document can be mutated by
     * {@link #insertString(int, java.lang.String, javax.swing.text.AttributeSet)}
     *  or {@link #remove(int, int) }.
     *
     * @since 3.17
     */
    public boolean isModifiable() {
        return modifiable;
    }
    
    /** Inserts string into document */
    public @Override void insertString(int offset, String text, AttributeSet attrs)
    throws BadLocationException {
//        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
//            // Disabled due to failing OpenEditorEnablesEditMenuFactoryTest
//            if (!SwingUtilities.isEventDispatchThread()) {
//                throw new IllegalStateException("BaseDocument.insertString not in EDT: offset=" + // NOI18N
//                        offset + ", text=" + org.netbeans.lib.editor.util.CharSequenceUtilities.debugText(text)); // NOI18N
//            }
//        }
        
        // Always acquire atomic lock (it simplifies processing and improves readability)
        atomicLockImpl();
        try {
            checkModifiable(offset);
            DocumentFilter filter = getDocumentFilter();
            if (filter != null) {
                filter.insertString(getFilterBypass(), offset, text, attrs);
            } else {
                handleInsertString(offset, text, attrs);
            }
        } finally {
            atomicUnlockImpl(true);
        }
    }
    
    void handleInsertString(int offset, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null || text.length() == 0) {
            return;
        }

        // Check offset correctness
        if (offset < 0 || offset > getLength()) {
            throw new BadLocationException("Wrong insert position " + offset, offset); // NOI18N
        }

        // possible CR-LF to LF conversion
        text = ReadWriteUtils.convertToNewlines(text);

        incrementDocVersion();

        preInsertCheck(offset, text, attrs);

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("insertString(): doc="); // NOI18N
            appendInfoTerse(sb);
            sb.append(modified ? "" : " - first modification"). // NOI18N
                    append(", offset=").append(Utilities.offsetToLineColumnString(this, offset)); // NOI18N
            if (!debugNoText) {
                sb.append(" \"");
                appendContext(sb, offset);
                sb.append("\" + \""); // NOI18N
                if (modified) {
                    CharSequenceUtilities.debugText(sb, text);
                } else { // For first modification display regular text for easier orientation
                    sb.append(text);
                }
                sb.append("\""); // NOI18N
            }

            if (debugStack) {
                LOG.log(Level.FINE, sb.toString(), new Throwable("Insert stack"));  // NOI18N
            } else {
                LOG.log(Level.FINE, sb.toString());
            }
        }

        UndoableEdit edit = getContent().insertString(offset, text);

        BaseDocumentEvent evt = getDocumentEvent(offset, text.length(), DocumentEvent.EventType.INSERT, attrs);

        preInsertUpdate(evt, attrs);

        // Store modification text as an event's property
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(evt);
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(evt, String.class, text);
        if (postModificationDepth > 0) {
            DocumentPostModificationUtils.markPostModification(evt);
        }

        if (edit != null) {
            evt.addEdit(edit);

            lastModifyUndoEdit = edit; // #8692 check last modify undo edit
        }

        modified = true;

        if (atomicDepth > 0) {
            ensureAtomicEditsInited();
            atomicEdits.addEdit(evt); // will be added
        }

        insertUpdate(evt, attrs);

        evt.end();

        fireInsertUpdate(evt);

        boolean isComposedText = ((attrs != null)
                && (attrs.isDefined(StyleConstants.ComposedTextAttribute)));

        if (composedText && !isComposedText) {
            composedText = false;
        }
        if (!composedText && isComposedText) {
            composedText = true;
        }

        if (atomicDepth == 0 && !isComposedText) { // !!! check
            fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
        }

        postModificationDepth++;
        try {
            if (postModificationDocumentListener != null) {
                postModificationDocumentListener.insertUpdate(evt);
            }
            if (postModificationDocumentListenerList.getListenerCount() > 0) {
                for (DocumentListener listener : postModificationDocumentListenerList.getListeners()) {
                    listener.insertUpdate(evt);
                }
            }
        } finally {
            postModificationDepth--;
        }
    }
    
    private void appendContext(StringBuilder sb, int offset) {
        CharSequence docText = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(this);
        int contextLen = 20; // Context in forward and backward directions
        int back = contextLen;
        int endOffset = offset;
        int startOffset = offset;
        while (back > 0 && startOffset > 0) {
            if (docText.charAt(startOffset) == '\n') {
                break;
            }
            startOffset--;
            back--;
        }
        int docTextLen = docText.length();
        int forward = contextLen;
        while (forward > 0 && endOffset < docTextLen) {
            if (docText.charAt(endOffset++) == '\n') {
                break;
            }
            forward--;
        }
        if (startOffset > 0) {
            sb.append("...");
        }
        CharSequenceUtilities.debugText(sb, docText.subSequence(startOffset, offset));
        sb.append("|"); // Denote caret
        CharSequenceUtilities.debugText(sb, docText.subSequence(offset, endOffset));
        if (endOffset < docTextLen) {
            sb.append("...");
        }
    }

    public void checkTrailingSpaces(int offset) {
        try {
            int lineNum = Utilities.getLineOffset(this, offset);
            int lastEditedLine = lastPositionEditedByTyping != null ? Utilities.getLineOffset(this, lastPositionEditedByTyping.getOffset()) : -1;
            if (lastEditedLine != -1 && lastEditedLine != lineNum) {
                // clear trailing spaces in the last edited line
                Element root = getDefaultRootElement();
                Element elem = root.getElement(lastEditedLine);
                int start = elem.getStartOffset();
                int end = elem.getEndOffset();
                String line = getText(start, end - start);

                int endIndex = line.length() - 1;
                if (endIndex >= 0 && line.charAt(endIndex) == '\n') {
                    endIndex--;
                    if (endIndex >= 0 && line.charAt(endIndex) == '\r') {
                        endIndex--;
                    }
                }

                int startIndex = endIndex;
                while (startIndex >= 0 && Character.isWhitespace(line.charAt(startIndex)) && line.charAt(startIndex) != '\n' &&
                        line.charAt(startIndex) != '\r') {
                    startIndex--;
                }
                startIndex++;
                if (startIndex >= 0 && startIndex <= endIndex) {
                    remove(start + startIndex, endIndex - startIndex + 1);
                }
            }
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }

    /** Removes portion of a document */
    public @Override void remove(int offset, int length) throws BadLocationException {
//        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
//            if (!SwingUtilities.isEventDispatchThread()) {
//                throw new IllegalStateException("BaseDocument.insertString not in EDT: offset=" + // NOI18N
//                        offset + ", len=" + length); // NOI18N
//            }
//        }

        // Always acquire atomic lock (it simplifies processing and improves readability)
        atomicLockImpl();
        try {
            checkModifiable(offset);
            DocumentFilter filter = getDocumentFilter();
            if (filter != null) {
                filter.remove(getFilterBypass(), offset, length);
            } else {
                handleRemove(offset, length);
            }
        } finally {
            atomicUnlockImpl(true);
        }
    }
    
    void handleRemove(int offset, int length) throws BadLocationException {
        if (length == 0) {
            return;
        }
        if (length < 0) {
            throw new IllegalArgumentException("len=" + length + " < 0"); // NOI18N
        }
        if (offset < 0) {
            throw new BadLocationException("Wrong remove position " + offset + " < 0", offset); // NOI18N
        }
        if (offset + length > getLength()) {
            throw new BadLocationException("Wrong (offset+length)=" + (offset+length) +
                    " > getLength()=" + getLength(), offset + length); // NOI18N
        }

        incrementDocVersion();

        int docLen = getLength();
        if (offset < 0 || offset > docLen) {
            throw new BadLocationException("Wrong remove position " + offset, offset); // NOI18N
        }
        if (offset + length > docLen) {
            throw new BadLocationException("End offset of removed text " // NOI18N
                    + (offset + length) + " > getLength()=" + docLen, // NOI18N
                    offset + length); // NOI18N
        }

        preRemoveCheck(offset, length);

        BaseDocumentEvent evt = getDocumentEvent(offset, length, DocumentEvent.EventType.REMOVE, null);
        // Store modification text as an event's property
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(evt);
        String removedText = getText(offset, length);
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(evt, String.class, removedText);
        if (postModificationDepth > 0) {
            DocumentPostModificationUtils.markPostModification(evt);
        }

        removeUpdate(evt);

        UndoableEdit edit = ((EditorDocumentContent) getContent()).remove(offset, removedText);
        if (edit != null) {
            evt.addEdit(edit);

            lastModifyUndoEdit = edit; // #8692 check last modify undo edit
        }

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("remove(): doc="); // NOI18N
            appendInfoTerse(sb);
            sb.append(",origDocLen=").append(docLen); // NOI18N
            sb.append(", offset=").append(Utilities.offsetToLineColumnString(this, offset)); // NOI18N
            sb.append(",len=").append(length); // NOI18N
            if (!debugNoText) {
                sb.append(" \"");
                appendContext(sb, offset);
                sb.append("\" - \""); // NOI18N
                CharSequenceUtilities.debugText(sb, ((ContentEdit) edit).getText());
                sb.append("\""); // NOI18N
            }

            if (debugStack) {
                LOG.log(Level.FINE, sb.toString(), new Throwable("Remove text")); // NOI18N
            } else {
                LOG.log(Level.FINE, sb.toString());
            }
        }

        if (atomicDepth > 0) { // add edits as soon as possible
            ensureAtomicEditsInited();
            atomicEdits.addEdit(evt); // will be added
        }

        postRemoveUpdate(evt);

        evt.end();

        fireRemoveUpdate(evt);

        postModificationDepth++;
        try {
            if (postModificationDocumentListener != null) {
                postModificationDocumentListener.removeUpdate(evt);
            }
            if (postModificationDocumentListenerList.getListenerCount() > 0) {
                for (DocumentListener listener : postModificationDocumentListenerList.getListeners()) {
                    listener.removeUpdate(evt);
                }
            }
        } finally {
            postModificationDepth--;
        }

        if (atomicDepth == 0 && !composedText) {
            fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
        }
    }

    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        // Always acquire atomic lock (it simplifies processing and improves readability)
        atomicLockImpl();
        try {
            checkModifiable(offset);
            DocumentFilter filter = getDocumentFilter();
            if (filter != null) {
                filter.replace(getFilterBypass(), offset, length, text, attrs);
            } else {
                handleRemove(offset, length);
                handleInsertString(offset, text, attrs);
            }
        } finally {
            atomicUnlockImpl(true);
        }
    }
    
    private void checkModifiable(int offset) throws BadLocationException {
        if (!modifiable) {
            throw new GuardedException("Modification prohibited", offset); // NOI18N
        }
    }

    private DocumentFilter.FilterBypass getFilterBypass() {
        if (filterBypass == null) {
            filterBypass = new FilterBypassImpl();
        }
        return filterBypass;
    }

    /** This method is called automatically before the document
    * insertion occurs and can be used to revoke the insertion before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    * @param offset position where the insertion will be done
    * @param text string to be inserted
    * @param a attributes of the inserted text
    */
    protected void preInsertCheck(int offset, String text, AttributeSet a)
    throws BadLocationException {
    }

    /** This method is called automatically before the document
    * removal occurs and can be used to revoke the removal before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    * @param offset position where the insertion will be done
    * @param len length of the removal
    */
    protected void preRemoveCheck(int offset, int len)
    throws BadLocationException {
    }

    protected @Override void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);

        BaseDocumentEvent baseE = (BaseDocumentEvent)chng;

        lineRootElement.insertUpdate(baseE, baseE, attr);

        fixLineSyntaxState.update(false);
        chng.addEdit(fixLineSyntaxState.createAfterLineUndo());
        fixLineSyntaxState = null;

        firePreInsertUpdate(chng);
    }

    protected void preInsertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        fixLineSyntaxState = new FixLineSyntaxState(chng);
        chng.addEdit(fixLineSyntaxState.createBeforeLineUndo());
    }
    
    void firePreRemoveUpdate(DefaultDocumentEvent chng) {
        // Notify the remove update listeners - before the actual remove happens
        // so that it adheres to removeUpdate() logic; also the listeners can check
        // positions' offsets before the actual removal happens.
        for (DocumentListener listener: updateDocumentListenerList.getListeners()) {
            listener.removeUpdate(chng);
        }
    }

    void firePreInsertUpdate(DefaultDocumentEvent chng) {
        for (DocumentListener listener: updateDocumentListenerList.getListeners()) {
            listener.insertUpdate(chng);
        }
    }

    protected @Override void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        firePreRemoveUpdate(chng);
        // Remember the line changes here but add them to chng during postRemoveUpdate()
        // in order to satisfy the legacy syntax update mechanism
        removeUpdateLineUndo = lineRootElement.legacyRemoveUpdate(chng);

        fixLineSyntaxState = new FixLineSyntaxState(chng);
        chng.addEdit(fixLineSyntaxState.createBeforeLineUndo());
    }

    protected @Override void postRemoveUpdate(DefaultDocumentEvent chng) {
        super.postRemoveUpdate(chng);

        // addEdit() for previously remembered removeUpdateLineUndo
        if (removeUpdateLineUndo != null) {
            chng.addEdit(removeUpdateLineUndo);
            removeUpdateLineUndo = null;
        }

        fixLineSyntaxState.update(false);
        chng.addEdit(fixLineSyntaxState.createAfterLineUndo());
        fixLineSyntaxState = null;
    }

    public String getText(int[] block) throws BadLocationException {
        return getText(block[0], block[1] - block[0]);
    }

    /**
     * @param pos position of the first character to get.
     * @param len number of characters to obtain.
     * @return array with the requested characters.
     */
    public char[] getChars(int pos, int len) throws BadLocationException {
        char[] chars = new char[len];
        getChars(pos, chars, 0, len);
        return chars;
    }

    /**
     * @param block two-element array with starting and ending offset
     * @return array with the requested characters.
     */
    public char[] getChars(int[] block) throws BadLocationException {
        return getChars(block[0], block[1] - block[0]);
    }

    /**
     * @param pos position of the first character to get.
     * @param ret destination array
     * @param offset offset in the destination array.
     * @param len number of characters to obtain.
     */
    public void getChars(int pos, char ret[], int offset, int len)
    throws BadLocationException {
        DocumentUtilities.copyText(this, pos, pos + len, ret, offset);
    }

    /** Find something in document using a finder.
    * @param finder finder to be used for the search
    * @param startPos position in the document where the search will start
    * @param limitPos position where the search will be end with reporting
    *   that nothing was found.
    */
    public int find(Finder finder, int startPos, int limitPos)
    throws BadLocationException {
        int docLen = getLength();
        if (limitPos == -1) {
            limitPos = docLen;
        }
        if (startPos == -1) {
            startPos = docLen;
        }

        if (finder instanceof AdjustFinder) {
            if (startPos == limitPos) { // stop immediately
                finder.reset(); // reset() should be called in all the cases
                return -1; // must stop here because wouldn't know if fwd/bwd search?
            }

            boolean forwardAdjustedSearch = (startPos < limitPos);
            startPos = ((AdjustFinder)finder).adjustStartPos(this, startPos);
            limitPos = ((AdjustFinder)finder).adjustLimitPos(this, limitPos);
            boolean voidSearch = (forwardAdjustedSearch ? (startPos >= limitPos) : (startPos <= limitPos));
            if (voidSearch) {
                finder.reset();
                return -1;
            }
        }

        finder.reset();
        if (startPos == limitPos) {
            return -1;
        }

        Segment text = new Segment();
        int gapStart = ((EditorDocumentContent)getContent()).getCharContentGapStart();
        if (gapStart == -1) {
            throw new IllegalStateException("Cannot get gapStart"); // NOI18N
        }

        int pos = startPos; // pos at which the search starts (continues)
        boolean fwdSearch = (startPos <= limitPos); // forward search
        if (fwdSearch) {
            while (pos >= startPos && pos < limitPos) {
                int p0; // low bound
                int p1; // upper bound
                if (pos < gapStart) { // part below gap
                    p0 = startPos;
                    p1 = Math.min(gapStart, limitPos);
                } else { // part above gap
                    p0 = Math.max(gapStart, startPos);
                    p1 = limitPos;
                }

                getText(p0, p1 - p0, text);
                pos = finder.find(p0 - text.offset, text.array,
                        text.offset, text.offset + text.count, pos, limitPos);

                if (finder.isFound()) {
                    return pos;
                }
            }

        } else { // backward search limitPos < startPos
            pos--; // start one char below the upper bound
            while (limitPos <= pos && pos <= startPos) {
                int p0; // low bound
                int p1; // upper bound
                if (pos < gapStart) { // part below gap
                    p0 = limitPos;
                    p1 = Math.min(gapStart, startPos);
                } else { // part above gap
                    p0 = Math.max(gapStart, limitPos);
                    p1 = startPos;
                }

                getText(p0, p1 - p0, text);
                pos = finder.find(p0 - text.offset, text.array,
                        text.offset, text.offset + text.count, pos, limitPos);

                if (finder.isFound()) {
                    return pos;
                }
            }
        }

        return -1; // position outside bounds => not found
    }

    /** Fire the change event to repaint the given block of text.
     * @deprecated Please use <code>JTextComponent.getUI().damageRange()</code> instead.
     */
    @Deprecated
    public void repaintBlock(int startOffset, int endOffset) {
        BaseDocumentEvent evt = getDocumentEvent(startOffset,
                endOffset - startOffset, DocumentEvent.EventType.CHANGE, null);
        fireChangedUpdate(evt);
    }

    public void print(PrintContainer container) {
        print(container, true, true,0,getLength());
    }

    /**
     * Print into given container.
     *
     * @param container printing container into which the printing will be done.
     * @param usePrintColoringMap use printing coloring settings instead
     *  of the regular ones.
     * @param lineNumberEnabled if set to false the line numbers will not be printed.
     *  If set to true the visibility of line numbers depends on the settings
     *  for the line number visibility.
     * @param startOffset start offset of text to print
     * @param endOffset end offset of text to print
     */
    public void print(PrintContainer container, boolean usePrintColoringMap, boolean lineNumberEnabled, int startOffset,
                      int endOffset) {
        readLock();
        try {
            EditorUI editorUI;
            EditorKit kit = getEditorKit();
            if (kit instanceof BaseKit) {
                editorUI = ((BaseKit) kit).createPrintEditorUI(this, usePrintColoringMap, lineNumberEnabled);
            } else {
                editorUI = new EditorUI(this, usePrintColoringMap, lineNumberEnabled);
            }

            DrawGraphics.PrintDG printDG = new DrawGraphics.PrintDG(container);
            DrawEngine.getDrawEngine().draw(printDG, editorUI, startOffset, endOffset, 0, 0, Integer.MAX_VALUE);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, null, e);
        } finally {
            readUnlock();
        }
    }

    /**
     * Print into given container.
     *
     * @param container printing container into which the printing will be done.
     * @param usePrintColoringMap use printing coloring settings instead
     *  of the regular ones.
     * @param lineNumberEnabled if null, the visibility of line numbers is the same as it is given by settings
     *  for the line number visibility, otherwise the visibility equals the boolean value of the parameter
     * @param startOffset start offset of text to print
     * @param endOffset end offset of text to print
     */
    public void print(PrintContainer container, boolean usePrintColoringMap, Boolean lineNumberEnabled, int startOffset,
                      int endOffset) {
        readLock();
        try {
            boolean lineNumberEnabledPar = true;
            boolean forceLineNumbers = false;
            if (lineNumberEnabled != null) {
                lineNumberEnabledPar = lineNumberEnabled.booleanValue();
                forceLineNumbers = lineNumberEnabled.booleanValue();
            }

            EditorUI editorUI;
            EditorKit kit = getEditorKit();
            if (kit instanceof BaseKit) {
                editorUI = ((BaseKit) kit).createPrintEditorUI(this, usePrintColoringMap, lineNumberEnabledPar);
            } else {
                editorUI = new EditorUI(this, usePrintColoringMap, lineNumberEnabledPar);
            }

            if (forceLineNumbers) {
                editorUI.setLineNumberVisibleSetting(true);
                editorUI.setLineNumberEnabled(true);
                editorUI.updateLineNumberWidth(0);
            }

            DrawGraphics.PrintDG printDG = new DrawGraphics.PrintDG(container);
            DrawEngine.getDrawEngine().draw(printDG, editorUI, startOffset, endOffset, 0, 0, Integer.MAX_VALUE);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, null, e);
        } finally {
            readUnlock();
        }
    }

    /** Create biased position in document */
    public Position createPosition(int offset, Position.Bias bias) throws BadLocationException {
        EditorDocumentContent content = (EditorDocumentContent) getContent();
        Position pos;
        if (bias == Position.Bias.Forward) {
            pos = content.createPosition(offset);
        } else {
            pos = content.createBackwardBiasPosition(offset);
        }
        return pos;
    }

    /** Return array of root elements - usually only one */
    public @Override Element[] getRootElements() {
        Element[] elems = new Element[1];
        elems[0] = getDefaultRootElement();
        return elems;
    }

    /** Return default root element */
    public @Override Element getDefaultRootElement() {
        if (defaultRootElem == null) {
            defaultRootElem = lineRootElement;
        }
        return defaultRootElem;
    }

    /** Runs the runnable under read lock. */
    public @Override void render(Runnable r) {
        readLock();
        try {
            r.run();
        } finally {
            readUnlock();
        }
    }

    /** Runs the runnable under write lock. This is a stronger version
    * of the runAtomicAsUser() method, because if there any locked sections
    * in the documents this methods breaks the modification locks and modifies
    * the document.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomic(Runnable r) {
        runAtomicAsUser(r);
    }

    /** Runs the runnable under write lock.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomicAsUser(Runnable r) {
        atomicLockImpl ();
        try {
            r.run();
        // Only attempt to recover (undo the document modifications) from runtime exceptions.
        // Do not attempt to recover from java.lang.Error or other Throwable subclasses.
        } catch (RuntimeException ex) {
            boolean completed = false;
            try {
                breakAtomicLock();
                completed = true;
            } finally {
                if (completed) {
                    throw ex;
                } else {
                    // Log thrown exception in case breakAtomicLock() throws an exception by itself.
                    LOG.log(Level.INFO, "Runtime exception thrown in BaseDocument.runAtomicAsUser() leading to breakAtomicLock():", ex);
                }
            }
        } finally {
            atomicUnlockImpl ();
        }
    }

    /** Insert contents of reader at specified position into document.
    * @param reader reader from which data will be read
    * @param pos on which position that data will be inserted
    */
    public void read(Reader reader, int pos)
    throws IOException, BadLocationException {
        extWriteLock();
        try {

            if (pos < 0 || pos > getLength()) {
                throw new BadLocationException("BaseDocument.read()", pos); // NOI18N
            }
            ReadWriteBuffer buffer = ReadWriteUtils.read(reader);
            if (!inited) { // Fill line-separator properties
                String lineSeparator = ReadWriteUtils.findFirstLineSeparator(buffer);
                if (lineSeparator == null) {
                    lineSeparator = (String) getProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                    if (lineSeparator == null) {
                        lineSeparator = ReadWriteUtils.getSystemLineSeparator();
                    }
                }
                putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, lineSeparator);
            }
            if (debugRead) {
                String ls = (String) getProperty(READ_LINE_SEPARATOR_PROP);
                if (ls != null) {
                    ls = org.netbeans.lib.editor.util.CharSequenceUtilities.debugText(text);
                }
                LOG.log(Level.INFO, "BaseDocument.read(): Will insert {0} chars, lineSeparatorProperty: \"{1}\", StreamDescriptionProperty: {2}\n",
                        new Object[] { buffer.length(), ls, getProperty(StreamDescriptionProperty)} );
            }
            insertString(pos, buffer.toString(), null);
            inited = true; // initialized but not modified

            // Workaround for #138951:
            // BaseDocument.read method is only called when loading the document from a file
            // or from JEditorPane.setText(), which two operations are equivalent in terms
            // that they reset document's content from an external source. Therefore the list
            // of remebered modified regions should be cleared.

            // Reset modified regions accounting after the initial load
            Boolean inPaste = BaseKit.IN_PASTE.get();
            if (inPaste == null || !inPaste) {
                ModRootElement modElementRoot = ModRootElement.get(this);
                if (modElementRoot != null) {
                    modElementRoot.resetMods(null);
                }
            }
            lastModifyUndoEdit = null;
        } finally {
            extWriteUnlock();
        }
    }

    /** Write part of the document into specified writer.
    * @param writer writer into which data will be written.
    * @param pos from which position get the data
    * @param len how many characters write
    */
    public void write(Writer writer, int pos, int len)
    throws IOException, BadLocationException {
        readLock();
        try {

            if ((pos < 0) || ((pos + len) > getLength())) {
                throw new BadLocationException("BaseDocument.write()", pos); // NOI18N
            }
            String lineSeparator = (String) getProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP);
            if (lineSeparator == null) {
                lineSeparator = (String) getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP);
                if (lineSeparator == null) {
                    lineSeparator = (String) getProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                    if (lineSeparator == null) {
                        lineSeparator = ReadWriteUtils.getSystemLineSeparator();
                    }
                }
            }
            CharSequence docText = (CharSequence) getProperty(CharSequence.class);
            // Skip extra '\n' (added by AbstractDocument convention) at the end of char sequence
            ReadWriteBuffer buffer = ReadWriteUtils.convertFromNewlines(docText, pos, pos + len, lineSeparator);
            ReadWriteUtils.write(writer, buffer);
            writer.flush();
        } finally {
            readUnlock();
        }
    }

    /** Invalidate the state-infos in all the syntax-marks
     * in the whole document. The Syntax can call this method
     * if it changes its internal state in the way that affects
     * the future returned tokens. The syntax-state-info in all
     * the marks is reset and it will be lazily restored when necessary.
     */
    public void invalidateSyntaxMarks() {
        extWriteLock();
        try {
            FixLineSyntaxState.invalidateAllSyntaxStateInfos(this);
            BaseDocumentEvent evt = getDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE, null);
            fireChangedUpdate(evt);
        } finally {
          extWriteUnlock();
        }
    }

    /** Get the number of spaces the TAB character ('\t') visually represents.
     * This is related to <code>SettingsNames.TAB_SIZE</code> setting.
     */
    public int getTabSize() {
        return tabSize;
    }

    /** Get the width of one indentation level.
     * The algorithm first checks whether there's a value for the INDENT_SHIFT_WIDTH
     * setting. If so it uses it, otherwise it uses <code>formatter.getSpacesPerTab()</code>.
     *
     * @see #getTabSize()
     * @deprecated Please use Editor Indentation API instead, for details see
     *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
     */
    @Deprecated
    public int getShiftWidth() {
        return shiftWidth;
    }

    /**
     * @deprecated Don't use implementation class of editor kits. Use mime type,
     *   <code>MimePath</code> and <code>MimeLookup</code>.
     */
    @Deprecated
    public final Class getKitClass() {
        return getEditorKit().getClass();
    }

    private EditorKit getEditorKit() {
        EditorKit editorKit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
        if (editorKit == null) {
            // Try 'text/plain'
            LOG.log(Level.CONFIG, "No registered editor kit for ''{0}'', trying ''text/plain''.", mimeType);
            editorKit = MimeLookup.getLookup("text/plain").lookup(EditorKit.class); //NOI18N
            if (editorKit == null) {
                LOG.config("No registered editor kit for 'text/plain', using default.");
                editorKit = new PlainEditorKit();
            }
        }
        return editorKit;
    }

//    private static String s2s(Object o) {
//        return o == null ? "null" : o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o));
//    }
//
    private void setMimeType(String mimeType) {
        if (!this.mimeType.equals(mimeType)) {
//            String oldMimeType = this.mimeType;
            this.mimeType = mimeType;

//            new Throwable("~~~ setMimeType: '" + oldMimeType + "' -> '" + mimeType + "'").printStackTrace(System.out);
//
            if (prefs != null && weakPrefsListener != null) {
                try {
                    prefs.removePreferenceChangeListener(weakPrefsListener);
//                    System.out.println("~~~ setMimeType: " + s2s(prefs) + " removing " + s2s(weakPrefsListener));
                } catch (IllegalArgumentException e) {
//                    System.out.println("~~~ IAE: doc=" + s2s(this) + ", '" + oldMimeType + "' -> '" + this.mimeType + "', prefs=" + s2s(prefs) + ", wpl=" + s2s(weakPrefsListener));
                }
                weakPrefsListener = null;
            }
            prefs = MimeLookup.getLookup(this.mimeType).lookup(Preferences.class);
            prefsListener.preferenceChange(null);
//            System.out.println("~~~ setMimeType: '" + this.mimeType + "' -> " + s2s(prefs));

            // reinitialize the document
            EditorKit kit = getEditorKit();
            if (kit instanceof BaseKit) {
                // careful here!! some kits set document's mime type from initDocument
                // even worse, the new mime type can be different from the one passed to the constructor,
                // which will result in recursive call to this method
                ((BaseKit) kit).initDocument(this);
            }

            if (weakPrefsListener == null) {
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
//                System.out.println("~~~ setMimeType: " + s2s(prefs) + " adding " + s2s(weakPrefsListener));
            }
        }
    }

    /** This method prohibits merging of the next document modification
    * with the previous one even if it would be normally possible.
    */
    public void resetUndoMerge() {
        undoMergeReset = true;
    }
    
    /* Defined because of the hack for undo()
     * in the BaseDocumentEvent.
     */
    protected @Override void fireChangedUpdate(DocumentEvent e) {
        super.fireChangedUpdate(e);
    }
    protected @Override void fireInsertUpdate(DocumentEvent e) {
        super.fireInsertUpdate(e);
    }
    protected @Override void fireRemoveUpdate(DocumentEvent e) {
        super.fireRemoveUpdate(e);
    }

    protected @Override void fireUndoableEditUpdate(UndoableEditEvent e) {
        // Possibly wrap contained edit
        if (undoEditWrappers != null) {
            UndoableEdit edit = e.getEdit();
            ListUndoableEdit listEdit = null;
            for (UndoableEditWrapper wrapper : undoEditWrappers) {
                UndoableEdit wrapEdit = wrapper.wrap(edit, this);
                if (wrapEdit != edit) {
                    if (listEdit == null) {
                        listEdit = new ListUndoableEdit(edit, wrapEdit);
                    } else {
                        listEdit.setDelegate(wrapEdit);
                    }
                    edit = wrapEdit;
                }
            }
            if (listEdit != null) {
                e = new UndoableEditEvent(this, listEdit);
            }
        }
        
	// Fire to the list of listeners that was used before the atomic lock started
        // This fixes issue #47881 and appears to be somewhat more logical
        // than the default approach to fire all the current listeners
	Object[] listeners = (atomicLockListenerList != null)
            ? atomicLockListenerList
            : listenerList.getListenerList();

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == UndoableEditListener.class) {
		((UndoableEditListener)listeners[i + 1]).undoableEditHappened(e);
	    }
	}

        // Since UndoManager may only do um.addEdit() the original resetting
        // in AtomicCompoundEdit.replaceEdit() would not be called in such case.
        undoMergeReset = false;
    }

    /** Extended write locking of the document allowing
    * reentrant write lock acquiring.
    */
    public final void extWriteLock() {
        super.writeLock(); // AD.writeLock() already reentrant for several JDK releases
    }

    /** Extended write unlocking.
    * @see #extWriteLock()
    */
    public final void extWriteUnlock() {
        super.writeUnlock(); // AD.writeUnlock() already reentrant for several JDK releases
    }

    /** 
     * 
     * @deprecated Please use {@link BaseDocument#runAtomic(java.lang.Runnable)} instead.
     */
    @Deprecated
    @Override
    public final void atomicLock () {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Use runAtomic() instead of atomicLock()", new Exception()); // NOI18N
        }
        atomicLockImpl(); // Need to be outside synchronized(this) due to VetoableChangeListener firing
    }
    
    final synchronized void atomicLockImpl () {
        if (runExclusiveDepth > 0) {
            throw new IllegalStateException(
                    "Document modifications or atomic locking not allowed in runExclusive()"); // NOI18N
        }
        boolean alreadyAtomicLocker = Thread.currentThread() == getCurrentWriter() && atomicDepth > 0;
        if (alreadyAtomicLocker) {
            atomicDepth++;
        }
        if (!alreadyAtomicLocker) {
            // Starting with openide.text v 6.58 it is no longer necessary to fire vetoable change listener
            // since the BaseDocument extends AbstractDocument and so the openide.text installs
            // a document filter which possibly prevents modifications on readonly files.
//            // Fire VetoableChangeListener outside Document lock
//            VetoableChangeListener l = (VetoableChangeListener) getProperty(MODIFICATION_LISTENER_PROP);
//            boolean modifiableLocal = true;
//            if (l != null) {
//                try {
//                    // Notify modification by Boolean.TRUE
//                    l.vetoableChange(new PropertyChangeEvent(this, "modified", null, Boolean.TRUE));
//                } catch (java.beans.PropertyVetoException ex) {
//                    modifiableLocal = false;
//                }
//            }
            // Acquire writeLock() and increment atomicDepth
            synchronized (this) {
                extWriteLock();
                atomicDepth++;
                if (atomicDepth == 1) { // lock really started
//                    Object o = getProperty(EDITABLE_PROP);
//                    if (o == null) {
//                        o = Boolean.TRUE;
//                    }
//                    modifiableChanged = modifiable != modifiableLocal || 
//                            o != modifiableLocal;                
//                    modifiable = modifiableLocal;
                    fireAtomicLock(atomicLockEventInstance);
                    // Copy the listener list - will be used for firing undo
                    atomicLockListenerList = listenerList.getListenerList();
                }
            }
        }
//        if (modifiableChanged) {
//            putProperty(EDITABLE_PROP, modifiable);
//        }
    }

    /** 
     * 
     * @deprecated Please use {@link BaseDocument#runAtomic(java.lang.Runnable)} instead.
     */
    @Deprecated
    @Override
    public final synchronized void atomicUnlock () {
        atomicUnlockImpl ();
    }
    
    final void atomicUnlockImpl () {
        atomicUnlockImpl(true);
    }

    final void atomicUnlockImpl (boolean notifyUnmodifyIfNoMods) {
        boolean noModsAndOuterUnlock = false;
        synchronized (this) {
            if (atomicDepth <= 0) {
                throw new IllegalStateException("atomicUnlock() without atomicLock()"); // NOI18N
            }

            if (--atomicDepth == 0) { // lock really ended
                fireAtomicUnlock(atomicLockEventInstance);

                noModsAndOuterUnlock = !checkAndFireAtomicEdits();
                atomicLockListenerList = null;
                extWriteUnlock();
            }
        }

        if (notifyUnmodifyIfNoMods && noModsAndOuterUnlock) {
            // Notify unmodification if there were no document modifications
            // inside the atomic section.
            // Fire VetoableChangeListener outside Document lock
            VetoableChangeListener l = (VetoableChangeListener) getProperty(MODIFICATION_LISTENER_PROP);
            if (l != null) {
                try {
                    // Notify unmodification by Boolean.FALSE
                    l.vetoableChange(new PropertyChangeEvent(this, "modified", null, Boolean.FALSE));
                } catch (java.beans.PropertyVetoException ex) {
                    // Ignored (should not be thrown)
                }
            }
        }
    }

    /** Is the document currently atomically locked?
    * It's not synced as this method must be called only from writer thread.
    */
    public final boolean isAtomicLock() {
        return (atomicDepth > 0);
    }

    /** Break the atomic lock so that doc is no longer in atomic mode.
    * All the performed changes are rolled back automatically.
    * Even after calling this method, the atomicUnlock() must still be called.
    * This method is not synced as it must be called only from writer thread.
    */
    public final void breakAtomicLock() {
        undoAtomicEdits();
    }

    public @Override void atomicUndo() {
        breakAtomicLock();
    }

    /**
     * Adapts old AtomicLockListener to the new API; wraps the old listener into a new interface
     */
    private static class OldListenerAdapter implements org.netbeans.api.editor.document.AtomicLockListener {
        private final AtomicLockListener delegate;

        public OldListenerAdapter(AtomicLockListener delegate) {
            this.delegate = delegate;
        }

        public void atomicLock(org.netbeans.api.editor.document.AtomicLockEvent evt) {
            delegate.atomicLock((AtomicLockEvent)evt);
        }

        public void atomicUnlock(org.netbeans.api.editor.document.AtomicLockEvent evt) {
            delegate.atomicUnlock((AtomicLockEvent)evt);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final OldListenerAdapter other = (OldListenerAdapter) obj;
            if (!Objects.equals(this.delegate, other.delegate)) {
                return false;
            }
            return true;
        }
    }

    public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener l) {
        listenerList.add(org.netbeans.api.editor.document.AtomicLockListener.class, l);
    }

    public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener l) {
        listenerList.remove(org.netbeans.api.editor.document.AtomicLockListener.class, l);
    }

    /**
     * @param l 
     * @deprecated use LineDocumentUtils.as(doc, AtomicLockDocument.class).addAtomicLockListener(l);
     */
    @Deprecated
    public @Override void addAtomicLockListener(AtomicLockListener l) {
        addAtomicLockListener(new OldListenerAdapter(l));
    }

    /**
     * @param l 
     * @deprecated use LineDocumentUtils.as(doc, AtomicLockDocument.class).removeAtomicLockListener(l);
     */
    @Deprecated
    public @Override void removeAtomicLockListener(AtomicLockListener l) {
        removeAtomicLockListener(new OldListenerAdapter(l));
    }

    private void fireAtomicLock(AtomicLockEvent evt) {
        EventListener[] listeners = listenerList.getListeners(org.netbeans.api.editor.document.AtomicLockListener.class);
        int cnt = listeners.length;
        for (int i = 0; i < cnt; i++) {
            ((org.netbeans.api.editor.document.AtomicLockListener)listeners[i]).atomicLock(evt);
        }
    }

    private void fireAtomicUnlock(AtomicLockEvent evt) {
        EventListener[] listeners = listenerList.getListeners(org.netbeans.api.editor.document.AtomicLockListener.class);
        int cnt = listeners.length;
        for (int i = 0; i < cnt; i++) {
            ((org.netbeans.api.editor.document.AtomicLockListener)listeners[i]).atomicUnlock(evt);
        }
    }

    protected final int getAtomicDepth() {
        return atomicDepth;
    }

    void runExclusive(Runnable r) {
        boolean writeLockDone = false;
        synchronized (this) {
            Thread currentWriter = getCurrentWriter();
            if (currentWriter != Thread.currentThread()) {
                assert (runExclusiveDepth == 0) : "runExclusiveDepth=" + runExclusiveDepth + " != 0"; // NOI18N
                writeLock();
                writeLockDone = true;
            }
            runExclusiveDepth++;   
        }
        try {
            r.run();
        } finally {
            runExclusiveDepth--;
            if (writeLockDone) {
                writeUnlock();
                assert (runExclusiveDepth == 0) : "runExclusiveDepth=" + runExclusiveDepth + " != 0"; // NOI18N
            }
            
        }
    }

    @Override
    public void addDocumentListener(DocumentListener listener) {
        if (LOG_LISTENER.isLoggable(Level.FINE)) {
            LOG_LISTENER.fine("ADD DocumentListener of class " + listener.getClass() + " to existing " +
                    org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentListenerCount(this) +
                    " listeners. Listener: " + listener + '\n'
            );
            if (LOG_LISTENER.isLoggable(Level.FINER)) {
                LOG_LISTENER.log(Level.FINER, "    StackTrace:\n", new Exception());
            }
        }
	if (!org.netbeans.lib.editor.util.swing.DocumentUtilities.addPriorityDocumentListener(
                this, listener, DocumentListenerPriority.DEFAULT))
            super.addDocumentListener(listener);
    }

    @Override
    public void removeDocumentListener(DocumentListener listener) {
        if (LOG_LISTENER.isLoggable(Level.FINE)) {
            LOG_LISTENER.fine("REMOVE DocumentListener of class " + listener.getClass() + " from existing " +
                    org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentListenerCount(this) +
                    " listeners. Listener: " + listener + '\n'
            );
            if (LOG_LISTENER.isLoggable(Level.FINER)) {
                LOG_LISTENER.log(Level.FINER, "    StackTrace:\n", new Exception());
            }
        }
	if (!org.netbeans.lib.editor.util.swing.DocumentUtilities.removePriorityDocumentListener(
                this, listener, DocumentListenerPriority.DEFAULT))
            super.removeDocumentListener(listener);
    }

    protected BaseDocumentEvent createDocumentEvent(int pos, int length,
            DocumentEvent.EventType type) {
        return new BaseDocumentEvent(this, pos, length, type);
    }

    /* package */ final BaseDocumentEvent getDocumentEvent(int pos, int length, DocumentEvent.EventType type, AttributeSet attribs) {
        BaseDocumentEvent bde = createDocumentEvent(pos, length, type);
        bde.attachChangeAttribs(attribs);
        return bde;
    }

    /**
     * Set or clear a special document listener that gets notified
     * after the modification and that is allowed to do further
     * mutations to the document.
     * <br>
     * Additional mutations will be made in a single atomic transaction
     * with an original mutation.
     * <br>
     * This functionality may be used for example by code templates
     * to synchronize other regions of the document with the one
     * currently being modified.
     * <br>
     * If there is an active post modification document listener
     * then each document modification is encapsulated in an atomic lock
     * transaction automatically to allow further changes inside a transaction.
     *
     * @deprecated Use addPostModificationDocumentListener(DocumentListener)
     */
    @Deprecated
    public void setPostModificationDocumentListener(DocumentListener listener) {
        this.postModificationDocumentListener = listener;
    }

    /**
     * Add a special document listener that gets notified
     * after the modification and that is allowed to do further
     * mutations to the document.
     * <br>
     * Additional mutations will be made in a single atomic transaction
     * with an original mutation.
     * <br>
     * This functionality may be used for example by code templates
     * to synchronize other regions of the document with the one
     * currently being modified.
     * <br>
     * If there is an active post modification document listener
     * then each document modification is encapsulated in an atomic lock
     * transaction automatically to allow further changes inside a transaction.
     *
     * @since 1.25
     */
    public void addPostModificationDocumentListener(DocumentListener listener) {
        postModificationDocumentListenerList.add(listener);
    }

    public void removePostModificationDocumentListener(DocumentListener listener) {
        postModificationDocumentListenerList.remove(listener);
    }

    /**
     * Add a special document listener that gets notified after physical
     * insertion/removal has been done but when the document event
     * (which is a {@link javax.swing.undo.CompoundEdit}) is still
     * opened for extra undoable edits that can be added by the clients (listeners).
     *
     * @param listener non-null listener to be added.
     * @since 1.27
     */
    public void addUpdateDocumentListener(DocumentListener listener) {
        updateDocumentListenerList.add(listener);
    }

    public void removeUpdateDocumentListener(DocumentListener listener) {
        updateDocumentListenerList.remove(listener);
    }

    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
        super.addUndoableEditListener(listener);
        if (LOG.isLoggable(Level.FINE)) {
            UndoableEditListener[] listeners = getUndoableEditListeners();
            if (listeners.length > 1) {
                // Having two UE listeners may be dangerous - for example
                // having two undo managers attached at once will lead to strange errors
                // since only one of the UMs will work normally while processing
                // in the other one will be (typically silently) failing.
                LOG.log(Level.INFO, "Two or more UndoableEditListeners attached", new Exception()); // NOI18N
            }
        }
    }

    /**
     * Add a custom undoable edit during atomic lock of the document.
     * <br>
     * For example code templates use this method to mark an insertion of a code template
     * skeleton into the document. Once the edit gets undone the CT editing will be cancelled.
     *
     * @param edit non-null undoable edit.
     * @throws IllegalStateException if the document is not under atomic lock.
     * @since 1.29
     */
    public void addUndoableEdit(UndoableEdit edit) {
        if (!isAtomicLock())
            throw new IllegalStateException("This method can only be called under atomic-lock."); // NOI18N
        ensureAtomicEditsInited();
        atomicEdits.addEdit(edit);
    }

    /** Was the document modified by either insert/remove
    * but not the initial read)?
    */
    public boolean isModified() {
        return modified;
    }

    public @Override Element getParagraphElement(int pos) {
        return lineRootElement.getElement(lineRootElement.getElementIndex(pos));
    }

    /** Returns object which represent list of annotations which are
     * attached to this document.
     * @return object which represent attached annotations
     */
    public Annotations getAnnotations() {
        synchronized (annotationsLock) {
            if (annotations == null) {
                annotations = new Annotations(this);
            }
            return annotations;
        }
    }

    /**
     * @see LineRootElement#prepareSyntax()
     */
    void prepareSyntax(Segment text, Syntax syntax, int reqPos, int reqLen,
    boolean forceLastBuffer, boolean forceNotLastBuffer) throws BadLocationException {
        FixLineSyntaxState.prepareSyntax(this, text, syntax, reqPos, reqLen,
            forceLastBuffer, forceNotLastBuffer);
    }

    int getTokenSafeOffset(int offset) {
        return FixLineSyntaxState.getTokenSafeOffset(this, offset);
    }

    /** Get position on line from visual column. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param visCol visual column
    * @param startLineOffset position of line start
    * @return position on line for particular x-coord
    */
    int getOffsetFromVisCol(int visCol, int startLineOffset)
    throws BadLocationException {
        int tabSize = getTabSize();
        CharSequence docText = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(this);
        int docTextLen = docText.length();
        int curVisCol = 0;
        int offset = startLineOffset;
        for (; offset < docTextLen; offset++) {
            if (curVisCol >= visCol) {
                return offset;
            }
            char ch = docText.charAt(offset);
            switch (ch) {
                case '\t':
                    curVisCol = (curVisCol + tabSize) / tabSize * tabSize;
                    break;
                case '\n':
                    return offset;
                default:
                    // #17356
                    int codePoint;
                    if (Character.isHighSurrogate(ch) && offset + 1 < docTextLen) {
                        codePoint = Character.toCodePoint(ch, docText.charAt(++offset));
                    } else {
                        codePoint = ch;
                    }
                    int w = WcwdithUtil.wcwidth(codePoint);
                    curVisCol += w > 0 ? w : 0;
                    break;
            }
        }
        return offset;
    }

    /** Get visual column from position. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param offset position for which the visual column should be returned
    *   the function itself computes the begining of the line first
    */
    int getVisColFromPos(int offset) throws BadLocationException {
        return Utilities.getVisColFromPos(this, offset);
    }

    protected Dictionary createDocumentProperties(Dictionary origDocumentProperties) {
        return new LazyPropertyMap(origDocumentProperties);
    }

    private void ensureAtomicEditsInited() {
        if (atomicEdits == null) {
            atomicEdits = new AtomicCompoundEdit();
        }
    }

    private boolean checkAndFireAtomicEdits() {
        if (atomicEdits != null && atomicEdits.size() > 0) {
            // Some edits performed
            atomicEdits.end();
            AtomicCompoundEdit nonEmptyAtomicEdits = atomicEdits;
            atomicEdits = null; // Clear the var to allow doc.runAtomic() in undoableEditHappened()
            fireUndoableEditUpdate(new UndoableEditEvent(this, nonEmptyAtomicEdits));
            return true;
        } else {
            return false;
        }
    }

    private void undoAtomicEdits() {
        if (atomicEdits != null && atomicEdits.size() > 0) {
            atomicEdits.end();
            if (atomicEdits.canUndo()) {
                atomicEdits.undo();
            } else {
                LOG.log(Level.WARNING,
                        "Cannot UNDO: " + atomicEdits.toString() + // NOI18N
                        " Edits: " + atomicEdits.getEdits(),       // NOI18N
                        new CannotUndoException());
            }
            atomicEdits = null;
        }
    }

    void clearAtomicEdits() {
        atomicEdits = null;
    }
    
    UndoableEdit startOnSaveTasks() {
        assert (atomicDepth > 0); // Should only be called under atomic lock
        // If there would be any pending edits
        // fire them so that they don't clash with on-save tasks in undo manager.
        // Pending edits could occur due to an outer atomic lock
        // around CES.saveDocument(). Anyway it would generally be an undesirable situation
        // due to possibly invalid lock order (doc's atomic lock would then precede on-save tasks' locks).
        checkAndFireAtomicEdits();
        ensureAtomicEditsInited();
        return atomicEdits;

    }
    
    void endOnSaveTasks(boolean success) {
        if (success) { // Possibly fire atomic edit
            checkAndFireAtomicEdits();
        } else { // Undo edits contained in atomic edit
            undoAtomicEdits();
        }
    }

    UndoableEdit markAtomicEditsNonSignificant() {
        assert (atomicDepth > 0); // Should only be called under atomic lock
        ensureAtomicEditsInited();
        atomicEdits.setSignificant(false);
        return atomicEdits;
    }
    
    void appendInfoTerse(StringBuilder sb) {
        sb.append(getClass().getSimpleName()).append("@").append(System.identityHashCode(this));
        sb.append(",version=").append(org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentVersion(this));
        sb.append(",StreamDesc=").append(getProperty(StreamDescriptionProperty));
    }
    
    public @Override String toString() {
        return super.toString() +
            ", mimeType='" + mimeType + "'" + //NOI18N
            ", kitClass=" + deprecatedKitClass + // NOI18N
            ", length=" + getLength() + // NOI18N
            ", version=" + org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentVersion(this) + // NOI18N
            ", file=" + getProperty(StreamDescriptionProperty); //NOI18N
    }

    /** Detailed debug info about the document */
    public String toStringDetail() {
        return toString() + ", content:\n  " + getContent().toString();
    }

    /* package */ void incrementDocVersion() {
        ((AtomicLong) getProperty(VERSION_PROP)).incrementAndGet();
        ((AtomicLong) getProperty(LAST_MODIFICATION_TIMESTAMP_PROP)).set(System.currentTimeMillis());
    }

    /** Compound edit that write-locks the document for the whole processing
     * of its undo operation.
     */
    class AtomicCompoundEdit extends StableCompoundEdit {

        private static final int MERGE_INDEX_NOT_INITIALIZED = -1;
        /**
         * Marker value for mergeEditIndex to signal that the merge is not possible
         * (value must be less than MERGE_INDEX_NOT_INITIALIZED).
         */
        private static final int MERGE_PROHIBITED = -2;

        private UndoableEdit previousEdit;

        private boolean nonSignificant;
        
        /**
         * If an edit gets added that prohibits merge then this flag is set to true.
         */
        private int mergeEditIndex = MERGE_INDEX_NOT_INITIALIZED;
        
        public @Override void undo() throws CannotUndoException {
            atomicLockImpl ();
            try {
                TokenHierarchyControl<?> thcInactive = thcInactive();
                try {
                    super.undo();
                } finally {
                    if (thcInactive != null) {
                        thcInactive.setActive(true);
                    }
                }
            } finally {
                atomicUnlockImpl ();
            }

            if (previousEdit != null) {
                previousEdit.undo();
            }

        }

        public @Override void redo() throws CannotRedoException {
            if (previousEdit != null) {
                previousEdit.redo();
            }

            atomicLockImpl ();
            try {
                TokenHierarchyControl<?> thcInactive = thcInactive();
                try {
                    super.redo();
                } finally {
                    if (thcInactive != null) {
                        thcInactive.setActive(true);
                    }
                }
            } finally {
                atomicUnlockImpl ();
            }
        }

        private TokenHierarchyControl<?> thcInactive() {
            TokenHierarchyControl<?> thc = null;
            if (getEdits().size() > DEACTIVATE_LEXER_THRESHOLD) {
                MutableTextInput<?> input = (MutableTextInput<?>)
                        BaseDocument.this.getProperty(MutableTextInput.class);
                if (input != null && (thc = input.tokenHierarchyControl()) != null && thc.isActive()) {
                    thc.setActive(false);
                } else {
                    thc = null;
                }
            }
            return thc;
        }

        public @Override void die() {
            super.die();

            if (previousEdit != null) {
                // Should not always be previousEdit != this ??
                // Apparently not, see the stacktrace in #145634.
                if (previousEdit != this) {
                    previousEdit.die();
                }
                previousEdit = null;
            }
        }

        public int size() {
            return getEdits().size();
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (super.addEdit(anEdit)) {
                if (mergeEditIndex >= MERGE_INDEX_NOT_INITIALIZED) { // Valid or not inited
                    if (anEdit instanceof BaseDocumentEvent) {
                        mergeEditIndex = size() - 1;
                    } else if (anEdit.getClass() != BaseDocument.AtomicCompoundEdit.class &&
                        !CaretUndo.isCaretUndoEdit(anEdit)
                    ) {
                        mergeEditIndex = MERGE_PROHIBITED; // Do not allow merging if there are unknown undoable edits
                    }
                }
                return true;
            }
            return false;
        }
        
        public @Override boolean replaceEdit(UndoableEdit anEdit) {
            if (nonSignificant) { // Non-significant edit must be replacing
                previousEdit = anEdit;
                // Becomes significant
                nonSignificant = false;
                return true;
            }
            if (!undoMergeReset && mergeEditIndex >= 0) { // Only merge if this edit contains BaseDocumentEvent child item
                List<UndoableEdit> thisEdits = getEdits();
                BaseDocumentEvent thisMergeEdit = (BaseDocumentEvent) thisEdits.get(mergeEditIndex);
                if (anEdit instanceof BaseDocument.AtomicCompoundEdit) {
                    BaseDocument.AtomicCompoundEdit anAtomicEdit
                            = (BaseDocument.AtomicCompoundEdit)anEdit;
                    List<UndoableEdit> anAtomicEditChildren = anAtomicEdit.getEdits();
                    for (int i = 0; i < anAtomicEditChildren.size(); i++) {
                        UndoableEdit child = (UndoableEdit)anAtomicEditChildren.get(i);
                        if (child instanceof BaseDocumentEvent && thisMergeEdit.canMerge((BaseDocumentEvent)child)) {
                            previousEdit = anEdit;
                            return true;
                        }
                    }
                } else if (anEdit instanceof BaseDocumentEvent) {
                    BaseDocumentEvent evt = (BaseDocumentEvent)anEdit;

                    if (thisMergeEdit.canMerge(evt)) {
                        previousEdit = anEdit;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean isSignificant() {
            return !nonSignificant;
        }

        public void setSignificant(boolean significant) {
            this.nonSignificant = !significant;
        }
        
        BaseDocumentEvent getMergeEdit() {
            return (BaseDocumentEvent) ((mergeEditIndex >= 0) ? getEdits().get(mergeEditIndex) : null);
        }

    } // End of AtomicCompoundEdit

    /** Property evaluator is useful for lazy evaluation
     * of properties of the document when
     * {@link javax.swing.text.Document#getProperty(Object)}
     * is called.
     */
    public static interface PropertyEvaluator {
        public Object getValue();
    }

    private static final class MimeTypePropertyEvaluator implements BaseDocument_PropertyHandler {

        private final BaseDocument doc;
        private String hackMimeType = null;

        public MimeTypePropertyEvaluator(BaseDocument baseDocument) {
            this.doc = baseDocument;
        }

        public @Override Object getValue() {
            if (hackMimeType == null) {
                return doc.mimeType;
            } else {
                return hackMimeType;
            }
        }

        public @Override Object setValue(Object value) {
            String mimeType = value == null ? null : value.toString();
            assert MimePath.validate(mimeType) : "Invalid mime type: '" + mimeType + "'"; //NOI18N

            // XXX: hack to support Tools-Options' "testNNNN_*" mime types
            boolean hackNewMimeType = mimeType != null && mimeType.startsWith("test"); //NOI18N

//            // Do not change anything, just sanity checks
//            if (value != null && LOG.isLoggable(Level.WARNING)) {
//                String msg = null;
//                if (doc.editorKit != null) {
//                    msg = "Trying to set " + MIME_TYPE_PROP + " property to '" + mimeType + //NOI18N
//                        "' on properly initialized document " + doc; //NOI18N
//                } else {
//                    // baseDocument initialized by the deprecated constructor
//                    if (!mimeType.equals(doc.getEditorKit().getContentType())) {
//                        msg = "Trying to set " + MIME_TYPE_PROP + " property to '" + mimeType + //NOI18N
//                            "', which is inconsistent with the content type of document's editor kit, document = " + doc; //NOI18N
//                    }
//                }
//                if (msg != null && !hackNewMimeType) {
//                    LOG.log(Level.WARNING, null, new Throwable(msg));
//                }
//            }

            String oldValue = (String) getValue();
            if (hackNewMimeType) {
                hackMimeType = mimeType;
            } else {
                doc.setMimeType(mimeType);
            }

            return oldValue;
        }
    } // End of MimeTypePropertyEvaluator class

    protected static class LazyPropertyMap extends Hashtable {

        private PropertyChangeSupport pcs = null;

        protected LazyPropertyMap(Dictionary dict) {
            super(5);

            Enumeration en = dict.keys();
            while (en.hasMoreElements()) {
                Object key = en.nextElement();
                super.put(key, dict.get(key));
            }
        }

        public @Override Object get(Object key) {
            Object val = super.get(key);
            if (val instanceof PropertyEvaluator) {
                val = ((PropertyEvaluator)val).getValue();
            }

            return val;
        }

        public @Override Object put(Object key, Object value) {
             if (key == PropertyChangeSupport.class && value instanceof PropertyChangeSupport) {
                 pcs = (PropertyChangeSupport) value;
             }

             Object old = null;
             boolean usePlainPut = true;

              if (key != null) {
                  Object val = super.get(key);
                  if (val instanceof BaseDocument_PropertyHandler) {
                      old = ((BaseDocument_PropertyHandler) val).setValue(value);
                      usePlainPut = false;
                  }
              }

             if (usePlainPut) {
                 old = super.put(key, value);
             }

             if (key instanceof String) {
                 if (pcs != null) {
                     pcs.firePropertyChange((String) key, old, value);
                 }
             }

             return old;
        }
    } // End of LazyPropertyMap class

    private static final class Accessor extends EditorPackageAccessor {

        @Override
        public UndoableEdit BaseDocument_markAtomicEditsNonSignificant(BaseDocument doc) {
            return doc.markAtomicEditsNonSignificant();
        }

        @Override
        public void BaseDocument_clearAtomicEdits(BaseDocument doc) {
            doc.clearAtomicEdits();
        }

        @Override
        public MarkVector BaseDocument_getMarksStorage(BaseDocument doc) {
            return null; // doc.marksStorage no longer supported; DrawEngine no longer used
        }

        @Override
        public Mark BaseDocument_getMark(BaseDocument doc, MultiMark multiMark) {
            return null; // doc.marks.get(multiMark) no longer supported; DrawEngine no longer used
        }

        @Override
        public void Mark_insert(Mark mark, BaseDocument doc, int pos) throws InvalidMarkException, BadLocationException {
            mark.insert(doc, pos);
        }

        @Override
        public void ActionFactory_reformat(Reformat formatter, Document doc, int startPos, int endPos, AtomicBoolean canceled) throws BadLocationException {
            ActionFactory.reformat(formatter, doc, startPos, endPos, canceled);
        }

        @Override
        public Object BaseDocument_newServices(BaseDocument doc) {
            return new ServicesImpl(doc);
        }

        @Override
        public int MarkBlockChain_adjustPos(MarkBlockChain chain, int pos, boolean thisBlock) {
            return thisBlock ?
                    chain.adjustToBlockHead(pos) : chain.adjustToPrevBlockEnd(pos);
        }
    }

    // XXX: the same as the one in CloneableEditorSupport
    private static final class PlainEditorKit extends DefaultEditorKit implements ViewFactory {
        static final long serialVersionUID = 1L;
        PlainEditorKit() {
        }

        /** @return cloned instance
        */
        public @Override Object clone() {
            return new PlainEditorKit();
        }

        /** @return this (I am the ViewFactory)
        */
        public @Override ViewFactory getViewFactory() {
            return this;
        }

        /** Plain view for the element
        */
        public @Override View create(Element elem) {
            return new WrappedPlainView(elem);
        }

        /** Set to a sane font (not proportional!). */
        public @Override void install(JEditorPane pane) {
            super.install(pane);
            pane.setFont(new Font("Monospaced", Font.PLAIN, pane.getFont().getSize() + 1)); //NOI18N
        }
    } // End of PlainEditorKit class

    class FilterBypassImpl extends DocumentFilter.FilterBypass {

        public Document getDocument() {
            return BaseDocument.this;
        }

        public void remove(int offset, int length) throws BadLocationException {
            handleRemove(offset, length);
        }

        public void insertString(int offset, String string, AttributeSet attrs) throws BadLocationException {
            handleInsertString(offset, string, attrs);
        }

        public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            handleRemove(offset, length);
            handleInsertString(offset, text, attrs);
        }
    }

    /**
    * Implementation of EditorDocumentServices for BaseDocument.
    *
    * @author Miloslav Metelka
    */
    private static final class BaseDocumentServices implements EditorDocumentServices {
        
        static final EditorDocumentServices INSTANCE = new BaseDocumentServices();

        @Override
        public void runExclusive(Document doc, Runnable r) {
            BaseDocument bDoc = (BaseDocument) doc;
            bDoc.runExclusive(r);
        }

        @Override
        public void resetUndoMerge(Document doc) {
            BaseDocument bDoc = (BaseDocument) doc;
            bDoc.resetUndoMerge();
        }

        @Override
        public UndoableEdit startOnSaveTasks(Document doc) {
            BaseDocument bDoc = (BaseDocument) doc;
            return bDoc.startOnSaveTasks();
        }

        @Override
        public void endOnSaveTasks(Document doc, boolean success) {
            BaseDocument bDoc = (BaseDocument) doc;
            bDoc.endOnSaveTasks(success);
        }

    }

    static final class ServicesImpl implements CharClassifier, 
            org.netbeans.api.editor.document.AtomicLockDocument {
        private final BaseDocument doc;

        public ServicesImpl(BaseDocument doc) {
            this.doc = doc;
        }
        
        @Override
        public boolean isIdentifierPart(char ch) {
            return doc.isIdentifierPart(ch);
        }

        @Override
        public boolean isWhitespace(char ch) {
            return doc.isWhitespace(ch);
        }

        public void atomicLock() {
            doc.atomicLockImpl();
        }

        public void atomicUnlock() {
            doc.atomicUnlockImpl();
        }

        @Override
        public void atomicUndo() {
            doc.atomicUndo();
        }

        @Override
        public void runAtomic(Runnable r) {
            doc.runAtomic(r);
        }
        
        @Override
        public void runAtomicAsUser(Runnable r) {
            doc.runAtomicAsUser(r);
        }

        @Override
        public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener l) {
            doc.addAtomicLockListener(l);
        }

        @Override
        public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener l) {
            doc.removeAtomicLockListener(l);
        }

        @Override
        public Document getDocument() {
            return doc;
        }

        /*
        @Override
        public int find(org.netbeans.api.editor.document.Finder f, int from, int limit) throws BadLocationException {
            return doc.find2(f, from, limit);
        }
        */
    }

}
