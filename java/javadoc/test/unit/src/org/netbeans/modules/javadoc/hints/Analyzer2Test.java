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
package org.netbeans.modules.javadoc.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import static org.netbeans.modules.javadoc.hints.JavadocHint.AVAILABILITY_KEY;
import static org.netbeans.modules.javadoc.hints.JavadocHint.SCOPE_KEY;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class Analyzer2Test extends NbTestCase {

    public Analyzer2Test(String name) {
        super(name);
    }
    
    public void test230606() throws Exception {
        HintTest.create()
                .input(
                "package org.pill.repository;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "import java.net.URI;\n"
                + "import java.nio.channels.SeekableByteChannel;\n"
                + "import java.nio.file.NoSuchFileException;\n"
                + "import java.nio.file.Path;\n"
                + "import java.nio.file.attribute.BasicFileAttributes;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " * A module repository.\n"
                + " * <p/>\n"
                + " * @author Gili Tzabari\n"
                + " */\n"
                + "interface Repository\n"
                + "{\n"
                + "	/**\n"
                + "	 * Adds a module to the repository.\n"
                + "	 * <p/>\n"
                + "	 * @param name the module name\n"
                + "	 * @return the inserted Module\n"
                + "	 * @throws NullPointerException     if name is null\n"
                + "	 * @throws IllegalArgumentException if name is an empty string\n"
                + "	 * @throws IOException              if an I/O error occurs\n"
                + "	 */\n"
                + "	String insertModule(String name)\n"
                + "		throws IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Adds a release to the repository.\n"
                + "	 * <p/>\n"
                + "	 * @param module  the module name of the release\n"
                + "	 * @param version the version number of the release\n"
                + "	 * @param path    the path of the file associated with the release\n"
                + "	 * @return the Release builder\n"
                + "	 * @throws NullPointerException     if module or version are null\n"
                + "	 * @throws IllegalArgumentException if version is an empty string\n"
                + "	 */\n"
                + "	String insertRelease(String module, String version, Path path);\n"
                + "\n"
                + "	/**\n"
                + "	 * Looks up a module by its name.\n"
                + "	 * <p/>\n"
                + "	 * @param name the module name\n"
                + "	 * @return null if the module was not found\n"
                + "	 * @throws IOException              if an I/O error occurs\n"
                + "	 * @throws NullPointerException     if name is null\n"
                + "	 * @throws IllegalArgumentException if name is an empty string\n"
                + "	 */\n"
                + "	String getModule(String name) throws IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Lists all releases associated with a module.\n"
                + "	 * <p/>\n"
                + "	 * @param module the module\n"
                + "	 * @return a list of releases\n"
                + "	 * @throws NullPointerException     if module is null\n"
                + "	 * @throws IOException              if an I/O error occurs\n"
                + "	 * @throws IllegalArgumentException if the module was not found\n"
                + "	 */\n"
                + "	List<String> getReleases(String module) throws IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Looks up a release in the repository.\n"
                + "	 * <p/>\n"
                + "	 * @param module  the module name\n"
                + "	 * @param version the module version\n"
                + "	 * @return null if the release was not found\n"
                + "	 * @throws NullPointerException     if module or version are null\n"
                + "	 * @throws IllegalArgumentException if version is an empty string\n"
                + "	 * @throws IOException              if an I/O error occurs\n"
                + "	 */\n"
                + "	URI getReleaseUri(String module, String version) throws IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Returns a SeekableByteChannel for reading a file.\n"
                + "	 * <p/>\n"
                + "	 * @param uri the file URI\n"
                + "	 * @return a read-only SeekableByteChannel\n"
                + "	 * @throws IllegalArgumentException if uri does not refer to a file\n"
                + "	 * @throws NoSuchFileException      if the file does not exist <i>(optional specific\n"
                + "	 *                                  exception)</i>\n"
                + "	 * @throws IOException              if an I/O error occurs opening the file\n"
                + "	 */\n"
                + "	SeekableByteChannel newByteChannel(URI uri) throws NoSuchFileException, IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Returns a file's attributes.\n"
                + "	 * <p/>\n"
                + "	 * @param uri the file's URI\n"
                + "	 * @return the file's attributes\n"
                + "	 * @throws NoSuchFileException if the file does not exist <i>(optional specific exception)</i>\n"
                + "	 * @throws IOException         if an I/O error occurs\n"
                + "	 */\n"
                + "	BasicFileAttributes readAttributes(URI uri) throws NoSuchFileException, IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Removes a module from the repository.\n"
                + "	 * <p/>\n"
                + "	 * @param module the module to removeRelease\n"
                + "	 * @throws NullPointerException    if module is null\n"
                + "	 * @throws IllegalStateException   if the module contains releases that must be removed first\n"
                + "	 * @throws IOException             if an I/O error occurs\n"
                + "	 */\n"
                + "	void removeModule(String module)\n"
                + "		throws  IOException;\n"
                + "\n"
                + "	/**\n"
                + "	 * Removes a release from the repository.\n"
                + "	 * <p/>\n"
                + "	 * @param release the release to removeRelease\n"
                + "	 * @throws NullPointerException    if release is null\n"
                + "	 * @throws IOException             if an I/O error occurs\n"
                + "	 */\n"
                + "	void removeRelease(String release)\n"
                + "		throws IOException;\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertNotContainsWarnings("Duplicate @throws tag: java.nio.file.NoSuchFileException");
    }
    
    // "4:7-4:38:warning:Unknown throwable: @throws java.io.IOException"
    
    public void testExceptionInheritanceAnalyzer() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "import java.nio.file.NoSuchFileException;\n"
                + "import java.io.IOException;\n"
                + "class ZimaImpl {\n"
                + "    /**\n"
                + "     * @throws NoSuchFileException Subclass of declared exception\n"
                + "     */\n"
                + "    public void leden() throws IOException {\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertNotContainsWarnings("Missing @throws tag for java.io.IOException")
                .assertNotContainsWarnings("Unknown throwable: @throws java.nio.file.NoSuchFileException");
    }
       
    public void testExceptionTypeAnalyzer() throws Exception {
        // issue NETBEANS-1615
        HintTest.create()
                .input(
                "package test;\n"
                + "interface Zima {\n"
                + "    /**\n"
                + "     * @throws X\n"
                + "     */\n"
                + "    <X extends Exception> void leden() throws X;\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertNotContainsWarnings("Missing @throws tag for X");
    }

    public void testInheritanceAnalyzer() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "class ZimaImpl extends Intermediate implements Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    public <T> String base(T prvniho) throws Exception {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    public <T> String leden(T prvniho) throws Exception {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}\n" +
                "abstract class Intermediate extends Base {}\n" +
                "abstract class Base {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param prvniho \n" +
                "     * @param <T> \n" +
                "     * @return \n" +
                "     * @throws Exception \n" +
                "     */\n" +
                "    abstract <T> String base(T prvniho) throws Exception;\n" +
                "}\n" +
                "interface Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param prvniho \n" +
                "     * @param <T> \n" +
                "     * @return \n" +
                "     * @throws Exception \n" +
                "     */\n" +
                "    <T> String leden(T prvniho) throws Exception;\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertNotContainsWarnings("Missing @throws tag for java.lang.Exception")
                .assertNotContainsWarnings("Missing @return tag.")
                .assertNotContainsWarnings("Missing @param tag for <T>")
                .assertNotContainsWarnings("Missing @param tag for prvniho");
    }
    
    public void testEmptyTags() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "class ZimaImpl implements Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    public <T> String leden(T prvniho) throws Exception {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}\n" +
                "interface Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param <>\n" +
                "     * @return <>\n" +
                "     * @throws <>\n" +
                "     */\n" +
                "    <T> String leden(T prvniho) throws Exception;\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class);
    }
    
    public void testUnknownHTMLTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <ralph> </ralph>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:3-2:10:warning:Unknown HTML Tag: <ralph>");
    }
    
    public void testWrongHTMLEndTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <b> </ralph>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:7-2:15:warning:Unknown HTML End Tag: </ralph>");
    }
    
    public void testUnexpectedHTMLEndTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <br> </strong>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:8-2:17:warning:Unexpected End Tag: </strong>");
    }
    
    public void testNoHTMLEndTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <br> </br>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:8-2:13:warning:Invalid End Tag: </br>");
    }
    
    public void testUnmatchedHTMLStartTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <b> <strong> <br> </b> </strong>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:7-2:15:warning:End Tag Missing: </strong>")
                .assertContainsWarnings("2:7-2:15:warning:End Tag Missing: </strong>");
    }
    
    public void testUnmatchedHTMLStartTag2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <strong>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertContainsWarnings("2:3-2:11:warning:End Tag Missing: </strong>");
    }
    
    public void testOptionalEndTag() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "/**\n" +
                " * <ul><li>One<li>Two</li></ul>\n" +
                " */\n" +
                "class Zima {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertWarnings();
    }    
}
