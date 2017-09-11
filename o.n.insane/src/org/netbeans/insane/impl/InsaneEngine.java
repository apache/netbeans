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

package org.netbeans.insane.impl;

import java.lang.reflect.*;
import java.util.*;

import org.netbeans.insane.scanner.*;


/**
 * InsaneEngine is the implementation of the unified heap walking algorithm,
 * which can recognize and visit both member and class fields of objects
 * on the heap. It starts from a set of root objects and notifies the registered
 * visitor about all found instances. It can also check the objects against
 * a provided filter to implement bounded heap scan.
 *
 * @author Nenik
 */
public final class InsaneEngine {

    private Filter filter;
    private Visitor visitor;
    private ObjectMap objects;
    private boolean analyzeStaticData;
    
    /** Creates a new instance of InsaneEngine
     * @param f Filter, can be null.
     */
    public InsaneEngine(Filter f, Visitor v, boolean analyzeStatic) {
        this(new SmallObjectMap2(), f, v, analyzeStatic);
    }

    public InsaneEngine(ObjectMap backend, Filter f, Visitor v, boolean analyzeStatic) {
        objects = backend;
        filter = f == null ? ScannerUtils.noFilter() : f;
        visitor = v;
        analyzeStaticData = analyzeStatic;
    }

        
    // normal set is enough, java.lang.Class have identity-based equals()
    private Set<Class> knownClasses = new HashSet<Class>();
    
    // The queue for BFS scan of the heap.
    // each thing, before added to the queue, is added
    // to the known* structures and reported to the visitor
    private Queue<Object> queue = new Queue<Object>();
    
    public void traverse(Collection roots) throws Exception {
        // process all given roots first - remember them and put them into queue
        for (Iterator it = roots.iterator(); it.hasNext(); ) recognize(it.next());
        
        while (!queue.isEmpty()) {
            process(queue.get());
        }
    }

    
    // recognize an object:
    //   * recognize it's class
    //   * report newly found items
    //   * add newly found items to the queue
    private void recognize(Object o) {
        assert o != null : "Recognize objects, not null";
        
        // dispatch the recognition
        if (o instanceof Class) {
            recognizeClass((Class)o);
        } else {
            recognizeObject(o);
        }
    }
    
    private void recognizeClass(Class cls) {
        // already known?
        if (knownClasses.contains(cls)) return;

        // my own classes
        if (cls.getName().startsWith("org.netbeans.insane.scanner")) return;

        if (!analyzeStaticData) {
            knownClasses.add(cls);
            visitor.visitClass(cls);
            return;
        }

try {        
        // check the superclass
        Class sup = cls.getSuperclass();
        if (sup != null) recognizeClass(sup);
        
        // check all interfaces
        Class[] ifaces = cls.getInterfaces();
        for (int i=0; i<ifaces.length; i++) recognizeClass(ifaces[i]);
        
        // mark the class as known
        knownClasses.add(cls);
        

        // scan fields for new types
        Field[] fields = cls.getDeclaredFields();
        for (int i=0; i<fields.length; i++) recognizeClass(fields[i].getType());

        // scan method signatures for new types
        Method[] methods = cls.getDeclaredMethods();
        for (int i=0; i<methods.length; i++) {
            recognizeClass(methods[i].getReturnType());
            Class[] params = methods[i].getParameterTypes();
            for (int j=0; j<params.length; j++) recognizeClass(params[j]);
        }

        // scan constructor signatures for new types
        Constructor[] cons = cls.getConstructors();
        for (int i=0; i<cons.length; i++) {
            Class[] params = cons[i].getParameterTypes();
            for (int j=0; j<params.length; j++) recognizeClass(params[j]);
        }

        } catch(Error e) {
            System.err.println("Failed analysing class " + cls.getName() +
                    " because of " + e);
        }
        // enqueue it for further processing of outgoing static references
        queue.add(cls);
        
        // finally report the class itself
        visitor.visitClass(cls);
    }
    
    private void recognizeObject(Object o) {
        // already known?
        if (objects.isKnown(o)) return;

        // my own data structures
        if (o.getClass().getName().startsWith("org.netbeans.insane.scanner")) return;

        // XXX - implicit safety filter. Really needed?
        assert !(o.getClass().getName().startsWith("sun.reflect."));
        
        // check its class first
        recognizeClass(o.getClass());
        
        // recognize it - mark it as known and assign ID to it
        objects.getID(o);
        
        // enqueue it for further processing of outgoing references
        queue.add(o);

        // then report the object itself
        visitor.visitObject(objects, o);
    }
    
