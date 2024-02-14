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

package org.netbeans.modules.java.editor.base.javadoc;

import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Pokorsky
 */
public class JavadocImportsTest extends JavadocTestSupport {

    public JavadocImportsTest(String name) {
        super(name);
    }

    public void testComputeUnresolvedImports() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@linkplain MethodUnresolved}\n" +
                "    * link3 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see SeeUnresolved\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "   }\n" +
                "   /**\n" +
                "    * {@link FieldUnresolved}\n" +
                "    */\n" +
                "   int field;\n" +
                "   /** {@link my.pkg.InnerInterfaceUnresolved */\n" +
                "   interface InnerInterface {}\n" +
                "   /** {@link InnerAnnotationTypeUnresolved} */\n" +
                "   @interface InnerAnnotationType {}\n" +
                "}\n" +
                "/** {@link EnumReferenceUnresolved} */\n" +
                "enum TopLevelEnum {\n" +
                "   /** {@link ConstantReferenceUnresolved} */" +
                "   E1\n" +
                "}\n";
        prepareTest(code);

        List<String> exp = Arrays.asList("MethodUnresolved", "SeeUnresolved",
                "ThrowsUnresolved", "FieldUnresolved", "my",
                "InnerAnnotationTypeUnresolved", "EnumReferenceUnresolved",
                "ConstantReferenceUnresolved"
                );
        Collections.sort(exp);
        
        Set<String> result = JavadocImports.computeUnresolvedImports(info);
        assertNotNull(result);
        
        List<String> sortedResult = new ArrayList<String>(result);
        Collections.sort(sortedResult);
        assertEquals(exp, sortedResult);
    }
    
    public void testComputeReferencedElements() throws Exception {
        String code = 
                "package p;\n" +
                "import java.io.IOException;\n" +
                "import java.util.Collections;\n" +
                "import java.util.List;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link3 {@linkplain Collections#binarySearch(java.util.List, Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see List\n" +
                "    * @throws IOException\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "   }\n" +
                "   /**\n" +
                "    * {@link Collections}\n" +
                "    */\n" +
                "   int field;\n" +
                "   /** {@link IOException */\n" +
                "   interface InnerInterface {}\n" +
                "   /** {@link Collections} */\n" +
                "   @interface InnerAnnotationType {}\n" +
                "}\n" +
                "/** {@link Collections} */\n" +
                "enum TopLevelEnum {\n" +
                "   /** {@link Collections} */" +
                "   E1\n" +
                "}\n";
        prepareTest(code);

        // C.m()
        TreePath member = findPath(code, "m() throws");
        assertNotNull(member);
        List <TypeElement> exp = Arrays.asList(
                info.getElements().getTypeElement("java.lang.Runnable"),
                info.getElements().getTypeElement("java.lang.Math"),
                info.getElements().getTypeElement("java.lang.Object"),
                info.getElements().getTypeElement("java.util.Collections"),
                info.getElements().getTypeElement("java.util.List"),
                info.getElements().getTypeElement("java.io.IOException")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        Set<TypeElement> result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        List<TypeElement> sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.field
        member = findPath(code, "field;");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.InnerInterface
        member = findPath(code, "InnerInterface {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.io.IOException")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.InnerAnnotationType
        member = findPath(code, "InnerAnnotationType {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);

        // TopLevelEnum
        member = findPath(code, "TopLevelEnum {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);

        // TopLevelEnum.E1
        member = findPath(code, "E1\n");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        sortedResult.sort(new ElementComparator());
        assertEquals(exp, sortedResult);
    }
    
    public void testComputeTokensOfReferencedElements() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        TreePath where = findPath(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, info.getTrees().getElement(where));
        assertNotNull(jdts);
        List<Token> exp;
        
        // toFind java.lang.Runnable
        Element toFind = info.getElements().getTypeElement("java.lang.Runnable");
        assertNotNull(toFind);
        List<Token> tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("Runnable", code.indexOf("link1")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind java.util.Collections
        toFind = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        exp = new ArrayList<Token>();
        jdts.move(code.indexOf("Collections", code.indexOf("link2")));
        assertTrue(jdts.moveNext());
        exp.add(jdts.token());
        jdts.move(code.indexOf("Collections", code.indexOf("* @see")));
        assertTrue(jdts.moveNext());
        exp.add(jdts.token());
        System.err.println("exp:");
        for (Token e : exp) {
            System.err.println(e.text());
        }
        System.err.println("tokens:");
        for (Token e : tokens) {
            System.err.println(e.text());
        }
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind Math#PI
        toFind = findElement(code, "PI;\n");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("PI", code.indexOf("unclosed link")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind Collections#binarySearch
        toFind = findElement(code, "binarySearch(Collections.<String>emptyList()");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("binarySearch", code.indexOf("link2")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
//        assertEquals(toFind.toString(), exp, tokens);
    }

    public void testComputeTokensOfReferencedElementsForParams() throws Exception {
        String code =
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * @param <T> type parameter\n" +
                "    * @param param2find regular parameter\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   public <T> void m(T param2find) throws java.io.IOException {\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        TreePath where = findPath(code, "m(T param2find) throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, info.getTrees().getElement(where));
        assertNotNull(jdts);
        List<Token> exp;

        // toFind param2find of C#m
        Element toFind = findElement(code, "param2find) throws");
        assertNotNull(toFind);
        List<Token> tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("param2find", code.indexOf("@param param2find")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);

        // toFind <T> of C#m
        toFind = findElement(code, "T> void m(T");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("T>", code.indexOf("@param <T>")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
    }

    public void testFindReferencedElementForParams() throws Exception {
        String code =
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * @param <T> type parameter\n" +
                "    * @param param2find regular parameter\n" +
                "    * @see java.util.Collections#enumeration(Collection)\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   public <T> void m(T param2find) throws java.io.IOException {\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m(T param2find) throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, where);
        assertNotNull(jdts);

        // resolve @param param2find
        Element exp = findElement(code, "param2find) throws");
        assertNotNull(exp);
        Element el = JavadocImports.findReferencedElement(info, code.indexOf("param2find", code.indexOf("@param param2find")));
        assertEquals(exp, el);

        // resolve @param <T>
        exp = findElement(code, "T> void m(T");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("T>", code.indexOf("@param <T>")));
        assertEquals(exp, el);
        
        el = JavadocImports.findReferencedElement(info, code.indexOf("enumeration")+2);
        assertEquals(el.getKind(), ElementKind.METHOD);
        assertEquals(el.getSimpleName().toString(), "enumeration");
        
    }
    
    public void testFindReferencedElement() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * local_link {@link #m()}\n" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @see \"java.io.File\"\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, where);
        assertNotNull(jdts);

        // java.lang.Runnable
        Element exp = info.getElements().getTypeElement("java.lang.Runnable");
        assertNotNull(exp);
        Element el = JavadocImports.findReferencedElement(info, code.indexOf("Runnable", code.indexOf("link1")));
        assertEquals(exp, el);

        // java.util.Collections in {@link Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("Collections", code.indexOf("link2")));
        assertEquals(exp, el);

        // java.util.Collections in @see Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("Collections", code.indexOf("@see")));
        assertEquals(exp, el);

        // java.util in @see Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        exp = exp.getEnclosingElement(); // package
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("util", code.indexOf("@see")));
        assertEquals(exp, el);

        // @see "java.io.File"; issue #131253
        el = JavadocImports.findReferencedElement(info, code.indexOf("File", code.indexOf("@see \"java.io.File\"")));
        assertNull(el);

        // java.util.Collections#binarySearch
//        exp = findElement(code, "binarySearch(Collections.<String>emptyList()");
//        assertNotNull(exp);
//        el = JavadocImports.findReferencedElement(info, code.indexOf("binarySearch", code.indexOf("link2")));
//        assertEquals(exp, el);

        // java.util.Collections#PI
        exp = findElement(code, "PI;\n");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("PI", code.indexOf("unclosed link")));
        assertEquals(exp, el);

        // #m()
        exp = where;
        el = JavadocImports.findReferencedElement(info, code.indexOf("m()", code.indexOf("local_link")));
        assertEquals(exp, el);

        // not java reference
        el = JavadocImports.findReferencedElement(info, code.indexOf("unclosed link"));
        assertNull(el);
    }
    
    public void testIsInsideReference() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, where);
        assertNotNull(jdts);

        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("link1")));
        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("@link", code.indexOf("link1")) + 2));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("Runnable", code.indexOf("link1"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("Collections", code.indexOf("link2"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("binarySearch", code.indexOf("link2"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("#", code.indexOf("link2"))));
        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("search}\n", code.indexOf("link2"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("util", code.indexOf("@see"))));
    }

    public void testIsInsideParamName() throws Exception {
        String code =
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * @param <T> type parameter\n" +
                "    * @param param2find regular parameter\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   public <T> void m(T param2find) throws java.io.IOException {\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m(T param2find) throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, where);
        assertNotNull(jdts);

        assertTrue(JavadocImports.isInsideParamName(jdts, code.indexOf("T> type parameter")));
        assertTrue(JavadocImports.isInsideParamName(jdts, code.indexOf("param2find regular parameter\n")));
        assertFalse(JavadocImports.isInsideParamName(jdts, code.indexOf("param <T>")));
    }
    
    private Element findElement(String code, String pattern) {
        return info.getTrees().getElement(findPath(code, pattern));
    }

    private TreePath findPath(String code, String pattern) {
        int offset = code.indexOf(pattern) + 1;
        return info.getTreeUtilities().pathFor(offset);
    }
    
    private static class ElementComparator implements Comparator<TypeElement> {

        public int compare(TypeElement o1, TypeElement o2) {
            // type elements are never null
            if (o1 == o2) {
                return 0;
            }
            
            return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
        }
        
    }
}
