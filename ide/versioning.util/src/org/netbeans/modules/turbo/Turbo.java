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
package org.netbeans.modules.turbo;

import org.openide.util.Lookup;
import org.openide.ErrorManager;

import java.util.*;
import java.lang.ref.WeakReference;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Turbo is general purpose entries/attributes dictionary with pluggable
 * layer enabling high scalability disk swaping implementations.
 * It allows to store several name identified values
 * for an entity identified by a key.
 *
 * <p>All methods take a <b><code>key</code></b> parameter. It
 * identifies entity to which attribute values are associated.
 * It must have properly implemented <code>equals()</code>,
 * <code>hashcode()</code> and <code>toString()</code> (returning
 * unique string) methods. Key lifetime must be well
 * understood to set cache strategy effeciently:
 * <ul>
 * <li>MAXIMUM: The implementation monitors key instance lifetime and does
 * not release data from memory until max. size limit reached or
 * key instance garbage collected whichever comes sooner. For
 * unbound caches the key must not be hard referenced from stored
 * value.
 *
 * <li>MINIMUM: For value lookups key's value based equalence is used.
 * Key's instance lifetime is monitored by instance based equalence.
 * Reasonable min limit must be set because there can be several
 * value equalent key instances but only one used key instance
 * actually stored in cache and lifetime monitored.
 * </ul>
 *
 * <p>Entry <b>name</b> fully indentifies contract between
 * consumers and providers. Contracts are described elsewhere.
 *
 * <p>The dictionary does not support storing <code>null</code>
 * <b>values</b>. Writing <code>null</code> value means that given
 * entry should be invalidated and removed (it actualy depends
 * on externally negotiated contract identified by name). Getting
 * <code>null</code> as requets result means that given value
 * is not (yet) known or does not exist at all.
 *
 * @author Petr Kuzel
 */
public final class Turbo {

    /** Default providers registry. */
    private static Lookup.Result providers;

    /** Custom providers 'registry'. */
    private final CustomProviders customProviders;

    private static WeakReference defaultInstance;

    private List listeners = new ArrayList(100);

    /** memory layer */
    private final Memory memory;

    private final Statistics statistics;

    private static Environment env;

    /**
     * Returns default instance. It's size is driven by
     * keys lifetime. It shrinks on keys become unreferenced
     * and grows without bounds on inserting keys.
     */
    public static synchronized Turbo getDefault() {
        Turbo turbo = null;
        if (defaultInstance != null) {
            turbo = (Turbo) defaultInstance.get();
        }

        if (turbo == null) {
            turbo = new Turbo(null, 47, -1);
            defaultInstance = new WeakReference(turbo);
        }

        return turbo;
    }

    /**
     * Creates new instance with customized providers layer.
     * @param providers never <code>null</null>
     * @param min minimum number of entries held by the cache
     * @param max maximum size or <code>-1</code> for unbound size
     *        (defined just by key instance lifetime)
     */
    public static synchronized Turbo createCustom(CustomProviders providers, int min, int max) {
        return new Turbo(providers, min, max);
    }

    private Turbo(CustomProviders customProviders, int min, int max) {
        statistics = Statistics.createInstance();
        memory = new Memory(statistics, min, max);
        this.customProviders = customProviders;
        if (customProviders == null && providers == null) {
            Lookup.Template t = new Lookup.Template(TurboProvider.class);
            synchronized(Turbo.class) {
                if (env == null) env = new Environment();
            }
            providers = env.getLookup().lookup(t);
        }
    }

    /** Tests can set different environment. Must be called before {@link #getDefault}. */
    static synchronized void initEnvironment(Environment environment) {
        assert env == null;
        env = environment;
        providers = null;
    }

    /** Logs cache statistics data. */
    protected void finalize() throws Throwable {
        super.finalize();
        statistics.shutdown();
    }

