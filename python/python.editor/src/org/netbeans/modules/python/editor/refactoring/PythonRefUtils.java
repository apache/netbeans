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
package org.netbeans.modules.python.editor.refactoring;

import java.awt.Color;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Name;

/**
 * Various utilies related to Python refactoring; the generic ones are based
 * on the ones from the Java refactoring module.
 * 
 */
public class PythonRefUtils {
    private PythonRefUtils() {
    }

    /** Compute the names (full and simple, e.g. Foo::Bar and Bar) for the given node, if any, and return as 
     * a String[2] = {name,simpleName} */
    public static String[] getNodeNames(PythonTree node) {
        String name = null;
        String simpleName = null;

        if (node instanceof Name) {
            name = ((Name)node).getInternalId();
        } else {
            name = PythonAstUtils.getName(node);
        }
        // TODO: Compute FQN


        if (simpleName == null) {
            simpleName = name;
        }

        return new String[]{name, simpleName};
    }

    public static CloneableEditorSupport findCloneableEditorSupport(PythonParserResult info) {
        DataObject dob = null;
        try {
            dob = DataObject.find(info.getSnapshot().getSource().getFileObject());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return PythonRefUtils.findCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }

    public static String htmlize(String input) {
        try {
            return XMLUtil.toElementContent(input);
        } catch (CharConversionException cce) {
            Exceptions.printStackTrace(cce);
            return input;
        }
    }

    /** Return the most distant method in the hierarchy that is overriding the given method, or null */
//    public static IndexedMethod getOverridingMethod(PythonElementCtx element, CompilationInfo info) {
//        PythonIndex index = PythonIndex.get(info.getIndex(PythonTokenId.PYTHON_MIME_TYPE), info.getFileObject());
//        String fqn = PythonAstUtils.getFqnName(element.getPath());
//
//        return index.getOverridingMethod(fqn, element.getName());
//    }
    public static String getHtml(String text) {
        StringBuffer buf = new StringBuffer();
        // TODO - check whether we need python highlighting or rhtml highlighting
        TokenHierarchy tokenH = TokenHierarchy.create(text, PythonTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(PythonMIMEResolver.PYTHON_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        @SuppressWarnings("unchecked")
        TokenSequence<? extends TokenId> tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<? extends TokenId> token = tok.token();
            String category = token.id().name();
            AttributeSet set = settings.getTokenFontColors(category);
            if (set == null) {
                category = token.id().primaryCategory();
                if (category == null) {
                    category = "whitespace"; //NOI18N
                }
                set = settings.getTokenFontColors(category);
            }
            String tokenText = htmlize(token.text().toString());
            buf.append(color(tokenText, set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    public static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        Project p = FileOwnerQuery.getOwner(file);
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (Project opened1 : opened) {
            if (p == opened1) {
                return true;
            }
        }
        return false;
    }
    // From PythonProjectType
    public static final String SOURCES_TYPE_PYTHON = "python"; // NOI18N

    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return false;
        }
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (Project prj : opened) {
            if (p == prj) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(SOURCES_TYPE_PYTHON);
                for (SourceGroup group : gr) {
                    if (fo == group.getRootFolder()) {
                        return true;
                    }
                    if (FileUtil.isParentOf(group.getRootFolder(), fo)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static boolean isClasspathRoot(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp != null) {
            FileObject f = cp.findOwnerRoot(fo);
            if (f != null) {
                return fo.equals(f);
            }
        }

        return false;
    }

    public static boolean isRefactorable(FileObject file) {
        return PythonUtils.canContainPython(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }

    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE).getResourceName(folder, '.', false);
    }

    public static FileObject getClassPathRoot(URL url) throws IOException {
        FileObject result = URLMapper.findFileObject(url);
        File f = FileUtil.normalizeFile(new File(url.getPath()));
        while (result == null) {
            result = FileUtil.toFileObject(f);
            f = f.getParentFile();
        }
        return ClassPath.getClassPath(result, ClassPath.SOURCE).findOwnerRoot(result);
    }

    public static ElementKind getElementKind(PythonElementCtx tph) {
        return tph.getKind();
    }

//    public static ClasspathInfo getClasspathInfoFor(FileObject... files) {
//        assert files.length > 0;
//        Set<URL> dependentRoots = new HashSet<URL>();
//        for (FileObject fo : files) {
//            Project p = null;
//            if (fo != null) {
//                p = FileOwnerQuery.getOwner(fo);
//            }
//            if (p != null) {
//                ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//                if (classPath == null) {
//                    return null;
//                }
//                FileObject ownerRoot = classPath.findOwnerRoot(fo);
//                if (ownerRoot != null) {
//                    URL sourceRoot = URLMapper.findURL(ownerRoot, URLMapper.INTERNAL);
//                    dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
//                    for (SourceGroup root : ProjectUtils.getSources(p).getSourceGroups(SOURCES_TYPE_PYTHON)) {
//                        dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
//                    }
//                } else {
//                    dependentRoots.add(URLMapper.findURL(fo.getParent(), URLMapper.INTERNAL));
//                }
//            } else {
//                for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
//                    for (FileObject root : cp.getRoots()) {
//                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
//                    }
//                }
//            }
//        }
//
//        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
//        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
//        ClassPath boot = files[0] != null ? ClassPath.getClassPath(files[0], ClassPath.BOOT) : nullPath;
//        ClassPath compile = files[0] != null ? ClassPath.getClassPath(files[0], ClassPath.COMPILE) : nullPath;
//
//        if (boot == null || compile == null) { // 146499
//            return null;
//        }
//
//        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
//        return cpInfo;
//    }
//
//    public static ClasspathInfo getClasspathInfoFor(PythonElementCtx ctx) {
//        return getClasspathInfoFor(ctx.getFileObject());
//    }
//
//    public static List<FileObject> getPythonFilesInProject(FileObject fileInProject) {
//        List<FileObject> list = new ArrayList<FileObject>(100);
//        ClasspathInfo cpInfo = PythonRefUtils.getClasspathInfoFor(fileInProject);
//        if (cpInfo == null) {
//            return list;
//        }
//        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
//        for (ClassPath.Entry entry : cp.entries()) {
//            FileObject root = entry.getRoot();
//            String name = root.getName();
//            // Skip non-refactorable parts in renaming
//            if (name.equals("vendor") || name.equals("script")) { // NOI18N
//                continue;
//            }
//            addPythonFiles(list, root);
//        }
//
//        return list;
//    }
//
//    private static void addPythonFiles(List<FileObject> list, FileObject f) {
//        if (f.isFolder()) {
//            for (FileObject child : f.getChildren()) {
//                addPythonFiles(list, child);
//            }
//        } else if (PythonUtils.canContainPython(f)) {
//            list.add(f);
//        }
//    }
}
