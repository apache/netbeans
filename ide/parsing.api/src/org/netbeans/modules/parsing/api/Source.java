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

package org.netbeans.modules.parsing.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.impl.ParserEventForward;
import org.netbeans.modules.parsing.impl.SchedulerAccessor;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.implspi.SchedulerControl;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.implspi.SourceFactory;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;


/**
 * The <code>Source</code> represents one file or document.
 *
 * <p>An instance of <code>Source</code>
 * can either be obtained for a <code>FileObject</code> or <code>Document</code>. If
 * a particular <code>FileObject</code> and a <code>Document</code> are tied together
 * in the way that the <code>Document</code> was loaded from the <code>FileObject</code>
 * using either of them will get the same <code>Source</code> instance.
 *
 * <p class="nonnormative">Please note that the infrastructure does not keep
 * <code>Source</code> instances forever and they can and will be garbage collected
 * if nobody references them. This also means that two successive <code>Source.create</code>
 * calls for the same file or document will return two different <code>Source</code>
 * instances if the first instance is garbage collected prior the second call.
 *
 * <p>
 * The Source can be created with a {@link Lookup} instance. This Lookup should be used
 * by parsing system, or callback tasks to locate services, that depend on the execution
 * context (ie which user is executing the task). Check the specific service's documentation 
 * on details of the service's context dependencies.
 * 
 * @author Jan Jancura
 * @author Tomas Zezula
 * @since 9.2 implements Lookup.Provider
 */
public final class Source implements Lookup.Provider {

    /**
     * Gets a <code>Source</code> instance for a file. The <code>FileObject</code>
     * passed to this method has to be a valid data file. There is no <code>Source</code>
     * representation for a folder.
     * 
     * @param fileObject The file to get <code>Source</code> for.
     *
     * @return The <code>Source</code> for the given file or <code>null</code>
     *   if the file doesn't exist.
     */
    // XXX: this should really be called 'get'
    public static Source create (
        FileObject          fileObject
    ) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        if (!fileObject.isValid() || !fileObject.isData()) {
            return null;
        }
        
