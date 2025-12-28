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

package org.netbeans.modules.parsing.api;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.EditorMimeTypes;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.impl.*;
import org.netbeans.modules.parsing.spi.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Parameters;


/**
 * ParserManager allows to start priority parsing request for one or more 
 * sources. 
 * 
 * @author Jan Jancura
 * @author Tomas Zezula
 */
public final class ParserManager {

    private ParserManager () {}

    /**
     * Priority request for parsing of list of {@link Source}s. Implementer
     * of this task have full control over the process of parsing of embedded 
     * languages. You can scan tree of embedded sources and start parsing for
     * all of them, or for some of them only.
     * This method is blocking. It means that only one parsing request per time
     * is allowed. But you can call another parsing request 
     * from your Task. This secondary parsing request is called 
     * immediately in the same thread (current thread).
     * <p>
     * This method is typically called as a response on some user request - 
     * during code completion for example. 
     * 
     * @param sources       A list of sources that should be parsed.
     * @param userTask      A task that will be started when parsing is done.
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        @NonNull final Collection<Source>
                            sources, 
        @NonNull final UserTask
                            userTask
    ) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        if (sources.size () == 1) {
            final Source source = sources.iterator().next();
            Parameters.notNull("sources[0]", source);   //NOI18N
            TaskProcessor.runUserTask (new UserTaskAction (source, userTask), sources);
        } else {
            TaskProcessor.runUserTask (new MultiUserTaskAction (sources, userTask), sources);
        }
    }

    /**
     * Priority request for parsing of list of {@link Source}s. Implementer
     * of this task have full control over the process of parsing of embedded
     * languages. You can scan tree of embedded sources and start parsing for
     * all of them, or for some of them only.
     * This method is blocking. It means that only one parsing request per time
     * is allowed. But you can call another parsing request
     * from your Task. This secondary parsing request is called
     * immediately in the same thread (current thread).
     * <p>
     * This method is typically called as a response on some user request -
     * during code completion for example.
     *
     * @param sources A list of sources that should be parsed.
     * @param processor The parse result processor function to invoke when parsing is done.
     * @since 9.31
     * @throws ParseException encapsulating the user exception.
     */
    public static void parse (
        @NonNull final Collection<Source>
                            sources,
        @NonNull final ResultProcessor
                            processor
    ) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("processor", processor);   //NOI18N
        UserTask userTask = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                processor.run(resultIterator);
            }
        };
        parse(sources, userTask);
    }

    /**
     * Performs the given task when the scan finished. When the background scan is active
     * the task is enqueued and the method returns, the task is performed when the
     * background scan completes by the thread doing the background scan. When no background
     * scan is running the method behaves exactly like the {#link ParserManager#parse},
     * it performs the given task synchronously (in caller thread). If there is an another {@link UserTask}
     * running this method waits until it's completed.
     * @param sources A list of sources that should be parsed.
     * @param userTask A task started when parsing is done.
     * @return {@link Future} which can be used to find out the state of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws ParseException encapsulating the user exception.
     */
    @NonNull
    public static Future<Void> parseWhenScanFinished (
            @NonNull final Collection<Source>  sources,
            @NonNull final UserTask userTask) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        if (sources.size () == 1)
            return RunWhenScanFinishedSupport.runWhenScanFinished (new UserTaskAction (sources.iterator ().next (), userTask), sources);
        else
            return RunWhenScanFinishedSupport.runWhenScanFinished (new MultiUserTaskAction (sources, userTask), sources);
    }

    /**
     * Performs the given task when the scan finished. When the background scan is active
     * the task is queued and the method returns, the task is performed when the
     * background scan completes by the thread doing the background scan. When no background
     * scan is running the method behaves exactly like the {#link ParserManager#parse},
     * it performs the given task synchronously (in caller thread). If there is an another {@link UserTask}
     * running this method waits until it's completed.
     * @param sources A list of sources that should be parsed.
     * @param processor The parse result processor function to invoke when parsing is done.
     * @since 9.31
     * @return {@link Future} which can be used to find out the state of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws ParseException encapsulating the user exception.
     */
    @NonNull
    public static Future<Void> parseWhenScanFinished (
            @NonNull final Collection<Source>  sources,
            @NonNull final ResultProcessor processor) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("processor", processor);   //NOI18N
        UserTask userTask = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                processor.run(resultIterator);
            }
        };
        return parseWhenScanFinished(sources, userTask);
    }

    //where

    private static class UserTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final Source source;

        public UserTaskAction (final Source source, final UserTask userTask) {
            assert source != null;
            assert userTask != null;
            this.userTask = userTask;
            this.source = source;
        }

        @Override
        public Void run () throws Exception {
            SourceCache sourceCache = SourceAccessor.getINSTANCE ().getCache (source);
            final ResultIterator resultIterator = new ResultIterator (sourceCache, userTask);
            try {
                TaskProcessor.callUserTask(userTask, resultIterator);
            } finally {
                ResultIteratorAccessor.getINSTANCE().invalidate(resultIterator);
            }
            return null;
        }
    }

    private static class MultiUserTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final List<Source> sources;

        public MultiUserTaskAction (final Collection<Source> sources, final UserTask userTask) {
            assert sources != null;
            assert userTask != null;
            this.userTask = userTask;
            this.sources = new ArrayList<>(sources);
        }

        @Override
        public Void run () throws Exception {
            final LowMemoryWatcher lMListener = LowMemoryWatcher.getInstance();
            Parser parser = null;
            final Collection<Snapshot> snapShots = new LazySnapshots(sources);
            for (int i = 0; i < sources.size(); ) {
                Source source = sources.get(i);
                if (parser == null) {
                    Lookup lookup = MimeLookup.getLookup (source.getMimeType ());
                    ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
                    if (parserFactory != null) {
                        parser = parserFactory.createParser (snapShots);
                    }
                }
                final SourceCache uncachedSourceCache = new SourceCache(source, null, parser);
                SourceCache origCache = SourceAccessor.getINSTANCE().getAndSetCache(source, uncachedSourceCache);
                final ResultIterator resultIterator = new ResultIterator (uncachedSourceCache, parser, userTask);
                try {
                    TaskProcessor.callUserTask(userTask, resultIterator);
                } finally {
                    if (resultIterator.getParserResult() == null
                            || ParserAccessor.getINSTANCE().processingFinished(resultIterator.getParserResult())) {
                        i++;
                    }
                    ResultIteratorAccessor.getINSTANCE().invalidate(resultIterator);
                    SourceAccessor.getINSTANCE().getAndSetCache(source, origCache);
                }
                if (lMListener.isLowMemory ())
                    parser = null;
            }
            return null;
        }
    }

    //where
    private static class LazySnapshots implements Collection<Snapshot> {

        private final Collection<? extends Source> sources;

        public LazySnapshots (final Collection<? extends Source> sources) {
            assert sources != null;
            this.sources  = sources;
        }

        @Override
        public int size() {
            return this.sources.size();
        }

        @Override
        public boolean isEmpty() {
            return this.sources.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Snapshot)) {
                return false;
            }
            final Snapshot snap =(Snapshot) o;
            return this.sources.contains(snap.getSource());
        }

        @Override
        public Iterator<Snapshot> iterator() {
            return new LazySnapshotsIt (this.sources.iterator());
        }

        public Object[] toArray() {
            final Object[] result = new Object[this.sources.size()];
            fill (result);
            return result;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            Class<?> arrayElementClass = a.getClass().getComponentType();
            if (!arrayElementClass.isAssignableFrom(Snapshot.class)) {
                throw new ArrayStoreException("Can't store Snapshot instances to an array of " + arrayElementClass.getName()); //NOI18N
            }

            final int size = this.sources.size();
            if (a.length < size) {
                @SuppressWarnings("unchecked") //NOI18N
                T[] arr = (T[])java.lang.reflect.Array.newInstance(arrayElementClass, size);
                a = arr;
            }

            fill (a);
            return a;
        }

        private void fill (Object[] array) {
            final Iterator<? extends Source> it = this.sources.iterator();
            for (int i=0; it.hasNext(); i++) {
                SourceCache sourceCache = SourceAccessor.getINSTANCE ().getCache (it.next());
                array[i] = sourceCache.getSnapshot();
            }
        }

        @Override
        public boolean add(Snapshot o) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            for (Object e : c) {
                if (!contains(e)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Snapshot> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        private static class LazySnapshotsIt implements Iterator<Snapshot> {

            private final Iterator<? extends Source> sourcesIt;

            public LazySnapshotsIt (final Iterator<? extends Source> sourcesIt) {
                assert sourcesIt != null;
                this.sourcesIt = sourcesIt;
            }

            @Override
            public boolean hasNext() {
                return sourcesIt.hasNext();
            }

            @Override
            public Snapshot next() {
                final SourceCache cache = SourceAccessor.getINSTANCE().getCache(sourcesIt.next());
                return cache.getSnapshot();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read only collection."); //NOI18N
            }

        }

    }
    
    /**
     * Runs given task in parser thread.
     * @param mimeType      specifying the parser
     * @param userTask      a user task
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        @NonNull final String mimeType,
        @NonNull final UserTask     userTask
    ) throws ParseException {
        Parameters.notNull("mimeType", mimeType);   //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        final Parser pf = findParser(mimeType);
        TaskProcessor.runUserTask (new MimeTaskAction(pf, userTask), Collections.<Source>emptyList ());
    }

    /**
     * Performs the given task when the scan finished. When the background scan is active
     * the task is enqueued and the method returns, the task is performed when the
     * background scan completes by the thread doing the background scan. When no background
     * scan is running the method behaves exactly like the {#link ParserManager#parse},
     * it performs the given task synchronously (in caller thread). If there is an another {@link UserTask}
     * running this method waits until it's completed.
     * @param mimeType A mimetype specifying the parser.
     * @param userTask A task started when parsing is done.
     * @return {@link Future} which can be used to find out the state of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws ParseException encapsulating the user exception.
     */
    @NonNull
    public static Future<Void> parseWhenScanFinished (
            @NonNull final String mimeType,
            @NonNull final UserTask userTask) throws ParseException {
        Parameters.notNull("mimeType", mimeType);   //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        final Parser pf = findParser(mimeType);
        return RunWhenScanFinishedSupport.runWhenScanFinished(new MimeTaskAction(pf, userTask), Collections.<Source>emptyList ());

    }

    //where

    private static class MimeTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final Parser parser;

        public MimeTaskAction (final Parser parser, final UserTask userTask) {
            assert userTask != null;
            assert parser != null;
            this.userTask = userTask;
            this.parser = parser;
        }

        @Override
        public Void run () throws Exception {
            TaskProcessor.callParse(parser, null, userTask, null);
            final Parser.Result result = TaskProcessor.callGetResult(parser, userTask);
            try {
                TaskProcessor.callUserTask(userTask, new ResultIterator (result));
            } finally {
                if (result != null) {
                    ParserAccessor.getINSTANCE ().invalidate (result);
                }
            }
            return null;
        }
    }

    private static Parser findParser (final String mimeType) {
        return Utilities.getEnvFactory().findMimeParser(Lookup.getDefault(), mimeType);
    }
    
    /**
     * Determines if the current execution is already called from the parsing
     * system. If code is called by the parser, it should avoid blocking on a lock
     * and must avoid blocking on other parsing results.
     * 
     * @return true, if the current thread executes code called from within the parser
     * @since 9.2
     */
    public static boolean isParsing() {
        return Utilities.holdsParserLock();
    }

    /**
     * Determines if the MIME type can be parsed. Rejects unknown MIME types (must be amongst <a href="https://github.com/apache/netbeans/tree/master/ide/parsing.indexing/src/org/netbeans/modules/parsing/impl/indexing/Util.java">getAllMimeTypes</a>.
     * Then accepts only text/* MIME type and specific hardcoded application/ MIME types.
     * 
     * @param mimeType the MIME type to check
     * @return true, if the MIME type can be parsed.
     * @since 9.2
     */
    public static boolean canBeParsed(String mimeType) {
        if (mimeType == null ||
            "content/unknown".equals(mimeType) ||    //NOI18N
            !EditorMimeTypes.getDefault().getSupportedMimeTypes().contains(mimeType)) {
            return false;
        }

        int slashIdx = mimeType.indexOf('/'); //NOI18N
        assert slashIdx != -1 : "Invalid mimetype: '" + mimeType + "'"; //NOI18N

        String type = mimeType.substring(0, slashIdx);
        if (type.equals("application")) { //NOI18N
            if (!mimeType.equals("application/x-httpd-eruby") && !mimeType.equals("application/xml-dtd")) { //NOI18N
                return false;
            }
        } else if (!type.equals("text")) { //NOI18N
            return false;
        }

//            if (allLanguagesParsersCount == -1) {
//                Collection<? extends ParserFactory> allLanguagesParsers = MimeLookup.getLookup(MimePath.EMPTY).lookupAll(ParserFactory.class);
//                allLanguagesParsersCount = allLanguagesParsers.size();
//            }
//            Collection<? extends ParserFactory> parsers = MimeLookup.getLookup(mimeType).lookupAll(ParserFactory.class);
//            if (parsers.size() - allLanguagesParsersCount > 0) {
//                return true;
//            }
//
//            // Ideally we should check that there are EmbeddingProviders registered for the
//            // mimeType, but let's assume that if there are TaskFactories they are either
//            // ordinary scheduler tasks or EmbeddingProviders. The former would most likely
//            // mean that there is also a Parser and would have been caught in the previous check.
//            if (allLanguagesTasksCount == -1) {
//                Collection<? extends TaskFactory> allLanguagesTasks = MimeLookup.getLookup(MimePath.EMPTY).lookupAll(TaskFactory.class);
//                allLanguagesTasksCount = allLanguagesTasks.size();
//            }
//            Collection<? extends TaskFactory> tasks = MimeLookup.getLookup(mimeType).lookupAll(TaskFactory.class);
//            if (tasks.size() - allLanguagesTasksCount > 0) {
//                return true;
//            }

        return true;
    }
}




