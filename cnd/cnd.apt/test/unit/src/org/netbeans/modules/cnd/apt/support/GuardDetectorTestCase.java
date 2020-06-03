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
package org.netbeans.modules.cnd.apt.support;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.structure.APTFileNode;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class GuardDetectorTestCase extends NbTestCase {
    private final static String LINE_COMMENT = "\n//line comment\n";
    private final static String DOXYGEN_LINE_COMMENT = "\n/// doxygen line comment\n";
    private final static String HEAD_BLOCK_COMMENT =
              "/**\n * head comment \n"
            + " * comment \n"
            + "*/\n"
            + "\n";

    private final static String INNER_BODY =
              "file body\n"
            + "// line comment\n"
            + "#if A\n"
            + "\tbody A\n"
            + "#else\n"
            + "\telse body A\n"
            + "#endif\n"
            + "body end\n";

    private final static String BLOCK_COMMENT = "\n/*\n\tblock comment 2\n*/\n";

    private final static String[] COMMENTS = new String[] { "", LINE_COMMENT, DOXYGEN_LINE_COMMENT, HEAD_BLOCK_COMMENT, BLOCK_COMMENT, ""};
    
    public GuardDetectorTestCase(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testPragmaOnce() throws Exception {
        String base =
                  "#pragma once\n"
                + INNER_BODY
                +"\n";
        String golden = APTUtils.getFileOnceMacroName(new APTFileNode(new FileSystemImpl(), getName(), APTFile.Kind.C_CPP)).toString();
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("pragma once is not detected:\n" + content, golden, guardMacro);
        }
    }

    private List<String> createEqualContents(String base) {
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < COMMENTS.length; i++) {
            String prefix = COMMENTS[i];
            for (int j = 0; j < COMMENTS.length; j++) {
                String postfix = COMMENTS[j];
                out.add(prefix + base + postfix);
            }
        }
        return out;
    }

    public void testGuardIfndefMacro() throws Exception {
        String base =
                "#ifndef GUARD/*comment*/\n"
                + DOXYGEN_LINE_COMMENT
                + "#define GUARD\n"
                + INNER_BODY
                + "#endif\n";
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("NO guard detected:\n" + content, "GUARD", guardMacro);
        }
        // in fact #define GUARD is not needed and file is still protected
        String noDefineContent = base.replace("#define GUARD", "");
        for (String content : createEqualContents(noDefineContent)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("NO guard detected:\n" + content, "GUARD", guardMacro);
        }
    }
    
    public void testGuardIfNotDefinedMacro() throws Exception {
        String base =
                  "#if !defined GUARD/*comment*/\n"
                + LINE_COMMENT
                + "#define GUARD\n"
                + INNER_BODY
                + "#endif\n";
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("NO guard detected:\n" + content, "GUARD", guardMacro);
        }
        // in fact #define GUARD is not needed and file is still protected
        String noDefineContent = base.replace("#define GUARD", "");
        for (String content : createEqualContents(noDefineContent)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("NO guard detected:\n" + content, "GUARD", guardMacro);
        }
        // in fact #define GUARD is not needed and file is still protected
        String guardIDInParens = base.replace("defined GUARD", "defined ( GUARD )");
        for (String content : createEqualContents(guardIDInParens)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("NO guard detected:\n" + content, "GUARD", guardMacro);
        }
    }

    public void testNoPragmaOnce() throws Exception {
        String base =
                "something\n"
                + "before pragma once\n"
                + "#pragma once\n"
                + INNER_BODY
                + "\n";
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("no pragma once", "", guardMacro);
        }
    }

    public void testNoGuardInFile() throws Exception {
        String base = "";
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("Guard detected:\n" + content, "", guardMacro);
        }
        base = "something";
        for (String content : createEqualContents(base)) {
            CharSequence guardMacro = getAPTGuardMacro(content);
            assertEquals("Guard detected:\n" + content, "", guardMacro);
        }
    }
    
    public void testNoGuardMacro() throws Exception {
        String __PLACEHOLDER__ = "__PLACEHOLDER__";
        String orig =
                  __PLACEHOLDER__
                + "\n// line comment\n"
                + "#define GUARD\n"
                + INNER_BODY
                + "#endif\n";
        String[] placeholders = new String[] {"#if !defined GUARD/*comment*/", "#ifndef GUARD/*comment*/"};
        int iteration = 0;
        for (String replacement : placeholders) {
            String base = orig.replace(__PLACEHOLDER__, replacement);
            for (String content : createEqualContents(base)) {
                String tokenBefore = "token_before\n" + content;
                CharSequence guardMacro = getAPTGuardMacro(tokenBefore);
                assertEquals("Error Guard detected:\n" + tokenBefore, "", guardMacro);
                String tokensBefore = "tokens before\n" + content;
                guardMacro = getAPTGuardMacro(tokensBefore);
                assertEquals("Error Guard detected:\n" + tokensBefore, "", guardMacro);
            }
            for (String content : createEqualContents(base)) {
                String tokensAfter = content + "tokens after\n";
                CharSequence guardMacro = getAPTGuardMacro(tokensAfter);
                assertEquals("Error Guard detected:\n" + tokensAfter, "", guardMacro);
                String tokenAfter = content + "token_after\n";
                guardMacro = getAPTGuardMacro(tokenAfter);
                assertEquals("Error Guard detected:\n" + tokenAfter, "", guardMacro);
            }
            // #ifndef GUARD && GUARD is just a warning and the first ID is checked
            if (iteration == 0) {
                // #if !defined GUARD && GUARD is error
                String notOneID = base.replaceAll("GUARD", "GUARD && OTHER");
                for (String content : createEqualContents(notOneID)) {
                    CharSequence guardMacro = getAPTGuardMacro(content);
                    assertEquals("Error Guard detected:\n" + content, "", guardMacro);
                }
            }
            iteration++;
        }

    }

    private String getAPTGuardMacro(String content) throws Exception {
        APTFile.Kind aptKind = APTFile.Kind.C_CPP;
        TokenStream ts = APTTokenStreamBuilder.buildLightTokenStream(this.getName(), content.toCharArray(), aptKind);
        APTFile apt = APTBuilder.buildAPT(new FileSystemImpl(), this.getName(), ts, aptKind);
        assertNotNull("failed to build APT Light", apt);
        CharSequence guardMacro = apt.getGuardMacro();
        assertNotNull("macro can not be null", guardMacro);
        ts = APTTokenStreamBuilder.buildTokenStream(this.getName(), content.toCharArray(), aptKind);
        apt = APTBuilder.buildAPT(new FileSystemImpl(), this.getName(), ts, aptKind);
        assertNotNull("failed to build APT", apt);
        assertEquals("different guards in APT Light and full APT", guardMacro, apt.getGuardMacro());
        return guardMacro.toString();
    }

    private static class FileSystemImpl extends FileSystem {

        public FileSystemImpl() {
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject getRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject findResource(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
