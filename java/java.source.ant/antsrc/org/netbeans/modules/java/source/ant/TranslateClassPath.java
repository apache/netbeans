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

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class TranslateClassPath extends Task {

    private String classpath;
    private String targetProperty;
    private boolean clean;
    private boolean moduleOriented;
    
    public void setClasspath(String cp) {
        this.classpath = cp;
    }
    
    public void setTargetProperty(String tp) {
        this.targetProperty = tp;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void setModuleOriented(boolean moduleOriented) {
        this.moduleOriented = moduleOriented;
    }

    @Override
    public void execute() throws BuildException {
        if (classpath == null) {
            throw new BuildException("Classpath must be set.");
        }
        if (targetProperty == null) {
            throw new BuildException("Target property must be set.");
        }
        
        Project p = getProject();

        String translated = translate(classpath);
        
        p.setProperty(targetProperty, translated);
    }
    
    private String translate(String classpath) {
        StringBuilder cp = new StringBuilder();
        boolean first = true;
        boolean disableSources = Boolean.valueOf(getProject().getProperty("maven.disableSources"));
        
        for (String path : PropertyUtils.tokenizePath(classpath)) {
            File[] files = translateEntry(path, disableSources);

            if (files.length == 0) {
                //TODO: log
//                LOG.log(Level.FINE, "cannot translate {0} to file", e.getURL().toExternalForm());
                continue;
            }

            for (File f : files) {
                if (!first) {
                    cp.append(File.pathSeparatorChar);
                }

                cp.append(f.getAbsolutePath());
                first = false;
            }
        }

        return cp.toString();
    }
    
    private File[] translateEntry(final String path, boolean disableSources) throws BuildException {
        final File entryFile = new HackedFile(path);
        try {
            final URL entry = FileUtil.urlForArchiveOrDir(entryFile);
            final SourceForBinaryQuery.Result2 r = SourceForBinaryQuery.findSourceRoots2(entry);
            boolean appendEntry = false;

            if (!disableSources && r.preferSources() && r.getRoots().length > 0) {
                final List<File> translated = new ArrayList<File>();
                for (FileObject source : r.getRoots()) {
                    final File sourceFile = FileUtil.toFile(source);
                    if (sourceFile == null) {
                        log("Source URL: " + source.toURL().toExternalForm() + " cannot be translated to file, skipped", Project.MSG_WARN);
                        appendEntry = true;
                        continue;
                    }

                    final Boolean bamiResult = clean ? BuildArtifactMapperImpl.clean(Utilities.toURI(sourceFile).toURL())
                                               : BuildArtifactMapperImpl.ensureBuilt(Utilities.toURI(sourceFile).toURL(), getProject(), true, true);
                    if (bamiResult == null) {
                        appendEntry = true;
                        continue;
                    }
                    if (!bamiResult) {
                        throw new UserCancel();
                    }
                    
                    for (URL binary : BinaryForSourceQuery.findBinaryRoots(source.toURL()).getRoots()) {
                        final FileObject binaryFO = URLMapper.findFileObject(binary);
                        final File finaryFile = binaryFO != null ? FileUtil.toFile(binaryFO) : null;
                        if (finaryFile != null) {
                            if (moduleOriented && finaryFile.isDirectory() && "jar".equals(entry.getProtocol())) {
                                boolean hasModuleInfo = finaryFile.listFiles(new FilenameFilter() {
                                    @Override
                                    public boolean accept(File dir, String name) {
                                        return "module-info.class".equals(name);
                                    }
                                }).length > 0;
                                if (!hasModuleInfo) {
                                    File jarFile = new File(finaryFile.getParentFile(), entryFile.getName());
                                    Jar jarTask = new Jar();
                                    jarTask.setProject(getProject());
                                    jarTask.setDestFile(jarFile);
                                    jarTask.setBasedir(finaryFile);
                                    jarTask.setExcludes(".netbeans*");
                                    jarTask.execute();
                                    translated.add(jarFile);
                                    continue;
                                }
                            }
                            translated.add(finaryFile);
                        }
                    }                    
                }
                
                if (appendEntry) {
                    translated.add(entryFile);
                }
                return translated.toArray(new File[0]);
            } else {
                return new File[] {entryFile};
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    private static final class HackedFile extends File {
        private static final java.nio.file.InvalidPathException IP =
            new java.nio.file.InvalidPathException("", "") {    //NOI18N
                @Override
                public Throwable fillInStackTrace() {
                    return this;
                }
        };

        private final String path;

        private HackedFile(String path) {
            super(path);
            this.path = path;
        }

        @Override
        public boolean isDirectory() {
            return exists() ?
                    super.isDirectory() :
                    path.endsWith(File.separator);
        }
        @Override
        public File getAbsoluteFile() {
            return this;
        }

        @Override
        public Path toPath() {
            if (exists()) {
                return super.toPath();
            } else {
                throw IP;
            }
        }
    }
}
