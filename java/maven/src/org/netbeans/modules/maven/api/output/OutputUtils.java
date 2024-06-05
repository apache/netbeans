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

package org.netbeans.modules.maven.api.output;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author mkleint
 */
public final class OutputUtils {

    // example: '[WARN]  	at java.base/java.util.ImmutableCollections.uoe(ImmutableCollections.java:142)'
    public static final Pattern linePattern = Pattern.compile("(?:\\[\\w+\\])?\\s*(?:\\[catch\\]\\s)?at\\s(.*)\\((?:Native Method|(.*)\\.java\\:(\\d+))\\)"); //NOI18N
 
    private static final Map<Project, StacktraceOutputListener> projectStacktraceListeners = new WeakHashMap<>();
    private static final Map<FileObject, StacktraceOutputListener> fileStacktraceListeners = new WeakHashMap<>();
    
    /** Creates a new instance of OutputUtils */
    private OutputUtils() {
    }
    
    /**
     * 
     * @param line
     * @param classPath
     * @return 
     * @deprecated use {@link #matchStackTraceLine(java.lang.String, org.openide.filesystems.FileObject)}  
     *              or {@link #matchStackTraceLine(java.lang.String, org.netbeans.api.project.Project)} instead.
     */
    @Deprecated
    public static OutputListener matchStackTraceLine(String line, ClassPath classPath) {
        StacktraceAttributes sa = matchStackTraceLine(line);
        return sa != null ? new ClassPathStacktraceOutputListener(classPath, sa) : null;
    }
    
