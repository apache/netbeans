/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            
            public void visitClass(Class cls) {
                for(int i=0; i<sub.length; i++) sub[i].visitClass(cls);
            }
            public void visitObject(ObjectMap map, Object object) {
                for(int i=0; i<sub.length; i++) sub[i].visitObject(map, object);
            }
            public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
                for(int i=0; i<sub.length; i++) sub[i].visitObjectReference(map, from, to, ref);
            }

            public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
                for(int i=0; i<sub.length; i++) sub[i].visitArrayReference(map, from, to, index);
            }
            
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
            private final IdentityHashMap<Object, Boolean> skip = new IdentityHashMap<Object, Boolean>();
            
            Except(Collection<Object> col) {
                for (Iterator<Object> it = col.iterator(); it.hasNext(); skip.put(it.next(), Boolean.TRUE));
            }

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
            private final Set<Field> skip = new HashSet<Field>(except);
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
        Class clsReference = Reference.class;
        try {
            Field referentOfReference = clsReference.getDeclaredField("referent");
            return skipReferencesFilter(Collections.singleton(referentOfReference));
        } catch (NoSuchFieldException x) {
            NoSuchFieldError err = new NoSuchFieldError(x.toString());
            err.initCause(x);
            throw err;
        }
    }
    
    public static Filter noFilter() {
        return new Filter() {
            public boolean accept(Object o, Object r, Field ref) {
                return true;
            }
        };
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
        return new HashSet<Object>(Arrays.asList(new Object[] {
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
        Runnable performer = new Runnable() {
            public void run() {
                try {
//                    suspendAllThreads(new HashSet(Arrays.asList(new Object[] {me, Thread.currentThread()})));
                    scan(f, v, roots, true);
                } catch (Exception e) {
                    ret[0] = e;
                } finally {
//                    resumeAllThreads();                    
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            performer.run();
        } else {
            SwingUtilities.invokeAndWait(performer);
        }
        if (ret[0] != null) throw ret[0];
    }
    
    private static Thread[] getAllThreads() {
        ThreadGroup act = Thread.currentThread().getThreadGroup();
        while (act.getParent() != null) act = act.getParent();
        Thread[] all = new Thread[2*act.activeCount()+5];
        int cnt = act.enumerate(all);
        Thread[] ret = new Thread[cnt];
        System.arraycopy(all, 0, ret, 0, cnt);
        return ret;
    }

    @SuppressWarnings("deprecation")
    private static void suspendAllThreads(Set except) {
        Thread[] threads = getAllThreads();
        
        for (int i=0; i<threads.length; i++) {
            if (!except.contains(threads[i])) {
                if ((threads[i].getName().indexOf("VM") == -1) &&
                (threads[i].getName().indexOf("CompilerTh") == -1) &&
                (threads[i].getName().indexOf("Signal") == -1)) {
                    System.out.println("suspending " + threads[i]);
                    threads[i].suspend();
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private static void resumeAllThreads() {
        Thread[] threads = getAllThreads();
        
        for (int i=0; i<threads.length; i++) {
            threads[i].resume();
        }
    }
    
    private static class ClassInfo {
        private static Map<Class, ClassInfo> classInfoRegistry = new WeakHashMap<Class, ClassInfo>();
        private int size;
        
        private ClassInfo(Class cls) {
//            this.cls = cls;
            if (cls.isArray()) {
                Class base = cls.getComponentType();
                size = -getSize(base, true);
            } else {
                // iterate all fields and sum the sizes
                int sum = 0;
                for (Class act = cls; act != null; act = act.getSuperclass()) {
                    Field[] flds = act.getDeclaredFields();
                    for (int i=0; i<flds.length; i++) { // count all nonstatic fields
                        if ((flds[i].getModifiers() & Modifier.STATIC) == 0) {
                            sum += getSize(flds[i].getType(), false);
                        }
                    } // fields
                } // classes
                size = (sum + 8 + 7) & 0xFFFFFFF8; // 8byte align
            }
        }
        
        static int sizeOf(Object o) {
            Class cls = o.getClass();
            ClassInfo info = classInfoRegistry.get(cls);
            if (info == null) {
                info = new ClassInfo(cls);
                classInfoRegistry.put(cls, info);
            }

            return (info.size > 0) ? info.size : 
                (19 - info.size*Array.getLength(o)) & 0xFFFFFFF8;
        }
        
        private static int getSize(Class type, boolean array) {
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
