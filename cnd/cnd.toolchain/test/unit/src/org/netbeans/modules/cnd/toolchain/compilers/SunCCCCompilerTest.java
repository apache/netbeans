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
package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public class SunCCCCompilerTest {

    private static final boolean TRACE = false;

    public SunCCCCompilerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParseCompilerOutputSunCC() {
        //System.setProperty("os.name", "SunOS");
        String s =
                "###     command line files and options (expanded):\n" +
                "### -dryrun -E xxx.c " +
                "### CC: Note: NLSPATH = /opt/SUNWspro/prod/bin/../lib/locale/%L/LC_MESSAGES/%N.cat:/opt/SUNWspro/prod/bin/../../lib/locale/%L/LC_MESSAGES/%N.cat\n" +
                "/opt/SUNWspro/prod/bin/ccfe -E -ptf /tmp/09752%1.%2 -ptx /opt/SUNWspro/prod/bin/CC -ptk \"-xdryrun -E  -xs \" -D__SunOS_5_11 -D__SUNPRO_CC=0x5100 -Dunix -Dsun -Di386 -D__i386 -D__unix -D__sun -D__SunOS -D__BUILTIN_VA_ARG_INCR -D__SVR4 -D__SUNPRO_CC_COMPAT=5 -D__SUN_PREFETCH -xdbggen=no%stabs+dwarf2 -xdbggen=incl -I-xbuiltin -xldscope=global -instlib=/opt/SUNWspro/prod/lib/libCstd.a -I/opt/SUNWspro/prod/include/CC/Cstd -I/opt/SUNWspro/prod/include/CC -I/opt/SUNWspro/prod/include/CC/rw7 -I/opt/SUNWspro/prod/include/cc -O0 xxx.c >&/tmp/ccfe.09752.0.err\n" +
                "/opt/SUNWspro/prod/bin/stdlibfilt -stderr </tmp/ccfe.09752.0.err\n" +
                "rm /tmp/ccfe.09752.0.err\n";

        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of SunStudio CC on Solaris");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        MySunCCCompiler instance = new MySunCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "OracleDeveloperStudio", "OracleDeveloperStudio", "/opt/SUNWspro/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("/opt/SUNWspro/prod/include/CC");
        golden.add("/opt/SUNWspro/prod/include/CC/Cstd");
        golden.add("/opt/SUNWspro/prod/include/CC/rw7");
        golden.add("/opt/SUNWspro/prod/include/CC/std");
        golden.add("/opt/SUNWspro/prod/include/cc");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputSunC() {
        //System.setProperty("os.name", "SunOS");
        String s =
                "/opt/SUNWspro/prod/bin/acomp -xldscope=global -i xxx.c -o - -xdbggen=no%stabs+dwarf2+usedonly -E -m32 -fparam_ir -Qy -D__SunOS_5_11 -D__SUNPRO_C=0x5100 -D__SVR4 -D__sun -D__SunOS -D__unix -D__i386 -D__BUILTIN_VA_ARG_INCR -D__C99FEATURES__ -Xa -D__PRAGMA_REDEFINE_EXTNAME -Dunix -Dsun -Di386 -D__RESTRICT -xc99=%all,no%lib -D__FLT_EVAL_METHOD__=-1 -I/opt/SUNWspro/prod/include/cc \"-g/opt/SUNWspro/prod/bin/cc -xdryrun -E \" -fsimple=0 -D__SUN_PREFETCH -destination_ir=%none\n";



        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of SunStudio C on Solaris");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        MySunCCCompiler instance = new MySunCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCompiler, "OracleDeveloperStudio", "OracleDeveloperStudio", "/opt/SUNWspro/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("/opt/SUNWspro/prod/include/cc");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputSunC2() {
        //System.setProperty("os.name", "SunOS");
        String s =
"/shared/dp/sstrunk/090219/inst/intel-Linux.inst/opt/sun/sunstudioceres/prod/bin/acomp -Qy -Xa -xc99=%all -i /dev/null "+
"-D__SUNPRO_C=0x5100 -D__unix -D__unix__ -Dlinux -D__linux -D__linux__ -D__gnu__linux__ \"-D__builtin_expect(e,x)=e\" "+
"-D__x86_64 -D__x86_64__ -D__amd64 -D__amd64__ -D_LP64 -D__LP64__ -D__BUILTIN_VA_STRUCT -D__C99FEATURES__ "+
"-D__PRAGMA_REDEFINE_EXTNAME -Dunix -D__RESTRICT -D__FLT_EVAL_METHOD__=0 -D__SUN_PREFETCH "+
"-I/shared/dp/sstrunk/090219/inst/intel-Linux.inst/opt/sun/sunstudioceres/prod/include/cc -fsimple=0 -m64 -fparam_ir "+
"-xF=%none -xdbggen=no%stabs+dwarf2+usedonly -xldscope=global -c99OS -xir_types "+
"\"-g/shared/dp/sstrunk/090219/inst/intel-Linux.inst/opt/sun/sunstudioceres/prod/bin/cc -xdryrun -E /dev/null \" "+
"-destination_ir=%none -E -o - \n";



        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of SunStudio C on Lunix");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("OracleDeveloperStudio", PlatformTypes.PLATFORM_LINUX);
        MySunCCCompiler instance = new MySunCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCompiler, "OracleDeveloperStudio", "OracleDeveloperStudio", "/opt/SUNWspro/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("/shared/dp/sstrunk/090219/inst/intel-Linux.inst/opt/sun/sunstudioceres/prod/include/cc");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));

        out = instance.pair.systemPreprocessorSymbolsList;
        Collections.<String>sort(out);
        golden = new ArrayList<String>();
        golden.add("_LP64");
        golden.add("__BUILTIN_VA_STRUCT");
        golden.add("__C99FEATURES__");
        golden.add("__FLT_EVAL_METHOD__=0");
        golden.add("__LP64__");
        golden.add("__PRAGMA_REDEFINE_EXTNAME");
        golden.add("__RESTRICT");
        golden.add("__SUNPRO_C=0x5100");
        golden.add("__SUN_PREFETCH");
        golden.add("__amd64");
        golden.add("__amd64__");
        golden.add("__builtin_expect(e,x)=e");
        golden.add("__gnu__linux__");
        golden.add("__linux");
        golden.add("__linux__");
        golden.add("__unix");
        golden.add("__unix__");
        golden.add("__x86_64");
        golden.add("__x86_64__");
        golden.add("linux");
        golden.add("unix");

        result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }
    private static final class MySunCCCompiler extends SunCCCompiler {
        CompilerDefinitions pair = new CompilerDefinitions();
        protected MySunCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
            super(env, flavor, kind, name, displayName, path);
        }
        @Override
        protected String normalizePath(String path) {
            return path;
        }
    }
}
