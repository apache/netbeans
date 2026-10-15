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

package org.netbeans.modules.parsing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;


/**
 * This class maintains cache of parser instance, snapshot and nested embeddings
 * for some block of code (or top level source).
 *
 * Threading: Instances of SourceCache and Source are synchronized using TaskProcessor.INTERNAL_LOCK
 * 
 * @author Jan Jancura
 */
//@ThreadSafe
public final class SourceCache {

    private static final Logger LOG = Logger.getLogger(SourceCache.class.getName());

    private static final Comparator<SchedulerTask> PRIORITY_ORDER = new Comparator<SchedulerTask>() {
        @Override
        public int compare(final SchedulerTask o1, final SchedulerTask o2) {
            final int p1 = o1.getPriority();
            final int p2 = o2.getPriority();
            return p1 < p2 ? -1 : p1 == p2 ? 0 : 1;
        }
    };
    
    private final Source    source;
    //@GuardedBy(this)
    private Embedding       embedding;
    private final String    mimeType;    

    public SourceCache (
        Source              source,
        Embedding           embedding
    ) {
        assert source != null;
        this.source = source;
        this.embedding = embedding;
        mimeType = embedding != null ?
            embedding.getMimeType () :
            source.getMimeType ();
        mimeType.getClass();
    }
    
    public SourceCache (
        Source              source,
        Embedding           embedding,
        Parser              parser
    ) {
        this(source, embedding);
        if (parser != null) {
            this.parserInitialized = true;
            this.parser = parser;
        }
    }

