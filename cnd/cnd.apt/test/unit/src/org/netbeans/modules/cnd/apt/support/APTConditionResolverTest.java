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

import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 *
 */
public class APTConditionResolverTest {
    private static final String MAX_INT_CHECK_CODE = "\n"
            + "#define INT_MAX __INT_MAX__\n"
            + "#define UINT_MAX (INT_MAX * 2U + 1U)\n"
            + "\n"
            + "# if (UINT_MAX) == 65535U\n"
            + "#     define SIZEOF_INT 2\n"
            + "#   elif ((UINT_MAX) == 4294967295U)\n"
            + "#     define SIZEOF_INT 4\n"
            + "#   else\n"
            + "#     error: unexpected int size, must be updated for this platform!\n"
            + "#   endif /* UINT_MAX */\n";
    
    private static final String MAX_LONG_CHECK_CODE ="\n"
            + "#define LONG_MAX __LONG_MAX__\n"
            + "#define ULONG_MAX (LONG_MAX * 2UL + 1UL)\n"
            + "\n"
            + "# if (ULONG_MAX) == 65535UL\n"
            + "#     define SIZEOF_LONG 2\n"
            + "#   elif ((ULONG_MAX) == 4294967295UL)\n"
            + "#     define SIZEOF_LONG 4\n"
            + "#   elif ((ULONG_MAX) == 18446744073709551615UL)\n"
            + "#     define SIZEOF_LONG 8\n"
            + "#   else\n"
            + "#     error: unsupported long size, must be updated for this platform!\n"
            + "#   endif /* ULONG_MAX */\n";
    

    @Test
    public void test2BytesInt() {
        doTestSizeof(MAX_INT_CHECK_CODE, "__INT_MAX__", Integer.toString((1<<15)-1), "SIZEOF_INT", 2);
    }

    @Test
    public void test4BytesInt() {
        doTestSizeof(MAX_INT_CHECK_CODE, "__INT_MAX__", Integer.toString((1<<31)-1), "SIZEOF_INT", 4);
    }

    @Test
    public void test2BytesLong() {
        doTestSizeof(MAX_LONG_CHECK_CODE, "__LONG_MAX__", Long.toString((1L << 15) - 1L), "SIZEOF_LONG", 2);
    }

    @Test
    public void test4BytesLong() {
        doTestSizeof(MAX_LONG_CHECK_CODE, "__LONG_MAX__", Long.toString((1L << 31) - 1L), "SIZEOF_LONG", 4);
    }

    @Test
    public void test8BytesLong() {
        doTestSizeof(MAX_LONG_CHECK_CODE, "__LONG_MAX__", Long.toString((1L << 63) - 1L), "SIZEOF_LONG", 8);
    }

    private void doTestSizeof(String src, String macroName, String maxValue, String testMacro, int sizeof) {
        if (APTTraceFlags.USE_CLANK) {
            return;
        }
        APTFileMacroMap mmap = new APTFileMacroMap(null, Arrays.asList(macroName+"="+maxValue));
        APTMacro macro__INT_MAX__ = mmap.getMacro(APTUtils.createIDENT(CharSequences.create(macroName)));
        assertNotNull(macro__INT_MAX__);
        APTFile.Kind aptKind = APTFile.Kind.C_CPP;
        TokenStream lexer = APTTokenStreamBuilder.buildTokenStream(src, aptKind);
        APTFile apt = APTBuilder.buildAPT(new DummyFileSystem(), "SIZEOF_CHECKER", lexer, aptKind);
        APTWalker walker = new TestWalker(apt, mmap);
        walker.visit();
        assertFalse("failed to evaluate " + testMacro + " for " + macroName + "=" + maxValue, walker.isStopped());
        APTMacro macroSIZEOF_INT = mmap.getMacro(APTUtils.createIDENT(CharSequences.create(testMacro)));
        assertNotNull(macroSIZEOF_INT);
        CharSequence value = APTUtils.stringize(macroSIZEOF_INT.getBody(), false);
        assertEquals(testMacro, Integer.toString(sizeof), value.toString());
    }

    private static final String LONG_VS_UNIT_CHECK_CODE =   "#define BIG_UINT_VALUE (0XFFFFFFFF  )\n" +
                                                            "#define LONG_VALUE    (0xFFFFFFF0UL)\n" +
                                                            "#if LONG_VALUE >= BIG_UINT_VALUE\n" +
                                                            "#error \"LONG_VALUE >= BIG_UINT_VALUE\"\n" +
                                                            "#endif";
    @Test
    public void testLongVsUint() {
        if (APTTraceFlags.USE_CLANK) {
            return;
        }
        // #229008 - inaccuracy tests: MySQL project has unresolved includes (#error zzzz)
        APTFileMacroMap mmap = new APTFileMacroMap(null, Collections.<String>emptyList());
        APTFile.Kind aptKind = APTFile.Kind.C_CPP;
        TokenStream lexer = APTTokenStreamBuilder.buildTokenStream(LONG_VS_UNIT_CHECK_CODE, aptKind);
        APTFile apt = APTBuilder.buildAPT(new DummyFileSystem(), "testLongVsUint", lexer, aptKind);
        APTWalker walker = new TestWalker(apt, mmap);
        walker.visit();
        assertFalse("failed to evaluate " + LONG_VS_UNIT_CHECK_CODE, walker.isStopped());
    }

    private static final class TestWalker extends APTAbstractWalker {

        public TestWalker(APTFile apt, APTMacroMap macros) {
            super(apt, APTHandlersSupportImpl.createPreprocHandler(macros, 
                    new APTIncludeHandlerImpl(createStartEntry(apt), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), null),
                    true, CharSequences.empty(), CharSequences.empty()), null);
        }
        
        private static StartEntry createStartEntry(APTFile apt) {
            StartEntry entry = new StartEntry(apt.getFileSystem(), apt.getPath().toString(), DummyProjectKey.getOrCreate(apt.getFileSystem()));
            return entry;
        }

        @Override
        protected boolean include(ResolvedPath resolvedPath, IncludeState inclState, APTInclude aptInclude, PostIncludeData postIncludeState) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected boolean hasIncludeActionSideEffects() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class DummyFileSystem extends FileSystem {

        public DummyFileSystem() {
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileObject getRoot() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileObject findResource(String name) {
            throw new UnsupportedOperationException();
        }
    }
}
