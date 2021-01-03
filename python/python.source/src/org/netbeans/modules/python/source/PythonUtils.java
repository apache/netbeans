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
package org.netbeans.modules.python.source;

import java.util.Comparator;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Name;

public class PythonUtils {
    public static boolean canContainPython(FileObject f) {
        String mimeType = f.getMIMEType();
        return PythonMIMEResolver.PYTHON_MIME_TYPE.equals(mimeType);
    // TODO:       "text/x-yaml".equals(mimeType) ||  // NOI18N
    // RubyInstallation.RHTML_MIME_TYPE.equals(mimeType);
    }

    public static boolean isPythonFile(FileObject f) {
        return PythonMIMEResolver.PYTHON_MIME_TYPE.equals(f.getMIMEType());
    }

    public static boolean isRstFile(FileObject f) {
        return "rst".equals(f.getExt()); // NOI18N
    }

    public static boolean isPythonDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N

        return PythonMIMEResolver.PYTHON_MIME_TYPE.equals(mimeType);
    }
    public static final String DOT__INIT__ = ".__init__"; // NOI18N

    // From PythonProjectType
    public static final String SOURCES_TYPE_PYTHON = "python"; // NOI18N

    // Cache
    private static FileObject prevParent;
    private static String prevRootUrl;

    /**
     * Produce the module name (including packages) for a given python source file.
     * 
     * @param fo The source file (can be null, if file is not))
     * @param file The parser file (can be null, if fo is not).
     * @param fileName The filename (basename only)
     * @param projectRelativeName If non null, the path from the project root down to this file
     * @return A string for the full package module name
     */
    public static String getModuleName(@NonNull FileObject fo) {

        // TODO - use PythonPlatform's library roots!

        String module = fo.getName();

        // First see if we're on the load path for the platform, and if so,
        // use that as the base
        // TODO - look up platform for the current search context instead of all platforms!!
        if (fo.getParent() != prevParent) {
            prevRootUrl = null;
            prevParent = fo.getParent();
        }

        String url = fo.toURL().toExternalForm();
        if (prevRootUrl == null) {
            boolean found = false;
            PythonPlatformManager manager = PythonPlatformManager.getInstance();

            PlatformSearch:
            for (String name : manager.getPlatformList()) {
                PythonPlatform platform = manager.getPlatform(name);
                if (platform != null) {
                    List<FileObject> unique = platform.getUniqueLibraryRoots();
                    for (FileObject root : unique) {
                        if (FileUtil.isParentOf(root, fo)) {
                            for (FileObject r : platform.getLibraryRoots()) {
                                if (FileUtil.isParentOf(r, fo)) {
                                    // See if the folder itself contains
                                    // an __init__.py file - if it does,
                                    // then include the directory itself
                                    // in the package name.
                                    if (r.getFileObject("__init__.py") != null) { // NOI18N
                                        r = r.getParent();
                                    }

                                    prevRootUrl = r.toURL().toExternalForm();
                                    found = true;
                                    break PlatformSearch;
                                }
                            }
                            break PlatformSearch;
                        }
                    }
                }
            }

            if (!found) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    Sources source = ProjectUtils.getSources(project);
                    // Look up the source path
                    SourceGroup[] sourceGroups = source.getSourceGroups(SOURCES_TYPE_PYTHON);
                    for (SourceGroup group : sourceGroups) {
                        FileObject folder = group.getRootFolder();
                        if (FileUtil.isParentOf(folder, fo)) {
                            // See if the folder itself contains
                            // an __init__.py file - if it does,
                            // then include the directory itself
                            // in the package name.
                            if (folder.getFileObject("__init__.py") != null) { // NOI18N
                                folder = folder.getParent();
                            }

                            prevRootUrl = folder.toURL().toExternalForm();
                            break;
                        }
                    }
                }
            }
        }

        if (prevRootUrl != null) {
            module = url.substring(prevRootUrl.length());
            if (module.startsWith("/")) {
                module = module.substring(1);
            }
        }

        // Strip off .y extension
        if (module.endsWith(".py")) { // NOI18N
            module = module.substring(0, module.length() - 3);
        }

        if (module.indexOf('/') != -1) {
            module = module.replace('/', '.');
        }

        if (module.endsWith(DOT__INIT__)) {
            module = module.substring(0, module.length() - DOT__INIT__.length());
        }

        return module;
    }

    // According to https://hg.python.org/cpython/file/3.5/Lib/keyword.py
    // and https://hg.python.org/cpython/file/2.7/Lib/keyword.py
    // Duplicate in org.netbeans.modules.python.editor.PythonCodeCompleter
    static final String[] PYTHON_KEYWORDS = new String[]{
        "False", // NOI18N
        "None", // NOI18N
        "True", // NOI18N
        "and", // NOI18N
        "as", // NOI18N
        "assert", // NOI18N
        "break", // NOI18N
        "class", // NOI18N
        "continue", // NOI18N
        "def", // NOI18N
        "del", // NOI18N
        "elif", // NOI18N
        "else", // NOI18N
        "except", // NOI18N
        "finally", // NOI18N
        "for", // NOI18N
        "from", // NOI18N
        "global", // NOI18N
        "if", // NOI18N
        "import", // NOI18N
        "in", // NOI18N
        "is", // NOI18N
        "lambda", // NOI18N
        "nonlocal", // NOI18N
        "not", // NOI18N
        "or", // NOI18N
        "pass", // NOI18N
        "raise", // NOI18N
        "return", // NOI18N
        "try", // NOI18N
        "while", // NOI18N
        "with", // NOI18N
        "yield", // NOI18N
        "async", // NOI18N, Python 3.5 only
        "await", // NOI18N, Python 3.5 only
        "exec", // NOI18N, Python 2 only
        "print", // NOI18N, Pytohn 2 only, function in python 3
    };

    public static boolean isPythonKeyword(String name) {
        for (String s : PYTHON_KEYWORDS) {
            if (s.equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return true iff the name is a class name
     * @param name The name
     * @param emptyDefault Whether empty or _ names should be considered a class name or not
     * @return True iff the name looks like a class name
     */
    public static boolean isClassName(String name, boolean emptyDefault) {
        if (name == null || name.length() == 0) {
            return emptyDefault;
        }
        if (name.startsWith("_") && name.length() > 1) {
            return Character.isUpperCase(name.charAt(1));
        }

        return Character.isUpperCase(name.charAt(0));
    }

    /**
     * Return true iff the name is a method name
     * @param name The name
     * @param emptyDefault Whether empty or _ names should be considered a class name or not
     * @return True iff the name looks like a method name
     */
    public static boolean isMethodName(String name, boolean emptyDefault) {
        if (name == null || name.length() == 0) {
            return emptyDefault;
        }
        if (name.startsWith("__") && name.length() > 2) {
            return Character.isLowerCase(name.charAt(2));
        }
        if (name.startsWith("_") && name.length() > 1) {
            return Character.isLowerCase(name.charAt(1));
        }

        return Character.isLowerCase(name.charAt(0));
    }

    public static boolean isValidPythonClassName(String name) {
        if (isPythonKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                return false;
            }

        }

        return true;
    }

    /** Is this name a valid operator name? */
    public static boolean isOperator(String name) {
        // TODO - update to Python
        if (name.length() == 0) {
            return false;
        }

        switch (name.charAt(0)) {
        case '+':
            return name.equals("+") || name.equals("+@");
        case '-':
            return name.equals("-") || name.equals("-@");
        case '*':
            return name.equals("*") || name.equals("**");
        case '<':
            return name.equals("<") || name.equals("<<") || name.equals("<=") || name.equals("<=>");
        case '>':
            return name.equals(">") || name.equals(">>") || name.equals(">=");
        case '=':
            return name.equals("=") || name.equals("==") || name.equals("===") || name.equals("=~");
        case '!':
            return name.equals("!=") || name.equals("!~");
        case '&':
            return name.equals("&") || name.equals("&&");
        case '|':
            return name.equals("|") || name.equals("||");
        case '[':
            return name.equals("[]") || name.equals("[]=");
        case '%':
            return name.equals("%");
        case '/':
            return name.equals("/");
        case '~':
            return name.equals("~");
        case '^':
            return name.equals("^");
        case '`':
            return name.equals("`");
        default:
            return false;
        }
    }

    public static boolean isValidPythonMethodName(String name) {
        if (isPythonKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        // TODO - allow operators
        if (isOperator(name)) {
            return true;
        }

        if (Character.isUpperCase(name.charAt(0)) || Character.isWhitespace(name.charAt(0))) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }

        }

        return true;
    }

    public static boolean isValidPythonIdentifier(String name) {
        if (isPythonKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            // Identifier char isn't really accurate - I can have a function named "[]" etc.
            // so just look for -obvious- mistakes
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }

        // TODO - make this more accurate, like the method validifier
        }

        return true;
    }

    /**
     * Ruby identifiers should consist of [a-zA-Z0-9_]
     * http://www.headius.com/rubyspec/index.php/Variables
     * <p>
     * This method also accepts the field/global chars
     * since it's unlikely
     */
    public static boolean isSafeIdentifierName(String name, int fromIndex) {
        int i = fromIndex;
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c == '$' || c == '@' || c == ':')) {
                break;
            }
        }
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c == '_') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    (c == '?') || (c == '=') || (c == '!'))) { // Method suffixes; only allowed on the last line

                if (isOperator(name)) {
                    return true;
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Return null if the given identifier name is valid, otherwise a localized
     * error message explaining the problem.
     */
    public static String getIdentifierWarning(String name, int fromIndex) {
        if (isSafeIdentifierName(name, fromIndex)) {
            return null;
        } else {
            return NbBundle.getMessage(PythonUtils.class, "UnsafeIdentifierName");
        }
    }

    /** @todo Move into GsfUtilities after 6.5 */
    public static int getOffsetByLineCol(String source, int line, int col) {
        int offset = 0;
        for (int i = 0; i < line; i++) {
            offset = source.indexOf('\n', offset);
            if (offset == -1) {
                offset = source.length();
                break;
            }
            offset++;
        }
        if (col > 0) { // -1: invalid
            offset += col;
        }

        return offset;
    }
    public static Comparator NAME_NODE_COMPARATOR = new Comparator<Name>() {
        @Override
        public int compare(Name n1, Name n2) {
            return n1.getInternalId().compareTo(n2.getInternalId());
        }
    };
    public static Comparator ATTRIBUTE_NAME_NODE_COMPARATOR = new Comparator<Object>() {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Object n1, Object n2) {
            String s1 = "";
            String s2 = "";

            if (n1 instanceof Name) {
                s1 = ((Name)n1).getInternalId();
            } else if (n1 instanceof Attribute) {
                Attribute a = (Attribute)n1;
                String v = PythonAstUtils.getName(a.getInternalValue());
                if (v != null) {
                    s1 = a.getInternalAttr() + "." + v;
                } else {
                    s1 = a.getInternalAttr();
                }
            }

            if (n2 instanceof Name) {
                s2 = ((Name)n2).getInternalId();
            } else if (n2 instanceof Attribute) {
                Attribute a = (Attribute)n2;
                String v = PythonAstUtils.getName(a.getInternalValue());
                if (v != null) {
                    s2 = a.getInternalAttr() + "." + v;
                } else {
                    s2 = a.getInternalAttr();
                }
            }

            return s1.compareTo(s2);
        }
    };
    public static Comparator NODE_POS_COMPARATOR = new Comparator<PythonTree>() {
        @Override
        public int compare(PythonTree p1, PythonTree p2) {
            int ret = p1.getCharStartIndex() - p2.getCharStartIndex();
            if (ret != 0) {
                return ret;
            }
            ret = p2.getCharStopIndex() - p1.getCharStopIndex();
            if (ret != 0) {
                return ret;
            }
            return p2.getAntlrType() - p1.getAntlrType();
        }
    };
}
