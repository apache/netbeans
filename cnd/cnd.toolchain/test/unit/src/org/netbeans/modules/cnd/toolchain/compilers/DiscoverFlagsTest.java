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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class DiscoverFlagsTest {
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
    public void testImprtantFlagsStudio() {
        String flags = "-O;-O0;-O1;-O2;-O3;-O4;-O5;-compat=g;-fast;-fopenmp;-m64;-mt;-mt=yes;-native;-std=c++03;-std=c++0x;-std=c++11;-xO1;-xO2;-xO3;-xO4;-xO5;"
                     + "-xautopar;-xchar=u;-xchar=unsigned;-xmaxopt=1;-xmaxopt=2;-xmaxopt=3;-xmaxopt=4;-xmaxopt=5;-xopenmp;-xopenmp=noopt;-xopenmp=parallel;";
        String golden ="-O(\\W|$|-)|-O0|-O1|-O2|-O3|-O4|-O5|-compat=.*|-fast|-fopenmp|-m64|-mt(\\W|$|-)|-mt=.*|-native|-std=.*|-xO1|-xO2|-xO3|-xO4|-xO5|"
                + "-xautopar|-xchar=.*|-xmaxopt=.*|-xopenmp(\\W|$|-)|-xopenmp=.*";
        List<String> list = new ArrayList<String>();
        for(String flag : flags.split(";")) {
            if (!flag.isEmpty()) {
                list.add(flag);
            }
        }
        String pattern = CCCCompiler.convertToRegularExpression(list);
        Pattern compile = Pattern.compile(pattern);
        for(String s : list) {
            Matcher matcher = compile.matcher(s);
            if (!matcher.find()) {
                Assert.assertTrue(false);
            }
        }
        //System.err.println(pattern);
        Assert.assertEquals(golden, pattern);
    }

    @Test
    public void testImprtantFlagsGnu() {
        String flags = "-O1;-O2;-O3;-O4;-O5;-Ofast;-Og;-Os;-ansi;-fPIC;-fPIE;-fasynchronous-unwind-tables;-fbuilding-libgcc;-fexceptions;"+
                       "-ffast-math;-ffinite-math-only;-ffreestanding;-fgnu-tm;-fhandle-exceptions;-fleading-underscore;-fnon-call-exceptions;-fopenmp;"+
                       "-fpic;-fpie;-fsanitize=address;"
                     + "-fshort-double;-fshort-wchar;-fsignaling-nans;-fstack-protector;-fstack-protector-all;"+
                       "-funsigned-char;-funwind-tables;-g;-ggdb;-gsplit-dwarf;-gtoggle;-m128bit-long-double;-m3dnow;-m64;-mabm;-madx;-maes;"+
                       "-march=amdfam10;-march=athlon;-march=bdver1;-march=bdver2;-march=bdver3;-march=btver1;-march=btver2;-march=core2;-march=corei7;"+
                       "-march=i386;-march=i486;-march=i586;-march=i686;-march=k6;-march=k8;-march=nocona;-march=opteron;-march=pentium;-march=pentium4;"+
                       "-march=pentiumpro;-march=prescott;"
                     + "-mavx;-mavx2;-mbmi;-mbmi2;-mf16c;-mfma;-mfma4;-mfsgsbase;-mlong-double-64;-mlwp;"+
                       "-mlzcnt;-mpclmul;-mpopcnt;-mprfchw;-mrdrnd;-mrdseed;-mrtm;-msse3;-msse4;-msse4.1;-msse4.2;-msse4a;-msse5;-mssse3;-mtbm;"+
                       "-mtune=amdfam10;-mtune=athlon;-mtune=bdver1;-mtune=bdver2;-mtune=bdver3;-mtune=btver1;-mtune=btver2;-mtune=core2;-mtune=corei7;-mtune=i386;"+
                       "-mtune=i486;-mtune=i586;-mtune=k6;-mtune=k8;-mtune=nocona;-mtune=opteron;-mtune=pentium;-mtune=pentium4;"+
                       "-mtune=pentiumpro;-mtune=prescott;"
                     + "-mx32;-mxop;-mxsave;-mxsaveopt;-pthreads;"+
                       "-std=c11;-std=c1x;-std=c89;-std=c90;-std=c99;-std=c9x;-std=gnu11;-std=gnu1x;-std=gnu99;-std=gnu9x;-std=iso9899:1990;"+
                       "-std=iso9899:199409;-std=iso9899:1999;-std=iso9899:199x;-std=iso9899:2011;";
        String golden ="-O1|-O2|-O3|-O4|-O5|-Ofast|-Og|-Os|-ansi|-fPIC|-fPIE|-fasynchronous-unwind-tables|-fbuilding-libgcc|-fexceptions|"
                + "-ffast-math|-ffinite-math-only|-ffreestanding|-fgnu-tm|-fhandle-exceptions|-fleading-underscore|-fnon-call-exceptions|-fopenmp|"
                + "-fpic|-fpie|-fsanitize=.*|-fshort-double|-fshort-wchar|-fsignaling-nans|-fstack-protector(\\W|$|-)|-fstack-protector-all|"
                + "-funsigned-char|-funwind-tables|-g(\\W|$|-)|-ggdb|-gsplit-dwarf|-gtoggle|-m128bit-long-double|-m3dnow|-m64|-mabm|-madx|-maes|"
                + "-march=.*|-mavx(\\W|$|-)|-mavx2|-mbmi(\\W|$|-)|-mbmi2|-mf16c|-mfma(\\W|$|-)|-mfma4|-mfsgsbase|-mlong-double-64|-mlwp|"
                + "-mlzcnt|-mpclmul|-mpopcnt|-mprfchw|-mrdrnd|-mrdseed|-mrtm|-msse3|-msse4(\\W|$|-)|-msse4.1|-msse4.2|-msse4a|-msse5|-mssse3|-mtbm|-mtune=.*|"
                + "-mx32|-mxop|-mxsave(\\W|$|-)|-mxsaveopt|-pthreads|-std=.*";
        List<String> list = new ArrayList<String>();
        for(String flag : flags.split(";")) {
            if (!flag.isEmpty()) {
                list.add(flag);
            }
        }
        String pattern = CCCCompiler.convertToRegularExpression(list);
        Pattern compile = Pattern.compile(pattern);
        for(String s : list) {
            Matcher matcher = compile.matcher(s);
            if (!matcher.find()) {
                Assert.assertTrue(false);
            }
        }
        //System.err.println(pattern);
        Assert.assertEquals(golden, pattern);
    }

    @Test
    public void testStudioOrdinaryFlag1() {
        String s = "-fopenmp                      Equivalent to -xopenmp=parallel";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fopenmp", res.get(0));
    }

    @Test
    public void testStudioOrdinaryFlag2() {
        String s = "-fsimple[=<n>]                Select floating-point optimization preferences <n>";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fsimple", res.get(0));
    }

    @Test
    public void testStudioAlternativeFlag1() {
        String s = "-fns[={yes|no}]               Select non-standard floating point mode";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-fns", res.get(0));
        Assert.assertEquals("-fns=yes", res.get(1));
        Assert.assertEquals("-fns=no", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag2() {
        String s = "-d{n|y}                       Dynamic [-dy] or static [-dn] option to linker";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-dn", res.get(0));
        Assert.assertEquals("-dy", res.get(1));
    }

    @Test
    public void testStudioAlternativeFlag3() {
        String s = "-B[static|dynamic]            Specify dynamic or static binding";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-B", res.get(0));
        Assert.assertEquals("-Bstatic", res.get(1));
        Assert.assertEquals("-Bdynamic", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag4() {
        String s = "-xmaxopt=[off,1,2,3,4,5]      Maximum optimization level allowed on #pragma opt";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(7, res.size());
        Assert.assertEquals("-xmaxopt=", res.get(0));
        Assert.assertEquals("-xmaxopt=off", res.get(1));
        Assert.assertEquals("-xmaxopt=1", res.get(2));
        Assert.assertEquals("-xmaxopt=2", res.get(3));
        Assert.assertEquals("-xmaxopt=3", res.get(4));
        Assert.assertEquals("-xmaxopt=4", res.get(5));
        Assert.assertEquals("-xmaxopt=5", res.get(6));
    }

    @Test
    public void testStudioAlternativeFlag5() {
        String s = "-filt[=<a>[,<a>]]             Control the filtering of both linker and compiler error messages;";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-filt", res.get(0));
    }
    
    @Test
    public void testStudioAlternativeFlag6() {
        String s = "-std=<a>                      Specify the c++ standard ; <a>={c++03|c++0x|c++11}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-std=c++03", res.get(0));
        Assert.assertEquals("-std=c++0x", res.get(1));
        Assert.assertEquals("-std=c++11", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag7() {
        String s = "-xipo[=<n>]                   Enable optimization and inlining across source files; <n>={0|1|2}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("-xipo", res.get(0));
        Assert.assertEquals("-xipo=0", res.get(1));
        Assert.assertEquals("-xipo=1", res.get(2));
        Assert.assertEquals("-xipo=2", res.get(3));
    }

    @Test
    public void testStudioAlternativeFlag8() {
        String s = "-xport64[=<a>]                Enable extra checking for code ported from 32-bit to 64-bit platforms;\n" +
            "                              <a>={no|implicit|full}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("-xport64", res.get(0));
        Assert.assertEquals("-xport64=no", res.get(1));
        Assert.assertEquals("-xport64=implicit", res.get(2));
        Assert.assertEquals("-xport64=full", res.get(3));
    }

    @Test
    public void testStudioAlternativeFlag9() {
        String s = "-O<n>                         Same as -xO<n>";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(6, res.size());
        Assert.assertEquals("-O0", res.get(0));
        Assert.assertEquals("-O1", res.get(1));
        Assert.assertEquals("-O2", res.get(2));
        Assert.assertEquals("-O3", res.get(3));
        Assert.assertEquals("-O4", res.get(4));
        Assert.assertEquals("-O5", res.get(5));
    }

    @Test
    public void testStudioIgnoredFlag1() {
        String s = "-Xlinker <arg>                Pass <arg> to linker";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testStudioIgnoredFlag2() {
        String s = "-ftrap=<t>                    Select floating-point trapping mode in effect at startup";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testStudioIgnoredFlag3() {
        String s = "-xlang=<a>[,<a>]              The set of languages used in the program; <a>={f90,f95,c99}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-xlang=f90", res.get(0));
        Assert.assertEquals("-xlang=f95", res.get(1));
        Assert.assertEquals("-xlang=c99", res.get(2));
    }
    
    @Test
    public void testStudioIgnoredFlag4() {
        String s = "-xprefetch_level[=<n>]        Controls the aggressiveness of the -xprefetch=auto option; <n>={1|2|3}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("-xprefetch_level", res.get(0));
        Assert.assertEquals("-xprefetch_level=1", res.get(1));
        Assert.assertEquals("-xprefetch_level=2", res.get(2));
        Assert.assertEquals("-xprefetch_level=3", res.get(3));
    }

    @Test
    public void testStudioIgnoredFlag5() {
        String s = "-xO<n>                        Generate optimized code; <n>={1|2|3|4|5}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(5, res.size());
        Assert.assertEquals("-xO1", res.get(0));
        Assert.assertEquals("-xO2", res.get(1));
        Assert.assertEquals("-xO3", res.get(2));
        Assert.assertEquals("-xO4", res.get(3));
        Assert.assertEquals("-xO5", res.get(4));
    }
    
    @Test
    public void testStudioIgnoredFlag6() {
        String s = "-xcheck[=<a>[,<a>]]           Generate runtime checks for error condition;\n" +
                   "                              <a>={stkovf,stkovf:diagnose,stkovf:detect,init_local}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(5, res.size());
        Assert.assertEquals("-xcheck", res.get(0));
        Assert.assertEquals("-xcheck=stkovf", res.get(1));
        Assert.assertEquals("-xcheck=stkovf:diagnose", res.get(2));
        Assert.assertEquals("-xcheck=stkovf:detect", res.get(3));
        Assert.assertEquals("-xcheck=init_local", res.get(4));
    }

    @Test
    public void testStudioIgnoredFlag7() {
        String s = "-xrestrict[=<f>]              Treat pointer valued function parameters as restricted; <f>={%none,%all,<function-name list>}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-xrestrict", res.get(0));
        Assert.assertEquals("-xrestrict=%none", res.get(1));
        Assert.assertEquals("-xrestrict=%all", res.get(2));
    }

    @Test
    public void testStudioIgnoredFlag8() {
        String s = "-xpatchpadding[=<a>]          Put space before start of code for hot patching. <a>={fix|patch|<integer>}";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-xpatchpadding", res.get(0));
        Assert.assertEquals("-xpatchpadding=fix", res.get(1));
        Assert.assertEquals("-xpatchpadding=patch", res.get(2));
    }

    @Test
    public void testGccOrdinaryFlag1() {
        String s = "  -fsched2-use-traces         Does nothing.  Preserved for backward\n" +
            "                              compatibility.";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fsched2-use-traces", res.get(0));
    }

    @Test
    public void testGccAlternativeFlag1() {
        String s = "  -fexcess-precision=[fast|standard] Specify handling of excess floating-point\n" +
            "                              precision";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-fexcess-precision=fast", res.get(0));
        Assert.assertEquals("-fexcess-precision=standard", res.get(1));
    }

    @Test
    public void testGccAlternativeFlag2() {
        String s = "  -finit-logical=<true|false> Initialize local logical variables";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-finit-logical=true", res.get(0));
        Assert.assertEquals("-finit-logical=false", res.get(1));
    }

    @Test
    public void testGccAlternativeFlag3() {
        String s = "  -O<number>                  Set optimization level to <number>";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(6, res.size());
        Assert.assertEquals("-O0", res.get(0));
        Assert.assertEquals("-O1", res.get(1));
        Assert.assertEquals("-O2", res.get(2));
        Assert.assertEquals("-O3", res.get(3));
        Assert.assertEquals("-O4", res.get(4));
        Assert.assertEquals("-O5", res.get(5));
    }

    @Test
    public void testGccAlternativeFlag4() {
        String s = "  -mtune=CPU              optimize for CPU, CPU is one of:\n" +
            "                           generic32, generic64, i8086, i186, i286, i386, i486,\n" +
            "                           i586, i686, pentium, pentiumpro, pentiumii,\n" +
            "                           pentiumiii, pentium4, prescott, nocona, core, core2,\n" +
            "                           corei7, l1om, k6, k6_2, athlon, opteron, k8,\n" +
            "                           amdfam10, bdver1";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(27, res.size());
        Assert.assertEquals("-mtune=generic32", res.get(0));
        Assert.assertEquals("-mtune=bdver1", res.get(26));
    }

    @Test
    public void testGccAlternativeFlag5() {
        String s = "  -march=CPU[,+EXTENSION...]\n" +
            "                          generate code for CPU and EXTENSION, CPU is one of:\n" +
            "                           generic32, generic64, i386, i486, i586, i686,\n" +
            "                           pentium, pentiumpro, pentiumii, pentiumiii, pentium4,\n" +
            "                           prescott, nocona, core, core2, corei7, l1om, k6,\n" +
            "                           k6_2, athlon, opteron, k8, amdfam10, bdver1\n" +
            "                          EXTENSION is combination of:\n" +
            "                           8087, 287, 387, no87, mmx, nommx, sse, sse2, sse3,\n" +
            "                           ssse3, sse4.1, sse4.2, sse4, nosse, avx, noavx, vmx,\n" +
            "                           smx, xsave, xsaveopt, aes, pclmul, fsgsbase, rdrnd,\n" +
            "                           f16c, fma, fma4, xop, lwp, movbe, ept, clflush, nop,\n" +
            "                           syscall, rdtscp, 3dnow, 3dnowa, padlock, svme, sse4a,\n" +
            "                           abm";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(24, res.size());
        Assert.assertEquals("-march=generic32", res.get(0));
        Assert.assertEquals("-march=bdver1", res.get(23));
    }

    @Test
    public void testGccIgnoredFlag1() {
        String s = "  -fsched-stalled-insns-dep=<number> Set dependence distance checking in\n" +
            "                              premature scheduling of queued insns";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGccIgnoredFlag2() {
        String s = "  -idirafter <dir>            Add <dir> to the end of the system include path";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGccIgnoredFlag3() {
        String s = "  --divide                do not treat `/' as a comment character";
        List<String> res = new ArrayList<String>();
        List<String> undef = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, undef, true);
        Assert.assertEquals(0, res.size());
    }
}