    private void process(Object o) throws Exception {
        // dispatch the processing
        if (o instanceof Class) {
            processClass((Class)o);
        } else {
            processObject(o);
        }
    }
    
    /* follow static references and possibly other class data
     */
    private void processClass(Class cls) throws Exception {
        if (! analyzeStaticData) return;

        if (cls.getName().startsWith("java.lang.reflect") ||
                    cls.getName().equals("java.lang.Class")) {
            return;
        }

        //if (cls.getName().startsWith("org.netbeans.insane.scanner")) return; // skip refs from myself

        ClassLoader cl = cls.getClassLoader();
        if ( cl != null) recognize(cl);

        // process only fields declared by this class,
        // fields of all superclasses were already processed in separate run.
        Field[] flds = null;
        try {
            flds = cls.getDeclaredFields();
        } catch (NoClassDefFoundError e) {
            System.err.println("Failed analysing class " + cls.getName() +
                    " because of " + e);
            return; // unlinkable class
        } catch (Throwable t) {
            System.err.println("Failed analysing class " + cls.getName() +
                    " because of " + t);
            return; // Some other problem
        }
        for (int i=0; i<flds.length; i++) {
            Field act = flds[i];
            if (((act.getModifiers() & Modifier.STATIC) != 0) &&
                                (!act.getType().isPrimitive())) {
                act.setAccessible(true);
                Object target;
                try {
                    target = act.get(null);
                } catch (Throwable t) {
                    System.err.println("Failed to read field " + act + " because of " + t);
                    continue;
                }
                if (target!= null) {
                    if (target.getClass().getName().startsWith("sun.reflect")) continue;

                    if (filter.accept(target, null, act)) {
                        recognize(target);
                        // can be refused by recognize, needs to recheck here
                        if (objects.isKnown(target)) visitor.visitStaticReference(objects, target, act);
                    }
                }
            }
        }
    }
    
    private void processObject(Object obj) throws Exception {
        assert objects.isKnown(obj) : "Objects in queue must be known";
        
        Class cls = obj.getClass();

        if (cls.getName().startsWith("java.lang.reflect")) return;
        
        if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            // enqueue all object array entries
            Object[] arr = (Object[])obj;
            // it may be Class[], so not recognizeObject directly
            for (int i=0; i<arr.length; i++) {
                Object target = arr[i];
                if (target != null) {
                    if (filter.accept(target, arr, null)) {
                        recognize(target);
                        if (objects.isKnown(target)) visitor.visitArrayReference(objects, obj, target, i);
                    }
                }
            }
        } else {
            // enqueue all instance fields of reference type
            while (cls != null) { // go over the class hierarchy
	       try {
                    Field[] flds = cls.getDeclaredFields();
                    for (int i=0; i<flds.length; i++) {
                        Field act = flds[i];

                        if (((act.getModifiers() & Modifier.STATIC) == 0) &&
                                    (!act.getType().isPrimitive())) {
                        
                            act.setAccessible(true);
                            Object target = act.get(obj);
                            if (target!= null) {
                                if (filter.accept(target, obj, act)) {
                                    recognize(target);
                                    if (objects.isKnown(target)) visitor.visitObjectReference(objects, obj, target, act);
                                }
                            }
                        }
                    }
                } catch(Error e) {
                    System.err.println("Skipped analysing class " + cls.getName() +
                        " because of " + e);
		}
                cls = cls.getSuperclass();
            }
        }
    }
    
    /*
     * A queue implementation that tries to be as effective
     * as LinkedList but use as little storage as ArrayList
     */
    private static class Queue<T> extends ArrayList<T> {
        private int offset = 0;
        
        public Queue() {}
        
        public void put(T o) {
            add(o);
        }
        
        public boolean isEmpty() {
            return offset >= size();
        }
                
        public Object get() {
            if (isEmpty()) throw new NoSuchElementException();
            Object o = get(offset++);
            if (offset > 1000) {
                removeRange(0, offset);
                offset = 0;
            }
            return o;
        }
    }
}
