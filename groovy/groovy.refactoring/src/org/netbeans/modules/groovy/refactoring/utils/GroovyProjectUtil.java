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

package org.netbeans.modules.groovy.refactoring.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.support.api.GroovyExtender;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class GroovyProjectUtil {

    public static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            // This doesn't seem to work well - a lot of the time, I'm right clicking
            // on the editor and it still has another activated view (this is on the mac)
            // and as a result does file-oriented refactoring rather than the specific
            // editor node...
            //            TopComponent activetc = TopComponent.getRegistry().getActivated();
            //            if (activetc instanceof CloneableEditorSupport.Pane) {
            //
            return true;
            //            }
        }

        return false;
    }


    public static boolean isOnSourceClasspath(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return false;
        }
        if (OpenProjects.getDefault().isProjectOpen(project)) {
            for (SourceGroup group : GroovySources.getGroovySourceGroups(ProjectUtils.getSources(project))) {
                if (group.getRootFolder().equals(fo)) {
                    return true;
                }
                if (FileUtil.isParentOf(group.getRootFolder(), fo)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        assert files.length > 0;
        final Set<URL> dependentRoots = new HashSet<URL>();
        for (FileObject fo : files) {
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);

                if (p != null) {
                    ClassPath sourceClasspath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                    if (sourceClasspath != null) {
                        final URL sourceRoot = URLMapper.findURL(sourceClasspath.findOwnerRoot(fo), URLMapper.INTERNAL);
                        for (URL root : SourceUtils.getDependentRoots(sourceRoot)) {
                            dependentRoots.add(root);
                        }
                    }

                    for (SourceGroup root : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
                    }

                } else {
                    for(ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                        for (FileObject root : cp.getRoots()) {
                            dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                        }
                    }
                }
            }
        }

        final ClassPath bootCP = getClassPath(files[0], ClassPath.BOOT);
        final ClassPath compileCP = getClassPath(files[0], ClassPath.COMPILE);
        final ClassPath sourceCP = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[0]));
        
        return ClasspathInfo.create(bootCP, compileCP, sourceCP);
    }

    private static ClassPath getClassPath(FileObject fo, String classPathType) {
        if (fo != null) {
            final ClassPath classPath = ClassPath.getClassPath(fo, classPathType);
            if (classPath != null) {
                return classPath;
            } else {
                return ClassPath.EMPTY;
            }
        } else {
            return ClassPath.EMPTY;
        }
    }

    public static List<FileObject> getGroovyFilesInProject(FileObject fileInProject) {
        ClasspathInfo cpInfo = GroovyProjectUtil.getClasspathInfoFor(fileInProject);
        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        List<FileObject> list = new ArrayList<FileObject>(100);
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject root = entry.getRoot();
            addGroovyFiles(list, root);
        }

        return list;
    }

    private static void addGroovyFiles(List<FileObject> list, FileObject f) {
        if (f.isFolder()) {
            for (FileObject child : f.getChildren()) {
                addGroovyFiles(list, child);
            }
        } else if (isGroovyFile(f)) {
            list.add(f);
        }
    }

    public static boolean isInGroovyProject(FileObject f) {
        Project project = FileOwnerQuery.getOwner(f);
        if (project != null) {
            return GroovyExtender.isActive(project);
        }
        return false;
    }

    public static boolean isGroovyFile(FileObject f) {
        return GroovyTokenId.GROOVY_MIME_TYPE.equals(f.getMIMEType());
    }

    private static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getLookup().lookup(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
        }
        return result;
    }

    public static Line getLine(FileObject fileObject, int lineNumber) {
        LineCookie lineCookie = getLineCookie(fileObject);
        assert lineCookie != null;
        try {
            return lineCookie.getLineSet().getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioobe) {
            // invalid line number for the document
            return null;
        }
    }

    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fileObject) {
        try {
            final DataObject dob = DataObject.find(fileObject);
            return GroovyProjectUtil.findCloneableEditorSupport(dob);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getLookup().lookup(OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getLookup().lookup(EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }

    // FIXME remove this
    public static BaseDocument getDocument(ParserResult info, FileObject fo) {
        BaseDocument doc = null;

        try {
            if (info != null) {
                doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            }

            if (doc == null) {
                // Gotta open it first
                DataObject dataObject = DataObject.find(fo);
                EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);

                if (ec != null) {
                    doc = (BaseDocument)ec.openDocument();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }

    /**
     * Sets the given <code>toAdd</code> as the following problem for
     * the given <code>existing</code> problem.
     *
     * @param toAdd the problem to add, may be null.
     * @param existing the problem whose following problem should be set, may be null.
     *
     * @return the existing problem with its following problem
     * set to the given problem or null if both of the params
     * were null.
     *
     */
    public static Problem addToEnd(Problem toAdd, Problem existing){
        if (existing == null){
            return toAdd;
        }
        if (toAdd == null){
            return existing;
        }

        Problem tail = existing;
        while(tail.getNext() != null){
            tail = tail.getNext();
        }
        tail.setNext(toAdd);

        return tail;
    }
}
