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
package org.netbeans.modules.python.editor.refactoring.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Icon;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 *
 * Based on the Java refactoring one, but hacked for Python (plus I didn't fully understand
 * what this class was for so it probably needs some cleanup and some work)
 * 
 */
public class ElementGripFactory {
    private static ElementGripFactory instance;
    private WeakHashMap<FileObject, Interval> map = new WeakHashMap<>();

    /**
     * Creates a new instance of ElementGripFactory
     */
    private ElementGripFactory() {
    }

    public static ElementGripFactory getDefault() {
        if (instance == null) {
            instance = new ElementGripFactory();
        }
        return instance;
    }

    public void cleanUp() {
        map.clear();
    }

    public ElementGrip get(FileObject fileObject, int position) {
        Interval start = map.get(fileObject);
        if (start == null) {
            return null;
        }
        try {
            return start.get(position).item;
        } catch (RuntimeException e) {
            return start.item;
        }
    }

    public ElementGrip getParent(ElementGrip el) {
        Interval start = map.get(el.getFileObject());
        return start.getParent(el);
    }

    public void put(FileObject parentFile, String name, OffsetRange range, Icon icon) {
        Interval root = map.get(parentFile);
        Interval i = Interval.createInterval(range, name, icon, root, null, parentFile);
        if (i != null) {
            map.put(parentFile, i);
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

        // TODO - figure out what is intended here!?
        public static Interval createInterval(OffsetRange range, String name, Icon icon,
                Interval root, Interval p, FileObject parentFile) {
            //Tree t = tp.getLeaf();
            //long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
            //long end = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
            long start = range.getStart();
            long end = range.getEnd();
//                Element current = info.getTrees().getElement(tp);
//                Tree.Kind kind = tp.getLeaf().getKind();
//                if (kind != Tree.Kind.CLASS && kind != Tree.Kind.METHOD) {
//                    if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
//                        //xxx: rather workaround. should be fixed better.
//                        return null;
//                    } else {
//                        return createInterval(tp.getParentPath(), info, root, p, parentFile);
//                    }
//                }
            Interval i = null;
//                if (root != null) {
//                    Interval o = root.get(start);
//                    if (o!= null && o.item.resolveElement(info).equals(current)) {
//                        if (p!=null)
//                            o.subintervals.add(p);
//                        return null;
//                    }
//                }
            if (i == null) {
                i = new Interval();
            }
            if (i.from != start) {
                i.from = start;
                i.to = end;
                ElementGrip currentHandle2 = new ElementGrip(name, parentFile, icon);
                i.item = currentHandle2;
            }
            if (p != null) {
                i.subintervals.add(p);
            }
//                if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            return i;
//                }
//                return createInterval(tp.getParentPath(), info, root, i, parentFile);
//            }
        }
    }
}
    
