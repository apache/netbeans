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
package org.netbeans.modules.refactoring.php;

import java.awt.Color;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;

/**
 * Various utilies related to Php refactoring; the generic ones are based
 * on the ones from the Java refactoring module.
 *
 * @author Jan Becicka, Tor Norbye, Jan Lahoda, Radek Matous
 */
public final class RefactoringUtils {

    private RefactoringUtils() {
    }

    public static Program getRoot(ParserResult info) {
        return (info instanceof PHPParseResult) ? ((PHPParseResult) info).getProgram() : null;
    }

    public static Source getSource(Document doc) {
        Source source = Source.create(doc);
        return source;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fo) {
        DataObject dob = null;
        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dob == null ? null : RefactoringUtils.findCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        obj = dob.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
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

    public static String getHtml(String text) {
        StringBuilder buf = new StringBuilder();
        // TODO - check whether we need Js highlighting or rhtml highlighting
        TokenHierarchy tokenH = TokenHierarchy.create(text, PHPTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(FileUtils.PHP_MIME_TYPE));
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
        StringBuilder buf = new StringBuilder(string);
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
        String htmlColor = "#" + colorR + colorG + colorB; //NOI18N

        return htmlColor;
    }

    public static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        Project p = ProjectConvertors.getNonConvertorOwner(file);
        return OpenProjects.getDefault().isProjectOpen(p);
    }

    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = ProjectConvertors.getNonConvertorOwner(fo);
        if (p == null) {
            return false;
        }
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < opened.length; i++) {
            if (p.equals(opened[i]) || opened[i].equals(p)) {
                //SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(JsProject.SOURCES_TYPE_Js);
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
                for (int j = 0; j < gr.length; j++) {
                    if (fo == gr[j].getRootFolder()) {
                        return true;
                    }
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo)) {
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
        return FileUtils.isPhpFile(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }

    /**
     * Creates or finds FileObject according to.
     * @param url
     * @return FileObject
     */
    public static FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null) {
                return result;
            }
            File f = Utilities.toFile(url.toURI());

            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public static boolean isOutsidePhp(Lookup lookup, FileObject fo) {
        return isOutsideLanguage(lookup, fo, PHPTokenId.language());
    }

    public static boolean isOutsidePHPDoc(Lookup lookup, FileObject fo) {
        return isOutsideLanguage(lookup, fo, PHPDocCommentTokenId.language());
    }

    public static boolean isOutsideLanguage(Lookup lookup, FileObject fo, Language<? extends TokenId> language) {
        boolean result = false;
        if (FileUtils.isPhpFile(fo)) {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (isFromEditor(ec)) {
                JTextComponent textC = ec.getOpenedPanes()[0];
                Document d = textC.getDocument();
                if (!(d instanceof BaseDocument)) {
                    result = true;
                } else {
                    int caret = textC.getCaretPosition();
                    result = LexUtilities.getMostEmbeddedTokenSequence(d, caret, true).language() != language;
                }
            }
        }
        return result;
    }

    public static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

    public static List<ASTNode> underCaret(ParserResult info, final int offset) {
        class Result extends Error {

            private final Stack<ASTNode> result;

            public Result(Stack<ASTNode> result) {
                this.result = result;
            }

            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        }
        try {
            new DefaultVisitor() {

                private final Stack<ASTNode> s = new Stack<>();

                @Override
                public void scan(ASTNode node) {
                    if (node == null) {
                        return;
                    }

                    if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                        s.push(node);
                        super.scan(node);
                        throw new Result(s);
                    }
                }
            }.scan(RefactoringUtils.getRoot(info));
        } catch (Result r) {
            return new LinkedList<>(r.result);
        }

        return Collections.emptyList();
    }

    public static boolean isQuoted(String value) {
        return value.length() >= 2
                && (value.startsWith("\"") || value.startsWith("'"))
                && (value.endsWith("\"") || value.endsWith("'"));
    }

    public static String dequote(String value) {
        assert isQuoted(value);

        return value.substring(1, value.length() - 1);
    }

    public static FileObject resolveInclude(ParserResult info, Include include) {
        Expression e = include.getExpression();
        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }
        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;
            if (Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName; //TODO: not nice
                return resolveRelativeFile(info, fileName);
            }
        }
        return null;
    }

    private static FileObject resolveRelativeFile(ParserResult info, String name) {
        PhpSourcePath psp = null;
        Project p = ProjectConvertors.getNonConvertorOwner(info.getSnapshot().getSource().getFileObject());

        if (p != null) {
            psp = p.getLookup().lookup(PhpSourcePath.class);
        }

        while (true) {
            FileObject result;

            if (psp != null) {
                result = PhpSourcePath.resolveFile(info.getSnapshot().getSource().getFileObject().getParent(), name);
            } else {
                result = info.getSnapshot().getSource().getFileObject().getParent().getFileObject(name);
            }

            if (result != null) {
                return result;
            }

            //try to strip a directory from the "name":
            int slash = name.indexOf('/');

            if (slash != (-1)) {
                name = name.substring(slash + 1);
            } else {
                return null;
            }
        }
    }

    public static FileObject getFile(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);

        if (o instanceof DataObject) {
            DataObject od = (DataObject) o;

            return od.getPrimaryFile();
        }

        return null;
    }

    public static boolean isUsersFile(FileObject fileObject) {
        boolean result = false;
        PhpSourcePath.FileType fileType = PhpSourcePath.getFileType(fileObject);
        if (fileType != PhpSourcePath.FileType.INCLUDE && fileType != PhpSourcePath.FileType.INTERNAL) {
            result = true;
        }
        return result;
    }
}