    private void setEmbedding (
        Embedding           embedding
    ) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            assert embedding.getMimeType ().equals (mimeType);
            this.embedding = embedding;
            snapshot = null;
        }
    }
    //@GuardedBy(this)
    private Snapshot        snapshot;

    public Snapshot getSnapshot () {
        boolean isEmbedding;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (snapshot != null) {
                return snapshot;
            }
            isEmbedding = embedding != null;
        }

        final Snapshot _snapshot = createSnapshot (isEmbedding);
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (snapshot == null) {
                snapshot = _snapshot;
            }
            return snapshot;
        }
    }
    
    public @NonNull Source getSource() {
        return this.source;
    }

    Snapshot createSnapshot (long[] idHolder) {
        assert idHolder != null;
        assert idHolder.length == 1;
        boolean isEmbedding;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            isEmbedding = embedding != null;
        }
        idHolder[0] = SourceAccessor.getINSTANCE ().getLastEventId (this.source);
        return createSnapshot (isEmbedding);
    }

    private Snapshot createSnapshot (boolean isEmbedding) {
        Snapshot _snapshot = isEmbedding ? embedding.getSnapshot () : source.createSnapshot ();
        assert mimeType.equals (_snapshot.getMimeType ());
        return _snapshot;
    }

    //@GuarderBy(this)
    private boolean         parserInitialized = false;
    //@GuardedBy(this)
    private Parser          parser;
    
    public Parser getParser () {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (parserInitialized) {
                return parser;
            }
        }
        Parser _parser;
        Lookup lookup = MimeLookup.getLookup (mimeType);
        ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
        if (parserFactory != null) {
            final Snapshot _snapshot = getSnapshot ();
            final Collection<Snapshot> _tmp = Collections.singleton (_snapshot);
            _parser = parserFactory.createParser (_tmp);
            if (_parser == null) {
                LOG.log(
                    Level.INFO,
                    "Parser factory: {0} returned null parser for {1}", //NOI18N
                    new Object[]{
                        parserFactory,
                        _snapshot
                    });
            }
        } else {
            return null;
        }

        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (!parserInitialized) {                                                                                
                parser = _parser;
                if (parser != null) {
                    parser.addChangeListener(WeakListeners.change(
                        SourceAccessor.getINSTANCE().getParserEventForward(source),
                        parser));
                }
                parserInitialized = true;
            }
            return parser;
        }
    }
    
    //@GuardedBy(this)
    private boolean                     parsed = false;
    
    public Result getResult (
        final Task                      task
    ) throws ParseException {
        assert TaskProcessor.holdsParserLock();
        Parser _parser = getParser ();
        if (_parser == null) return null;
        boolean _parsed;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            _parsed = this.parsed;
            this.parsed = true; //Optimizstic update
        }
        if (!_parsed) {
            boolean parseSuccess = false;
            try {
                final Snapshot _snapshot = getSnapshot ();
                final FileObject file = _snapshot.getSource().getFileObject();
                if (file != null && !file.isValid()) {
                    return null;
                }
                SourceModificationEvent event = SourceAccessor.getINSTANCE ().getSourceModificationEvent (source);
                TaskProcessor.callParse(_parser, snapshot, task, event);
                SourceAccessor.getINSTANCE ().parsed (source);
                parseSuccess = true;
            } finally {
                if (!parseSuccess) {
                    synchronized (TaskProcessor.INTERNAL_LOCK) {
                        parsed = false;
                    }
                }
            }
        }
        return TaskProcessor.callGetResult(_parser, task);
    }
    
    public void invalidate () {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            snapshot = null;
            parsed = false;
            embeddings = null;
            providerOrder = null;
            upToDateEmbeddingProviders.clear();
            for (SourceCache sourceCache : embeddingToCache.values ())
                sourceCache.invalidate ();
        }
    }

    public void invalidate (final Snapshot preRendered) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            invalidate();
            snapshot = preRendered;
        }
    }
                
    //@GuardedBy(this)
    private volatile List<Embedding>
                            embeddings;

    //@GuardedBy(this)
    private List<EmbeddingProvider>
                            providerOrder;

    //@GuardedBy(this)
    private final Map<EmbeddingProvider,List<Embedding>>
                            embeddingProviderToEmbedings = new HashMap<EmbeddingProvider,List<Embedding>> ();
    
    public Iterable<Embedding> getAllEmbeddings () {
        List<Embedding> result = this.embeddings;
        if (result != null) {
            return result;
        }

retry:  while (true) {
            final Snapshot snpsht = getSnapshot();
            final Collection<SchedulerTask> tsks = createTasks();
            final Set<EmbeddingProvider> currentUpToDateProviders;

            synchronized (TaskProcessor.INTERNAL_LOCK) {
                //BEGIN
                currentUpToDateProviders = new HashSet<EmbeddingProvider>(upToDateEmbeddingProviders);
            }

            final Map<EmbeddingProvider,List<Embedding>> newEmbeddings = new LinkedHashMap<EmbeddingProvider, List<Embedding>> ();
            for (SchedulerTask schedulerTask : tsks) {
                if (schedulerTask instanceof EmbeddingProvider) {
                    final EmbeddingProvider embeddingProvider = (EmbeddingProvider) schedulerTask;
                    if (newEmbeddings.containsKey(embeddingProvider)) {
                        LOG.log(Level.WARNING, "EmbeddingProvider: {0} is registered several time.", embeddingProvider);    //NOI18N
                    }
                    else if (currentUpToDateProviders.contains(embeddingProvider)) {
                        newEmbeddings.put(embeddingProvider,null);
                    } else {
                        final List<Embedding> embForProv = TaskProcessor.callEmbeddingProvider(embeddingProvider,snpsht);
                        if (embForProv == null) {
                            throw new NullPointerException(String.format("The %s returned null embeddings!", embeddingProvider));
                        }
                        newEmbeddings.put(embeddingProvider, embForProv);
                    }
                }
            }

            synchronized (TaskProcessor.INTERNAL_LOCK) {
                if (this.embeddings == null) {
                    if (!upToDateEmbeddingProviders.equals(currentUpToDateProviders)) {
                        //ROLLBACK and RETRY
                        continue retry;
                    }
                    result = new LinkedList<Embedding> ();
                    for (Map.Entry<EmbeddingProvider,List<Embedding>> entry : newEmbeddings.entrySet()) {
                        final EmbeddingProvider embeddingProvider = entry.getKey();
                        final List<Embedding> embeddingsForProvider = entry.getValue();
                        if (embeddingsForProvider == null) {
                            List<Embedding> _embeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                            result.addAll (_embeddings);
                        } else {
                            List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                            updateEmbeddings (embeddingsForProvider, oldEmbeddings, false, null);
                            embeddingProviderToEmbedings.put (embeddingProvider, embeddingsForProvider);
                            upToDateEmbeddingProviders.add (embeddingProvider);
                            result.addAll (embeddingsForProvider);
                        }
                    }
                    //COMMIT
                    this.embeddings = result;
                    this.providerOrder = new ArrayList<EmbeddingProvider>(newEmbeddings.keySet());
                }                
                return this.embeddings;
            }
        }
    }

    //@GuardedBy(this)
    private final Set<EmbeddingProvider> upToDateEmbeddingProviders = new HashSet<EmbeddingProvider> ();

    
    void refresh (EmbeddingProvider embeddingProvider, Class<? extends Scheduler> schedulerType) {
        List<Embedding> _embeddings = TaskProcessor.callEmbeddingProvider(embeddingProvider, getSnapshot ());
        List<Embedding> oldEmbeddings;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
            updateEmbeddings (_embeddings, oldEmbeddings, true, schedulerType);
            embeddingProviderToEmbedings.put (embeddingProvider, _embeddings);            
            upToDateEmbeddingProviders.add (embeddingProvider);
            if (this.embeddings != null) {
                final Embedding insertBefore = findNextEmbedding(providerOrder, embeddingProviderToEmbedings, embeddingProvider);
                this.embeddings.removeAll(oldEmbeddings);
                int index = insertBefore == null ? -1 : this.embeddings.indexOf(insertBefore);
                this.embeddings.addAll(index == -1 ? this.embeddings.size() : index, _embeddings);
            }
        }
    }

    private Embedding findNextEmbedding (
            final List<EmbeddingProvider> order,
            final Map<EmbeddingProvider,List<Embedding>> providers2embs,
            final EmbeddingProvider provider) {
        if (order != null) {
            boolean accept = false;
            for (EmbeddingProvider p : order) {
                if (accept) {
                    final Collection<Embedding> c = providers2embs.get(p);
                    if (c != null && !c.isEmpty()) {
                        return c.iterator().next();
                    }
                } else if (p.equals(provider)) {
                    accept = true;
                }
            }
        }
        return null;
    }
    
    private void updateEmbeddings (
            final List<Embedding>                 embeddings,
            final List<Embedding>                 oldEmbeddings,
            final boolean                         updateTasks,
            final Class<? extends Scheduler>      schedulerType) {
        assert Thread.holdsLock(TaskProcessor.INTERNAL_LOCK);
        final List<SourceCache> toBeSchedulled = new ArrayList<SourceCache> ();
            
        if (oldEmbeddings != null && embeddings.size () == oldEmbeddings.size ()) {
            for (int i = 0; i < embeddings.size (); i++) {
                if (embeddings.get (i) == null) {
                    throw new NullPointerException ();
                }
                SourceCache cache = embeddingToCache.remove (oldEmbeddings.get (i));
                if (cache != null) {
                    cache.setEmbedding (embeddings.get (i));
                    assert embeddings.get (i).getMimeType ().equals (cache.getSnapshot ().getMimeType ());
                    embeddingToCache.put (embeddings.get (i), cache);
                } else {
                    cache = getCache(embeddings.get(i));
                }

                if (updateTasks) {
                    toBeSchedulled.add (cache);
                }
            }
        } else {
            if (oldEmbeddings != null) {
                for (Embedding _embedding : oldEmbeddings) {
                    SourceCache cache = embeddingToCache.remove (_embedding);
                    if (cache != null) {
                        cache.removeTasks ();
                    }
                }
            }
            if (updateTasks) {
                for (Embedding _embedding : embeddings) {
                    SourceCache cache = getCache (_embedding);
                    toBeSchedulled.add (cache);
                }
            }
        }        
        for (SourceCache cache : toBeSchedulled) {
            cache.scheduleTasks (schedulerType);
        }
    }
    
    //@GuardedBy(this)
    private final Map<Embedding,SourceCache>
                            embeddingToCache = new HashMap<Embedding,SourceCache> ();
    
    public SourceCache getCache (Embedding embedding) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            SourceCache sourceCache = embeddingToCache.get (embedding);
            if (sourceCache == null) {
                sourceCache = new SourceCache (source, embedding);
                assert embedding.getMimeType ().equals (sourceCache.getSnapshot ().getMimeType ());
                embeddingToCache.put (embedding, sourceCache);
            }
            return sourceCache;
        }
    }

    
    // tasks management ........................................................
    
    //@GuardedBy(this)
    private List<SchedulerTask> 
                            tasks;
    //@GuardedBy(this)
    private Set<SchedulerTask> 
                            pendingTasks;
    
    private Collection<SchedulerTask> createTasks () {
        List<SchedulerTask> tasks1 = null;
        Set<SchedulerTask> pendingTasks1 = null;
        if (tasks == null) {
            tasks1 = new ArrayList<SchedulerTask> ();
            pendingTasks1 = new HashSet<SchedulerTask> ();
            Lookup lookup = MimeLookup.getLookup (mimeType);
            Collection<? extends TaskFactory> factories = lookup.lookupAll (TaskFactory.class);
            Snapshot fakeSnapshot = null;
            for (TaskFactory factory : factories) {
                // #185586 - this is here in order not to create snapshots (a copy of file/doc text)
                // if there is no task that would really need it (eg. in C/C++ projects there is no parser
                // registered and no tasks will ever run on these files, even though there may be tasks
                // registered for all mime types
                Collection<? extends SchedulerTask> newTasks = factory.create(getParser() != null ? 
                    getSnapshot() :
                    fakeSnapshot == null ?
                        fakeSnapshot = SourceAccessor.getINSTANCE().createSnapshot(
                            "", //NOI18N
                            new int [] { 0 },
                            source,
                            MimePath.get (mimeType),
                            new int[][] {new int[] {0, 0}},
                            new int[][] {new int[] {0, 0}}) :
                        fakeSnapshot
                );
                if (newTasks != null) {
                    tasks1.addAll (newTasks);
                    pendingTasks1.addAll (newTasks);
                }
            }
            tasks1.sort(PRIORITY_ORDER);
        }
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if ((tasks == null) && (tasks1 != null)) {
                tasks = tasks1;
                pendingTasks = pendingTasks1;
            }
            if (tasks != null) {
                // this should be normal return in most cases
                return tasks;
            }
        }
        // recurse and hope
        return createTasks();
    }
    
    //tzezula: probably has race condition
    public void scheduleTasks (Class<? extends Scheduler> schedulerType) {
        //S ystem.out.println("scheduleTasks " + schedulerType);
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        final List<Pair<SchedulerTask,Class<? extends Scheduler>>> add = new ArrayList<Pair<SchedulerTask,Class<? extends Scheduler>>> ();
        Collection<SchedulerTask> tsks = createTasks();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            for (SchedulerTask task : tsks) {
                if (schedulerType == null || task instanceof EmbeddingProvider) {
                    if (!pendingTasks.remove (task)) {
                        remove.add (task);
                    }                   
                    add.add (Pair.<SchedulerTask,Class<? extends Scheduler>>of(task,null));
                } else if (task.getSchedulerClass () == schedulerType) {
                    if (!pendingTasks.remove (task)) {
                        remove.add (task);
                    }
                    add.add (Pair.<SchedulerTask,Class<? extends Scheduler>>of(task,schedulerType));
                }
            }
        }        
        if (!add.isEmpty ()) {
            LOG.fine("Change tasks for source " + source + " - add: " + add + ", remove " + remove);
            TaskProcessor.updatePhaseCompletionTask(add, remove, source, this);
        }
    }

    public void unscheduleTasks (Class<? extends Scheduler> schedulerType) {
        //S ystem.out.println("unscheduleTasks " + schedulerType);
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (tasks != null) {
                for (SchedulerTask task : tasks) {
                    if (schedulerType == null ||
                        task.getSchedulerClass () == schedulerType ||
                        task instanceof EmbeddingProvider
                    ) {
                        remove.add (task);
                    }
                }
            }
        }
        if (!remove.isEmpty ()) {
            TaskProcessor.removePhaseCompletionTasks(remove, source);
        }
    }

    //jjancura: probably has race condition too
    public void sourceModified () {
        SourceModificationEvent sourceModificationEvent = SourceAccessor.getINSTANCE ().getSourceModificationEvent (source);
        if (sourceModificationEvent == null)
            return;
        final Map<Class<? extends Scheduler>,? extends SchedulerEvent> schedulerEvents =
                SourceAccessor.getINSTANCE ().createSchedulerEvents (source, 
                        Utilities.getEnvFactory().getSchedulers(source.getLookup()), sourceModificationEvent);
        if (schedulerEvents.isEmpty ()) {
            return;
        }
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        final List<Pair<SchedulerTask,Class<? extends Scheduler>>> add = new ArrayList<Pair<SchedulerTask,Class<? extends Scheduler>>>();
        Collection<SchedulerTask> tsks = createTasks();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            for (SchedulerTask task : tsks) {
                Class<? extends Scheduler> schedulerClass = task.getSchedulerClass ();
                if (schedulerClass != null &&
                    !schedulerEvents.containsKey (schedulerClass)
                )
                    continue;
                if (!pendingTasks.remove (task)) {
                    remove.add (task);
                }
                add.add (Pair.<SchedulerTask,Class<? extends Scheduler>>of(task,null));
            }
        }
        if (!add.isEmpty ()) {
            TaskProcessor.updatePhaseCompletionTask (add, remove, source, this);
        }
    }
    
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ("SourceCache ");
        sb.append (hashCode ());
        sb.append (": ");
        Snapshot _snapshot = getSnapshot ();
        Source _source = _snapshot.getSource ();
        FileObject fileObject = _source.getFileObject ();
        if (fileObject != null)
            sb.append (fileObject.getNameExt ());
        else
            sb.append (mimeType).append (" ").append (_source.getDocument (false));
        if (!_snapshot.getMimeType ().equals (_source.getMimeType ())) {
            sb.append ("( ").append (_snapshot.getMimeType ()).append (" ");
            sb.append (_snapshot.getOriginalOffset (0)).append ("-").append (_snapshot.getOriginalOffset (_snapshot.getText ().length () - 1)).append (")");
        }
        return sb.toString ();
    }
    
    //@NotThreadSafe - has to be called in GuardedBy(this)
    private void removeTasks () {
        if (tasks != null) {
            TaskProcessor.removePhaseCompletionTasks (tasks, source);
        }
        tasks = null;
    }
}




