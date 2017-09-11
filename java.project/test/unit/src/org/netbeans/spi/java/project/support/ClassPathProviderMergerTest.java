/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.project.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
public class ClassPathProviderMergerTest extends NbTestCase {
    
    public ClassPathProviderMergerTest(String testName) {
        super(testName);
    }            

   /**
     * Test of merge method, of class ClassPathProviderMerger.
     */
    public void testMerge() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ProviderImpl defaultCP = new ProviderImpl();
        //for some weird reason the specific path doesn't work in this module.
        // it worked fine in Java Support APIs module before moving here
//        URL url = createURLReference("org/netbeans/modules/java/project/");
        URL url = createURLReference("");
        defaultCP.paths.put(ClassPath.COMPILE, ClassPathSupport.createClassPath(url));
        ClassPathProviderMerger instance = new ClassPathProviderMerger(defaultCP);
        ClassPathProvider result = instance.merge(lookup);
        ClassPath cp = result.findClassPath(null, ClassPath.BOOT);
        assertNull(cp);
        
        ClassPath compile = result.findClassPath(null, ClassPath.COMPILE);
        assertNotNull(compile);
        FileObject[] fos = compile.getRoots();
        assertNotNull(fos);
        assertEquals(1, fos.length);
        
        final AtomicInteger count = new AtomicInteger();
        compile.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                final String propName = evt.getPropertyName();
                if (ClassPath.PROP_ENTRIES.equals(propName) ||
                    ClassPath.PROP_ROOTS.equals(propName)) {
                    count.incrementAndGet();
                }
            }
        });
        
        ProviderImpl additional = new ProviderImpl();
        //for some weird reason the specific path doesn't work in this module.
        // it worked fine in Java Support APIs module before moving here
