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
package org.netbeans.modules.cnd.refactoring.support;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.filesystems.FileObject;

/** 
 * This is factory and container to store wrappers around refactoring elements
 * it wrappers protect from removed or changed elements
 * 
 * based on Java's ElementGripFactory
 */
public class ElementGripFactory {

    private static final ElementGripFactory instance = new ElementGripFactory();
    private final WeakHashMap<FileObject, Interval> map = new WeakHashMap<>();

    /**
     * Creates a new instance of ElementGripFactory
     */
    private ElementGripFactory() {
    }

    public static ElementGripFactory getDefault() {
        return instance;
    }

    public void cleanUp() {
        synchronized (map) {
            map.clear();
        }
    }

    public ElementGrip get(FileObject fileObject, int position) {
        Interval start;
        synchronized (map) {
            start = map.get(fileObject);
        }
        if (start == null) {
            return null;
        }
        Interval interval = start.get(position);
        if (interval != null) {
            return interval.item;
        } else {
            return start.item;
        }
    }

    public ElementGrip getParent(ElementGrip el) {
        Interval start;
        synchronized (map) {
            start = map.get(el.getFileObject());
        }
        return start == null ? null : start.getParent(el);
    }

    public ElementGrip putInComposite(FileObject parentFile, CsmOffsetable csmObj) {
        put(parentFile, csmObj);
        ElementGrip composite = get(parentFile, csmObj.getStartOffset());
        if (composite != null) {
            // init parent of element grip
            composite.initParent();
            ElementGrip elemParent = composite.getParent();
            while (elemParent != null) {
                elemParent.initParent();
                elemParent = elemParent.getParent();
            }
        }
        return composite;
    }

    public void put(FileObject parentFile, CsmOffsetable csmObj) {
        Interval root;
        synchronized (map) {
            root = map.get(parentFile);
        }
        Interval i = Interval.createInterval(csmObj, root, null, parentFile);
        if (i != null) {
            synchronized (map) {
                map.put(parentFile, i);
            }
        }
    }

    private static class Interval {

        long from = -1, to = -1;
        Set<Interval> subintervals = new HashSet<>();
        ElementGrip item = null;

        Interval get(long position) {
            if (from <= position && to >= position) {
                for (Interval o : subintervals) {
                    Interval ob = o.get(position);
                    if (ob != null) {
                        return ob;
                    }
                }
                return this;
            }
            return null;
        }

        ElementGrip getParent(ElementGrip eh) {
            for (Interval i : subintervals) {
                if (i.item.equals(eh)) {
                    return this.item;
                } else {
                    ElementGrip e = i.getParent(eh);
                    if (e != null) {
                        return e;
                    }
                }
            }
            return null;
        }

        public static Interval createInterval(CsmOffsetable csmObj, Interval root, Interval previous, FileObject parentFile) {
            long start = csmObj.getStartOffset();
            long end = csmObj.getEndOffset();
            CsmObject encl = CsmRefactoringUtils.getEnclosingElement(csmObj);
            if (!CsmRefactoringUtils.isLangContainerFeature(csmObj)) {
                if (!CsmKindUtilities.isOffsetable(encl)) {
                    //this is file as enclosing element for macro and include directives
                    return null;
                } else {
                    return createInterval((CsmOffsetable) encl, root, previous, parentFile);
                }
            }
            Interval i = null;
            if (root != null) {
                Interval o = root.get(start);
                if (o != null && csmObj != null && csmObj.equals(o.item.getResolved())) {
                    if (previous != null) {
                        o.subintervals.add(previous);
                    }
                    return null;
                }
            }
            if (i == null) {
                i = new Interval();
            }
            if (i.from != start) {
                i.from = start;
                i.to = end;
                ElementGrip currentHandle2 = new ElementGrip(csmObj);
                i.item = currentHandle2;
            }
            if (previous != null) {
                i.subintervals.add(previous);
            }
            if (!CsmKindUtilities.isOffsetable(encl)) {
                return i;
            }
            return createInterval((CsmOffsetable) encl, root, i, parentFile);
        }

        @Override
        public String toString() {
            return "" + from + "-" + to + " :" + item; // NOI18N
        }
    }
}
    
