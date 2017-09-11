/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.spi.java.classpath.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class MuxClassPathImplementationTest extends NbTestCase {

    public MuxClassPathImplementationTest(final String name) {
        super(name);
    }

    public void testSelects() throws IOException {
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final URL cp1r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root1"));   //NOI18N
        final URL cp2r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root1"));   //NOI18N
        final URL cp2r2 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root2"));   //NOI18N
        final SelectorImpl selector = new SelectorImpl(
                    ClassPathSupport.createClassPath(cp1r1),
                    ClassPathSupport.createClassPath(cp2r1, cp2r2));
        final ClassPath cp = ClassPathSupport.createMultiplexClassPath(selector);
        List<URL> res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r1), res);
        selector.select(1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(cp2r1, cp2r2), res);
        selector.select(0);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r1), res);
    }

    public void testBaseCpChanges() throws IOException {
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final URL cp1r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root1"));   //NOI18N
        final URL cp1r2 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root2"));   //NOI18N
        final URL cp2r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root1"));   //NOI18N
        final URL cp2r2 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root2"));   //NOI18N
        final MutableClassPathImpl cp1 = new MutableClassPathImpl(cp1r1);
        final MutableClassPathImpl cp2 = new MutableClassPathImpl(cp2r1);
        final SelectorImpl selector = new SelectorImpl(
                ClassPathFactory.createClassPath(cp1),
                ClassPathFactory.createClassPath(cp2));
        final ClassPath cp = ClassPathSupport.createMultiplexClassPath(selector);
        List<URL> res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r1), res);
        cp1.add(cp1r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(cp1r1, cp1r2), res);
        cp1.remove(cp1r1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r2), res);
        cp1.remove(cp1r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.emptyList(), res);
        selector.select(1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp2r1), res);
        cp2.add(cp2r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(cp2r1, cp2r2), res);
        cp2.remove(cp2r1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp2r2), res);
        cp2.remove(cp2r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.emptyList(), res);
    }

    public void testEvents() throws IOException {
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final URL cp1r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root1"));   //NOI18N
        final URL cp1r2 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root2"));   //NOI18N
        final URL cp2r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root1"));   //NOI18N
        final URL cp2r2 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root2"));   //NOI18N
        final MutableClassPathImpl cp1 = new MutableClassPathImpl(cp1r1);
        final MutableClassPathImpl cp2 = new MutableClassPathImpl(cp2r1);
        final SelectorImpl selector = new SelectorImpl(
                ClassPathFactory.createClassPath(cp1),
                ClassPathFactory.createClassPath(cp2));
        final ClassPath cp = ClassPathSupport.createMultiplexClassPath(selector);
        List<URL> res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r1), res);
        final MockPropertyChangeListener mpcl = new MockPropertyChangeListener(ClassPath.PROP_ENTRIES);
        mpcl.ignore(ClassPath.PROP_FLAGS);
        mpcl.ignore(ClassPath.PROP_INCLUDES);
        mpcl.ignore(ClassPath.PROP_ROOTS);
        cp.addPropertyChangeListener(mpcl);
        cp1.add(cp1r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(cp1r1, cp1r2), res);
        mpcl.assertEventCount(1);
        cp1.remove(cp1r1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp1r2), res);
        mpcl.assertEventCount(1);
        selector.select(1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp2r1), res);
        mpcl.assertEventCount(1);
        cp2.add(cp2r2);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(cp2r1, cp2r2), res);
        mpcl.assertEventCount(1);
        cp2.remove(cp2r1);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp2r2), res);
        mpcl.assertEventCount(1);
        cp1.remove(cp1r2);
        mpcl.assertEventCount(0);
        cp1.add(cp1r1);
        mpcl.assertEventCount(0);
        res = cp.entries().stream()
                .map((e)->e.getURL())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(cp2r2), res);
    }

    public void testPropagatesFlags() throws IOException {
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final URL cp1r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root1"));   //NOI18N
        final URL cp2r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root1"));   //NOI18N
        final MutableClassPathImpl cpImpl1 = new MutableClassPathImpl(cp1r1);
        final MutableClassPathImpl cpImpl2 = new MutableClassPathImpl(cp2r1)
                .add(ClassPath.Flag.INCOMPLETE);
        final SelectorImpl selector = new SelectorImpl(
                    ClassPathFactory.createClassPath(cpImpl1),
                    ClassPathFactory.createClassPath(cpImpl2));
        final ClassPath cp = ClassPathSupport.createMultiplexClassPath(selector);
        assertEquals(0, cp.getFlags().size());
        selector.select(1);
        assertEquals(1, cp.getFlags().size());
        selector.select(0);
        assertEquals(0, cp.getFlags().size());
        cpImpl1.add(ClassPath.Flag.INCOMPLETE);
        assertEquals(1, cp.getFlags().size());
        cpImpl1.remove(ClassPath.Flag.INCOMPLETE);
        assertEquals(0, cp.getFlags().size());
        selector.select(1);
        assertEquals(1, cp.getFlags().size());
    }

    public void testFlagsEvents() throws IOException {
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final URL cp1r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp1_root1"));   //NOI18N
        final URL cp2r1 = FileUtil.urlForArchiveOrDir(new File(wd, "cp2_root1"));   //NOI18N
        final MutableClassPathImpl cpImpl1 = new MutableClassPathImpl(cp1r1);
        final MutableClassPathImpl cpImpl2 = new MutableClassPathImpl(cp2r1)
                .add(ClassPath.Flag.INCOMPLETE);
        final SelectorImpl selector = new SelectorImpl(
                    ClassPathFactory.createClassPath(cpImpl1),
                    ClassPathFactory.createClassPath(cpImpl2));
        final ClassPath cp = ClassPathSupport.createMultiplexClassPath(selector);
        assertEquals(0, cp.getFlags().size());
        final MockPropertyChangeListener l = new MockPropertyChangeListener();
        cp.addPropertyChangeListener(l);
        selector.select(1);
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS, ClassPath.PROP_FLAGS);
        selector.select(0);
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS, ClassPath.PROP_FLAGS);
        cpImpl1.add(ClassPath.Flag.INCOMPLETE);
        l.assertEvents(ClassPath.PROP_FLAGS);
        cpImpl1.remove(ClassPath.Flag.INCOMPLETE);
        l.assertEvents(ClassPath.PROP_FLAGS);
        selector.select(1);
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS, ClassPath.PROP_FLAGS);
    }

    private static final class SelectorImpl implements ClassPathSupport.Selector {
        private final PropertyChangeSupport cs = new PropertyChangeSupport(this);
        private final ClassPath[] cps;
        private int activeIndex;

        SelectorImpl(ClassPath... cps) {
            this.cps = cps;
        }

        void select(int index) {
            if (index < 0 || index >= cps.length) {
                throw new IndexOutOfBoundsException("cps: " + cps.length + ", index: " + index);
            }
            activeIndex = index;
            cs.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
        }

        @Override
        public ClassPath getActiveClassPath() {
            return cps[activeIndex];
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            cs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            cs.removePropertyChangeListener(listener);
        }
    }

    private static final class MutableClassPathImpl implements FlaggedClassPathImplementation {
        private final PropertyChangeSupport cs = new PropertyChangeSupport(this);
        private final List<PathResourceImplementation> resources = new ArrayList<>();
        private final Set<ClassPath.Flag> flags = EnumSet.noneOf(ClassPath.Flag.class);

        public MutableClassPathImpl(URL... roots) {
            for (URL root : roots) {
                resources.add(ClassPathSupport.createResource(root));
            }
        }

        @NonNull
        public MutableClassPathImpl add(@NonNull final ClassPath.Flag flag) {
            assert flag != null;
            if (flags.add(flag)) {
                cs.firePropertyChange(PROP_FLAGS, null, null);
            }
            return this;
        }

        @NonNull
        public MutableClassPathImpl remove(@NonNull final ClassPath.Flag flag) {
            assert flag != null;
            if (flags.remove(flag)) {
                cs.firePropertyChange(PROP_FLAGS, null, null);
            }
            return this;
        }

        public MutableClassPathImpl add(URL root) {
            resources.add(ClassPathSupport.createResource(root));
            cs.firePropertyChange(PROP_RESOURCES, null, null);
            return this;
        }

        public MutableClassPathImpl remove(URL root) {
            boolean changed = false;
            for (Iterator<PathResourceImplementation> it = resources.iterator(); it.hasNext();) {
                final PathResourceImplementation pr = it.next();
                if (pr.getRoots()[0].equals(root)) {
                    it.remove();
                    changed = true;
                    break;
                }
            }
            if (changed) {
                cs.firePropertyChange(PROP_RESOURCES, null, null);
            }
            return this;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            return Collections.unmodifiableList(resources);
        }

        @Override
        @NonNull
        public Set<ClassPath.Flag> getFlags() {
            return Collections.unmodifiableSet(flags);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            cs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            cs.removePropertyChangeListener(listener);
        }
    }

}