//        additional.paths.put(ClassPath.COMPILE, ClassPathSupport.createClassPath(createURLReference("org/netbeans/spi/java/project/classpath/")));
//        additional.paths.put(ClassPath.BOOT, ClassPathSupport.createClassPath(createURLReference("org/netbeans/spi/java/project/support/")));
        additional.paths.put(ClassPath.COMPILE, ClassPathSupport.createClassPath(createURLReference("")));
        additional.paths.put(ClassPath.BOOT, ClassPathSupport.createClassPath(createURLReference("")));
        
        ic.add(additional);
        
        fos = compile.getRoots();
        assertNotNull(fos);
        assertEquals(2, fos.length);
        assertEquals(2, count.get()); // why 2 changes are fired?
        
        cp = result.findClassPath(null, ClassPath.COMPILE);
        assertEquals(cp, compile);
        
        cp = result.findClassPath(null, ClassPath.BOOT);
        assertNotNull(cp);
        fos = cp.getRoots();
        assertNotNull(fos);
        assertEquals(fos.length, 1);
        
        
        ic.remove(additional);
        
        fos = compile.getRoots();
        assertNotNull(fos);
        assertEquals(1, fos.length);
        assertEquals(4, count.get()); // why 2 changes are fired?
        

    }

    public void testMissingEvents() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ProviderImpl defaultCP = new ProviderImpl();

        final URL root1 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root1")));
        final URL root2 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root2")));
        final MutableCPImpl cpImpl = new MutableCPImpl(root1);

        defaultCP.paths.put(ClassPath.COMPILE, ClassPathFactory.createClassPath(cpImpl));
        ClassPathProviderMerger instance = new ClassPathProviderMerger(defaultCP);
        ClassPathProvider result = instance.merge(lookup);

        ClassPath compile = result.findClassPath(null, ClassPath.COMPILE);
        assertNotNull(compile);
        assertEquals(1, compile.entries().size());

        cpImpl.add(root2);
        assertEquals(2, compile.entries().size());
    }
    
    public final void testPropagatesFlags() throws IOException {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        final URL root1 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root1")));
        final URL root2 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root2")));

        ProviderImpl defaultCP = new ProviderImpl();
        final MutableCPImpl cpImpl = new MutableCPImpl(root1);
        defaultCP.paths.put(ClassPath.COMPILE, ClassPathFactory.createClassPath(cpImpl));
        ClassPathProviderMerger instance = new ClassPathProviderMerger(defaultCP);
        ClassPathProvider result = instance.merge(lookup);

        ClassPath compile = result.findClassPath(null, ClassPath.COMPILE);
        assertNotNull(compile);
        assertEquals(0, compile.getFlags().size());

        final ProviderImpl additional = new ProviderImpl();
        final MutableCPImpl addCpImpl = new MutableCPImpl(root2);
        addCpImpl.add(ClassPath.Flag.INCOMPLETE);
        additional.paths.put(ClassPath.COMPILE, ClassPathFactory.createClassPath(addCpImpl));
        ic.add(additional);

        assertEquals(1, compile.getFlags().size());
        assertSame(compile, result.findClassPath(null, ClassPath.COMPILE));
        
        addCpImpl.remove(ClassPath.Flag.INCOMPLETE);
        assertEquals(0, compile.getFlags().size());
        addCpImpl.add(ClassPath.Flag.INCOMPLETE);
        assertEquals(1, compile.getFlags().size());
    }

    public void testFlagsEvents() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        final URL root1 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root1")));
        final URL root2 = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(getWorkDir(),"root2")));

        ProviderImpl defaultCP = new ProviderImpl();
        final MutableCPImpl cpImpl = new MutableCPImpl(root1);
        defaultCP.paths.put(ClassPath.COMPILE, ClassPathFactory.createClassPath(cpImpl));
        ClassPathProviderMerger instance = new ClassPathProviderMerger(defaultCP);
        ClassPathProvider result = instance.merge(lookup);

        ClassPath compile = result.findClassPath(null, ClassPath.COMPILE);
        assertNotNull(compile);

        final AtomicInteger count = new AtomicInteger();
        compile.addPropertyChangeListener((evt) -> {
            if (ClassPath.PROP_FLAGS.equals(evt.getPropertyName())) {
                count.incrementAndGet();
            }
        });
        final ProviderImpl additional = new ProviderImpl();
        final MutableCPImpl addCpImpl = new MutableCPImpl(root2);
        addCpImpl.add(ClassPath.Flag.INCOMPLETE);
        additional.paths.put(ClassPath.COMPILE, ClassPathFactory.createClassPath(addCpImpl));
        ic.add(additional);
        assertEquals(1, count.get());
        count.set(0);
        addCpImpl.remove(ClassPath.Flag.INCOMPLETE);
        assertEquals(1, count.get());
        count.set(0);
        addCpImpl.add(ClassPath.Flag.INCOMPLETE);
        assertEquals(1, count.get());
        count.set(0);
    }

    private static URL createURLReference(String path) {
        URL url = ClassPathProviderMergerTest.class.getClassLoader().getResource(path);

        return url;
    }
    
    private class ProviderImpl implements ClassPathProvider {
        
        public Map<String, ClassPath> paths = new HashMap<String, ClassPath>();
        
        public ClassPath findClassPath(FileObject file, String type) {
            return paths.get(type);
        }
        
    }

    private static final class MutableCPImpl implements FlaggedClassPathImplementation {
        private final List<PathResourceImplementation> roots;
        private final Set<ClassPath.Flag> flags;
        private final PropertyChangeSupport support;


        MutableCPImpl(URL... urls) {
            roots = new ArrayList<>();
            flags = EnumSet.noneOf(ClassPath.Flag.class);
            this.support = new PropertyChangeSupport(this);
            add(urls);
        }

        void add(ClassPath.Flag... flgs) {
            for (ClassPath.Flag flg : flgs) {
                flags.add(flg);
            }
            support.firePropertyChange(PROP_FLAGS, null, null);
        }

        void remove(ClassPath.Flag... flgs) {
            for (ClassPath.Flag flg : flgs) {
                flags.remove(flg);
            }
            support.firePropertyChange(PROP_FLAGS, null, null);
        }

        void add(URL... urls) {
            for (URL url : urls) {
                roots.add(ClassPathSupport.createResource(url));
            }
            support.firePropertyChange(PROP_RESOURCES, null, null);
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            return roots;
        }

        @Override
        public Set<ClassPath.Flag> getFlags() {
            return this.flags;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

    }

}

