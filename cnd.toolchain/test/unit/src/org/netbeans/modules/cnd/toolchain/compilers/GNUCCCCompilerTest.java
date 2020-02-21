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
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.toolchain.compilers.CCCCompiler.CompilerDefinitions;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 */
public class GNUCCCCompilerTest {

    private static final boolean TRACE = false;

    public GNUCCCCompilerTest() {
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
    public void testPatternCpp() {
        String s = "c++";
        s = s.replace("+", "\\+");
        s = ".*\\.(" + s + ")$"; //NOI18N;
        Pattern pattern = Pattern.compile(s);
        assert(pattern.matcher("file.c++").find());
        assert(!pattern.matcher("file.cpp").find());
    }

    @Test
    public void testParseCompilerOutputGcc() {
        //System.setProperty("os.name", "SunOS");
        String s =
                "{2} bash-3.00#g++ -x c++ -E -v tmp.cpp\n" +
                "Reading specs from /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/specs\n" +
                "Configured with: /builds/sfw10-gate/usr/src/cmd/gcc/gcc-3.4.3/configure --prefix=/usr/sfw --with-as=/usr/sfw/bin/gas --with-gnu-as --with-ld=/usr/ccs/bin/ld --without-gnu-ld --enable-languages=c,c++ --enable-shared\n" +
                "Thread model: posix\n" +
                "gcc version 3.4.3 (csl-sol210-3_4-branch+sol_rpath)\n" +
                " /usr/sfw/libexec/gcc/i386-pc-solaris2.10/3.4.3/cc1plus -E -quiet -v tmp.cpp\n" +
                "ignoring nonexistent directory \"/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../i386-pc-solaris2.10/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/i386-pc-solaris2.10\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/backward\n" +
                " /usr/local/include\n" +
                " /usr/sfw/include\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include\n" +
                " /usr/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of GCC on Solaris");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("GNU", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "GNU", "GNU", "/usr/sfw/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("/usr/include");
        golden.add("/usr/local/include");
        golden.add("/usr/sfw/include");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/backward");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/i386-pc-solaris2.10");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
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
    public void testParseCompilerOutputMac() {
        //System.setProperty("os.name", "Darwin");
        String s =
                "jorge@macbook: $ gcc -E -v -x c++ /dev/null\n" +
                "Using built-in specs.\n" +
                "Target: i686-apple-darwin9\n" +
                "Configured with: /var/tmp/gcc/gcc-5465~16/src/configure --disable-checking -enable-werror --prefix=/usr --mandir=/share/man --enable-languages=c,objc,c++,obj-c++ --program-transform-name=/^[cg][^.-]*$/s/$/-4.0/ --with-gxx-include-dir=/include/c++/4.0.0 --with-slibdir=/usr/lib --build=i686-apple-darwin9 --with-arch=apple --with-tune=generic --host=i686-apple-darwin9 --target=i686-apple-darwin9\n" +
                "Thread model: posix\n" +
                "gcc version 4.0.1 (Apple Inc. build 5465)\n" +
                "/usr/libexec/gcc/i686-apple-darwin9/4.0.1/cc1plus -E -quiet -v -D__DYNAMIC__ /dev/null -fPIC -mmacosx-version-min=10.5.6 -mtune=generic -march=apple -D__private_extern__=extern\n" +
                "ignoring nonexistent directory \"/usr/lib/gcc/i686-apple-darwin9/4.0.1/../../../../i686-apple-darwin9/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                " /usr/include/c++/4.0.0\n" +
                " /usr/include/c++/4.0.0/i686-apple-darwin9\n" +
                " /usr/include/c++/4.0.0/backward\n" +
                " /usr/local/include\n" +
                " /usr/lib/gcc/i686-apple-darwin9/4.0.1/include\n" +
                " /usr/include\n" +
                " /System/Library/Frameworks (framework directory)\n" +
                " /Library/Frameworks (framework directory)\n" +
                "End of search list.\n" +
                "# 1 \"/dev/null\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"/dev/null\"\n";

        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of GNU on Mac");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("GNU", PlatformTypes.PLATFORM_MACOSX);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "GNU", "GNU", "/usr/sfw/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();

        golden.add("/Library/Frameworks"+Driver.FRAMEWORK);
        golden.add("/System/Library/Frameworks"+Driver.FRAMEWORK);
        golden.add("/usr/include");
        golden.add("/usr/include/c++/4.0.0");
        golden.add("/usr/include/c++/4.0.0/backward");
        golden.add("/usr/include/c++/4.0.0/i686-apple-darwin9");
        golden.add("/usr/lib/gcc/i686-apple-darwin9/4.0.1/include");
        golden.add("/usr/local/include");

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
    public void testParseCompilerOutputMinGW1() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "C:\\MinGW\\bin>g++.exe -x c++ -E -v tmp.cpp\n" +
                "Reading specs from C:/MinGW/lib/gcc/mingw32/3.4.5/specs\n" +
                "Configured with: ../gcc-3.4.5/configure --with-gcc --with-gnu-ld --with-gnu-as --host=mingw32 --target=mingw32 --prefix=/mingw --enable-threads --disable-nls --enable-languages=c,c++,f77,ada,objc,java --disable-win32-registry --disable-shared --enable-sjlj-exceptions --enable-libgcj --disable-java-awt --without-x --enable-java-gc=boehm --disable-libgcj-debug--enable-interpreter --enable-hash-synchronization --enable-libstdcxx-debug\n" +
                "Thread model: win32\n" +
                "gcc version 3.4.5 (mingw special)\n" +
                "cc1plus -E -quiet -v -iprefix C:\\MinGW\\bin\\../lib/gcc/mingw32/3.4.5/ tmp.cpp\n" +
                "ignoring nonexistent directory \"C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../mingw32/include\"\n" +
                "ignoring nonexistent directory \"/mingw/mingw32/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/mingw32\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/backward\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/include\n" +
                "/mingw/include/c++/3.4.5\n" +
                "/mingw/include/c++/3.4.5/mingw32\n" +
                "/mingw/include/c++/3.4.5/backward\n" +
                "/mingw/include\n" +
                "/mingw/include\n" +
                "/mingw/lib/gcc/mingw32/3.4.5/include\n" +
                "/mingw/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";

        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of MinGW on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("MinGW", PlatformTypes.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "MinGW", "MinGW", "C:\\MinGW\\bin\\g++");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/backward");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/mingw32");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/include");
        golden.add("C:/MinGW/include");
        golden.add("C:/MinGW/include/c++/3.4.5");
        golden.add("C:/MinGW/include/c++/3.4.5/backward");
        golden.add("C:/MinGW/include/c++/3.4.5/mingw32");
        golden.add("C:/MinGW/lib/gcc/mingw32/3.4.5/include");

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
    public void testParseCompilerOutputMinGW2() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "D:\\tec\\MinGW\\bin>g++.exe -x c++ -E -v tmp.cpp\n" +
                "Using built-in specs.\n" +
                "Target: mingw32\n" +
                "Configured with: ../gcc-4.3.2/cnfigure --prefix=/mingw --build=mingw32 --enable-languages=c,ada,c++,fortran,objc,obj-c++ --with-bugurl=http://www.tdragon.net/recentgcc/bugs.php --disable-nls --disable-win32-registry --enable-libgomp --disable-werror --enable-threads --disable-symvers --enable-cxx-flags='-fno-function-sections -fno-data-sections' --enable-fully-dynamic-string --enable-version-specific-runtime-libs --enable-sjlj-exceptions --with-pkgversion='4.3.2-tdm-1 for MinGW'\n" +
                "Thread model: win32\n" +
                "gcc version 4.3.2 (4.3.2-tdm-1 for MinGW)\n" +
                "COLLECT_GCC_OPTIONS='-E' '-v' '-mtune=i386'\n" +
                "d:/tec/mingw/bin/../libexec/gcc/mingw32/4.3.2/cc1plus.exe -E -quiet -v -iprefix d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/ tmp.cpp -mtune=i386\n" +
                "ignoring nonexistent directory \"d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../mingw32/include\"\n" +
                "ignoring nonexistent directory \"d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/../../../../mingw32/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/mingw32\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/backward\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../include\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include-fixed\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/mingw32\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/backward\n" +
                "/mingw/lib/gcc/mingw32/../../../include\n" +
                "d:/tec/mingw/lib/gcc/../../include\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include-fixed\n" +
                "/mingw/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command-line>\"\n" +
                "# 1 \"tmp.cpp\"\n" +
                "COMPILER_PATH=d:/tec/mingw/bin/../libexec/gcc/mingw32/4.3.2/;d:/tec/mingw/bin/../libexec/gcc/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../../mingw32/bin/\n" +
                "LIBRARY_PATH=d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/;d:/tec/mingw/bin/../lib/gcc/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../../mingw32/lib/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../;/mingw/lib/\n" +
                "COLLECT_GCC_OPTIONS='-E' '-v' '-mtune=i386'\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of TDM MinGW on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("MinGW_TDM", PlatformTypes.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "MinGW_TDM", "MinGW_TDM", "D:\\tec\\mingw\\bin\\g++.exe");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("D:/tec/mingw/include");
        golden.add("D:/tec/mingw/lib/gcc/mingw32/../../../include");
        golden.add("d:/tec/mingw/lib/gcc/../../include");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include-fixed");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/backward");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/mingw32");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../include");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include-fixed");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/backward");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/mingw32");

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
    public void testParseCompilerOutputMinGWCygwin() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "c:\\msys64\\usr\\bin\\g++ -E -v -x c++ tmp.cpp\n" +
                "gcc version 6.3.0 (GCC)\n" +
                "#include <...> search starts here:\n" +
                " /usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++\n" +
                " /usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++/x86_64-pc-msys\n" +
                " /usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++/backward\n" +
                " /usr/lib/gcc/x86_64-pc-msys/6.3.0/include\n" +
                " /usr/lib/gcc/x86_64-pc-msys/6.3.0/include-fixed\n" +
                " /usr/include\n" +
                " /usr/lib/../lib/../include/w32api\n" +
                "End of search list.\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of TDM MinGW on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("MinGW", PlatformTypes.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "MinGW", "MinGW", "c:\\msys64\\usr\\bin\\g++.exe");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("c:/msys64/usr/include");
        golden.add("c:/msys64/usr/lib/../lib/../include/w32api");
        golden.add("c:/msys64/usr/lib/gcc/x86_64-pc-msys/6.3.0/include");
        golden.add("c:/msys64/usr/lib/gcc/x86_64-pc-msys/6.3.0/include-fixed");
        golden.add("c:/msys64/usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++");
        golden.add("c:/msys64/usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++/backward");
        golden.add("c:/msys64/usr/lib/gcc/x86_64-pc-msys/6.3.0/include/c++/x86_64-pc-msys");

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
    public void testParseCompilerOutputCygwin() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "$ g++.exe -x c++ -E -v tmp.cpp\n" +
                "Reading specs from /usr/lib/gcc/i686-pc-cygwin/3.4.4/specs\n" +
                "Configured with: /usr/build/package/orig/test.respin/gcc-3.4.4-3/configure --verbose --prefix=/usr --exec-prefix=/usr --sysconfdir=/etc --libdir=/usr/lib --libexecdir=/usr/lib --mandir=/usr/share/man --infodir=/usr/share/info --enable-languages=c,ada,c++,d,f77,pascal,java,objc --enable-nls --without-included-gettext --enable-version-specific-runtime-libs --without-x --enable-libgcj --disable-java-awt --with-system-zlib --enable-interpreter --disable-libgcj-debug --enable-threads=posix --enable-java-gc=boehm --disable-win32-registry --enable-sjlj-exceptions --enable-hash-synchronization --enable-libstdcxx-debug\n" +
                "Thread model: posix\n" +
                "gcc version 3.4.4 (cygming special, gdc 0.12, using dmd 0.125)\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/cc1plus.exe -E -quiet -v -D__CYGWIN32__ -D__CYGWIN__ -Dunix -D__unix__ -D__unix -idirafter /usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api -idirafter /usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/lib/../../include/w32api tmp.cpp -mtune=pentiumpro\n" +
                "ignoring nonexistent directory \"/usr/local/include\"\n" +
                "ignoring nonexistent directory \"/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/include\"\n" +
                "ignoring duplicate directory \"/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/lib/../../include/w32api\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include\n" +
                "/usr/include\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of Cygwin on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "Cygwin", "Cygwin", "C:\\cygwin\\bin\\g++.exe");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin");
        golden.add("C:/cygwin/usr/include");
        //golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api");
        //golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include");
        //golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        //golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward");
        //golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin");
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
    public void testParseCompilerOutputCygwinMingw() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "C:\\cygwin64\\bin\\i686-w64-mingw32-g++.exe -E -v -x c++ tmp.cpp\n" +
                "gcc version 5.4.0 (GCC)\n" +
                "#include <...> search starts here:\n" +
                 "/usr/lib/gcc/i686-w64-mingw32/5.4.0/include/c++\n" +
                 "/usr/lib/gcc/i686-w64-mingw32/5.4.0/include/c++/i686-w64-mingw32\n" +
                 "/usr/lib/gcc/i686-w64-mingw32/5.4.0/include/c++/backward\n" +
                 "/usr/lib/gcc/i686-w64-mingw32/5.4.0/include\n" +
                 "/usr/lib/gcc/i686-w64-mingw32/5.4.0/include-fixed\n" +
                 "/usr/i686-w64-mingw32/sys-root/mingw/include\n" +
                "End of search list.\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of Cygwin on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "Cygwin", "Cygwin", "C:\\cygwin64\\bin\\i686-w64-mingw32-g++.exe");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<>();
        golden.add("C:/cygwin64/lib/gcc/i686-w64-mingw32/5.4.0/include");
        golden.add("C:/cygwin64/lib/gcc/i686-w64-mingw32/5.4.0/include-fixed");
        golden.add("C:/cygwin64/lib/gcc/i686-w64-mingw32/5.4.0/include/c++");
        golden.add("C:/cygwin64/lib/gcc/i686-w64-mingw32/5.4.0/include/c++/backward");
        golden.add("C:/cygwin64/lib/gcc/i686-w64-mingw32/5.4.0/include/c++/i686-w64-mingw32");
        golden.add("C:/cygwin64/usr/i686-w64-mingw32/sys-root/mingw/include");
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
    public void testParseCompilerOutputIcc() {
        //System.setProperty("os.name", "SunOS");
        String s =
                "$ icc -x c -E -dM -v /tmp/c.c\n" +
                "Version 12.0.5\n" +
                "/usr/local/remote/packages/icc_remote/12.0.5.225_fixbug13889838/composerxe-2011.5.225/bin/intel64/mcpcom    -_g -mP3OPT_inline_alloca -D__HONOR_STD -D__ICC=1200 -D__INTEL_COMPILER=1200 -D_MT \"-_Asystem(unix)\" -D__ELF__ \"-_Acpu(x86_64)\" \"-_Amachine(x86_64)\" -D__INTEL_COMPILER_BUILD_DATE=20120716 -D__PTRDIFF_TYPE__=long \"-D__SIZE_TYPE__=unsigned long\" -D__WCHAR_TYPE__=int \"-D__WINT_TYPE__=unsigned int\" \"-D__INTMAX_TYPE__=long int\" \"-D__UINTMAX_TYPE__=long unsigned int\" -D__LONG_MAX__=9223372036854775807L -D__QMSPP_ -D__OPTIMIZE__ -D__NO_MATH_INLINES -D__NO_STRING_INLINES -D__GNUC__=4 -D__GNUC_MINOR__=1 -D__GNUC_PATCHLEVEL__=2 -D__NO_INLINE__ -D__i686 -D__i686__ -D__pentiumpro -D__pentiumpro__ -D__pentium4 -D__pentium4__ -D__tune_pentium4__ -D__MMX__ -D__LP64__ -D_LP64 -D__GXX_ABI_VERSION=1002 \"-D__USER_LABEL_PREFIX__= \" -D__REGISTER_PREFIX__= -D__INTEL_RTTI__ -D__SSE2__ -D__SSE__ -D__unix__ -D__unix -D__linux__ -D__linux -D__gnu_linux__ -B -Dunix -Dlinux -D__x86_64 -D__x86_64__ -_k -_8 -_l -_a -_b -E --gnu_version=412 -_W5 --gcc-extern-inline --dM --multibyte_chars --array_section --simd --simd_func -mP1OPT_print_version=FALSE -mP1OPT_version=12.0-intel64 /tmp/c.c\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                " /usr/local/remote/packages/icc_remote/12.0.5.225_fixbug13889838/composerxe-2011.5.225/compiler/include/intel64\n" +
                " /usr/local/remote/packages/icc_remote/12.0.5.225_fixbug13889838/composerxe-2011.5.225/compiler/include\n" +
                " /usr/local/include\n" +
                " /usr/include\n" +
                " /usr/lib/gcc/x86_64-redhat-linux/4.1.2/include\n" +
                "End of search list.\n" +
                "#define __SIGNED_CHARS__ 1\n" +
                "#define __PRAGMA_REDEFINE_EXTNAME 1\n" +
                "#define __DATE__ \"Jul 16 2012\"\n" +
                "#define __TIME__ \"01:14:06\"\n" +
                "#define __STDC__ 1\n" +
                "#define __STDC_DEC_FP__ 200704L\n" +
                "#define __DEC_EVAL_METHOD__ 2\n" +
                "#define __cilk 200\n" +
                "#define __LONG_DOUBLE_SIZE__ 80\n" +
                "#define __EDG__ 1\n" +
                "#define __EDG_VERSION__ 401\n" +
                "#define __EDG_SIZE_TYPE__ unsigned long\n" +
                "#define __EDG_PTRDIFF_TYPE__ long\n" +
                "#define __DBL_DIG__ 15\n" +
                "#define __DBL_MAX_10_EXP__ 308\n" +
                "#define __DBL_MIN_10_EXP__ -307\n" +
                "#define __DBL_HAS_DENORM__ 1\n" +
                "#define __FLT_DIG__ 6\n" +
                "#define __FLT_MAX_10_EXP__ 38\n" +
                "#define __FLT_MIN_10_EXP__ -37\n" +
                "#define __FLT_RADIX__ 2\n" +
                "#define __FLT_HAS_DENORM__ 1\n" +
                "#define __INT_MAX__ 2147483647\n" +
                "#define __LDBL_DIG__ 18\n" +
                "#define __LDBL_HAS_INFINITY__ 1\n" +
                "#define __LDBL_HAS_QUIET_NAN__ 1\n" +
                "#define __LDBL_MAX_10_EXP__ 4932\n" +
                "#define __LDBL_MIN_10_EXP__ -4931\n" +
                "#define __LDBL_HAS_DENORM__ 1\n" +
                "#define __DBL_HAS_INFINITY__ 1\n" +
                "#define __DBL_HAS_QUIET_NAN__ 1\n" +
                "#define __DECIMAL_DIG__ 21\n" +
                "#define __FINITE_MATH_ONLY__ 0\n" +
                "#define __FLT_HAS_INFINITY__ 1\n" +
                "#define __FLT_HAS_QUIET_NAN__ 1\n" +
                "#define __SCHAR_MAX__ 127\n" +
                "#define __SHRT_MAX__ 32767\n" +
                "#define __WCHAR_MAX__ 2147483647\n" +
                "#define __CHAR_BIT__ 8\n" +
                "#define __DBL_MANT_DIG__ 53\n" +
                "#define __DBL_MAX_EXP__ 1024\n" +
                "#define __TARG_DBL_MAX_EXP__ 1024\n" +
                "#define __DBL_MIN_EXP__ -1021\n" +
                "#define __FLT_MANT_DIG__ 24\n" +
                "#define __FLT_MAX_EXP__ 128\n" +
                "#define __FLT_MIN_EXP__ -125\n" +
                "#define __LDBL_MANT_DIG__ 64\n" +
                "#define __LDBL_MAX_EXP__ 16384\n" +
                "#define __LDBL_MIN_EXP__ -16381\n" +
                "#define __DBL_DENORM_MIN__ 4.9406564584124654e-324\n" +
                "#define __DBL_EPSILON__ 2.2204460492503131e-16\n" +
                "#define __DBL_MAX__ 1.7976931348623157e+308\n" +
                "#define __DBL_MIN__ 2.2250738585072014e-308\n" +
                "#define __FLT_DENORM_MIN__ 1.40129846e-45F\n" +
                "#define __FLT_EPSILON__ 1.19209290e-7F\n" +
                "#define __FLT_MAX__ 3.40282347e+38F\n" +
                "#define __FLT_MIN__ 1.17549435e-38F\n" +
                "#define __LDBL_DENORM_MIN__ 3.64519953188247460253e-4951L\n" +
                "#define __LDBL_EPSILON__ 1.08420217248550443401e-19L\n" +
                "#define __LDBL_MAX__ 1.18973149535723176502e+4932L\n" +
                "#define __LDBL_MIN__ 3.36210314311209350626e-4932L\n" +
                "#define __LONG_LONG_MAX__ 0x7fffffffffffffff\n" +
                "#define __DEC32_MAX__ 9.999999E96DF\n" +
                "#define __DEC64_MAX__ 9.999999999999999E384DD\n" +
                "#define __DEC128_MAX__ 9.999999999999999999999999999999999E6144DL\n" +
                "#define __DEC32_MANT_DIG__ 7\n" +
                "#define __DEC64_MANT_DIG__ 16\n" +
                "#define __DEC128_MANT_DIG__ 34\n" +
                "#define __DEC32_MIN_EXP__ -95\n" +
                "#define __DEC64_MIN_EXP__ -383\n" +
                "#define __DEC128_MIN_EXP__ -6143\n" +
                "#define __DEC32_MAX_EXP__ 96\n" +
                "#define __DEC64_MAX_EXP__ 384\n" +
                "#define __DEC128_MAX_EXP__ 6144\n" +
                "#define __DEC32_EPSILON__ 1E-6DF\n" +
                "#define __DEC64_EPSILON__ 1E-15DD\n" +
                "#define __DEC128_EPSILON__ 1E-33DL\n" +
                "#define __DEC32_MIN__ 1E-95DF\n" +
                "#define __DEC64_MIN__ 1E-383DD\n" +
                "#define __DEC128_MIN__ 1E-6143DL\n" +
                "#define __DEC32_SUBNORMAL_MIN__ 0.000001E-95DF\n" +
                "#define __DEC64_SUBNORMAL_MIN__ 0.000000000000001E-383DD\n" +
                "#define __DEC128_SUBNORMAL_MIN__ 0.000000000000000000000000000000001E-6143DL\n" +
                "#define __VERSION__ \"Intel(R) C++ gcc 4.1 mode\"\n" +
                "#define __HONOR_STD 1\n" +
                "#define __ICC 1200\n" +
                "#define __INTEL_COMPILER 1200\n" +
                "#define _MT 1\n" +
                "#define __ELF__ 1\n" +
                "#define __INTEL_COMPILER_BUILD_DATE 20120716\n" +
                "#define __PTRDIFF_TYPE__ long\n" +
                "#define __SIZE_TYPE__ unsigned long\n" +
                "#define __WCHAR_TYPE__ int\n" +
                "#define __WINT_TYPE__ unsigned int\n" +
                "#define __INTMAX_TYPE__ long int\n" +
                "#define __UINTMAX_TYPE__ long unsigned int\n" +
                "#define __LONG_MAX__ 9223372036854775807L\n" +
                "#define __QMSPP_ 1\n" +
                "#define __OPTIMIZE__ 1\n" +
                "#define __NO_MATH_INLINES 1\n" +
                "#define __NO_STRING_INLINES 1\n" +
                "#define __GNUC__ 4\n" +
                "#define __GNUC_MINOR__ 1\n" +
                "#define __GNUC_PATCHLEVEL__ 2\n" +
                "#define __NO_INLINE__ 1\n" +
                "#define __i686 1\n" +
                "#define __i686__ 1\n" +
                "#define __pentiumpro 1\n" +
                "#define __pentiumpro__ 1\n" +
                "#define __pentium4 1\n" +
                "#define __pentium4__ 1\n" +
                "#define __tune_pentium4__ 1\n" +
                "#define __MMX__ 1\n" +
                "#define __LP64__ 1\n" +
                "#define _LP64 1\n" +
                "#define __GXX_ABI_VERSION 1002\n" +
                "#define __USER_LABEL_PREFIX__\n" +
                "#define __REGISTER_PREFIX__\n" +
                "#define __INTEL_RTTI__ 1\n" +
                "#define __SSE2__ 1\n" +
                "#define __SSE__ 1\n" +
                "#define __unix__ 1\n" +
                "#define __unix 1\n" +
                "#define __linux__ 1\n" +
                "#define __linux 1\n" +
                "#define __gnu_linux__ 1\n" +
                "#define unix 1\n" +
                "#define linux 1\n" +
                "#define __x86_64 1\n" +
                "#define __x86_64__ 1\n" +
                "rm /tmp/iccCQQENwlibgcc\n" +
                "\n" +
                "rm /tmp/iccQQaIIqgnudirs\n" +
                "\n" +
                "rm /tmp/iccYdmYFkarg\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of icc on Linux");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("Intel", PlatformTypes.PLATFORM_LINUX);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, PredefinedToolKind.CCCompiler, "ICC", "ICC", "/usr/sfw/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        List<String> golden = new ArrayList<>();
        golden.add("/usr/local/remote/packages/icc_remote/12.0.5.225_fixbug13889838/composerxe-2011.5.225/compiler/include/intel64");
        golden.add("/usr/local/remote/packages/icc_remote/12.0.5.225_fixbug13889838/composerxe-2011.5.225/compiler/include");
        golden.add("/usr/local/include");
        golden.add("/usr/include");
        golden.add("/usr/lib/gcc/x86_64-redhat-linux/4.1.2/include");
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
        golden = new ArrayList<>();
        golden.add("__SIGNED_CHARS__=1");
        golden.add("__PRAGMA_REDEFINE_EXTNAME=1");
        golden.add("__DATE__=\"Jul 16 2012\"");
        golden.add("__TIME__=\"01:14:06\"");
        golden.add("__STDC__=1");
        golden.add("__STDC_DEC_FP__=200704L");
        golden.add("__DEC_EVAL_METHOD__=2");
        golden.add("__cilk=200");
        golden.add("__LONG_DOUBLE_SIZE__=80");
        golden.add("__EDG__=1");
        golden.add("__EDG_VERSION__=401");
        golden.add("__EDG_SIZE_TYPE__=unsigned long");
        golden.add("__EDG_PTRDIFF_TYPE__=long");
        golden.add("__DBL_DIG__=15");
        golden.add("__DBL_MAX_10_EXP__=308");
        golden.add("__DBL_MIN_10_EXP__=-307");
        golden.add("__DBL_HAS_DENORM__=1");
        golden.add("__FLT_DIG__=6");
        golden.add("__FLT_MAX_10_EXP__=38");
        golden.add("__FLT_MIN_10_EXP__=-37");
        golden.add("__FLT_RADIX__=2");
        golden.add("__FLT_HAS_DENORM__=1");
        golden.add("__INT_MAX__=2147483647");
        golden.add("__LDBL_DIG__=18");
        golden.add("__LDBL_HAS_INFINITY__=1");
        golden.add("__LDBL_HAS_QUIET_NAN__=1");
        golden.add("__LDBL_MAX_10_EXP__=4932");
        golden.add("__LDBL_MIN_10_EXP__=-4931");
        golden.add("__LDBL_HAS_DENORM__=1");
        golden.add("__DBL_HAS_INFINITY__=1");
        golden.add("__DBL_HAS_QUIET_NAN__=1");
        golden.add("__DECIMAL_DIG__=21");
        golden.add("__FINITE_MATH_ONLY__=0");
        golden.add("__FLT_HAS_INFINITY__=1");
        golden.add("__FLT_HAS_QUIET_NAN__=1");
        golden.add("__SCHAR_MAX__=127");
        golden.add("__SHRT_MAX__=32767");
        golden.add("__WCHAR_MAX__=2147483647");
        golden.add("__CHAR_BIT__=8");
        golden.add("__DBL_MANT_DIG__=53");
        golden.add("__DBL_MAX_EXP__=1024");
        golden.add("__TARG_DBL_MAX_EXP__=1024");
        golden.add("__DBL_MIN_EXP__=-1021");
        golden.add("__FLT_MANT_DIG__=24");
        golden.add("__FLT_MAX_EXP__=128");
        golden.add("__FLT_MIN_EXP__=-125");
        golden.add("__LDBL_MANT_DIG__=64");
        golden.add("__LDBL_MAX_EXP__=16384");
        golden.add("__LDBL_MIN_EXP__=-16381");
        golden.add("__DBL_DENORM_MIN__=4.9406564584124654e-324");
        golden.add("__DBL_EPSILON__=2.2204460492503131e-16");
        golden.add("__DBL_MAX__=1.7976931348623157e+308");
        golden.add("__DBL_MIN__=2.2250738585072014e-308");
        golden.add("__FLT_DENORM_MIN__=1.40129846e-45F");
        golden.add("__FLT_EPSILON__=1.19209290e-7F");
        golden.add("__FLT_MAX__=3.40282347e+38F");
        golden.add("__FLT_MIN__=1.17549435e-38F");
        golden.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L");
        golden.add("__LDBL_EPSILON__=1.08420217248550443401e-19L");
        golden.add("__LDBL_MAX__=1.18973149535723176502e+4932L");
        golden.add("__LDBL_MIN__=3.36210314311209350626e-4932L");
        golden.add("__LONG_LONG_MAX__=0x7fffffffffffffff");
        golden.add("__DEC32_MAX__=9.999999E96DF");
        golden.add("__DEC64_MAX__=9.999999999999999E384DD");
        golden.add("__DEC128_MAX__=9.999999999999999999999999999999999E6144DL");
        golden.add("__DEC32_MANT_DIG__=7");
        golden.add("__DEC64_MANT_DIG__=16");
        golden.add("__DEC128_MANT_DIG__=34");
        golden.add("__DEC32_MIN_EXP__=-95");
        golden.add("__DEC64_MIN_EXP__=-383");
        golden.add("__DEC128_MIN_EXP__=-6143");
        golden.add("__DEC32_MAX_EXP__=96");
        golden.add("__DEC64_MAX_EXP__=384");
        golden.add("__DEC128_MAX_EXP__=6144");
        golden.add("__DEC32_EPSILON__=1E-6DF");
        golden.add("__DEC64_EPSILON__=1E-15DD");
        golden.add("__DEC128_EPSILON__=1E-33DL");
        golden.add("__DEC32_MIN__=1E-95DF");
        golden.add("__DEC64_MIN__=1E-383DD");
        golden.add("__DEC128_MIN__=1E-6143DL");
        golden.add("__DEC32_SUBNORMAL_MIN__=0.000001E-95DF");
        golden.add("__DEC64_SUBNORMAL_MIN__=0.000000000000001E-383DD");
        golden.add("__DEC128_SUBNORMAL_MIN__=0.000000000000000000000000000000001E-6143DL");
        golden.add("__VERSION__=\"Intel(R) C++ gcc 4.1 mode\"");
        golden.add("__HONOR_STD=1");
        golden.add("__ICC=1200");
        golden.add("__INTEL_COMPILER=1200");
        golden.add("_MT=1");
        golden.add("__ELF__=1");
        golden.add("__INTEL_COMPILER_BUILD_DATE=20120716");
        golden.add("__PTRDIFF_TYPE__=long");
        golden.add("__SIZE_TYPE__=unsigned long");
        golden.add("__WCHAR_TYPE__=int");
        golden.add("__WINT_TYPE__=unsigned int");
        golden.add("__INTMAX_TYPE__=long int");
        golden.add("__UINTMAX_TYPE__=long unsigned int");
        golden.add("__LONG_MAX__=9223372036854775807L");
        golden.add("__QMSPP_=1");
        golden.add("__OPTIMIZE__=1");
        golden.add("__NO_MATH_INLINES=1");
        golden.add("__NO_STRING_INLINES=1");
        golden.add("__GNUC__=4");
        golden.add("__GNUC_MINOR__=1");
        golden.add("__GNUC_PATCHLEVEL__=2");
        golden.add("__NO_INLINE__=1");
        golden.add("__i686=1");
        golden.add("__i686__=1");
        golden.add("__pentiumpro=1");
        golden.add("__pentiumpro__=1");
        golden.add("__pentium4=1");
        golden.add("__pentium4__=1");
        golden.add("__tune_pentium4__=1");
        golden.add("__MMX__=1");
        golden.add("__LP64__=1");
        golden.add("_LP64=1");
        golden.add("__GXX_ABI_VERSION=1002");
        golden.add("__USER_LABEL_PREFIX__=");
        golden.add("__REGISTER_PREFIX__=");
        golden.add("__INTEL_RTTI__=1");
        golden.add("__SSE2__=1");
        golden.add("__SSE__=1");
        golden.add("__unix__=1");
        golden.add("__unix=1");
        golden.add("__linux__=1");
        golden.add("__linux=1");
        golden.add("__gnu_linux__=1");
        golden.add("unix=1");
        golden.add("linux=1");
        golden.add("__x86_64=1");
        golden.add("__x86_64__=1");
        Collections.<String>sort(golden);
        
        result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        for(int i = 0; i < Math.min(golden.size(), out.size()); i++) {
            String s1 = golden.get(i);
            String s2 = out.get(i);
            int k = s1.indexOf('=');
            String key1;
            String val1;
            if (k > 0) {
                key1 = s1.substring(0,k);
                val1= s1.substring(k+1);
            } else {
                key1 = s1;
                val1= null;
            }
            k = s2.indexOf('=');
            String key2;
            String val2;
            if (k > 0) {
                key2 = s2.substring(0,k);
                val2= s2.substring(k+1);
            } else {
                key2 = s2;
                val2= null;
            }
            if (!key1.equals(key2)) {
                System.out.println("Macros diff "+golden.get(i)+" != "+out.get(i));
                assert false;
            } else {
                if (val1 == null && val2 == null) {
                    // equals
                    continue;
                }
                if (val1 == null && "1".equals(val2) ||
                    "1".equals(val1) && val2 == null) {
                    // equals
                    continue;
                }
                if (val1 != null && val2 != null && val1.equals(val2)) {
                    // equals
                    continue;
                }
                System.out.println("Macros diff "+golden.get(i)+" != "+out.get(i));
                assert false;
            }
        }
        assert golden.size() == out.size();
    }
    
    private static final class MyGNUCCCompiler  extends GNUCCCompiler {
        CompilerDefinitions pair = new CompilerDefinitions();
        protected MyGNUCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
            super(env, flavor, kind, name, displayName, path);
        }
        @Override
        protected String normalizePath(String path) {
            return path;
        }
    }
}