        return _get(fileObject.getMIMEType(), fileObject, Lookup.getDefault());
    }
    
    /**
     * Gets a <code>Source</code> instance for a file. The <code>FileObject</code>
     * passed to this method has to be a valid data file. There is no <code>Source</code>
     * representation for a folder.
     * <p/>
     * This form allows to specify Lookup that provides access to context-dependent
     * services for the parsing system and the user tasks called from parsing API.
     * 
     * @param fileObject The file to get <code>Source</code> for.
     * @param lkp The Lookup that provides the context
     * @return The <code>Source</code> for the given file or <code>null</code>
     *   if the file doesn't exist.
     * @since 9.2
     */
    public static Source create (
        FileObject          fileObject, Lookup lkp
    ) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        Parameters.notNull("lkp", lkp); //NOI18N
        if (!fileObject.isValid() || !fileObject.isData()) {
            return null;
        }
        return _get(fileObject.getMIMEType(), fileObject, lkp);
    }

    /**
     * Gets a <code>Source</code> instance for a <code>Document</code>. This method
     * is consistent with {@link #create(org.openide.filesystems.FileObject)} in the way
     * that they both will return the same <code>Source</code> instance for
     * documents loaded from files. For example the following asserts will never fail
     * (providing that relevant method calls return non-null).
     * 
     * <pre>
     * // #1
     * Source source = Source.create(file);
     * assert(source == Source.create(source.getFileObject()));
     * assert(source == Source.create(source.getDocument()));
     *
     * // #2
     * Source source = Source.create(document);
     * assert(source == Source.create(source.getDocument()));
     * assert(source == Source.create(source.getFileObject()));
     * </pre>
     *
     * <p class="nonnormative">Please note that you can get <code>Source</code> instance for any arbitrary
     * document no matter if it was loaded from a file or not. However, the editor
     * infrastructure generally does not support creation of fileless documents that
     * are later saved and re-bound to a <code>FileObject</code>. If you wish to do
     * something like that you will have to create a new <code>Document</code> instance
     * loaded from the <code>FileObject</code> and use it instead of the original one.
     *
     * @param document The <code>Document</code> to get <code>Source</code> for.
     *
     * @return The <code>Source</code> for the given document; never <code>null</code>.
     */
    // XXX: this should really be called 'get'
    public static Source create (
        Document            document
    ) {
        return create(document, Lookup.getDefault());
    }
    
    /**
     * Gets a <code>Source</cide> instance for a <code>Document</code>. For details, please 
     * see {@link #create(javax.swing.text.Document)}; this form allows to specify
     * a Lookup that provides access to user or context-dependent services.
     * 
     * @param document the document to get <code>Source</code> for
     * @param lkp the context for the source
     * @return the Source instance
     * @see #create(javax.swing.text.Document) 
     * @since 9.2
     */
    public static Source create (
        Document            document, Lookup lkp
    ) {
        Parameters.notNull("document", document); //NOI18N

        String mimeType = DocumentUtilities.getMimeType(document);
        if (mimeType == null) {
            throw new NullPointerException("Netbeans documents must have 'mimeType' property: " //NOI18N
                + document.getClass() + "@" + Integer.toHexString(System.identityHashCode(document))); //NOI18N
        }

        synchronized (Source.class) {
            @SuppressWarnings("unchecked") //NOI18N
            Reference<Source> sourceRef = (Reference<Source>) document.getProperty(Source.class);
            Source source = sourceRef == null ? null : sourceRef.get();

            //#154813: the file may have been moved:
            if (source != null && source.getFileObject() != null && !source.getFileObject().isValid()) {
                source = null;
            }
            
            if (source == null) {
                FileObject fileObject = Utilities.getFileObject(document);
                if (fileObject != null) {
                    source = Source._get(mimeType, fileObject, lkp);
                } else {
                    if ("text/x-dialog-binding".equals(mimeType)) { //NOI18N
                        InputAttributes attributes = (InputAttributes) document.getProperty(InputAttributes.class);
                        LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                        Document doc = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
                        if (doc != null) {
                            fileObject = Utilities.getFileObject(doc);
                        } else {
                            fileObject = (FileObject) attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                        }
                    }
                    source = new Source(mimeType, document, fileObject, lkp);
                }
                document.putProperty(Source.class, new WeakReference<Source>(source));
            }

            assert source != null : "No Source for " + document; //NOI18N
            return source;
        }
    }

    /**
     * Gets this <code>Source</code>'s mime type. It's the mime type of the <code>Document</code>
     * represented by this source. If the document has not yet been loaded it's
     * the mime type of the <code>FileObject</code>.
     * 
     * @return The mime type.
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * Gets the <code>Document</code> represented by this source. This method
     * returns either the document, which was used to obtain this <code>Source</code>
     * instance in {@link #create(javax.swing.text.Document)} or the document that
     * has been loaded from the <code>FileObject</code> used in {@link #create(org.openide.filesystems.FileObject)}.
     *
     * <p>Please note that this method can return <code>null</code> in case that
     * this <code>Source</code> was created for a file and there has not been yet
     * a document loaded from this file.
     * 
     * @return The <code>Document</code> represented by this <code>Source</code>
     *   or <code>null</code> if no document has been loaded yet.
     */
    public Document getDocument (boolean forceOpen) {
        if (preferFile.get()) {
            if (!forceOpen) {
                return null;
            } else {
                boolean ae = false;
                assert  ae = true;
                if (ae) {
                    LOG.log(
                       Level.INFO,
                       "Calling Source.getDocument(forceOpen=true) for Source preferring files -> possible performance problem {0}",    //NOI18N
                       Arrays.asList(Thread.currentThread().getStackTrace()));
                }
            }
        }
        if (document != null) return document;
        assert fileObject != null;
        try {
            return sourceEnv.readDocument(fileObject, forceOpen);
        } catch (IOException ioe) {
            LOG.log (Level.WARNING, null, ioe);
            return null;
        }
    }
    
    /**
     * Gets the <code>FileObject</code> represented by this source. This method
     * returns either the file, which was used to obtain this <code>Source</code>
     * instance in {@link #create(org.openide.filesystems.FileObject)} or the file that
     * the document represented by this <code>Source</code> was loaded from.
     *
     * <p>Please note that this method can return <code>null</code> in case that
     * this <code>Source</code> was created for a fileless document (ie. <code>Document</code>
     * instance that was not loaded from a file).
     *
     * @return The <code>FileObject</code> or <code>null</code> if this <code>Source</code>
     *   was created for a fileless document.
     */
    public FileObject getFileObject () {
        return fileObject;
    }

    /**
     * Creates a new <code>Snapshot</code> of the contents of this <code>Source</code>.
     * A snapshot is an immutable static copy of the contents represented by this
     * <code>Source</code>. The snapshot is created from the document, if it exists.
     * If the document has not been loaded yet the snapshot will be created from the
     * file.
     * 
     * @return The <code>Snapshot</code> of the current content of this source.
     */
    public Snapshot createSnapshot () {
        final CharSequence [] text = new CharSequence [] {""}; //NOI18N
        final int [][] lineStartOffsets = new int [][] { new int [] { 0 } };
        final Document doc = getDocument (false);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, null, new Throwable("Creating snapshot: doc=" + doc + ", file=" + fileObject)); //NOI18N
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Creating snapshot: doc={0}, file={1}", new Object [] { doc, fileObject }); //NOI18N
        }
        try {
            if (doc == null) {
                // Ideally we should use CloneableEditorSupport.getEditorKit (mimeType),
                // which would return EditorKit implementation registered for this Source's mimeType.
                // However, since all Netbeans kit's are subclasses of BaseKit and BaseKit
                // delegates its read/write methods to a document (ie. BaseDocument in this case)
                // this has severe performance implications such as #157676.
                //
                // The code below is a copy of CharacterConversions.lineSeparatorToLineFeed() method
                // and it handles line-end conversion. In general EditorKits can do more conversions,
                // but they usually don't and this should be good enough for Snapshots.
                try {
                    if (fileObject.isValid ()) {
                        if (fileObject.getSize() <= Utilities.getMaxFileSize()) {
                            final InputStream is = fileObject.getInputStream ();
                            assert is != null : "FileObject.getInputStream() returned null for FileObject: " + FileUtil.getFileDisplayName(fileObject); //NOI18N
                            try {
                                InputStreamReader reader = new InputStreamReader (
                                        is,
                                        FileEncodingQuery.getEncoding (fileObject)
                                );
                                try {
                                    StringBuilder output = new StringBuilder(Math.max(16, (int) fileObject.getSize()));
                                    List<Integer> lso = new LinkedList<Integer>();
                                    boolean lastCharCR = false;
                                    char [] buffer = new char [65536];
                                    int size = -1;

                                    final char LF = '\n'; //NOI18N, Unicode line feed (0x000A)
                                    final char CR = '\r'; //NOI18N, Unicode carriage return (0x000D)
                                    final char LS = 0x2028; // Unicode line separator (0x2028)
                                    final char PS = 0x2029; // Unicode paragraph separator (0x2029)

                                    lso.add(0);
                                    while(-1 != (size = reader.read(buffer, 0, buffer.length))) {
                                        for(int i = 0; i < size; i++) {
                                            char ch = buffer[i];
                                            if (lastCharCR && ch == LF) {
                                                // CR-LF pair changed to single LF
                                                continue;
                                            }
                                            if (ch == CR) {
                                                // convert to LF; subsequent LF will be skipped
                                                output.append(LF);
                                                lso.add(output.length());
                                                lastCharCR = true;
                                            } else if (ch == LS || ch == PS) { // Unicode LS, PS
                                                output.append(LF);
                                                lso.add(output.length());
                                                lastCharCR = false;
                                            } else { // current char not CR
                                                lastCharCR = false;
                                                output.append(ch);
                                            }
                                        }
                                    }

                                    int [] lsoArr = new int [lso.size()];
                                    int idx = 0;
                                    for(Integer offset : lso) {
                                        lsoArr[idx++] = offset;
                                    }

                                    text[0] = output;
                                    lineStartOffsets[0] = lsoArr;
                                } finally {
                                    reader.close ();
                                }
                            } finally {
                                is.close ();
                            }
                        } else {
                            LOG.log(
                                Level.WARNING,
                                "Source {0} of size: {1} has been ignored due to large size. Files large then {2} bytes are ignored, you can increase the size by parse.max.file.size property.",  //NOI18N
                                new Object[]{
                                    FileUtil.getFileDisplayName(fileObject),
                                    fileObject.getSize(),
                                    Utilities.getMaxFileSize()
                                });
                        }
                    }
                } catch (FileNotFoundException fnfe) {
                    // working with a stale FileObject, just ignore this (eg see #158119)
                } catch (IOException ioe) {
                    LOG.log (Level.WARNING, null, ioe);
                }
            } else {
                final Document d = doc;
                d.render (new Runnable () {
                    public @Override void run () {
                        try {
                            int length = d.getLength ();
                            if (length < 0) {
                                text[0] = ""; //NOI18N
                                lineStartOffsets[0] = new int [] { 0 };
                            } else {
                                Element root = DocumentUtilities.getParagraphRootElement(d);
                                int [] lso = new int [root.getElementCount()];
                                for(int i = 0; i < lso.length; i++) {
                                    lso[i] = root.getElement(i).getStartOffset();
                                }
                                text[0] = d.getText (0, length);
                                lineStartOffsets[0] = lso;
                            }
                        } catch (BadLocationException ble) {
                            LOG.log (Level.WARNING, null, ble);
                        }
                    }
                });
            }
        } catch (OutOfMemoryError oome) {
            // Use empty snapshot
            text[0] = ""; //NOI18N
            lineStartOffsets[0] = new int [] { 0 };

            // Diagnostics and workaround for issues such as #170290
            LOG.log(Level.INFO, null, oome);

            if (doc != null) {
                LOG.warning("Can't create snapshot of " + doc + ", size=" + doc.getLength() + ", mimeType=" + mimeType); //NOI18N
            } else {
                LOG.warning("Can't create snapshot of " + fileObject + ", size=" + fileObject.getSize() + ", mimeType=" + mimeType); //NOI18N
            }
        }

        return Snapshot.create (
            text[0], lineStartOffsets[0], this,
            MimePath.get (mimeType),
            new int[][] {new int[] {0, 0}},
            new int[][] {new int[] {0, 0}}
        );
    }

    public @Override String toString() {
        return super.toString() + "[mimeType=" + mimeType + ", fileObject=" + fileObject + ", document=" + document; //NOI18N
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    // -J-Dorg.netbeans.modules.parsing.api.Source.level=FINE
    private static final Logger LOG = Logger.getLogger(Source.class.getName());
    private static final ThreadLocal<Boolean> suppressListening = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    private static final ThreadLocal<Boolean> preferFile = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    static {
        SourceAccessor.setINSTANCE(new MySourceAccessor());
    }
        
    private final Lookup context;
    private final String mimeType;
    private final FileObject fileObject;
    private final Document document;

    private final Set<SourceFlags> flags = Collections.synchronizedSet(EnumSet.noneOf(SourceFlags.class));
    
    private int taskCount;
    private volatile Parser cachedParser;
    private final AtomicReference<ExtendableSourceModificationEvent> sourceModificationEvent = new AtomicReference<>();
    private final ASourceModificationEvent unspecifiedSourceModificationEvent = new ASourceModificationEvent (this, true, -1, -1);
    private Map<Class<? extends Scheduler>, SchedulerEvent> schedulerEvents;
    //GuardedBy(this)
    private SourceCache     cache;
    //GuardedBy(flags)
    private volatile long eventId;
    private volatile boolean listeningOnChanges;

    /**
     * SourceEnvironment callback
     */
    private final SourceControl ctrl = new SourceControl(this);
    /**
     * Binding of source to its environment
     */
    private final SourceEnvironment sourceEnv;
    /**
     * Demultiplexes events from all parsers to SourceEnvironment.
     */
    private final ParserEventForward peFwd;

    @SuppressWarnings("LeakingThisInConstructor")
    private Source (
        String              mimeType,
        Document            document,
        FileObject          fileObject,
        Lookup              context
    ) {
        this.mimeType =     mimeType;
        this.document =     document;
        this.fileObject =   fileObject;
        this.context =      context;
        this.peFwd = new ParserEventForward();
        this.sourceEnv = Utilities.createEnvironment(this, ctrl);
    }
    
    @NonNull
    private static Source _get(
            @NonNull final String mimeType,
            @NonNull final FileObject fileObject, 
            @NonNull final Lookup context) {
        synchronized (Source.class) {
            final Source source = SourceFactory.getDefault().createSource(fileObject, mimeType, context);
            return source;
        }
    }

    private void assignListeners () {
        final boolean listen = !suppressListening.get();
        if (listen) {
            if (listeningOnChanges) {
                return;
            }
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                if (listeningOnChanges) {
                    return;
                }
                try {
                    sourceEnv.activate();
                } finally {
                    listeningOnChanges = true;
                }
            }
        }
    }

    private void setFlags(final Set<SourceFlags> flags) {
        // synchronized because of eventId
        synchronized (flags) {
            this.flags.addAll(flags);
            eventId++;
        }
    }

    private void setSourceModification (boolean sourceChanged, int startOffset, int endOffset) {
        ExtendableSourceModificationEvent oldSourceModificationEvent;
        ExtendableSourceModificationEvent newSourceModificationEvent;
        do {
            oldSourceModificationEvent = sourceModificationEvent.get();
            if (oldSourceModificationEvent == null) {
                newSourceModificationEvent = new ASourceModificationEvent (this, sourceChanged, startOffset, endOffset);
            } else {
                newSourceModificationEvent = oldSourceModificationEvent.add(sourceChanged, startOffset, endOffset);
            }
        } while (!sourceModificationEvent.compareAndSet(oldSourceModificationEvent, newSourceModificationEvent));
    }

    private SourceCache getCache() {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (cache == null)
                cache = new SourceCache (this, null);
            return cache;
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject") // accessing internals is accessor's job
    private static class MySourceAccessor extends SourceAccessor {
        
        @Override
        public void setFlags (final Source source, final Set<SourceFlags> flags)  {
            assert source != null;
            assert flags != null;
            source.setFlags(flags);
        }

        @Override
        public boolean testFlag (final Source source, final SourceFlags flag) {
            assert source != null;
            assert flag != null;
            return source.flags.contains(flag);
        }

        @Override
        public boolean cleanFlag (final Source source, final SourceFlags flag) {
            assert source != null;
            assert flag != null;  
            return source.flags.remove(flag);            
        }

        @Override
        public boolean testAndCleanFlags (final Source source, final SourceFlags test, final Set<SourceFlags> clean) {
            assert source != null;
            assert test != null;
            assert clean != null;
            synchronized (source.flags) {
                boolean res = source.flags.contains(test);
                source.flags.removeAll(clean);
                return res;
            }
        }

        @Override
        public void invalidate (final Source source, final boolean force) {
            assert source != null;
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                final boolean invalid = source.flags.remove(SourceFlags.INVALID);
                if (force || invalid) {
                    final SourceCache cache = getCache(source);
                    assert cache != null;
                    cache.invalidate();
                }
            }
        }

        @Override
        public boolean invalidate(Source source, long id, Snapshot snapshot) {
            assert source != null;
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                if (snapshot == null) {
                    return !source.flags.contains(SourceFlags.INVALID);
                }
                else {
                    //The eventId and flags are bound
                    long eventId;
                    eventId = source.eventId;
                    if (id != eventId) {
                        return false;
                    }
                    else {
                        source.flags.remove(SourceFlags.INVALID);
                        final SourceCache cache = getCache(source);
                        assert cache != null;
                        cache.invalidate();
                        return true;
                    }
                }
            }
        }

        @Override
        public void attachScheduler(Source src, SchedulerControl sched, boolean attach) {
            src.sourceEnv.attachScheduler(sched, attach);
        }
        
        @Override
        public Parser getParser(Source source) {
            assert source != null;
            return source.cachedParser;
        }

        @Override
        public void setParser(Source source, Parser parser) throws IllegalStateException {
            assert source != null;
            assert parser != null;
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                if (source.cachedParser != null) {
                    throw new IllegalStateException();
                }
                source.cachedParser = parser;
            }
        }
        
        @Override
        public void assignListeners (@NonNull final Source source) {
            assert source != null;
            source.assignListeners();
        }
        
        @Override
        public SourceControl getEnvControl(final Source source) {
            assert source != null;
            return source.ctrl;
        }

        @Override
        public SourceEnvironment getEnv(final Source source) {
            assert source != null;
            return source.sourceEnv;
        }

        @Override
        public long getLastEventId (final Source source) {
            assert source != null;
            return source.eventId;
        }

        @Override
        public void setSourceModification (Source source, boolean sourceChanged, int startOffset, int endOffset) {
            assert source != null;
            source.setSourceModification(sourceChanged, startOffset, endOffset);
        }

        @Override
        public void mimeTypeMayChanged(@NonNull final Source source) {
            assert source != null;
            final FileObject file = source.getFileObject();
            if (file != null && !Objects.equals(source.getMimeType(), file.getMIMEType())) {
                synchronized (Source.class) {
                    SourceFactory.getDefault().removeSource(file);
                }
            }
        }

        @Override
        public void parsed (Source source) {
            source.sourceModificationEvent.set(null);
        }

        @Override
        public SourceModificationEvent getSourceModificationEvent (Source source) {
            assert source != null;
            SourceModificationEvent event = (SourceModificationEvent) source.sourceModificationEvent.get();
            if (event == null) {
                event = source.unspecifiedSourceModificationEvent;
            }
            return event;
        }

        @Override
        public Map<Class<? extends Scheduler>,SchedulerEvent> createSchedulerEvents(
                @NonNull final Source source,
                @NonNull final Iterable<? extends Scheduler> schedulers,
                @NonNull final SourceModificationEvent sourceModificationEvent) {
            Parameters.notNull("source", source);   //NOI18N
            Parameters.notNull("schedulers", schedulers);   //NOI18N
            Parameters.notNull("sourceModificationEvent", sourceModificationEvent); //NOI18N
            final Map<Class<? extends Scheduler>,SchedulerEvent> result = new HashMap<Class<? extends Scheduler>, SchedulerEvent>();
            for (Scheduler scheduler : schedulers) {
                final SchedulerEvent schedulerEvent = SchedulerAccessor.get ().createSchedulerEvent (scheduler, sourceModificationEvent);
                if (schedulerEvent != null) {
                    result.put (scheduler.getClass (), schedulerEvent);
                }
            }
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                source.schedulerEvents = result;
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public void setSchedulerEvent(
                final @NonNull Source source,
                final @NonNull Scheduler scheduler,
                final @NonNull SchedulerEvent event) {
            Parameters.notNull("source", source);           //NOI18N
            Parameters.notNull("scheduler", scheduler);     //NOI18N
            Parameters.notNull("event", event);             //NOI18N
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                if (source.schedulerEvents == null) {
                    source.schedulerEvents = new HashMap<Class<? extends Scheduler>, SchedulerEvent>();
                }
                source.schedulerEvents.put(scheduler.getClass(), event);
            }
        }

        @Override
        public SchedulerEvent getSchedulerEvent (Source source, Class<? extends Scheduler> schedulerType) {
            if (schedulerType == null) {
                return null;
            }
            if (source.schedulerEvents == null)
                return null;
            return source.schedulerEvents.get (schedulerType);
        }

        @Override
        public SourceCache getCache (final Source source) {
            assert source != null;
            return source.getCache();
        }

        @Override
        public int taskAdded(final Source source) {
            assert source != null;
            int ret;
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                ret = source.taskCount++;
            }
            return ret;
        }

        @Override
        public int taskRemoved(final Source source) {
            assert source != null;
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                return --source.taskCount;
            }
        }

        @Override
        public Source get(final FileObject file) {
            synchronized (Source.class) {
                return SourceFactory.getDefault().getSource(file);
            }
        }

        @Override
        public void suppressListening(
                final boolean suppress,
                final boolean prefer) {
            suppressListening.set(suppress);
            preferFile.set(prefer);
        }

        @Override
        public int getLineStartOffset(Snapshot snapshot, int lineIdx) {
            assert snapshot != null;
            assert snapshot.lineStartOffsets != null : "Line offsets can only be obtained for a top-level snapshot"; //NOI18N
            assert lineIdx >= 0 && lineIdx <= snapshot.lineStartOffsets.length :
                "Invalid lineIdx=" + lineIdx + ", lineStartOffsets.length=" + snapshot.lineStartOffsets.length; //NOI18N

            if (lineIdx < snapshot.lineStartOffsets.length) {
                return snapshot.lineStartOffsets[lineIdx];
            } else {
                return snapshot.getText().length();
            }
        }

        @Override
        public Snapshot createSnapshot(CharSequence text, int[] lineStartOffsets, Source source, MimePath mimePath, int[][] currentToOriginal, int[][] originalToCurrent) {
            return Snapshot.create(text, lineStartOffsets, source, mimePath, currentToOriginal, originalToCurrent);
        }

        @Override
        public SourceCache getAndSetCache(Source source, SourceCache sourceCache) {
            SourceCache origCache = source.cache;

            source.cache = sourceCache;

            return origCache;
        }

        @Override
        @NonNull
        public ParserEventForward getParserEventForward(@NonNull final Source source) {
            return source.peFwd;
        }

        @Override
        public Source create(@NonNull final FileObject file, @NonNull final String mimeType, @NonNull final Lookup context) {
            return new Source(mimeType, null, file, context);
        }
    } // End of MySourceAccessor class
    private static interface ExtendableSourceModificationEvent {
         ExtendableSourceModificationEvent add(final boolean changed, int start, int end);
    }
    private static class ASourceModificationEvent extends SourceModificationEvent implements ExtendableSourceModificationEvent {

        private final int         startOffset;
        private final int         endOffset;

        ASourceModificationEvent (
            Object          source,
            boolean         sourceChanged,
            int             _startOffset,
            int             _endOffset
        ) {
            super (source, sourceChanged);
            startOffset = _startOffset;
            endOffset = _endOffset;
        }

        @Override
        public int getAffectedStartOffset() {
            return startOffset;
        }

        @Override
        public int getAffectedEndOffset() {
            return endOffset;
        }

        @Override
        public String toString () {
            //XXX: Never change the toString value, some tests depends on it!
            return "SourceModificationEvent " + startOffset + ":" + endOffset;
        }

        @NonNull
        @Override
        public ExtendableSourceModificationEvent add(final boolean changed, int start, int end) {
            final boolean oldChanged = sourceChanged();
            if (oldChanged == changed) {
                start = Math.min(start, startOffset);
                end = Math.max(end, endOffset);
                return new ASourceModificationEvent (getSource(), oldChanged, start, end);
            } else {
                final ASourceModificationEvent other = new ASourceModificationEvent(getSource(), changed, start, end);
                return oldChanged ?
                        new AComposite(other, this) :
                        new AComposite(this, other);
            }
        }
    }

    private static class AComposite extends SourceModificationEvent.Composite implements ExtendableSourceModificationEvent {
        private final int startOffset;
        private final int endOffset;

        AComposite(
                @NonNull final ASourceModificationEvent read,
                @NonNull final ASourceModificationEvent write) {
            super(read, write);
            this.startOffset = Math.min(read.getAffectedStartOffset(), write.getAffectedStartOffset());
            this.endOffset = Math.max(read.getAffectedEndOffset(), write.getAffectedEndOffset());
        }

        @Override
        public int getAffectedStartOffset() {
            return startOffset;
        }

        @Override
        public int getAffectedEndOffset() {
            return endOffset;
        }

        @Override
        public String toString () {
            //XXX: Never change the toString value, some tests depends on it!
            return "SourceModificationEvent " + startOffset + ":" + endOffset;
        }

        @Override
        public ASourceModificationEvent getWriteEvent() {
            return (ASourceModificationEvent) super.getWriteEvent();
        }

        @Override
        public ASourceModificationEvent getReadEvent() {
            return (ASourceModificationEvent) super.getReadEvent();
        }

        @NonNull
        @Override
        public ExtendableSourceModificationEvent add(boolean changed, int start, int end) {
            if (changed) {
                return new AComposite(getReadEvent(), (ASourceModificationEvent)getWriteEvent().add(changed, start, end));
            } else {
                return new AComposite((ASourceModificationEvent)getReadEvent().add(changed, start, end), getWriteEvent());
            }
        }
    }

    /**
     * Returns a Lookup providing a context for the source.
     * In the presence of multiple scopes or users in the system, this Lookup may
     * provide access to necessary context-dependent services or scope-dependent data.
     * <p/>
     * A Source may be created with a Lookup, or it will use the default Lookup as a context
     * @return Lookup.
     * @since 9.2
     */
    public Lookup getLookup() {
        return context;
    }
}
