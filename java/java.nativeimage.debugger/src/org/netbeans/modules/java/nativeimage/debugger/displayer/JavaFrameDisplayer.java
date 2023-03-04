/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.nativeimage.debugger.displayer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Display native frames like Java frames.
 *
 * @author martin
 */
public final class JavaFrameDisplayer implements FrameDisplayer {

    private final ClassPath sourcePath;

    public JavaFrameDisplayer(Project project) {
        this.sourcePath = findSourcePath(project);
    }

    @Override
    public DisplayedFrame displayed(NIFrame frame) {
        String functionName = frame.getFunctionName();
        if ("??".equals(functionName)) {    // NOI18N
            return null;
        }
        return createJavaFrame(frame);
    }

    private DisplayedFrame createJavaFrame(NIFrame frame) {
        return DisplayedFrame.newBuilder(getDisplayName(frame))
                .description(getDescription(frame))
                .line(frame.getLine())
                .sourceURISupplier(() -> getSourceURI(frame))
                .build();
    }

    private static String getDisplayName(NIFrame frame) {
        String functionName = frame.getFunctionName();
        String clsMethod;
        int methodEnd = functionName.indexOf('(');
        if (methodEnd < 0) {
            methodEnd = functionName.length();
        }
        int methodStart = functionName.indexOf("::");
        if (methodStart > 0) {
            int clsStart = functionName.lastIndexOf('.', methodStart);
            if (clsStart < 0) {
                clsStart = 0;
            } else {
                clsStart++;
            }
            String clazz = functionName.substring(clsStart, methodStart);
            String method = functionName.substring(methodStart + 2, methodEnd);
            clsMethod = clazz + '.' + method;
        } else {
            methodStart = functionName.lastIndexOf('.', methodEnd);
            if (methodStart < 0) {
                clsMethod = functionName.substring(0, methodEnd);
            } else {
                int clsStart = functionName.lastIndexOf('.', methodStart - 1);
                if (clsStart < 0) {
                    clsStart = 0;
                } else {
                    clsStart++;
                }
                clsMethod = functionName.substring(clsStart, methodEnd);
            }
        }
        int line = frame.getLine();
        if (line < 0) {
            return clsMethod;
        } else {
            return clsMethod + ':' + line;
        }
    }

    private static String getDescription(NIFrame frame) {
        String functionName = frame.getFunctionName();
        int methodEnd = functionName.indexOf('(');
        if (methodEnd < 0) {
            methodEnd = functionName.length();
        }
        String clsMethod = functionName.substring(0, methodEnd);
        clsMethod = clsMethod.replace("::", ".");
        int line = frame.getLine();
        if (line < 0) {
            return clsMethod;
        } else {
            return clsMethod + ':' + line;
        }
    }

    private URI getSourceURI(NIFrame frame) {
        String functionName = frame.getFunctionName();
        int methodEnd = functionName.indexOf('(');
        if (methodEnd < 0) {
            methodEnd = functionName.length();
        }
        if (methodEnd > 0) {
            int methodStart = functionName.indexOf("::");
            if (methodStart < 0) {
                methodStart = functionName.lastIndexOf('.', methodEnd);
            }
            if (methodStart > 0) {
                String className = functionName.substring(0, methodStart);
                URI uri = findClassURI(sourcePath, className);
                if (uri != null) {
                    return uri;
                }
            }
        }
        String fullFileName = frame.getFullFileName();
        if (fullFileName != null && !fullFileName.isEmpty()) {
            return new File(fullFileName).toURI();
        } else {
            return null;
        }
    }

    private static URI findClassURI(ClassPath sourcePath, String className) {
        String sourceName = className;
        int i = sourceName.indexOf ('$');
        if (i > 0) {
            sourceName = sourceName.substring (0, i);
        }
        sourceName = sourceName.replace('.', '/') + ".java";
        FileObject resource;
        if (sourcePath != null) {
            resource = sourcePath.findResource(sourceName);
        } else {
            resource = GlobalPathRegistry.getDefault().findResource(sourceName);
        }
        if (resource != null) {
            return resource.toURI();
        } else {
            return null;
        }
    }

    private static ClassPath findSourcePath(Project project) {
        if (project != null) {
            List<FileObject> allSourceRoots = new ArrayList<>();
            Set<FileObject> preferredRoots = new HashSet<>();
            Set<FileObject> addedBinaryRoots = new HashSet<>();
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup sg : sgs) {
                ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE);
                if (ecp == null) {
                    ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
                }
                if (ecp != null) {
                    FileObject[] binaryRoots = ecp.getRoots();
                    for (FileObject fo : binaryRoots) {
                        if (addedBinaryRoots.contains(fo)) {
                            continue;
                        }
                        addedBinaryRoots.add(fo);
                        FileObject[] roots = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
                        for (FileObject fr : roots) {
                            if (!preferredRoots.contains(fr)) {
                                allSourceRoots.add(fr);
                                preferredRoots.add(fr);
                            }
                        }
                    }
                }
            }
            return createClassPath(allSourceRoots);
        } else {
            return null;
        }
    }

    private static ClassPath createClassPath(Collection<FileObject> froots) {
        List<PathResourceImplementation> pris = new ArrayList<> ();
        for (FileObject fo : froots) {
            if (fo != null && fo.canRead()) {
                try {
                    URL url = fo.toURL();
                    pris.add(ClassPathSupport.createResource(url));
                } catch (IllegalArgumentException iaex) {
                    // Can be thrown from ClassPathSupport.createResource()
                    // Ignore - bad source root
                }
            }
        }
        return ClassPathSupport.createClassPath(pris);
    }

}
