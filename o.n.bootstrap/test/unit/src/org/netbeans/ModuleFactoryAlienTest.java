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
 * Software is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
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

package org.netbeans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.fakepkg.FakeIfceHidden;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/** Verify contracts needed by Netigso.
 */
public class ModuleFactoryAlienTest extends SetupHid {

    static {
        MockLookup.setInstances(new Factory());
    }

    public ModuleFactoryAlienTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }



    public void testFactoryCreatesAlienModules() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        Module m2, m3;
        try {
            String mf = "AlienName: test-name.m2\n" +
                "AlienVersion: 1.1.0\n" +
                "AlienExport: org.fakepkg\n\n";
            File j2 = changeManifest(new File(jars, "simple-module.jar"), mf);
            m2 = mgr.create(j2, null, false, false, false);
            mf = "AlienName: test-name\n" +
                "AlienVersion: 1.1.0\n" +
                "AlienExport: org.fakepkg\n\n";
            File j3 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mf);
            m3 = mgr.create(j3, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m2, m3)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        ClassLoader l;
        URL u;
        Class<?> clazz;


        l = Thread.currentThread().getContextClassLoader();
        u = l.getResource("org/fakepkg/Something.txt");
        assertNotNull("Resource found", u);

        assertEquals("No dependencies", 0, m3.getDependencies().size());

        clazz = l.loadClass("org.fakepkg.FakeIfce");
        assertNotNull("Class loaded", clazz);
        assertEquals("it is our fake class", FakeIfceHidden.class, clazz);


        l = m3.getClassLoader();

        assertNotNull("Classloader found", l);
        assertEquals("My classloader", Loader.class, l.getClass());

        u = l.getResource("org/fakepkg/Something.txt");
        assertNotNull("Resource found", u);

        clazz = l.loadClass("org.fakepkg.FakeIfce");
        assertNotNull("Class loaded", clazz);
        assertEquals("it is our fake class", FakeIfceHidden.class, clazz);

        assertEquals("No dependencies", 0, m3.getDependencies().size());
    }

    public void testAlienCanDependOnNetBeans() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> both = null;
        try {
            String mfBar = "AlienName: org.bar\n" +
                "AlienExport: org.bar\n" +
                "AlienImport: org.foo\n" +
                "\n\n";

            File j1 = new File(jars, "simple-module.jar");
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            AlienModule am = (AlienModule)m2;
            am.loader.l = new URLClassLoader(new URL[] { Utilities.toURI(am.jar).toURL() }, m1.getClassLoader());

            assertFalse("Finish without exception", m2.provides("false"));


            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public static class Factory extends ModuleFactory {

        static void clear() {
        }

        public Factory() {
        }

        static void registerBundle(Module m) throws IOException {

        }

        @Override
        public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws InvalidException {
            Module m = super.createFixed(mani, history, loader, autoload, eager, mgr, ev);
            try {
                registerBundle(m);
            } catch (IOException ex) {
                throw (InvalidException)new InvalidException(m, ex.getMessage()).initCause(ex);
            }
            return m;
        }

        @Override
        public Module create(
            File jar, Object history,
            boolean reloadable, boolean autoload, boolean eager,
            ModuleManager mgr, Events ev
        ) throws IOException {
            try {
                Module m = super.create(jar, history, reloadable, autoload, eager, mgr, ev);
                registerBundle(m);
                return m;
            } catch (InvalidException ex) {
                Manifest mani = ex.getManifest();
                if (mani != null) {
                    String name = mani.getMainAttributes().getValue("AlienName"); // NOI18N
                    if (name == null) {
                        throw ex;
                    }
                    return new AlienModule(mani, jar, mgr, ev, history, reloadable, autoload, eager);
                }
                throw ex;
            }
        }

    }

    static final class AlienModule extends Module {
        private Manifest manifest;
        private Loader loader;
        private String name;
        private File jar;

        public AlienModule(Manifest m, File jar, ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
            super(mgr, ev, history, reloadable, autoload, eager);

            this.manifest = m;
            this.name = manifest.getMainAttributes().getValue("AlienName");
            this.jar = jar;
        }

        @Override
        public String[] getProvides() {
            return new String[0];
        }

        @Override
        public String getCodeName() {
            return name;
        }

        @Override
        public String getCodeNameBase() {
            return getCodeName();
        }

        @Override
        public int getCodeNameRelease() {
            return -1;
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return new SpecificationVersion("1.0");
        }

        @Override
        public String getImplementationVersion() {
            return "testimpl"; // NOI18N
        }

        @Override
        protected void parseManifest() throws InvalidException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<File> getAllJars() {
            return Collections.emptyList();
        }

        @Override
        public void setReloadable(boolean r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reload() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderUp(Set<Module> parents) throws IOException {
            loader = new Loader(this);
        }

        @Override
        protected void classLoaderDown() {
            loader = null;
        }

        @Override
        public ClassLoader getClassLoader() throws IllegalArgumentException {
            if (loader == null) {
                throw new IllegalArgumentException("No classloader for " + getCodeNameBase()); // NOI18N
            }
            return loader;
        }

        @Override
        protected void cleanup() {
        }

        @Override
        protected void destroy() {
        }

        @Override
        public boolean isFixed() {
            return false;
        }

        @Override
        public Manifest getManifest() {
            return manifest;
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            // TBD;
            return null;
        }

        @Override
        public String toString() {
            return "Alien: " + getCodeName();
        }
    }

    static final class Loader extends ProxyClassLoader {
        final AlienModule am;
        ClassLoader l;

        public Loader(AlienModule mf) throws MalformedURLException {
            super(new ClassLoader[0], true);
            Set<String> pkgs = new HashSet<String>();
            pkgs.add(mf.getManifest().getMainAttributes().getValue("AlienExport"));
            addCoveredPackages(pkgs);
            this.am = mf;
        }

        @Override
        public URL findResource(String name) {
            if ("org/fakepkg/Something.txt".equals(name)) {
                URL u = ModuleFactoryAlienTest.class.getResource("/org/fakepkg/resource1.txt");
                assertNotNull("text found", u);
                return u;
            }
            return null;
        }

        @Override
        public Enumeration<URL> findResources(String name) {
            return Enumerations.empty();
        }

        @Override
        protected Class<?> doLoadClass(String pkg, String name) {
            if (name.equals("org.fakepkg.FakeIfce")) {
                return FakeIfceHidden.class;
            }
            return null;
        }

        @Override
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c != null) {
                return c;
            }
            if (l != null) {
                try {
                    c = l.loadClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } catch (ClassNotFoundException x) {}
            }
            return super.loadClass(name, resolve);
        }

        @Override
        public String toString() {
            return "Alien[test]";
        }
    }

}
