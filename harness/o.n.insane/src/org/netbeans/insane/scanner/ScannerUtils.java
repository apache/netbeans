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

package org.netbeans.insane.scanner;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.insane.impl.InsaneEngine;

/**
 *
 * @author  Nenik
 */
public final class ScannerUtils {

    /** Static factory class, no instance ever allowed */
    private ScannerUtils() { assert false; }

    /** Creates a visitor that will wrap and delegate to more visitors
     * during one scan
     *
     * @param parts aray of Visitors to delegate to.
     * @return a wrapper Visitor
     */
    public static Visitor compoundVisitor(final Visitor[] parts) {
        return new Visitor() {
            private final Visitor[] sub = parts.clone();
            
            @Override
            public void visitClass(Class cls) {
                for(int i=0; i<sub.length; i++) sub[i].visitClass(cls);
            }
            @Override
            public void visitObject(ObjectMap map, Object object) {
                for(int i=0; i<sub.length; i++) sub[i].visitObject(map, object);
            }
            @Override
            public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
                for(int i=0; i<sub.length; i++) sub[i].visitObjectReference(map, from, to, ref);
            }

            @Override
            public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
                for(int i=0; i<sub.length; i++) sub[i].visitArrayReference(map, from, to, index);
            }
            
            @Override
            public void visitStaticReference(ObjectMap map, Object to, Field ref) {
                for(int i=0; i<sub.length; i++) sub[i].visitStaticReference(map, to, ref);
            }
        };
    }
    
    /** Creates a filter that will wrap and delegate to more filters, performing
     * a logical and operation on their results
     *
     * @param parts aray of Filters to delegate to.
     * @return a wrapper Filter
     */
    // does AND operation
    public static Filter compoundFilter(final Filter[] parts) {
        return new Filter() {
            private final Filter[] sub = parts.clone();
            @Override
            public boolean accept(Object o, Object r, Field ref) {
                for (int i=0; i<sub.length; i++) {
                    if (!sub[i].accept(o, r, ref)) return false;
                }
                return true;
            }
        };
    }
    
    /**
     * A Filter factory that creates a Filter ignoring given collection
     * of objects and/or their outgoing references.
     *
     * @param except a Collection of objects to be ignored
     * @param include whether ignore the objects themselves (false) or only
     *        their outgoing references (true).
     * @return the Filter implementation
     */
    public static Filter skipObjectsFilter(Collection<Object> except, final boolean include) {
        class Except implements Filter {
            private final IdentityHashMap<Object, Boolean> skip = new IdentityHashMap<>();
            
            Except(Collection<Object> col) {
                col.forEach(o -> skip.put(o, Boolean.TRUE));
            }

            @Override
            public boolean accept(Object o, Object refFrom, Field ref) {
                return !skip.containsKey(include ? refFrom : o);
            }
        }
        return new Except(except);
    }
    
    /**
     * A Filter factory that creates a Filter ignoring given references
     *
     * @param except a Collection of Fields to be ignored.
     * @return the Filter implementation
     */
    public static Filter skipReferencesFilter(final Collection<Field> except) {
        return new Filter() {
            private final Set<Field> skip = new HashSet<>(except);
            @Override
            public boolean accept(Object o, Object r, Field ref) {
                return !skip.contains(ref);
            }
        };       
    }
    
    
    /**
     * A Filter factory that creates a Filter ignoring weak and soft references.
     *
     * @return the Filter implementation
     */
    public static Filter skipNonStrongReferencesFilter() {
        try {
            Field referentOfReference = Reference.class.getDeclaredField("referent");
            return skipReferencesFilter(Collections.singleton(referentOfReference));
        } catch (NoSuchFieldException x) {
            NoSuchFieldError err = new NoSuchFieldError(x.toString());
            err.initCause(x);
            throw err;
        }
    }
    
    public static Filter noFilter() {
        return (Object o, Object r, Field ref) -> true;
    }
    
    /*
     * Computes the amount of heap space used for a single object.
     * 
     * @param o the object to be evaluated
     * @return the size of the object, not counting the size
     * of the objects referenced from this object
     */
    public static int sizeOf(Object o) {
        return ClassInfo.sizeOf(o);
    }
    
    /*
     * Computes the amount of heap space used for a graph of objects.
     * 
     * @param roots a Collection of objects to be evaluated.
     * @param f a Filter for excluding objects. null means accept all objects.
     * @return the size of all objects in the filtered transitive closure
     *         of the roots collection.
     */
    public static int recursiveSizeOf(Collection roots, Filter f) throws Exception {
        // XXX - can be more effective to compute the size in special-purpose visitor
        CountingVisitor counter = new CountingVisitor();
        scan(f, counter, roots, false);
        return counter.getTotalSize();
    }
    
    /**
     * Traverse the graph of objects reachable from roots Collection, notifying
     * the Visitor.
     * 
     * @param f a Filter for excluding objects. null means accept all objects.
     * @param v a Visitor to be notified on all found objects and references.
     * @param roots a Collection of objects to be evaluated.
     */
    public static void scan(Filter f, Visitor v, Collection roots, boolean analyzeStaticData) throws Exception {
        new InsaneEngine(f, v, analyzeStaticData).traverse(roots);
    }

    
    /**
     * @return a set of objects that may be sufficient to transitively reference
     * near all objects on the java heap.
     */
    public static Set<Object> interestingRoots() {
        return new HashSet<>(Arrays.asList(new Object[] {
            Thread.currentThread(),
            ScannerUtils.class.getClassLoader()
        }));
    }
    
    
    /**
     * Traverse the graph of objects reachable from roots Collection, notifying
     * the Visitor. It performs the scan from inside AWT queue and tries to
     * suspend other threads during the scan.
     * 
     * @param f a Filter for excluding objects. null means accept all objects.
     * @param v a Visitor to be notified on all found objects and references.
     * @param roots a Collection of objects to be evaluated.
     */
    public static void scanExclusivelyInAWT(final Filter f, final Visitor v, final Set roots) throws Exception {
        final Thread me = Thread.currentThread();
        final Exception[] ret = new Exception[1];
        Runnable performer = () -> {
            try {
                scan(f, v, roots, true);
            } catch (Exception e) {
                ret[0] = e;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            performer.run();
        } else {
            SwingUtilities.invokeAndWait(performer);
        }
        if (ret[0] != null) throw ret[0];
    }
    
    private static class ClassInfo {
        private static final Map<Class<?>, ClassInfo> classInfoRegistry = new WeakHashMap<>();
        private final int size;
        
        private ClassInfo(Class<?> cls) {
            if (cls.isArray()) {
                Class<?> base = cls.getComponentType();
                size = -getSize(base, true);
            } else {
                // iterate all fields and sum the sizes
                int sum = 0;
                for (Class<?> act = cls; act != null; act = act.getSuperclass()) {
                    // count all nonstatic fields
                    for(Field field : act.getDeclaredFields()) {
                        if ((field.getModifiers() & Modifier.STATIC) == 0) {
                            sum += getSize(field.getType(), false);
                        }
                    } // fields
                } // classes
                size = (sum + 8 + 7) & 0xFFFFFFF8; // 8byte align
            }
        }
        
        static int sizeOf(Object o) {
            Class<?> cls = o.getClass();
            ClassInfo info = classInfoRegistry.get(cls);
            if (info == null) {
                info = new ClassInfo(cls);
                classInfoRegistry.put(cls, info);
            }

            return (info.size > 0) ? info.size : 
                (19 - info.size*Array.getLength(o)) & 0xFFFFFFF8;
        }
        
        private static int getSize(Class<?> type, boolean array) {
            if (array) {
                if (type == Byte.TYPE || type == Boolean.TYPE) {
                    return 1;
                } else if (type == Short.TYPE || type == Character.TYPE) {
                    return 2;
                } else if (type == Integer.TYPE || type == Float.TYPE) {
                    return 4;
                } else if (type == Long.TYPE || type == Double.TYPE) {
                    return 8;
                } else {
                    return 4; // some //subclass// of Object[]
                }
            } else {
                if (type == Long.TYPE || type == Double.TYPE) {
                    return 8;
                } else {
                    return 4;
                }
            }
        }
    }

   
}
