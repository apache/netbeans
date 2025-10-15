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
package org.netbeans.modules.web.el.completion;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.web.el.ELTestBaseForTestProject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELCompletionTest extends ELTestBaseForTestProject {

    private static final List<String> LIST_METHODS_JDK7 = Arrays.asList("add", "addAll", "clear", "contains", "containsAll",
            "equals", "get", "hashCode", "indexOf", "iterator", "lastIndexOf" , "listIterator", "remove", "removeAll",
            "retainAll", "set", "size", "subList", "toArray",
            // stream is not JDK7 method but it's supported by EL for jdk7
            "stream()");

    private static final List<String> STRING_METHODS_JDK7 = Arrays.asList("charAt", "codePointAt", "codePointBefore",
            "codePointCount", "compareTo", "compareToIgnoreCase", "concat", "contains", "contentEquals", "endsWith",
            "equals", "equalsIgnoreCase", "getBytes", "getBytes", "getBytes", "getChars", "hashCode", "indexOf",
            "intern", "lastIndexOf", "length", "matches", "offsetByCodePoints", "regionMatches", "replace",
            "replaceAll", "replaceFirst", "split", "startsWith", "subSequence", "substring", "toCharArray",
            "toLowerCase", "toString", "toUpperCase", "trim", "bytes");

    private static final List<String> STRING_METHODS_JDK17 = Arrays.asList(
            "charAt", "chars", "codePointAt", "codePointBefore", "codePointCount", "codePoints", "compareTo",
            "compareToIgnoreCase", "concat", "contains", "contentEquals", "describeConstable", "endsWith", "equals",
            "equalsIgnoreCase", "formatted", "getBytes", "getChars", "getClass", "hashCode", "indent", "indexOf",
            "intern", "isBlank", "isEmpty", "lastIndexOf", "length", "lines", "matches", "notify",
            "notifyAll", "offsetByCodePoints", "regionMatches", "repeat", "replace", "replaceAll",
            "replaceFirst", "resolveConstantDesc", "split", "startsWith", "strip", "stripIndent",
            "stripLeading", "stripTrailing", "subSequence", "substring", "toCharArray", "toLowerCase",
            "toString", "toUpperCase", "transform", "translateEscapes", "trim", "wait");

    public ELCompletionTest(String name) {
        super(name);
    }

    public void testCompletionForBean() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion04.xhtml", "#{bean.^}", false);
    }

    public void testCompletionForArray() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion01.xhtml", "#{bean.myArray.^}", false);
    }

    public void testCompletionForList01() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion02.xhtml", "#{bean.myList.^}", false, LIST_METHODS_JDK7);
    }

    public void testCompletionForList02() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion18.xhtml", "#{bean.myList[^}", false, LIST_METHODS_JDK7);
    }

    public void testCompletionForList03() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion19.xhtml", "#{bean.myList['^}", false, LIST_METHODS_JDK7);
    }

    public void testCompletionForList04() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion20.xhtml", "#{bean.myList['^']}", false, LIST_METHODS_JDK7);
    }

    public void testCompletionForString() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion03.xhtml", "#{bean.myString.^}", false, STRING_METHODS_JDK7);
    }

    public void testCompletionForStaticIterableElement() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion05.xhtml", "#{[\"one\", 2].^}", false);
    }

    public void testCompletionForStream() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion06.xhtml", "#{[\"one\", 2].stream().^}", false);
    }

    public void testCompletionForStreamMax() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion07.xhtml", "#{[1, 2].stream().max().^}", false);
    }

    public void testCompletionForContinuosStream() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion08.xhtml", "#{[1, 2, 3, 4, 5].stream().substream(5).distinct().^}", false);
    }

    public void testCompletionForOptional() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion09.xhtml", "#{[1,2,3].stream().average().^}", false);
    }

    public void testCompletionAfterSimicolon() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion10.xhtml", "#{a = 5; bean.^}", false);
    }

    public void testCompletionInsideStream() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion11.xhtml", "#{[1,2,3].stream().^average()}", false);
    }

    public void testSimpleAssignement() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion12.xhtml", "#{v.^}", false);
    }

    public void testAssignementOnTheSameLine() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion13.xhtml", "#{v = {\"one\":1, \"two\":2, \"three\":3}; v.^}", false);
    }

    public void testAssignementsCompletionChain() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion14.xhtml", "#{v.stream().average().^}", false);
    }

    public void testAssignementsOfBean() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion15.xhtml", "#{c.^}", false);
    }

    public void testAssignementsOfStream() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion16.xhtml", "#{b.^}", false);
    }

    public void testCompleteAssignements() throws Exception {
        List<String> toCheck = Arrays.asList("b", "fact");
        checkCompletion("projects/testWebProject/web/completion/completion17.xhtml", "#{^}", false, toCheck);
    }

    public void testCompleteAfterAccessToList() throws Exception {
        List<String> toCheck = Arrays.asList("concat", "toCharArray", "trim", "bytes");
        checkCompletion("projects/testWebProject/web/completion/completion21.xhtml", "#{bean.myList[0].^}", false, toCheck);
    }

    public void testJavaCompletion01() throws Exception {
        List<String> toCheck = Arrays.asList("AssertionError", "Boolean", "Double", "Enum", "StringBuilder", "instanceof", "true", "bundle");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion01.xhtml", "#{^}", false, toCheck);
    }

    public void testJavaCompletion02() throws Exception {
        List<String> toCheck = Arrays.asList("getBoolean", "parseBoolean", "toString", "valueOf", "FALSE", "TRUE", "TYPE");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion02.xhtml", "#{(Boolean.^}", false, toCheck);
    }

    public void testJavaCompletion03() throws Exception {
        List<String> toCheck = Arrays.asList("getBoolean", "parseBoolean", "toString", "valueOf", "FALSE", "TRUE", "TYPE");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion03.xhtml", "#{(Boolean.^)}", false, toCheck);
    }

    public void testJavaCompletion04() throws Exception {
        List<String> toCheck = Arrays.asList("getBoolean", "parseBoolean", "toString", "valueOf", "FALSE", "TRUE", "TYPE");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion04.xhtml", "#{(java.lang.Boolean).^}", false, toCheck);
    }

    public void testJavaCompletion05() throws Exception {
        List<String> toCheck = Arrays.asList("BigDecimal", "BigInteger", "BitSieve", "MathContext", "MutableBigInteger", "SignedMutableBigInteger");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion05.xhtml", "#{java.math.^}", false, toCheck);
    }

    public void testJavaCompletion06() throws Exception {
        List<String> toCheck = Arrays.  asList("Boolean");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion06.xhtml", "#{Bo^}", false, toCheck);
    }

    public void testIssue229822() throws Exception {
        List<String> toCheck = Arrays.  asList("NoPkgBean");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion07.xhtml", "#{NoPkg^}", false, toCheck);
    }

    public void testIssue231092() throws Exception {
        List<String> toCheck = Arrays.  asList("VerifyError", "Void");
        checkCompletion("projects/testWebProject/web/completion/java/java_completion08.xhtml", "#{V^}", false, toCheck);
    }

    public void testIssue233928() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion22.xhtml", "#{request.r^}", false);
    }

    public void testIssue234865() throws Exception {
        List<String> toCheck = Arrays.asList("AssertionError", "Boolean", "Double", "Enum", "StringBuilder", "instanceof", "true", "bundle");
        checkCompletion("projects/testWebProject/web/completion/completion23.xhtml", "#{['word', 4].stream().peek(i->^)}", false, toCheck);
    }

    public void testIssue235971_1() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion24.xhtml", "#{bean.myString.toS^tring()}", false);
    }

    public void testIssue235971_2() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion24.xhtml", "#{bean.mySt^ring.toString()}", false);
    }

    public void testIssue235971_3() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion24.xhtml", "#{bean.myL^ist}", false);
    }

    public void testIssue236576() throws Exception {
        List<String> toCheck = Arrays.asList("AssertionError", "Boolean", "Double", "Enum", "StringBuilder", "instanceof", "true", "bundle");
        checkCompletion("projects/testWebProject/web/completion/completion24.xhtml", "#{(()->2 * ^)}", false, toCheck);
    }

    public void testIssue236574() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion25.xhtml", "#{['pear', 'whatever'].stream().distinct().substream(5).^}", false);
    }

    public void testIssue236148() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/completion26.xhtml", "#{v = {\"one\":1, \"two\":2, \"three\":3}; v.^", false);
    }

    public void testIssue241160_1() throws Exception {
        checkCompletion("projects/testWebProject/web/completion/issue241160.xhtml", "#{cc.attrs.muj.^}", false, STRING_METHODS_JDK17);
    }

    public void testIssue241160_2() throws Exception {
        List<String> toCheck = Arrays.asList("charAt", "length", "subSequence", "toString");
        checkCompletion("projects/testWebProject/web/completion/issue241160.xhtml", "#{cc.attrs.muj.subSequence(0, 1).^}", false, toCheck);
    }

}
