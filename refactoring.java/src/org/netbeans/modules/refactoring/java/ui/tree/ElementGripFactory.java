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
package org.netbeans.modules.refactoring.java.ui.tree;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ElementGripFactory {

    private static ElementGripFactory instance;
    private final Map<FileObject, Interval> map = new WeakHashMap<FileObject, Interval>();
    private final Map<FileObject, Boolean> testFiles = new WeakHashMap<FileObject, Boolean>();

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
        testFiles.clear();
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
        return start == null ? null : start.getParent(el);
    }

    public void put(FileObject parentFile, Boolean inTestfile) {
        testFiles.put(parentFile, inTestfile);
    }

    public Boolean inTestFile(FileObject parentFile) {
        return testFiles.get(parentFile);
    }

    public void put(FileObject parentFile, TreePath tp, CompilationInfo info) {
        Interval root = map.get(parentFile);
        Interval i = Interval.createInterval(tp, info, root, null, parentFile);
        if (i != null) {
            map.put(parentFile, i);
        }
    }

    private static class Interval {

        long from = -1, to = -1;
        Set<Interval> subintervals = new HashSet<Interval>();
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

        public static Interval createInterval(TreePath tp, CompilationInfo info, Interval root, Interval p, FileObject parentFile) {
            Tree t = tp.getLeaf();
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
            long end = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
            Element current = info.getTrees().getElement(tp);
            Tree.Kind kind = tp.getLeaf().getKind();
            if (!TreeUtilities.CLASS_TREE_KINDS.contains(kind) && kind != Tree.Kind.METHOD) {
                if (tp.getParentPath() == null || tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                    //xxx: rather workaround. should be fixed better.
                    return null;
                } else {
                    return createInterval(tp.getParentPath(), info, root, p, parentFile);
                }
            }
            Interval i = null;
            if (root != null) {
                Interval o = root.get(start);
                if (o != null && current != null && current.equals(o.item.resolveElement(info))) {
                    // Update start/end, from/to
                    o.from = start;
                    o.to = end;
                    if (p != null) {
                        o.subintervals.add(p);
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
                ElementGrip currentHandle2 = new ElementGrip(tp, info);
                i.item = currentHandle2;
            }
            if (p != null) {
                i.subintervals.add(p);
            }
            if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                return i;
            }
            return createInterval(tp.getParentPath(), info, root, i, parentFile);
        }
    }
}
