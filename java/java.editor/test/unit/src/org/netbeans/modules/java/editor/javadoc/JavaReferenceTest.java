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

package org.netbeans.modules.java.editor.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.javadoc.JavadocTestSupport;
import org.netbeans.modules.java.editor.base.javadoc.JavadocCompletionUtils;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaReferenceTest extends JavadocTestSupport {

    public JavaReferenceTest(String name) {
        super(name);
    }

    public void testResolve() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@linkplain IOException}\n" +
                "    * link3 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.io.IOException\n" +
                "    * @throws java.io.IOException\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "link1 {@link ";
        int offset = code.indexOf(what) + what.length();
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        String dump = insertPointer(code, offset);
        assertNotNull(dump, jdts);
        TreePath javadocPath = JavadocCompletionUtils.findJavadoc(info, offset);
        assertNotNull(dump, javadocPath);
        DocSourcePositions sourcePositions = info.getDocTrees().getSourcePositions();
        DocCommentTree dcTree = info.getDocTrees().getDocCommentTree(javadocPath);
        TypeElement scope = (TypeElement) info.getTrees().getElement(javadocPath).getEnclosingElement();
        
        // link1
        DocTreePath path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        assertNotNull(dump, path.getLeaf());
        int[] tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        JavaReference ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        Element exp = info.getElements().getTypeElement("java.lang.Runnable");
        Element result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // link2
        what = "link2 {@linkplain ";
        offset = code.indexOf(what) + what.length();
        path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump, path.getLeaf());
        tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = null;
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // link3
        what = "link3 {@link ";
        offset = code.indexOf(what) + what.length();
        path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump, path.getLeaf());
        tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = findCollectionsBinaryMethod(code, "Collections.<String>binary");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // unclosed link
        what = "unclosed link {@value ";
        offset = code.indexOf(what) + what.length();
        path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump, path.getLeaf());
        tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = findCollectionsBinaryMethod(code, "double pi = Math.P");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // see
        what = "@see ";
        offset = code.indexOf(what) + what.length();
        path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump, path.getLeaf());
        tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = info.getElements().getTypeElement("java.io.IOException");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // throws
        what = "@throws ";
        offset = code.indexOf(what) + what.length();
        path = info.getTreeUtilities().pathFor(javadocPath, dcTree, offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump, path.getLeaf());
        tagSpan = new int[] {
            (int)sourcePositions.getStartPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf()),
            (int)sourcePositions.getEndPosition(javadocPath.getCompilationUnit(), dcTree, path.getLeaf())
        };

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = info.getElements().getTypeElement("java.io.IOException");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
    }
    
    private Element findCollectionsBinaryMethod(String code, String where) {
        int pos = code.indexOf(where) + where.length();
        return info.getTrees().getElement(info.getTreeUtilities().pathFor(pos));
    }
    
}