    /**
     * Reads given attribute for given entity key.
     * @param key a entity key, never <code>null</code>
     * @param name identifies requested entry, never <code>null</code>
     * @return entry value or <code>null</code> if it does not exist or unknown.
     */
    public Object readEntry(Object key, String name) {

        statistics.attributeRequest();

        // check memory cache

        if (memory.existsEntry(key, name)) {
            Object value = memory.get(key, name);
            statistics.memoryHit();
            return value;
        }

        // iterate over providers
        List speculative = new ArrayList(57);
        Object value = loadEntry(key, name, speculative);
        memory.put(key, name, value != null ? value : Memory.NULL);
        // XXX should fire here?  yes if name avalability changes should be
        // dispatched to clients that have not called prepare otherwise NO.

        // refire speculative results, can be optinized later on to fire
        // them lazilly on prepareAttribute or isPrepared calls
        Iterator it = speculative.iterator();
        while (it.hasNext()) {
            Object[] next = (Object[]) it.next();
            Object sKey =  next[0];
            String sName = (String) next[1];
            Object sValue = next[2];
            assert sKey != null;
            assert sName != null;
            fireEntryChange(sKey, sName, sValue);
        }

        return value;
    }

    private Iterator providers() {
        if (customProviders == null) {
            Collection plugins = providers.allInstances();
            List all = new ArrayList(plugins.size() +1);
            all.addAll(plugins);
            all.add(DefaultTurboProvider.getDefault());
            return all.iterator();
        } else {
            return customProviders.providers();
        }
    }

