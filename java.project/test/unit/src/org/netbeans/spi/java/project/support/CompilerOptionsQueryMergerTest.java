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
package org.netbeans.spi.java.project.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class CompilerOptionsQueryMergerTest extends NbTestCase {

    private FileObject root1;
    private FileObject root2;
    private FileObject fileInRoot1;

    public CompilerOptionsQueryMergerTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(CPProvider.class);
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        root1 = FileUtil.createFolder(wd, "root1");     //NOI18N
        root2 = FileUtil.createFolder(wd, "root2");     //NOI18N
        fileInRoot1 = FileUtil.createFolder(root1, "pkg");  //NOI18N
        CPProvider.add(root1, ClassPathSupport.createClassPath(root1));
    }

    public void testUnmergedResults() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1", "a2");   //NOI18N
        impl2.addRoot(root2).addArgs("b1", "b2");   //NOI18N
        final Lookup baseLkp = Lookups.fixed(
                impl1,
                impl2);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res1 = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","a2"), //NOI18N
                res1.getArguments());
        final CompilerOptionsQueryImplementation.Result res2 = merged.getOptions(root2);
        assertEquals(
                Arrays.asList("b1","b2"), //NOI18N
                res2.getArguments());
    }

    public void testMergedResults() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1", "a2");   //NOI18N
        impl2.addRoot(root1).addArgs("b1", "b2");   //NOI18N
        final Lookup baseLkp = Lookups.fixed(
                impl1,
                impl2);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res1 = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","a2","b1","b2"), //NOI18N
                res1.getArguments());
        final CompilerOptionsQueryImplementation.Result res2 = merged.getOptions(root2);
        assertNull(res2);
    }

    public void testCaching() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1", "a2");   //NOI18N
        impl2.addRoot(root2).addArgs("b1", "b2");;  //NOI18N
        final Lookup baseLkp = Lookups.fixed(
                impl1,
                impl2);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res1a = merged.getOptions(root1);
        final CompilerOptionsQueryImplementation.Result res1b = merged.getOptions(root1);
        assertTrue(res1a == res1b);
        final CompilerOptionsQueryImplementation.Result res1c = merged.getOptions(fileInRoot1);
        assertTrue(res1a == res1c);
        final CompilerOptionsQueryImplementation.Result res2a = merged.getOptions(root2);
        final CompilerOptionsQueryImplementation.Result res2b = merged.getOptions(root2);
        assertTrue(res2a == res2b);
        assertFalse(res1a == res2a);
    }

    public void testLkpChanges() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1", "a2");   //NOI18N
        impl2.addRoot(root1).addArgs("b1", "b2");   //NOI18N

        final InstanceContent ic = new InstanceContent();
        final Lookup baseLkp = new AbstractLookup(ic);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        assertNull(merged.getOptions(root1));
        ic.add(impl1);
        final CompilerOptionsQueryImplementation.Result res = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","a2"), //NOI18N
                res.getArguments());
        ic.add(impl2);
        assertEquals(
                Arrays.asList("a1","a2","b1","b2"), //NOI18N
                res.getArguments());
    }

    public void testArgsChanges() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1");   //NOI18N
        impl2.addRoot(root1).addArgs("b1");   //NOI18N
        final Lookup baseLkp = Lookups.fixed(
                impl1,
                impl2);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","b1"), //NOI18N
                res.getArguments());
        impl1.addArgs("a2");    //NOI18N
        assertEquals(
                Arrays.asList("a1","a2","b1"), //NOI18N
                res.getArguments());
        impl2.addArgs("b2");    //NOI18N
        assertEquals(
                Arrays.asList("a1","a2","b1","b2"), //NOI18N
                res.getArguments());
    }

    public void testLkpEvents() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1", "a2");   //NOI18N
        impl2.addRoot(root1).addArgs("b1", "b2");   //NOI18N

        final InstanceContent ic = new InstanceContent();
        final Lookup baseLkp = new AbstractLookup(ic);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        assertNull(merged.getOptions(root1));
        ic.add(impl1);
        final CompilerOptionsQueryImplementation.Result res = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","a2"), //NOI18N
                res.getArguments());
        final MockChangeListener listener = new MockChangeListener();
        res.addChangeListener(listener);
        ic.add(impl2);
        listener.assertEventCount(1);
        assertEquals(
                Arrays.asList("a1","a2","b1","b2"), //NOI18N
                res.getArguments());
    }

    public void testArgsEvents() {
        final CompilerOptionsQueryImpl impl1 = new CompilerOptionsQueryImpl();
        final CompilerOptionsQueryImpl impl2 = new CompilerOptionsQueryImpl();
        impl1.addRoot(root1).addArgs("a1");   //NOI18N
        impl2.addRoot(root1).addArgs("b1");   //NOI18N
        final Lookup baseLkp = Lookups.fixed(
                impl1,
                impl2);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res = merged.getOptions(root1);
        assertEquals(
                Arrays.asList("a1","b1"), //NOI18N
                res.getArguments());
        final MockChangeListener listener = new MockChangeListener();
        res.addChangeListener(listener);
        impl1.addArgs("a2");    //NOI18N
        listener.assertEventCount(1);
        assertEquals(
                Arrays.asList("a1","a2","b1"), //NOI18N
                res.getArguments());
        impl2.addArgs("b2");    //NOI18N
        listener.assertEventCount(1);
        assertEquals(
                Arrays.asList("a1","a2","b1","b2"), //NOI18N
                res.getArguments());
    }
    
    public void testDeadlock_PropChangeUnderProjectMutexWriteAccess() throws Exception {
        final DeadlockCompilerOptionsQueryImpl impl = new DeadlockCompilerOptionsQueryImpl();
        final Lookup baseLkp = Lookups.fixed(impl);
        final CompilerOptionsQueryImplementation merged =
                LookupMergerSupport.createCompilerOptionsQueryMerger()
                .merge(baseLkp);
        final CompilerOptionsQueryImplementation.Result res = merged.getOptions(root1);
        assertEquals(Collections.singletonList("DEFAULT"), res.getArguments());    //NOI18N
        final RequestProcessor deadLockMaker = new RequestProcessor("Deadlock Maker", 1);   //NOI18N
        final CountDownLatch startTread = new CountDownLatch(1);
        final CountDownLatch startSelf = new CountDownLatch(1);
        final CountDownLatch endThread = new CountDownLatch(1);
        deadLockMaker.execute(()-> {
            try {
                Utilities.acquireParserLock();
                try {
                    startTread.await();
                    startSelf.countDown();
                    ProjectManager.mutex().readAccess(() -> {
                        System.out.println("EXEC");
                    });
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                } finally {
                    Utilities.releaseParserLock();
                }
            } finally {
                endThread.countDown();
            }
        });
        ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>)() -> {
            startTread.countDown();
            startSelf.await();
            impl.change("NEW"); //NOI18N
            return null;
        });                
        endThread.await();
        assertEquals(Collections.singletonList("NEW"), res.getArguments());    //NOI18N
    }

    private static final class DeadlockCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
        private final R res = new R();

        @Override
        public Result getOptions(FileObject file) {
            return res;
        }
        
        void change(String newValue) {
            res.option = newValue;
            res.cs.fireChange();
        }
        
        private static final class R extends CompilerOptionsQueryImplementation.Result {
            private final ChangeSupport cs = new ChangeSupport(this);
            private volatile String option = "DEFAULT";

            @Override
            public List<? extends String> getArguments() {
                Utilities.acquireParserLock();
                try {
                    return Collections.singletonList(option);
                } finally {
                    Utilities.releaseParserLock();
                }
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
                cs.addChangeListener(listener);
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
                cs.removeChangeListener(listener);
            }            
        }
        
    }

    private static final class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
        private final Res res;
        private final Set<FileObject> roots;

        CompilerOptionsQueryImpl() {
            res = new Res();
            roots = new HashSet<>();
        }

        @NonNull
        CompilerOptionsQueryImpl addRoot(@NonNull final FileObject root) {
            Parameters.notNull("root", root);   //NOI18N
            roots.add(root);
            return this;
        }

        @NonNull
        CompilerOptionsQueryImpl addArgs(@NonNull final String... args) {
            Collections.addAll(res.args, args);
            res.cs.fireChange();
            return this;
        }

        @NonNull
        CompilerOptionsQueryImpl clearArgs() {
            res.args.clear();
            res.cs.fireChange();
            return this;
        }

        @CheckForNull
        @Override
        public Result getOptions(@NonNull final FileObject file) {
            for (FileObject root : roots) {
                if (file == root || FileUtil.isParentOf(root, file)) {
                    return res;
                }
            }
            return null;
        }

        private static final class Res extends Result {
            private final List<String> args;
            private final ChangeSupport cs;

            Res() {
                args = new ArrayList<>();
                cs = new ChangeSupport(this);
            }

            @Override
            public List<? extends String> getArguments() {
                return Collections.unmodifiableList(args);
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
                cs.addChangeListener(listener);
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
                cs.removeChangeListener(listener);
            }
        }
    }

    public static final class CPProvider implements ClassPathProvider {

        private static final Map<FileObject,ClassPath> roots = new HashMap<>();

        static void add(FileObject root, ClassPath cp) {
            roots.put(root, cp);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for (Map.Entry<FileObject,ClassPath> root : roots.entrySet()) {
                if (ClassPath.SOURCE.equals(type) && isArtifact(root.getKey(), file)) {
                    return root.getValue();
                }
            }
            return null;
        }

        private static boolean isArtifact(
                @NonNull final FileObject root,
                @NonNull final FileObject file) {
            return root == file ||
                    FileUtil.isParentOf(root, file);
        }
    }
}
