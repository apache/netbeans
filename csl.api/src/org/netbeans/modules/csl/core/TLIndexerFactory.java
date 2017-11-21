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

package org.netbeans.modules.csl.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;


/**
 *
 * @author hanz
 */
public final class TLIndexerFactory extends EmbeddingIndexerFactory {

    private static final Logger LOG = Logger.getLogger (TLIndexerFactory.class.getName());

    public static final String  INDEXER_NAME = "TLIndexer"; //NOI18N
    public static final int     INDEXER_VERSION = 5;

    public static final String FIELD_GROUP_NAME = "groupName"; //NOI18N
    public static final String FIELD_DESCRIPTION = "description"; //NOI18N
    public static final String FIELD_LINE_NUMBER = "lineNumber"; //NOI18N
    
    /**
     * Special feature, which runs only processing that result in error badge markers. Serves as an optimization
     * for background scanning when action item list is invisible or the indexed root is out of its scope.
     */
    public static final String FEATURE_ERROR_BADGES = "errorBadges"; // NOI18N

    private static final Map<Indexable, Collection<SimpleError>> errors = new IdentityHashMap<Indexable, Collection<SimpleError>>();
    private static final Map<Indexable, List<Integer>> lineStartOffsetsCache = new IdentityHashMap<Indexable, List<Integer>>();

    @Override
    public boolean scanStarted(final Context context) {
        return true;
    }

    @Override
    public void scanFinished(Context context) {
        if (!context.checkForEditorModifications()) {
            commitErrors(context.getRootURI(), errors, lineStartOffsetsCache);
        }
    }

    private static void commitErrors(URL root, Map<Indexable, Collection<SimpleError>> errors, Map<Indexable, List<Integer>> lineStartOffsetsCache) {
        for (Entry<Indexable, Collection<SimpleError>> e : errors.entrySet()) {
            ErrorsCache.setErrors(root, e.getKey(), e.getValue(), new ErrorConvertorImpl(lineStartOffsetsCache.get(e.getKey())));
        }

        errors.clear();
        lineStartOffsetsCache.clear();
    }

    @Override
    public synchronized EmbeddingIndexer createIndexer (
        Indexable               indexable,
        Snapshot                snapshot
    ) {
        assert errors != null;
        assert lineStartOffsetsCache != null;

        return new TLIndexer(errors, lineStartOffsetsCache);
    }

    @Override
    public void filesDeleted (
        Iterable<? extends Indexable>
                                deleted,
        Context                 context
    ) {
        for (Indexable indexable : deleted) {
            ErrorsCache.setErrors(context.getRootURI(), indexable, Collections.<SimpleError>emptyList(), DUMMY);
        }
    }

    @Override
    public void rootsRemoved (final Iterable<? extends URL> removedRoots) {
        //Not needed now
    }

    @Override
    public void filesDirty (
        Iterable<? extends Indexable>
                                dirty,
        Context                 context
    ) {
    }

    @Override
    public String getIndexerName () {
        return INDEXER_NAME;
    }

    @Override
    public int getIndexVersion () {
        return INDEXER_VERSION;
    }

    private static final class ErrorConvertorImpl implements Convertor<SimpleError>{
        private final List<Integer> lineStartOffsets;
        public ErrorConvertorImpl(List<Integer> lineStartOffsets) {
            this.lineStartOffsets = lineStartOffsets;
        }
        @Override
        public ErrorKind getKind(SimpleError error) {
            if (error.getSeverity() == Severity.WARNING) {
                return ErrorKind.WARNING;
            } else if (error.isBadging()) {
                    return ErrorKind.ERROR;
                } else {
                    return ErrorKind.ERROR_NO_BADGE;
                }
            }
        @Override
        public int getLineNumber(SimpleError error) {
            int originalOffset = error.getStartPosition(); //snapshot offset
            int lineNumber = 1;
            if (originalOffset >= 0) {
                int idx = Collections.binarySearch(lineStartOffsets, originalOffset);
                if (idx < 0) {
                    // idx == (-(insertion point) - 1) -> (insertion point) == -idx - 1
                    int ln = -idx - 1;
                    assert ln >= 1 && ln <= lineStartOffsets.size() :
                        "idx=" + idx + ", lineNumber=" + ln + ", lineStartOffsets.size()=" + lineStartOffsets.size(); //NOI18N
                    if (ln >= 1 && ln <= lineStartOffsets.size()) {
                        lineNumber = ln;
                    }
                } else {
                    lineNumber = idx + 1;
                }
            }

            return lineNumber;
        }
        @Override
        public String getMessage(SimpleError error) {
            return error.getDisplayName();
        }
    }
    
    private static final ErrorConvertorImpl DUMMY = new ErrorConvertorImpl(Collections.<Integer>emptyList());

    
    // innerclasses ............................................................

    private static final class TLIndexer extends EmbeddingIndexer {

        private final Map<Indexable, Collection<SimpleError>> errors;
        private final Map<Indexable, List<Integer>> lineStartOffsetsCache;

        public TLIndexer(Map<Indexable, Collection<SimpleError>> errors, Map<Indexable, List<Integer>> lineStartOffsetsCache) {
            this.errors = errors;
            this.lineStartOffsetsCache = lineStartOffsetsCache;
        }