    /**
     * Iterate over providers asking for attribute values
     */
    private Object loadEntry(Object key, String name, List speculative) {

        TurboProvider provider;
        Iterator it = providers();
        while (it.hasNext()) {
            provider = (TurboProvider) it.next();
            try {
                if (provider.recognizesAttribute(name) && provider.recognizesEntity(key)) {
                    TurboProvider.MemoryCache cache = TurboProvider.MemoryCache.createDefault(memory, speculative);
                    Object value = provider.readEntry(key, name, cache);
                    statistics.providerHit();
                    return value;
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // error in provider
                ErrorManager.getDefault().annotate(t, "Error in provider " + provider + ", skipping... "); // NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);  // XXX in Junit mode writes to stdout ommiting annotation!!!
            }
        }

        return null;
    }

    /**
     * Writes given attribute, value pair and notifies all listeners.
     * Written value is stored both into memory and providers layer.
     * The call speed depends on actually used provider. However do not
     * rely on synchronous provider  call. In future it may return and
     * fire immediately on writing into memory layer, populating providers
     * asychronously on background.
     *
     * <p>A client calling this method is reponsible for passing
     * valid value that is accepted by attribute serving providers.
     *
     * @param key identifies target, never <code>null</code>
     * @param name identifies attribute, never <code>null</code>
     * @param value actual attribute value to be stored, <code>null</code> behaviour
     * is defined specificaly for each name. It always invalidates memory entry
     * and it either invalidates or removes value from providers layer
     * (mening: value unknown versus value is known to not exist).
     *
     * <p>Client should consider a size of stored value. It must be corelated
     * with Turbo memory layer limits to avoid running ouf of memory.
     *
     * @return <ul>
     * <li><code>false</code> on write failure caused by a provider denying the value.
     * It means attribute contract violation and must be handled e.g.:
     * <p><code>
     *    boolean success = faq.writeAttribute(fo, name, value);<br>
     *    assert success : "Unexpected name[" + name + "] value[" + value + "] denial for " + key + "!";
     * </code>

     * <li><code>true</code> in all other cases includins I/O error.
     * After all it's just best efford cache. All values can be recomputed.
     * </ul>
     */
    public boolean writeEntry(Object key, String name, Object value) {

        if (value != null) {
            Object oldValue = memory.get(key, name);
            if (oldValue != null && oldValue.equals(value)) return true;  // XXX assuming provider has the same value, assert it!
        }

        int result = storeEntry(key, name, value);
        if (result >= 0) {
            // no one denied keep at least in memory cache
            memory.put(key, name, value);
            fireEntryChange(key, name, value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Stores directly to providers.
     * @return 0 success, -1 contract failure, 1 other failure
     */
    int storeEntry(Object key, String name, Object value) {
        TurboProvider provider;
        Iterator it = providers();
        while (it.hasNext()) {
            provider = (TurboProvider) it.next();
            try {
                if (provider.recognizesAttribute(name) && provider.recognizesEntity(key)) {
                    if (provider.writeEntry(key, name, value)) {
                        return 0;
                    } else {
                        // for debugging purposes log which provider rejected defined name contract
                        IllegalArgumentException ex = new IllegalArgumentException("Attribute[" + name + "] value rejected by " + provider);
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        return -1;
                    }
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // error in provider
                ErrorManager.getDefault().annotate(t, "Error in provider " + provider + ", skipping... "); // NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
        }
        return 1;
    }

    /**
     * Checks value instant availability and possibly schedules its background
     * loading. It's designed to be called from UI tread.
     *
     * @return <ul>
     * <li><code>false</code> if not ready and providers must be consulted. It
     * asynchronously fires event possibly with <code>null</code> value
     * if given attribute does not exist.
     *
     * <li>
     * If <code>true</code> it's
     * ready and stays ready at least until next {@link #prepareEntry},
     * {@link #isPrepared}, {@link #writeEntry} <code>null</code> call
     * or {@link #readEntry} from the same thread.
     * </ul>
     */
    public boolean prepareEntry(Object key, String name) {

        statistics.attributeRequest();

        // check memory cache

        if (memory.existsEntry(key, name)) {
            statistics.memoryHit();
            return true;
        }

        // start asynchronous providers queriing
        scheduleLoad(key, name);
        return false;
    }

    /**
     * Checks name instant availability. Note that actual
     * value may be still <code>null</code>, in case
     * that it's known that value does not exist.
     *
     * @return <ul>
     * <li><code>false</code> if not present in memory for instant access.
     *
     * <li><code>true</code> when it's
     * ready and stays ready at least until next {@link #prepareEntry},
     * {@link #isPrepared}, {@link #writeEntry} <code>null</code> call
     * or {@link #readEntry} from the same thread.
     * </ul>
     */
    public boolean isPrepared(Object key, String name) {
        return memory.existsEntry(key, name);
    }

    /**
     * Gets key instance that it actually used in memory layer.
     * Client should keep reference to it if it wants to use
     * key lifetime monitoring cache size strategy.
     *
     * @param key key never <code>null</code>
     * @return key instance that is value-equalent or <code>null</code>
     * if monitored instance does not exist.
     */
    public Object getMonitoredKey(Object key) {
        return memory.getMonitoredKey(key);
    }

    public void addTurboListener(TurboListener l) {
        synchronized(listeners) {
            List<TurboListener> copy = new ArrayList<>(listeners);
            copy.add(l);
            listeners = copy;
        }
    }

    public void removeTurboListener(TurboListener l) {
        synchronized(listeners) {
            List<TurboListener> copy = new ArrayList<>(listeners);
            copy.remove(l);
            listeners = copy;
        }

    }

    protected void fireEntryChange(Object key, String name, Object value) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            TurboListener next = (TurboListener) it.next();
            next.entryChanged(key, name, value);
        }
    }

    /** For debugging purposes only. */
    public String toString() {
        StringBuffer sb = new StringBuffer("Turbo delegating to:");  // NOI18N
        Iterator it = providers();
        while (it.hasNext()) {
            TurboProvider provider = (TurboProvider) it.next();
            sb.append(" [" + provider + "]");   // NOI18N
        }
        return sb.toString();
    }

    /** Defines binding to external world. Used by tests. */
    static class Environment {
        /** Lookup that serves providers. */
        public Lookup getLookup() {
            return Lookup.getDefault();
        }
    }

    // Background loading ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    /** Holds keys that were requested for background status retrieval. */
    private final Set prepareRequests = Collections.synchronizedSet(new LinkedHashSet(27));

    private static PreparationTask preparationTask;

    /** Tries to locate meta on disk on failure it forward to repository */
    private void scheduleLoad(Object key, String name) {
        synchronized(prepareRequests) {
            if (preparationTask == null) {
                preparationTask = new PreparationTask(prepareRequests);
                Utils.postParallel(preparationTask, 0);
                statistics.backgroundThread();
            }
            preparationTask.notifyNewRequest(new Request(key, name));
        }
    }

    /** Requests queue entry featuring value based identity. */
    private final static class Request {
        private final Object key;
        private final String name;

        public Request(Object key, String name) {
            this.name = name;
            this.key = key;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;

            final Request request = (Request) o;

            if (name != null ? !name.equals(request.name) : request.name != null) return false;
            if (key != null ? !key.equals(request.key) : request.key != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (key != null ? key.hashCode() : 0);
            result = 29 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        public String toString() {
            return "Request[key=" + key + ", attr=" + name  + "]";
        }
    }

    /**
     * On background fetches data from providers layer.
     */
    private final class PreparationTask implements Runnable {

        private final Set requests;

        private static final int INACTIVITY_TIMEOUT = 123 * 1000; // 123 sec

        public PreparationTask(Set requests) {
            this.requests = requests;
        }

        public void run() {
            try {
                Thread.currentThread().setName("Turbo Async Fetcher");  // NOI18N
                while (waitForRequests()) {
                    Request request;
                    synchronized (requests) {
                        request = (Request) requests.iterator().next();
                        requests.remove(request);
                    }
                    Object key = request.key;
                    String name = request.name;
                    Object value;
                    boolean fire;
                    if (memory.existsEntry(key, name)) {

                        synchronized(Memory.class) {
                            fire = memory.existsEntry(key, name)  == false;
                            value = memory.get(key, name);
                        }
                        if (fire) {
                            statistics.providerHit(); // from our perpective we achieved hit
                        }
                    } else {
                        value = loadEntry(key, name, null);
                        // possible thread switch, so atomic fire test must be used
                        synchronized(Memory.class) {
                            fire = memory.existsEntry(key, name)  == false;
                            Object oldValue = memory.get(key, name);
                            memory.put(key, name, value != null ? value : Memory.NULL);
                            fire |= (oldValue != null && !oldValue.equals(value))
                                 || (oldValue == null && value != null);
                        }
                    }

                    // some one was faster, probably previous disk read that silently fetched whole directory
                    // our contract was to fire event once loading, stick to it. Note that get()
                    // silently populates stable memory area
//                    if (fire) {   ALWAYS because of above loadAttribute(key, name, null);
                        fireEntryChange(key, name, value);  // notify as soon as available in memory
//                    }

                }
            } catch (InterruptedException ex) {
                synchronized(requests) {
                    // forget about recent requests
                    requests.clear();
                }
            } finally {
                synchronized(requests) {
                    preparationTask = null;
                }
            }
        }

        /**
         * Wait for requests, it no request comes until timeout
         * it ommits suicide. It's respawned on next request however.
         */
        private boolean waitForRequests() throws InterruptedException {
            synchronized(requests) {
                if (requests.size() == 0) {
                    requests.wait(INACTIVITY_TIMEOUT);
                }
                return requests.size() > 0;
            }
        }

        public void notifyNewRequest(Request request) {
            synchronized(requests) {
                if (requests.add(request)) {
                    statistics.queueSize(requests.size());
                    requests.notify();
                } else {
                    statistics.duplicate();
                    statistics.providerHit();
                }
            }
        }

        public String toString() {
            return "Turbo.PreparationTask queue=[" + requests +"]";  // NOI18N
        }
    }


}
