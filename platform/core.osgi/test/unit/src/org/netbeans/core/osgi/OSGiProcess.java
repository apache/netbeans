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
package org.netbeans.core.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarFile;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.netbeans.SetupHid;
import org.netbeans.nbbuild.MakeOSGi;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import static org.junit.Assert.assertTrue;

class OSGiProcess {

    private static final File platformDir = new File(System.getProperty("platform.dir"));
    static {
        assertTrue(platformDir.toString(), platformDir.isDirectory());
    }

    private final File workDir;
    private int backwards = 1;
    private final Set<String> modules = new HashSet<String>();
    private final List<NewModule> newModules = new ArrayList<NewModule>();
    private int newModuleCount = 0;

    OSGiProcess(File workDir) {
        this.workDir = workDir;
    }

    public class NewModule {

        private final int counter;
        private final Map<String, String> sources = new HashMap<String, String>();
        private final List<Class<?>> classes = new ArrayList<Class<?>>();
        private String manifest;

        private NewModule() {
            counter = ++newModuleCount;
        }

        public NewModule sourceFile(String path, String... contents) {
            sources.put(path, join(contents));
            return this;
        }

        public NewModule clazz(Class<?> clazz) {
            classes.add(clazz);
            return this;
        }

        public <T> NewModule service(Class<T> xface, Class<? extends T> impl) {
            sources.put("META-INF/services/" + xface.getName(), impl.getName() + "\n");
            return this;
        }

        public <T> NewModule namedservice(String path, Class<T> xface, Class<? extends T> impl) {
            sources.put("META-INF/namedservices/" + path + "/" + xface.getName(), impl.getName() + "\n");
            return this;
        }

        public NewModule manifest(String... contents) {
            manifest = "Manifest-Version: 1.0\n" + join(contents) + "\n";
            return this;
        }

        public OSGiProcess done() {
            return OSGiProcess.this;
        }

    }

    public NewModule newModule() {
        NewModule m = new NewModule();
        newModules.add(m);
        return m;
    }

    /** If true, start bundles in reverse lexicographic order, just to shake things up. */
    public OSGiProcess backwards() {
        backwards = -1;
        return this;
    }

    /**
     * Include an extra module in the runtime beyond the minimum.
     * Transitive declared dependencies are included too.
     * Will also be present in compile classpath for {@link #sourceFile}.
     */
    public OSGiProcess module(String cnb) throws IOException {
        if (modules.add(cnb)) {
            JarFile jar = new JarFile(new File(platformDir, "modules/" + cnb.replace('.', '-') + ".jar"));
            try {
                String deps = jar.getManifest().getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies");
                if (deps != null) {
                    for (Dependency dep : Dependency.create(Dependency.TYPE_MODULE, deps)) {
                        String cnb2 = dep.getName().replaceFirst("/\\d+$", "");
                        if (new File(platformDir, "modules/" + cnb2.replace('.', '-') + ".jar").isFile()) {
                            module(cnb2);
                        }
                    }
                }
            } finally {
                jar.close();
            }
        }
        return this;
    }

    public void run(boolean fullShutDown) throws Exception {
        MakeOSGi makeosgi = new MakeOSGi();
        Project antprj = new Project();
        /* XXX does not work, why?
        DefaultLogger logger = new DefaultLogger();
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        antprj.addBuildListener(logger);
         */
        makeosgi.setProject(antprj);
        FileSet fs = new FileSet();
        fs.setProject(antprj);
        fs.setDir(platformDir);
        fs.createInclude().setName("lib/*.jar");
        fs.createInclude().setName("core/*.jar");
        fs.createInclude().setName("modules/org-netbeans-core-osgi.jar");
        for (String module : modules) {
            fs.createInclude().setName("modules/" + module.replace('.', '-') + ".jar");
        }
        makeosgi.add(fs);
        File extra = new File(workDir, "extra");
        List<File> cp = new ArrayList<File>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!entry.isEmpty()) {
                cp.add(new File(entry));
            }
        }
        for (String module : modules) {
            cp.add(new File(platformDir, "modules/" + module.replace('.', '-') + ".jar"));
        }
        for (NewModule newModule : newModules) {
            File srcdir = new File(workDir, "custom" + newModule.counter);
            for (Map.Entry<String,String> entry : newModule.sources.entrySet()) {
                TestFileUtils.writeFile(new File(srcdir, entry.getKey()), entry.getValue());
            }
            for (Class<?> clazz : newModule.classes) {
                File f = new File(srcdir, clazz.getName().replace('.', File.separatorChar) + ".class");
                File dir = f.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new IOException("could not make dir " + dir);
                }
                OutputStream os = new FileOutputStream(f);
                try {
                    InputStream is = OSGiProcess.class.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
                    try {
                        FileUtil.copy(is, os);
                    } finally {
                        is.close();
                    }
                } finally {
                    os.close();
                }
            }
            if (newModule.manifest != null) {
                TestFileUtils.writeFile(new File(workDir, "custom" + newModule.counter + ".mf"), newModule.manifest);
            }
            SetupHid.createTestJAR(workDir, extra, "custom" + newModule.counter, null, cp.toArray(new File[0]));
            File jar = new File(extra, "custom" + newModule.counter + ".jar");
            cp.add(jar); // for use in subsequent modules
            makeosgi.add(new FileResource(jar));
        }
        File bundles = new File(workDir, "bundles");
        if (!bundles.mkdir()) {
            throw new IOException("could not create " + bundles);
        }
        makeosgi.setDestdir(bundles);
        makeosgi.execute();
        /* Would need to introspect manifestContents above:
        assertTrue(new File(bundles, "custom-1.0.0.jar").isFile());
         */
        Map<String,String> config = new HashMap<String,String>();
        File cache = new File(workDir, "cache");
        config.put(Constants.FRAMEWORK_STORAGE, cache.toString());
        Framework f = ServiceLoader.load(FrameworkFactory.class).iterator().next().newFramework(config);
        f.start();
        List<Bundle> installed = new ArrayList<Bundle>();
        for (File bundle : bundles.listFiles()) {
            installed.add(f.getBundleContext().installBundle(Utilities.toURI(bundle).toString()));
        }
        installed.sort(new Comparator<Bundle>() {
            public @Override int compare(Bundle b1, Bundle b2) {
                return b1.getSymbolicName().compareTo(b2.getSymbolicName()) * backwards;
            }
        });
        for (Bundle bundle : installed) {
            bundle.start();
        }
        if (fullShutDown) {
            for (Bundle bundle : installed) {
                bundle.stop();
            }
        }
        if (f.getState() != Bundle.STOPPING) {
            f.stop();
        }
        f.waitForStop(0);
    }

    private static String join(String[] contents) {
        StringBuilder b = new StringBuilder();
        for (String line : contents) {
            b.append(line).append('\n');
        }
        return b.toString();
    }

}
