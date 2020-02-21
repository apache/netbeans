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
package org.netbeans.modules.cnd.toolchain.api;

import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;

/**
 */
public class ScannerTestCase extends NbTestCase {

    public ScannerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testGNUpatterns() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "lifedialog.cpp:458: warning: comparison between signed and unsigned integer expressions";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("lifedialog.cpp"));
                assertTrue(m.group(2).equals("458"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }

    public void testGNUpatterns_01() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "../src/CLucene/config/gunichartables.cpp:132:3: warning: #warning \"===== Using internal character function =====\"";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("../src/CLucene/config/gunichartables.cpp"));
                assertTrue(m.group(2).equals("132"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }
    public void testGNUpatterns_02() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.cc:41: warning: ISO C++ forbids declaration of `main' with no type";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cc"));
                assertTrue(m.group(2).equals("41"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }
    public void testGNUpatterns_03() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.cc:53: error: 'gtk_main' was not declared in this scope";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cc"));
                assertTrue(m.group(2).equals("53"));
                assertTrue(m.group(3).indexOf("error")>=0);
                break;
            }
        }
        assertTrue(find);
    }

    public void testGNUpatterns_04() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
        ArrayList<String> testPatterns = new ArrayList<String>();
        testPatterns.add("cd ../Quote_7 && /usr/sfw/bin/gmake -f Makefile");
      	testPatterns.add("cd ../Quote_7&& /usr/sfw/bin/gmake -f Makefile");
      	testPatterns.add("cd ../Quote_7 ; /usr/sfw/bin/gmake -f Makefile");
      	testPatterns.add("cd ../Quote_7; /usr/sfw/bin/gmake -f Makefile");
      	testPatterns.add("cd ../Quote_7");
        String p = scanner.getChangeDirectoryPattern();
        String p2 = "cd\\s+((\"[^\"]*\"|'[^']*'|\\.|[^\\s;&])+)";
        assertEquals(p, p2);
        Pattern pattern = Pattern.compile(p);
        for (String s : testPatterns) {
            Matcher m = pattern.matcher(s);
            if (m.find()){
                assertEquals(trimQuotes(m.group(1)), "../Quote_7");
            } else {
                assertTrue("String "+s+" does not match pattern "+p, false);
            }
        }
    }

    public void testGNUpatterns_05() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
        ArrayList<String> testPatterns = new ArrayList<String>();
      	testPatterns.add("cd \"../Quote 7\"");
      	testPatterns.add("cd '../Quote 7'");
        String p = scanner.getChangeDirectoryPattern();
        String p2 = "cd\\s+((\"[^\"]*\"|'[^']*'|\\.|[^\\s;&])+)";
        assertEquals(p, p2);
        Pattern pattern = Pattern.compile(p);
        for (String s : testPatterns) {
            Matcher m = pattern.matcher(s);
            if (m.find()){
                assertEquals(trimQuotes(m.group(1)), "../Quote 7");
            } else {
                assertTrue("String "+s+" does not match pattern "+p, false);
            }
        }
    }
    
    public void testGNUpatterns_05_1() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
        ArrayList<String> testPatterns = new ArrayList<String>();
      	testPatterns.add("gmake[1]: Entering directory '/export/home/hudson/.netbeans/remote/enum/beta-SunOS-x86_64/var/tmp/alex-cnd-test-downloads/pkg-config-0.25'");
      	testPatterns.add("gmake[1]: Entering directory `/export/home/hudson/.netbeans/remote/enum/beta-SunOS-x86_64/var/tmp/alex-cnd-test-downloads/pkg-config-0.25'");
        String p = scanner.getEnterDirectoryPattern();
        String p2 = ".*make(?:\\.exe)?(?:\\[([0-9]+)\\])?: Entering[\\w+\\s+]+[`|']([^']*)'";
        assertEquals(p, p2);
        Pattern pattern = Pattern.compile(p);
        for (String s : testPatterns) {
            Matcher m = pattern.matcher(s);
            if (m.find()){
                assertEquals(trimQuotes(m.group(2)), "/export/home/hudson/.netbeans/remote/enum/beta-SunOS-x86_64/var/tmp/alex-cnd-test-downloads/pkg-config-0.25");
            } else {
                assertTrue("String "+s+" does not match pattern "+p, false);
            }
        }
    }

    public void testGNUpatterns_06() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
        ArrayList<String> testPatterns = new ArrayList<String>();
      	testPatterns.add("                 from ../ccutil/platform.h:9,");
        for (String s : testPatterns) {
            boolean found = false;
            for (String p : scanner.getStackNextPattern()) {
                Pattern pattern = Pattern.compile(p);
                Matcher m = pattern.matcher(s);
                if (m.find()) {
                    assertEquals(trimQuotes(m.group(1)), "../ccutil/platform.h");
                    found = true;
                    break;
                }
            }
            assertTrue("String " + s + " does not match stack next patterns", found);
        }
    }
    
    public void testGNUpatterns_07() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
        ArrayList<String> testPatterns = new ArrayList<String>();
      	//testPatterns.add("../sdl.h: In member function 'void std::vector<_Tp, _Alloc>::resize(size_t, _Tp) [with _Tp = sdl::SdlTlink, _Alloc = std::allocator<sdl::SdlTlink>]':");
      	testPatterns.add("../sdl.h:94:   instantiated from 'Tid sdl::SdlTable<Telem, Tid>::AddElement() [with Telem = sdl::SdlTlink, Tid = sdl::Id<151>]'");
      	testPatterns.add("../sdlAct.C:630:   instantiated from here");
        testPatterns.add("main.cpp:15:89:   required from here");
        testPatterns.add("/tools/GCC/4.7.2/include/c++/4.7.2/bits/stl_algobase.h:1035:37:   required from 'bool std::equal(_II1, _II1, _II2) [with _II1 = __gnu_cxx::__normal_iterator<const char*, std::basic_string<char> >; _II2 = std::basic_string<char>]'");
        testPatterns.add("/tools/gcc/4.5.3/intel-S2/include/c++/4.5.3/bits/stl_algobase.h:832:58:   instantiated from 'bool std::__equal_aux(_II1, _II1, _II2) [with _II1 = const char*, _II2 = std::basic_string<char>]'");
        testPatterns.add("main.cpp:5:   instantiated from here");
        for (String s : testPatterns) {
            //System.err.println("Line:    "+s);
            boolean found = false;
            String name = null;
            for(String p : scanner.getStackHeaderPattern()) {
                Pattern pattern = Pattern.compile(p);
                Matcher m = pattern.matcher(s);
                if (m.matches()) {
                    name = m.group(1).trim();
                    found = true;
                    break;
                }
            }
            if (!found) {
                for(String p : scanner.getStackNextPattern()) {
                    Pattern pattern = Pattern.compile(p);
                    Matcher m = pattern.matcher(s);
                    if (m.matches()) {
                        name = m.group(1).trim();
                        found = true;
                        break;
                    }
                }
            }
            assertTrue("String " + s + " does not match stack patterns", found);
        }
    }

    public void testGNUpatterns_08() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.f90:1.29:";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.f90"));
                assertTrue(m.group(2).equals("1"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }

    public void testGNUpatterns_09() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.f90:2:";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.f90"));
                assertTrue(m.group(2).equals("2"));
                assertTrue(m.groupCount()==2);
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatternAten() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleSolarisStudio_12.2", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertTrue(Pattern.compile(toolchain.getCpp().getVersionPattern()).matcher("CC: Sun C++ 5.11 SunOS_i386 2010/06/09").find());
        assertTrue(Pattern.compile(toolchain.getC().getVersionPattern()).matcher("C: Sun C 5.11 SunOS_i386 2010/06/09").find());
        assertTrue(Pattern.compile(toolchain.getCpp().getVersionPattern()).matcher("CC: Sun C++ 5.11 SunOS_i386 Aten 2010/06/09").find());
        assertTrue(Pattern.compile(toolchain.getC().getVersionPattern()).matcher("C: Sun C 5.11 SunOS_i386 Aten 2010/06/09").find());
    }

    public void testSUNpatterns() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"life.cpp\", line 550: Warning: prior hides Life::prior.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("life.cpp"));
                assertTrue(m.group(2).equals("550"));
                assertEquals("warning", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_01() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"../src/CLucene/util/bufferedstream.h\", line 96:     Where: Instantiated from jstreams::BufferedInputStream<char>::read(const char*&, int, int).";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("../src/CLucene/util/bufferedstream.h"));
                assertTrue(m.group(2).equals("96"));
                assertEquals("warning", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_02() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"./CLucene/util/inputstreambuffer.h\", line 45: Error: Non-virtual function jstreams::InputStreamBuffer<char>::read(const char*&, int) declared pure.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("./CLucene/util/inputstreambuffer.h"));
                assertTrue(m.group(2).equals("45"));
                assertEquals("error", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_03() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"main.cpp\", 行 13: 警告: #warning 日本.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cpp"));
                assertTrue(m.group(2).equals("13"));
                assertEquals("error", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_04() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"main.cpp\", 行 14: エラー: #error 日本.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cpp"));
                assertTrue(m.group(2).equals("14"));
                assertEquals("error", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }


    public void testMSVCpatterns() throws Exception {
	String s = "../../../hbver.c(308) : error C2039: 'wProductType' : is not a member of";
	Pattern pattern = Pattern.compile("^([^\\($]*)\\(([0-9]+)\\) : ([^:$]*):([^$]*)"); // NOI18N
	Matcher m = pattern.matcher(s);
	assertTrue(m.matches());
	assertTrue(m.group(1).equals("../../../hbver.c"));
	assertTrue(m.group(2).equals("308"));
	assertTrue(m.group(3).indexOf("error")>=0);
    }

    public void testCygwinLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testArmLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testDJGPPLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_WINDOWS);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testGnuFortranLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testGnuCluceneLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testMSVCLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testGnuClearmakeLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    private String trimQuotes(String s){
        if (s.length()>2) {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                return s.substring(1, s.length()-1);
            }else if (s.startsWith("'") && s.endsWith("'")) {
                return s.substring(1, s.length()-1);
            }
        }
        return s;
    }

    private void doTest(File logFile, ScannerDescriptor scanner, PrintStream ref) throws Exception {
        BufferedReader reader = Files.newBufferedReader(logFile.toPath(), Charset.forName("UTF-8"));
        String line;
        int lineCnt = 0;
        while ((line = reader.readLine()) != null) {
            ++lineCnt;
            Pattern pattern;
            List<String> match;

            pattern = Pattern.compile(scanner.getChangeDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " changeDirectory { dir: " + match.get(0) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getMakeAllInDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " makeAllInDirectory { dir: " + match.get(0) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getEnterDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " enterDirectory { depth: " + getInt(match.get(0)) + "; dir: " + match.get(1) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getLeaveDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " leaveDirectory { depth: " + getInt(match.get(0)) + "; dir: " + match.get(1) + " }");
                continue;
            }

            for(String p: scanner.getStackHeaderPattern()) {
                pattern = Pattern.compile(p);
                if ((match = match(pattern, line)) != null) {
                    ref.println(lineCnt + " stackHeader { file: " + match.get(0) + " }");
                    continue;
                }
            }

            for(String p: scanner.getStackNextPattern()) {
                pattern = Pattern.compile(p);
                if ((match = match(pattern, line)) != null) {
                    ref.println(lineCnt + " stackNext { file: " + match.get(0) + " }");
                    continue;
                }
            }

            for (ScannerPattern scannerPattern : scanner.getPatterns()) {
                pattern = Pattern.compile(scannerPattern.getPattern());
                if ((match = match(pattern, line)) != null) {
                    ref.println(lineCnt + " " + scannerPattern.getSeverity() + " { file: " + match.get(0) + "; line: " + getInt(match.get(1)) + " }");
                    break;
                }
            }
        }
        compareReferenceFiles();
    }

    private List<String> match(Pattern pattern, String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < m.groupCount(); ++i) {
                list.add(m.group(i + 1));
            }
            return list;
        } else {
            return null;
        }
    }

    private int getInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private File getLogs() {
        String fullClassName = this.getClass().getName();
        String logFileName = fullClassName.replace('.', '/') + '/' + getName() + ".dat";
        return new File(getDataDir(), logFileName);
    }
}
