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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.netbeans.api.debugger.jpda.JPDAThread;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.Exceptions;

/**
 * Helps to translate one tree to another.
 *
 * Used just for ThreadsTreeModel
 *
 * @author   Jan Jancura
 */
public final class ObjectTranslation {
    
    private static final int THREAD_ID = 0;
    private static final int LOCALS_ID = 2;
    
    private JPDADebuggerImpl debugger;
    private int translationID;
    
    /* original Object to a new one.*/
    private final Map<Mirror, WeakReference<Object>> cache = new WeakHashMap<>();
    private final Map<JPDAThread, Map<Mirror, WeakReference<Object>>> threadCache = new WeakHashMap<>();
    
    
    /**
     * Creates a new instance of translating tree model for given 
     * tree model.
     *
     * @param model a tree model to be translated
     */
    private ObjectTranslation (JPDADebuggerImpl debugger, int translationID) {
        this.debugger = debugger;
        this.translationID = translationID;
    }
    
    /**
     * Creates a new translated node for given original one.
     *
     * @param o a node to be translated
     * @return a new translated node
     */
    private Object createTranslation (Object o) {
        switch (translationID) {
            case THREAD_ID:
                if (o instanceof ThreadReference) {
                    return new JPDAThreadImpl ((ThreadReference) o, debugger);
                } else if (o instanceof ThreadGroupReference) {
                    return new JPDAThreadGroupImpl ((ThreadGroupReference) o, debugger);
                } else {
                    return null;
                }
            case LOCALS_ID:
                if (o instanceof ArrayType) {
                    return new JPDAArrayTypeImpl(debugger, (ArrayType) o);
                }
                if (o instanceof ReferenceType) {
                    return new JPDAClassTypeImpl(debugger, (ReferenceType) o);
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    private Object createTranslation (Object o, Object v) {
        switch (translationID) {
            case LOCALS_ID:
                if (o instanceof LocalVariable && (v == null || v instanceof Value)) {
                    LocalVariable lv = (LocalVariable) o;
                    org.netbeans.api.debugger.jpda.LocalVariable local;
                    if (v instanceof ObjectReference || v == null) {
                        local = new ObjectLocalVariable (
                            debugger, 
                            (ObjectReference) v, 
                            null, 
                            lv, 
                            JPDADebuggerImpl.getGenericSignature (lv), 
                            null
                        );
                    } else {
                        local = new Local (debugger, (PrimitiveValue) v, null, lv, null);
                    }
                    return local;
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    private void verifyTranslation (Object t, Object o, Object v) {
        switch (translationID) {
            case LOCALS_ID:
                if (t instanceof AbstractVariable) {
                    AbstractVariable local = ((AbstractVariable) t);
                    Value lv = local.getInnerValue();
                    if (lv == null && v != null || lv != null && !lv.equals(v)) {
                        local.setInnerValue((Value) v);
                    }
                    return ;
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object. It allows thread safe
     * concurrent translation.
     *
     * @param o the Mirror object in the debuggee
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o) {
        Object r = null;
        boolean create = false;
        synchronized (cache) {
            WeakReference wr = cache.get(o);
            if (wr != null) {
                r = wr.get();
            }
            if (r == null) {
                r = new TranslationFuture();
                create = true;
                cache.put (o, new WeakReference<>(r));
            }
        }
        if (r instanceof TranslationFuture) {
            TranslationFuture tf = (TranslationFuture) r;
            if (create) {
                r = createTranslation(o);
                tf.complete(r);
                synchronized (cache) {
                    cache.put(o, new WeakReference<>(r));
                }
            } else {
                r = tf.get();
            }
        }
        return r;
    }
    
    /**
     * Gen an existing wrapper object translation of a debuggee Mirror.
     *
     * @param o the Mirror object in the debuggee
     * @return translated object or <code>null</code> when there is no existing
     *         translation.
     */
    public Object translateExisting(Mirror o) {
        Object r = null;
        synchronized (cache) {
            WeakReference wr = cache.get(o);
            if (wr != null) {
                r = wr.get();
                if (r instanceof TranslationFuture) {
                    r = ((TranslationFuture) r).get();
                }
            }
        }
        return r;
    }
    
    /**
     * Get all live objects that were translated.
     */
    public Collection getTranslated() {
        Collection translated = new HashSet();
        synchronized (cache) {
            Collection references = cache.values();
            for (Iterator it = references.iterator(); it.hasNext(); ) {
                WeakReference wr = (WeakReference) it.next();
                Object r = wr.get();
                if (r instanceof TranslationFuture) {
                    r = ((TranslationFuture) r).get();
                }
                if (r != null) {
                    translated.add(r);
                }
            }
        }
        return translated;
    }
    
    /**
     * Get existing translated objects.
     *
     * @param <T> the type of translated objects we're looking for
     * @param test a test function that returns non-null objects of type
     *             <code>T</code> that we're looking for
     * @return a list of translated objects selected by the test function.
     */
    public <T> List<? extends T> getTranslated(Function<Object, T> test) {
        List<T> translated = new ArrayList<>();
        synchronized (cache) {
            Collection references = cache.values();
            for (Iterator it = references.iterator(); it.hasNext(); ) {
                WeakReference wr = (WeakReference) it.next();
                Object r = wr.get();
                if (r instanceof TranslationFuture) {
                    r = ((TranslationFuture) r).get();
                }
                T t;
                if (r != null && (t = test.apply(r)) != null) {
                    translated.add(t);
                }
            }
        }
        return translated;
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object. It allows thread safe
     * concurrent translation.
     *
     * @param o the Mirror object in the debuggee
     * @param v an additional argument used for the translation
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o, Object v) {
        Object r = null;
        boolean verify = false;
        boolean create = false;
        synchronized (cache) {
            WeakReference wr = cache.get(o);
            if (wr != null) {
                r = wr.get();
            }
            if (r == null) {
                r = new TranslationFuture();
                create = true;
                cache.put(o, new WeakReference<>(r));
            } else {
                verify = true;
            }
        }
        if (r instanceof TranslationFuture) {
            TranslationFuture tf = (TranslationFuture) r;
            if (create) {
                r = createTranslation(o, v);
                tf.complete(r);
                synchronized (cache) {
                    cache.put(o, new WeakReference<>(r));
                }
            } else {
                r = tf.get();
            }
        }
        if (verify) {
            verifyTranslation(r, o, v);
        }
        return r;
    }
    
    /**
     * Translates a debuggee Mirror to a thread-specific wrapper object.
     *
     * @param thread the thread on which the object lives
     * @param o the Mirror object in the debuggee
     * @param v an additional argument used for the translation
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translateOnThread (JPDAThread thread, Mirror o, Object v) {
        Object r = null;
        boolean verify = false;
        synchronized (threadCache) {
            Map<Mirror, WeakReference<Object>> cache = threadCache.get(thread);
            if (cache == null) {
                cache = new WeakHashMap<>();
                threadCache.put(thread, cache);
            }
            WeakReference wr = cache.get (o);
            if (wr != null)
                r = wr.get ();
            if (r == null) {
                r = createTranslation (o, v);
                cache.put (o, new WeakReference<Object>(r));
            } else {
                verify = true;
            }
        }
        if (verify) {
            verifyTranslation(r, o, v);
        }
        return r;
    }

    /**
     * Explicitly remove the translation of the mirror object.
     */
    public void remove(Mirror o) {
        synchronized (cache) {
            cache.remove(o);
        }
    }
    
    public static ObjectTranslation createThreadTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, THREAD_ID);
    }
    
    public static ObjectTranslation createLocalsTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, LOCALS_ID);
    }

    private static final class TranslationFuture extends CompletableFuture<Object> {

        @Override
        public Object get() {
            try {
                return super.get();
            } catch (ExecutionException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