        @Override
        protected void index (
            Indexable           indexable,
            Result              parserResult,
            Context             context
        ) {
            boolean process = true;

            if (context.checkForEditorModifications()) {
                return;
            }
            FileObject root = context.getRoot();
            
            // Avoid indexing, if the tasklist is not visible, or the file is out of scope
            // some ErrorFilter impls (HTML actually) use CPU-intensive hints, and they
            // slow down the scanning even though the user is not interested in the result.
            boolean allTasks = TasklistStateBackdoor.getInstance().isObserved();

            if (allTasks && root != null) {
                FileObject indexedFile = root.getFileObject(indexable.getRelativePath());
                // TSScope.isInScope is rater expensive for everything but current editor scope
                if (TasklistStateBackdoor.getInstance().isCurrentEditorScope()) {
                    process = TasklistStateBackdoor.getInstance().getScope().isInScope(indexedFile);
                } else {
                    // index if observable && !editor scope
                    process = true;
                }
            }            
            
            ParserResult gsfParserResult = (ParserResult) parserResult;

            if (!errors.containsKey(indexable)) {
                commitErrors(context.getRootURI(), errors, lineStartOffsetsCache);
            }

            Collection<SimpleError> storedErrors = this.errors.get(indexable);

            if (storedErrors == null) {
                this.errors.put(indexable, storedErrors = new ArrayList<SimpleError>());
            }

            if (errors == null) {
                return;
            }

            //filter all the errors and retain only those suitable for the tasklist
            List<Error> filteredErrors  = null;

            if (allTasks && process) {
                List<? extends Error> e = ErrorFilterQuery.getFilteredErrors(gsfParserResult,  ErrorFilter.FEATURE_TASKLIST);
                if (e != null) {
                    filteredErrors = (List<Error>)e;
                }
            }
            List<? extends Error> lst  = ErrorFilterQuery.getFilteredErrors(gsfParserResult,  FEATURE_ERROR_BADGES);
            //convert the Error-s to SimpleError-s instancies. For more info look at the SimpleError class javadoc
            List<SimpleError> simplifiedErrors = new ArrayList<SimpleError>();
            Set<String> seenErrorKeys = new HashSet<String>();
            if (lst != null) {
                if (filteredErrors == null) {
                    filteredErrors = new ArrayList<Error>(lst.size());
                } 
                filteredErrors.addAll(lst);
            } else if (filteredErrors == null) {
                filteredErrors = new ArrayList(gsfParserResult.getDiagnostics());
            }
            // must translate diagnostics offsets into file/document offsets.
            boolean errorTooLargeForParser = false; 
            for (Error err : filteredErrors) {
                int startPos = err.getStartPosition();
                startPos = gsfParserResult.getSnapshot().getOriginalOffset(startPos);
                String ek = Integer.toString(startPos) + ":" + err.getKey(); // NOI18N
                
                //Guess if there is parsing error too large
                if ("PARSING".equals(err.getKey()) && err.getDescription() != null && err.getDescription().contains("too large")) {
                    errorTooLargeForParser = true;
                }
                if (!seenErrorKeys.add(ek)) {
                    continue;
                }
                
                simplifiedErrors.add(simplify(err, startPos));
            }
            storedErrors.addAll(simplifiedErrors);
            
            if (!storedErrors.isEmpty() && !(storedErrors.size() == 1 && errorTooLargeForParser)) {
                List<Integer> lineStartOffsets = lineStartOffsetsCache.get(indexable);

                if (lineStartOffsets == null) {
                    lineStartOffsetsCache.put(indexable, getLineStartOffsets(gsfParserResult.getSnapshot().getSource()));
                }                
            } else if (storedErrors.size() == 1 && errorTooLargeForParser) {
                List<Integer> lineStartOffsets = lineStartOffsetsCache.get(indexable);

                if (lineStartOffsets == null) {
                    lineStartOffsets = new ArrayList<>();
                    lineStartOffsets.add(0);
                    lineStartOffsetsCache.put(indexable, getLineStartOffsets(gsfParserResult.getSnapshot().getSource()));
                }   
            }
        }
        
        private static List<Integer> getLineStartOffsets(Source source) {
            List<Integer> lineStartOffsets = new ArrayList<Integer>();

            lineStartOffsets.add(0);

            CharSequence text = source.createSnapshot().getText();
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') { //NOI18N
                    lineStartOffsets.add(i + 1);
                }
            }

            return lineStartOffsets;
        }
    } // End of TLIndexer class

    
    /** 
     * The main purpose of the SimpleError class is to allow the tasklist to hold 
     * all the errors returned from many parse results. Some of the parser implementations
     * set various stuff to the error parameters and some of these objects may hold 
     * quite big chunks of objects. 
     * 
     * For more info look at the 
     * Bug 196490 - Javascript parser errors indirectly refers to their parser 
     * result and snapshot which causes OOM during tasklist indexing
     * 
     */
    private static class SimpleError {
        
        private String displayName;
        private String description;
        private int startPosition;
        private Severity severity;
        private boolean isBadging;

        public SimpleError(String displayName, String description, int startPosition, Severity severity, boolean isBadging) {
            this.displayName = displayName;
            this.description = description;
            this.startPosition = startPosition;
            this.severity = severity;
            this.isBadging = isBadging;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Severity getSeverity() {
            return severity;
        }

        public int getStartPosition() {
            return startPosition;
        }
        
        public boolean isBadging() {
            return isBadging;
        }
                
    }
    
    private static SimpleError simplify(Error error, int pos) {
        return new SimpleError(error.getDisplayName(), 
                error.getDescription(), 
                pos == -1 ? error.getStartPosition() : pos,
                error.getSeverity(),
                error instanceof Badging && ((Badging) error).showExplorerBadge());
    }
    
}