    /**
     * 
     * @param line
     * @param project
     * @return 
     */
    public static OutputListener matchStackTraceLine(String line, Project project) {
        StacktraceAttributes sa = matchStackTraceLine(line);
        if(sa != null) {
            synchronized(projectStacktraceListeners) {
                StacktraceOutputListener list = projectStacktraceListeners.get(project);
                if(list == null) {
                    list = new ProjectStacktraceOutputListener(project);
                    projectStacktraceListeners.put(project, list);
                }
                return list;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param line
     * @param fileObject
     * @return 
     */
    public static OutputListener matchStackTraceLine(String line, FileObject fileObject) {
        StacktraceAttributes sa = matchStackTraceLine(line);
        if(sa != null) {
            synchronized(fileStacktraceListeners) {
                StacktraceOutputListener list = fileStacktraceListeners.get(fileObject);
                if(list == null) {
                    list = new FileObjectStacktraceOutputListener(fileObject);
                    fileStacktraceListeners.put(fileObject, list);
                }
                return list;
            }
        }
        return null;
    }
    
    static StacktraceAttributes matchStackTraceLine(String line) {
        if (!line.endsWith(")")) {
            return null; // fast path -> not a stack trace
        }
        Matcher match = linePattern.matcher(line);
        if (match.matches() && match.groupCount() == 3) {
            String method = match.group(1);
            String file = match.group(2);
            String lineNum = match.group(3);
            int index = file == null || file.isEmpty() ? -1 : method.indexOf(file);
            if (index > -1) {
                return new StacktraceAttributes(method, file, lineNum);
            }
        }
        return null;
    }
    
    static final class StacktraceAttributes {
        final String method;
        final String file;
        final String lineNum;
        public StacktraceAttributes(String method, String file, String lineNum) {
            this.method = method;
            this.file = file;
            this.lineNum = lineNum;
        }
    }
    
    private abstract static class StacktraceOutputListener implements OutputListener {
        
        protected abstract ClassPath getClassPath();
        
        protected StacktraceAttributes getStacktraceAttributes(String line) {
            return matchStackTraceLine(line);
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
    //            cookie.getLineSet().getCurrent(line).show(Line.SHOW_SHOW);
        }

        /** Called when some sort of action is performed on a line.
         * @param ev the event describing the line
         */
        @Override
        @NbBundle.Messages({
            "# {0} - class name",
            "NotFound=Class \"{0}\" not found on classpath", 
            "# {0} - file name",
            "NoSource=Source file not found for \"{0}\""
        })
        public void outputLineAction(OutputEvent ev) {
            StacktraceAttributes sa = matchStackTraceLine(ev.getLine());
            if(sa == null || sa.method == null || sa.file == null) {
                Logger.getLogger(OutputUtils.class.getName()).log(Level.WARNING, "No file found for output line {0}", ev.getLine()); // NOI18N
                StatusDisplayer.getDefault().setStatusText(Bundle.NoSource(ev.getLine()));
                return;
            }

            // example: at java.base/java.io.FileReader.<init>(FileReader.java:60)
            int start = sa.method.indexOf('/') + 1;
            int end = sa.method.lastIndexOf(sa.file);
            String packageName = sa.method.substring(start, end).replace('.', '/'); //NOI18N
            String resourceName = packageName + sa.file + ".class"; //NOI18N

            // issue #258546; have to check all resources. javafx unpacks all classes to target,
            // SourceForBinaryQuery then fails to find the according java file ...            
            ClassPath classPath = getClassPath();
            List<FileObject> resources = classPath.findAllResources(resourceName);
            if (resources != null && !resources.isEmpty()) {
                // find and open source file
                for (FileObject resource : resources) {
                    FileObject root = classPath.findOwnerRoot(resource);
                    if (root != null) {
                        URL url = URLMapper.findURL(root, URLMapper.INTERNAL);
                        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(url);
                        FileObject[] rootz = res.getRoots();
                        for (int i = 0; i < rootz.length; i++) {
                            String path = packageName + sa.file + ".java"; //NOI18N
                            FileObject javaFo = rootz[i].getFileObject(path);
                            if (javaFo != null) {
                                try {
                                    DataObject obj = DataObject.find(javaFo);
                                    EditorCookie cookie = obj.getLookup().lookup(EditorCookie.class);
                                    if(cookie != null) {
                                        int lineInt = Integer.parseInt(sa.lineNum);
                                        try {
                                            cookie.getLineSet().getCurrent(lineInt - 1).show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                        } catch (IndexOutOfBoundsException x) { // #155880
                                            cookie.open();
                                        }
                                    } else {
                                        Logger.getLogger(OutputUtils.class.getName()).log(Level.WARNING, "No cookie found for dataobject {0}", obj); // NOI18N
                                    }
                                    return;
                                } catch (DataObjectNotFoundException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
                // open class file as fallback
                try {
                    FileObject resource = classPath.findResource(resourceName);
                    if (resource != null) {
                        DataObject dao = DataObject.find(resource);
                        OpenCookie cookie = dao.getLookup().lookup(OpenCookie.class);
                        if (cookie != null) {
                            cookie.open();
                        }
                    }
                } catch (DataObjectNotFoundException ignore) {}
            } else {
                StatusDisplayer.getDefault().setStatusText(Bundle.NotFound(sa.file));
            }
        }

        /** Called when a line is cleared from the buffer of known lines.
         * @param ev the event describing the line
         */
        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }

    private static ClassPath createProxyClassPath(Stream<ClassPath> paths) {
        List<ClassPath> cp = paths.collect(Collectors.toList());
        return ClassPathSupport.createProxyClassPath(cp.toArray(new ClassPath[0]));
    }
    
    private static class ProjectStacktraceOutputListener extends StacktraceOutputListener {
        private final WeakReference<Project> ref;

        public ProjectStacktraceOutputListener(Project project) {
            this.ref = new WeakReference<>(project);
        }

        @Override
        protected ClassPath getClassPath() {
            Project prj = ref.get();
            if(prj != null) {
                ProjectSourcesClassPathProvider prov = prj.getLookup().lookup(ProjectSourcesClassPathProvider.class);
                // appending compile cp probably doesn't hurt although everything should be in exec cp already
                return createProxyClassPath(
                    Stream.of(Stream.of(prov.getProjectClassPaths(ClassPath.EXECUTE)),
                              Stream.of(prov.getProjectClassPaths(ClassPath.COMPILE)),
                              Stream.of(prov.getProjectClassPaths(ClassPath.BOOT)))
                          .flatMap(s -> s)
                );
            }
            return null;
        }        
    }
    
    private static class FileObjectStacktraceOutputListener extends StacktraceOutputListener {
        private final WeakReference<FileObject> ref;

        public FileObjectStacktraceOutputListener(FileObject file) {
            this.ref = new WeakReference<>(file);
        }

        @Override
        protected ClassPath getClassPath() {
            FileObject fileObject = ref.get();
            if(fileObject != null) {
                return createProxyClassPath(
                    Stream.of(ClassPath.getClassPath(fileObject, ClassPath.EXECUTE),
                              ClassPath.getClassPath(fileObject, ClassPath.COMPILE),
                              ClassPath.getClassPath(fileObject, ClassPath.BOOT)));
            }
            return null;
        }        
    }
    
    @Deprecated
    private static class ClassPathStacktraceOutputListener extends StacktraceOutputListener {
        private final ClassPath classPath;
        private final StacktraceAttributes sa;

        public ClassPathStacktraceOutputListener(ClassPath classPath, StacktraceAttributes sa) {
            this.classPath = classPath;
            this.sa = sa;
        }

        @Override
        protected ClassPath getClassPath() {
            return classPath;
        }        

        @Override
        protected StacktraceAttributes getStacktraceAttributes(String line) {
            return sa;
        }
    }
    
}
