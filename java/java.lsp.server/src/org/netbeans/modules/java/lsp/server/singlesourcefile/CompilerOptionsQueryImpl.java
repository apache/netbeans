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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=CompilerOptionsQueryImplementation.class, position=99),
    @ServiceProvider(service=ClassPathProvider.class, position=9999), //DefaultClassPathProvider has 10000
    @ServiceProvider(service=CompilerOptionsQueryImpl.class)
})
public class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation, ClassPathProvider, SourceLevelQueryImplementation2 {

    private final Map<NbCodeLanguageClient, Configuration> file2Configuration = new WeakHashMap<>();

    @Override
    public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
        if (isSingleSourceFile(file)) {
            NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
            if (client != null) {
                return getConfiguration(client).compilerOptions;
            }
        }
        return null;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (isSingleSourceFile(file)) {
            NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
            if (client != null) {
                switch (type) {
                    case ClassPath.COMPILE: case JavaClassPathConstants.MODULE_CLASS_PATH:
                        return getConfiguration(client).compileClassPath;
                    case JavaClassPathConstants.MODULE_COMPILE_PATH:
                        return getConfiguration(client).moduleCompileClassPath;
                }
            }
        }
        return null;
    }

    @Override
    public SourceLevelQueryImplementation2.Result getSourceLevel(FileObject file) {
        if (isSingleSourceFile(file)) {
            NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
            if (client != null) {
                return getConfiguration(client).sourceLevelResult;
            }
        }
        return null;
    }

    public boolean setConfiguration(NbCodeLanguageClient client, String vmOptions) {
        return getConfiguration(client).setConfiguration(vmOptions);
    }

    private synchronized Configuration getConfiguration(NbCodeLanguageClient client) {
        return file2Configuration.computeIfAbsent(client, cl -> {
            return new Configuration();
        });
    }

    //copied from SingleSourceFileUtil:
    static boolean isSingleSourceFile(FileObject fObj) {
        Project p = FileOwnerQuery.getOwner(fObj);
        if (p != null || !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        return true;
    }

    private static final class OptionsResultImpl extends CompilerOptionsQueryImplementation.Result {

        private final ChangeSupport cs = new ChangeSupport(this);
        private List<String> args = Collections.emptyList();

        public List<String> doParse(String line) {
            return parseLine(line);
        }

        private void setArguments(List<String> newArguments) {
            synchronized (this) {
                args = newArguments;
            }
            cs.fireChange();
        }

        @Override
        public synchronized List<? extends String> getArguments() {
            return args;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

    }

    private static final class SourceLevelResultImpl implements SourceLevelQueryImplementation2.Result {

        private static final String DEFAULT_SL = String.valueOf(SourceVersion.latest().ordinal() - SourceVersion.RELEASE_0.ordinal());
        private final ChangeSupport cs = new ChangeSupport(this);
        private String sourceLevel = DEFAULT_SL;

        @Override
        public synchronized String getSourceLevel() {
            return sourceLevel;
        }

        private void setSourceLevel(String sourceLevel) {
            synchronized (this) {
                this.sourceLevel = sourceLevel;
            }
            cs.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

    }

    private static final class Configuration {
        private List<String> currentOptions = Collections.emptyList();
        public final OptionsResultImpl compilerOptions;
        public final SourceLevelResultImpl sourceLevelResult;
        public final ProxyClassPathImplementation compileClassPathImplementation;
        public final ProxyClassPathImplementation compileModulePathImplementation;
        public final ClassPath compileClassPath;
        public final ClassPath moduleCompileClassPath;

        public Configuration() {
            compilerOptions = new OptionsResultImpl();
            sourceLevelResult = new SourceLevelResultImpl();
            compileClassPathImplementation = new ProxyClassPathImplementation();
            compileModulePathImplementation = new ProxyClassPathImplementation();
            compileClassPath = ClassPathFactory.createClassPath(compileClassPathImplementation);
            moduleCompileClassPath = ClassPathFactory.createClassPath(compileModulePathImplementation);
        }

        private boolean setConfiguration(String vmOptions) {
            List<String> newOptions = compilerOptions.doParse(vmOptions);

            synchronized (this) {
                if (currentOptions.equals(newOptions)) {
                    return false;
                }

                currentOptions = newOptions;
            }

            compilerOptions.setArguments(newOptions);

            String classpath = "";
            String modulepath = "";
            String sourceLevel = SourceLevelResultImpl.DEFAULT_SL;

            for (int i = 0; i < newOptions.size() - 1; i++) {
                String parameter = newOptions.get(i + 1);

                switch (newOptions.get(i)) {
                    case "-classpath": case "-cp": case "--class-path":
                        classpath = parameter;
                        break;
                    case "--module-path": case "-p":
                        modulepath = parameter;
                        break;
                    case "--source":
                        sourceLevel = parameter;
                        break;
                }
            }

            compileClassPathImplementation.setDelegates(spec2CP(classpath));
            compileModulePathImplementation.setDelegates(spec2CP(modulepath));

            sourceLevelResult.setSourceLevel(sourceLevel);

            return true;
        }

        private List<ClassPathImplementation> spec2CP(String spec) {
            List<PathResourceImplementation> entries;

            if (spec.isEmpty()) {
                entries = Collections.emptyList();
            } else {
                entries = ClassPathSupport.createClassPath(spec)
                                          .entries()
                                          .stream()
                                          .map(e -> e.getURL())
                                          .map(ClassPathSupport::createResource)
                                          .collect(Collectors.toList());
            }
            return Arrays.asList(ClassPathSupport.createClassPathImplementation(entries));
        }
    }

    private static final class ProxyClassPathImplementation implements ClassPathImplementation {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private List<ClassPathImplementation> delegates = Collections.emptyList();
        private List<? extends PathResourceImplementation> cachedResources = null;

        public void setDelegates(List<ClassPathImplementation> delegates) {
            synchronized (delegates) {
                this.delegates = new ArrayList<>(delegates);
                this.cachedResources = null;
            }
            pcs.firePropertyChange(PROP_RESOURCES, null, null);
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<ClassPathImplementation> delegates;

            synchronized (this) {
                if (cachedResources != null) {
                    return cachedResources;
                }

                delegates = this.delegates;
            }

            List<PathResourceImplementation> allResources = new ArrayList<>();

            delegates.stream().map(d -> d.getResources()).forEach(allResources::addAll);

            allResources = Collections.unmodifiableList(allResources);

            synchronized (this) {
                if (cachedResources == null && this.delegates == delegates) {
                    cachedResources = allResources;
                }
            }

            return allResources;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

    }

}
