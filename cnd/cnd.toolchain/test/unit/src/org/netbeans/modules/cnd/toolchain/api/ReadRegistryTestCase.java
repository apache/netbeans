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

import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.BaseFolder;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;

/**
 *
 */
public class ReadRegistryTestCase extends NbTestCase {

    private static final boolean TRACE = false;

    public ReadRegistryTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCygwin() throws Exception {
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        assertNotNull(d);
        assertTrue("Cygwin".equals(d.getName()));
        List<BaseFolder> list = d.getBaseFolders();
        assertNotNull(list);
        assertTrue(list.size()>1);
        BaseFolder folder = list.get(1);
        String base = folder.getFolderPattern();
        assertNotNull(base);
        if (TRACE) {
            System.out.println("Search for [" + base + "]");
        }
        Pattern p = Pattern.compile(base);
        String result = null;
        for (String line : getCygwinRegestry().split("\n")) {
            Matcher m = p.matcher(line);
            if (m.find() && m.groupCount() == 1) {
                result = m.group(1).trim();
                if (TRACE) {
                    System.out.println("Found [" + result + "]");
                }
            }
        }
        assertNotNull(result);
        result += "\\" + folder.getFolderSuffix();
        assertTrue("D:\\cygwin\\bin".equals(result));
        if (TRACE) {
            System.out.println("Compiler path [" + result + "]");
        }
        p = Pattern.compile(folder.getFolderPathPattern(), Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher(result).find());
    }

    public void testCygwin17() throws Exception {
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        assertNotNull(d);
        assertTrue("Cygwin".equals(d.getName()));
        List<BaseFolder> list = d.getBaseFolders();
        assertNotNull(list);
        assertTrue(list.size()>1);
        BaseFolder folder = list.get(0);
        String base = folder.getFolderPattern();
        assertNotNull(base);
        if (TRACE) {
            System.out.println("Search for [" + base + "]");
        }
        Pattern p = Pattern.compile(base);
        String result = null;
        for (String line : getCygwin17Regestry().split("\n")) {
            Matcher m = p.matcher(line);
            if (m.find() && m.groupCount() == 1) {
                result = m.group(1).trim();
                if (TRACE) {
                    System.out.println("Found [" + result + "]");
                }
            }
        }
        assertNotNull(result);
        result += "\\" + folder.getFolderSuffix();
        assertTrue("C:\\cygwin17\\bin".equals(result));
        if (TRACE) {
            System.out.println("Compiler path [" + result + "]");
        }
        p = Pattern.compile(folder.getFolderPathPattern(), Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher(result).find());
    }

    public void testMinGW() throws Exception {
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("MinGW", PlatformTypes.PLATFORM_WINDOWS);
        assertNotNull(d);
        assertTrue("MinGW".equals(d.getName()));
        List<BaseFolder> list = d.getBaseFolders();
        assertNotNull(list);
        assertTrue(list.size()>0);
        String base = list.get(0).getFolderPattern();
        assertNotNull(base);
        if (TRACE) {
            System.out.println("Search for [" + base + "]");
        }
        Pattern p = Pattern.compile(base);
        String result = null;
        for (String line : getMingwRegestry().split("\n")) {
            Matcher m = p.matcher(line);
            if (m.find() && m.groupCount() == 1) {
                result = m.group(1).trim();
                if (TRACE) {
                    System.out.println("Found [" + result + "]");
                }
            }
        }
        assertNotNull(result);
        result += "\\" + list.get(0).getFolderSuffix();
        assertTrue("d:\\MinGW\\bin".equals(result));
        if (TRACE) {
            System.out.println("Compiler path [" + result + "]");
        }
        p = Pattern.compile(list.get(0).getFolderPathPattern(), Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher(result).find());

        List<BaseFolder> list2 = d.getCommandFolders();
        assertNotNull(list2);
        assertTrue(list2.size()>0);
        String command = list2.get(0).getFolderPattern();
        assertNotNull(command);
        if (TRACE) {
            System.out.println("Search for [" + command + "]");
        }
        p = Pattern.compile(command);
        result = null;
        for (String line : getMsysRegestry().split("\n")) {
            Matcher m = p.matcher(line);
            if (m.find() && m.groupCount() == 1) {
                result = m.group(1).trim();
                if (TRACE) {
                    System.out.println("Found [" + result + "]");
                }
            }
        }
        assertNotNull(result);
        result += "\\" + list2.get(0).getFolderSuffix();
        assertTrue("d:\\msys\\1.0\\bin".equals(result));
        if (TRACE) {
            System.out.println("Command path [" + result + "]");
        }
        p = Pattern.compile(list2.get(0).getFolderPathPattern(), Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher(result).find());
    }

// Commented becouse not supported windows flags and commands.
//    public void testIntel() throws Exception {
//        ToolchainDescriptor d = ToolchainManager.getInstance().getToolchain("Intel", PlatformTypes.PLATFORM_WINDOWS);
//        assertTrue("Intel".equals(d.getName()));
//        assertNotNull(d);
//        String base = d.getBaseFolderPattern();
//        assertNotNull(base);
//        System.out.println("Search for ["+base+"]");
//        Pattern p = Pattern.compile(base);
//        String result = null;
//        for(String line : getIntelRegestry().split("\n")){
//           Matcher m = p.matcher(line);
//           if (m.find() && m.groupCount()==1){
//               result = m.group(1).trim();
//               System.out.println("Found ["+result+"]");
//           }
//        }
//        assertNotNull(result);
//        result += "\\"+d.getBaseFolderSuffix();
//        assertTrue("C:\\Program Files\\Intel\\Compiler\\C++\\10.1.021\\IA32\\Bin".equals(result));
//        System.out.println("Compiler path ["+result+"]");
//        p = Pattern.compile(d.getBaseFolderPathPattern(), Pattern.CASE_INSENSITIVE);
//        assertTrue(p.matcher(result).find());
//    }
    public void testGNU() throws Exception {
        String[] DEVELOPMENT_MODE_OPTIONS = {
            "", // Fast Build // NOI18N
            "-g", // Debug" // NOI18N
            "-g -O", // Performance Debug" // NOI18N
            "-g", // Test Coverage // NOI18N
            "-g -O2", // Dianosable Release // NOI18N
            "-O2", // Release // NOI18N
            "-O3", // Performance Release // NOI18N
        };
        String[] WARNING_LEVEL_OPTIONS = {
            "-w", // No Warnings // NOI18N
            "", // Default // NOI18N
            "-Wall", // More Warnings // NOI18N
            "-Werror", // Convert Warnings to Errors // NOI18N
        };
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        CompilerDescriptor c = d.getC();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
        c = d.getCpp();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
        c = d.getFortran();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
    }

    public void testSunStudioC() throws Exception {
        String[] DEVELOPMENT_MODE_OPTIONS = {
            "", // Fast Build // NOI18N
            "-g", // Debug" // NOI18N
            "-g -xO2", // Performance Debug" // NOI18N
            "-xprofile=tcov -xinline=", // Test Coverage // NOI18N
            "-g -O", // Dianosable Release // NOI18N
            "-fast -g", // Release // NOI18N
            "-fast -xipo -g", // Performance Release // NOI18N
        };
        String[] WARNING_LEVEL_OPTIONS = {
            "-w", // No Warnings // NOI18N
            "", // Default // NOI18N
            "+w", // More Warnings // NOI18N
            "-errwarn=%all", // Convert Warnings to Errors // NOI18N
        };
        String[] MT_LEVEL_OPTIONS = {
            "", // None // NOI18N
            "-mt", // Safe // NOI18N
            "-xautopar -xvector -xreduction -xloopinfo -O3", // Automatic // NOI18N
            "-xopenmp -O3", // Open MP // NOI18N
        };
        String[] STANDARD_OPTIONS = {
            "-xc99=none", // Old // NOI18N
            "-xc99=none", // Legacy // NOI18N
            "", // Default // NOI18N
            "-xstrconst -xc99", // Modern // NOI18N
        };
        String[] LANGUAGE_EXT_OPTIONS = {
            "-Xc", // None // NOI18N
            "", // Default // NOI18N
            "", // All // NOI18N
        };
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertNotNull(d);
        assertTrue("OracleDeveloperStudio".equals(d.getName()));
        CompilerDescriptor c = d.getC();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
        assertTrue(checkArrays(MT_LEVEL_OPTIONS, c.getMultithreadingFlags()));
        assertTrue(checkArrays(STANDARD_OPTIONS, c.getStandardFlags()));
        assertTrue(checkArrays(LANGUAGE_EXT_OPTIONS, c.getLanguageExtensionFlags()));
    }

    public void testSunStudioCpp() throws Exception {
        String[] DEVELOPMENT_MODE_OPTIONS = {
            "", // Fast Build // NOI18N
            "-g", // Debug" // NOI18N
            "-g0 -xO2", // Performance Debug" // NOI18N
            "-xprofile=tcov +d -xinline=", // Test Coverage // NOI18N
            "-g0 -O", // Dianosable Release // NOI18N
            "-fast -g0", // Release // NOI18N
            "-fast -xipo -g0", // Performance Release // NOI18N
        };
        String[] WARNING_LEVEL_OPTIONS = {
            "-w", // No Warnings // NOI18N
            "", // Default // NOI18N
            "+w", // More Warnings // NOI18N
            "-xwe", // Convert Warnings to Errors // NOI18N
        };
        String[] LIBRARY_LEVEL_OPTIONS = {
            "-library=no%Cstd,no%Crun -filt=no%stdlib", // NOI18N
            "-library=no%Cstd -filt=no%stdlib", // NOI18N
            "-library=iostream,no%Cstd -filt=no%stdlib", // NOI18N
            "", // NOI18N
            "-library=stlport4,no%Cstd", // NOI18N
        };
        String[] MT_LEVEL_OPTIONS = {
            "", // None // NOI18N
            "-mt", // Safe // NOI18N
            "-xautopar -xvector -xreduction -xloopinfo -O3", // Automatic // NOI18N
            "-xopenmp -O3", // Open MP // NOI18N
        };
        String[] STANDARD_OPTIONS = {
            "-compat", // Old // NOI18N
            "-features=no%localfor,no%extinl,no%conststrings", // Legacy // NOI18N
            "", // Default // NOI18N
            "-features=no%anachronisms,no%transitions,tmplife", // Modern // NOI18N
        };
        String[] LANGUAGE_EXT_OPTIONS = {
            "-features=no%longlong", // None // NOI18N
            "", // Default // NOI18N
            "-features=extensions,tmplrefstatic,iddollar", // All // NOI18N
        };
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertNotNull(d);
        assertTrue("OracleDeveloperStudio".equals(d.getName()));
        CompilerDescriptor c = d.getCpp();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
        assertTrue(checkArrays(MT_LEVEL_OPTIONS, c.getMultithreadingFlags()));
        assertTrue(checkArrays(STANDARD_OPTIONS, c.getStandardFlags()));
        assertTrue(checkArrays(LANGUAGE_EXT_OPTIONS, c.getLanguageExtensionFlags()));
    }

    public void testSunStudioFortran() throws Exception {
        String[] DEVELOPMENT_MODE_OPTIONS = {
            "", // Fast Build // NOI18N
            "-g", // Debug" // NOI18N
            "-g -O2", // Performance Debug" // NOI18N
            "-g", // Test Coverage // NOI18N
            "-g -O", // Dianosable Release // NOI18N
            "-fast -g", // Release // NOI18N
            "-fast -g", // Performance Release // NOI18N
        };
        String[] WARNING_LEVEL_OPTIONS = {
            "-w", // No Warnings // NOI18N
            "-w1", // Default // NOI18N
            "-w2", // More Warnings // NOI18N
            "-errwarn", // Convert Warnings to Errors // NOI18N
        };
        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("OracleDeveloperStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertNotNull(d);
        CompilerDescriptor c = d.getFortran();
        assertNotNull(c);
        assertTrue(checkArrays(DEVELOPMENT_MODE_OPTIONS, c.getDevelopmentModeFlags()));
        assertTrue(checkArrays(WARNING_LEVEL_OPTIONS, c.getWarningLevelFlags()));
    }

    public void testUnknownService() throws Exception {
        ToolchainDescriptor d;
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_SOLARIS_SPARC);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_WINDOWS);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_MACOSX);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_GENERIC);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));
        d = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_NONE);
        assertNotNull(d);
        assertTrue("GNU".equals(d.getName()));

        CompilerFlavor f;
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_SOLARIS_SPARC);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_SOLARIS_INTEL);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_LINUX);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_WINDOWS);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_MACOSX);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_GENERIC);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
        f = CompilerFlavor.getUnknown(PlatformTypes.PLATFORM_NONE);
        assertNotNull(f);
        assertTrue("Unknown".equals(f.getToolchainDescriptor().getName()));
        assertTrue(f.isGnuCompiler());
    }

    public void testVersionPatternIntel() throws Exception {
        String output =
                "Intel(R) C++ Compiler for applications running on IA-32, Version 10.1    Build 20080312 Package ID: w_cc_p_10.1.021\n" +
                "Copyright (C) 1985-2008 Intel Corporation.  All rights reserved.\n" +
                "30 DAY EVALUATION LICENSE\n" +
                "\n" +
                "icl: NOTE: The evaluation period for this product ends on 9-aug-2008 UTC.\n" +
                "icl: command line error: no files specified; for help type \"icl /help\"\n";
        Pattern pattern = Pattern.compile(".*Intel\\(R\\) C\\+\\+ Compiler");
        assertTrue(pattern.matcher(output).find());
    }

//    public void testVersionPatternCygwin() throws Exception {
//        String output = "gcc-4 (GCC) 4.3.2 20080827 (beta) 2\n"+
//        "Copyright (C) 2008 Free Software Foundation, Inc.\n"+
//        "This is free software; see the source for copying conditions.  There is NO\n"+
//        "warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n";
////gcc-4 (GCC) 4.3.2 20080827 (beta) 2
////Copyright (C) 2008 Free Software Foundation, Inc.
////This is free software; see the source for copying conditions.  There is NO
////warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//        ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
//        assertNotNull(d);
//        String s = d.getC().getVersionPattern();
////.*\\(GCC\\) 4\\.[3-9]
//        Pattern pattern = Pattern.compile(s);
//        assertTrue(pattern.matcher(output).find());
//        output = "  (GCC) 4.2.3\n";
//        pattern = Pattern.compile(".*\\(GCC\\) 4\\.[3-9]");
//        assertFalse(pattern.matcher(output).find());
//    }

    public void testIntelErrorPattern() throws Exception {
        String output =
                "icpc    -c -g -o build/Debug/Intel-Linux-x86/main.o main.cc\n" +
                "/usr/include/c++/4.1.3/backward/backward_warning.h(32): warning #1224: #warning directive: This file includes at least one deprecated or antiquated header. Please consider using one of the 32 headers found in section 17.4.1.2 of the C++ standard. Examples include substituting the <X> header for the <X.h> header for C++ includes, or <iostream> instead of the deprecated header <iostream.h>. To disable this warning use -Wno-deprecated.\n" +
                " #warning This file includes at least one deprecated or antiquated header. \\\n" +
                "  ^\n" +
                "\n" +
                "main.cc(56): error: identifier \"lll\" is undefined\n" +
                "     lll\n" +
                "     ^\n" +
                "\n" +
                "main.cc(58): error: expected a \";\"\n" +
                "     if (argc > 1) {\n" +
                "     ^\n" +
                "\n" +
                "main.cc(65): warning #12: parsing restarts here after previous syntax error\n" +
                "     return 0;\n" +
                "             ^\n" +
                "\n" +
                "compilation aborted for main.cc (code 2)\n" +
                "make[1]: *** [build/Debug/Intel-Linux-x86/main.o] Error 2\n" +
                "make[1]: Leaving directory `/set/ide/mars/NetBeansProjects/HelloApp_2/helloapp'\n" +
                "make: *** [.build-impl] Error 2\n" +
                "\n" +
                "Build failed. Exit value 2.\n";
        Pattern pattern1 = Pattern.compile("([a-zA-Z]:[^:\n]*|[^:\n]*):([^:\n]*):([^:\n]*):([^\n]*)");
        Pattern pattern2 = Pattern.compile("([^:\n]*):([0-9]+): ([a-zA-Z]*):*.*");
        Pattern pattern3 = Pattern.compile("([^\\(\n]*)\\(([0-9]+)\\): ([^:\n]*): ([^\n]*)");
        String golden = "/usr/include/c++/4.1.3/backward/backward_warning.h(32);main.cc(56);main.cc(58);main.cc(65);";
        StringBuilder buf = new StringBuilder();
        for (String s : output.split("\n")) {
            buf.append(getFile(pattern1, "1", s));
            buf.append(getFile(pattern2, "2", s));
            buf.append(getFile(pattern3, "3", s));
        }
        assertTrue(golden.equals(buf.toString()));
    }

    private String getFile(Pattern pattern, String prefix, String s) {
        Matcher m = pattern.matcher(s);
        if (m.find()) {
            int i = 0;
            try {
                i = Integer.valueOf(m.group(2));
            } catch (NumberFormatException e) {
                return "";
            }
            if (TRACE) {
                System.out.println("String " + s);
            }
            if (TRACE) {
                System.out.println("Pattern " + prefix);
            }
            if (TRACE) {
                System.out.println("\tFile " + m.group(1) + "\n\tLine " + i + "\n\tSeverity " + m.group(3) + "\n\tMessage " + m.group(4));
            }
            return m.group(1) + "(" + i + ");";
        }
        return "";
    }

    private boolean checkArrays(String[] g, String[] f) {
        if (g.length != f.length) {
            if (TRACE) {
                System.out.println("Expected length " + g.length + " found " + f.length);
            }
            return false;
        }
        for (int i = 0; i < g.length; i++) {
            if (!g[i].equals(f[i])) {
                if (TRACE) {
                    System.out.println("Expected flag[" + i + "]:\n\t[" + g[i] + "]\nfound\t[" + f[i] + "]");
                }
                return false;
            }
        }
        return true;
    }

    private String getMsysRegestry() {
        return "\n" +
                "! REG.EXE VERSION 3.0\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\microsoft\\windows\\currentversion\\uninstall\\msys-1.0_is1\n" +
                "   Inno Setup: Setup Version   REG_SZ  2.0.19\n" +
                "   Inno Setup: App Path        REG_SZ  d:\\msys\\1.0\n" +
                "   Inno Setup: Icon Group      REG_SZ  MinGW\n" +
                "   Inno Setup: User    REG_SZ  Alex\n" +
                "   Inno Setup: Setup Type      REG_SZ  i386\n" +
                "   Inno Setup: Selected Components     REG_SZ  i386\n" +
                "   Inno Setup: Deselected Components   REG_SZ\n" +
                "   DisplayName REG_SZ  \"Minimal SYStem 1.0.10\"\n" +
                "   UninstallString     REG_SZ  d:\\msys\\1.0\\uninstall\\unins000.exe\n" +
                "   DisplayVersion      REG_SZ  1.0.10\n" +
                "   Publisher   REG_SZ  MinGW\n" +
                "   URLInfoAbout        REG_SZ  http://www.mingw.org/\n" +
                "   HelpLink    REG_SZ  mailto:\n" +
                "   URLUpdateInfo       REG_SZ  http://sf.net/projects/mingw/\n" +
                "\n";

    }

    private String getMingwRegestry() {
        return "\n" +
                "! REG.EXE VERSION 3.0\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\microsoft\\windows\\currentversion\\uninstall\\MinGW\n" +
                "   NSIS:StartMenuDir   REG_SZ  MinGW\n" +
                "   DisplayName REG_SZ  MinGW 5.1.3\n" +
                "   UninstallString     REG_SZ  d:\\MinGW\\uninst.exe\n" +
                "   InstallLocation     REG_SZ  d:\\MinGW\n" +
                "   DisplayVersion      REG_SZ  5.1.3\n" +
                "   URLInfoAbout        REG_SZ  http://www.mingw.org\n" +
                "   Publisher   REG_SZ  MinGW\n" +
                "\n";
    }

    private String getCygwinRegestry() {
        return "\n" +
                "! REG.EXE VERSION 3.0\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\cygnus solutions\\cygwin\\mounts v2\\/\n" +
                "   native      REG_SZ  D:\\cygwin\n" +
                "   flags       REG_DWORD       0xa\n" +
                "\n";
    }

    private String getCygwin17Regestry() {
        return "\n" +
               "HKEY_LOCAL_MACHINE\\software\\cygwin\\Program Options\n" +
               "\n" +
               "HKEY_LOCAL_MACHINE\\software\\cygwin\\setup\n" +
               "   rootdir    REG_SZ    C:\\cygwin17\n" +
               "\n";
    }

    private String getIntelRegestry() {
        return "\n" +
                "! REG.EXE VERSION 3.0\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\\101.021\n" +
                "   Revision    REG_DWORD       0x15\n" +
                "   Major Version       REG_DWORD       0xa\n" +
                "   Minor Version       REG_DWORD       0x1\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\\101.021\\IA32\n" +
                "   ProductDir  REG_SZ  C:\\Program Files\\Intel\\Compiler\\C++\\10.1.021\\IA32\n" +
                "   BinDir      REG_SZ  $(ICInstallDir)Bin;C:\\Program Files\\Common Files\\Intel\\Shared Files\\Ia32\\Bin\n" +
                "   IncludeDir  REG_SZ  $(ICInstallDir)Include\n" +
                "   LibDir      REG_SZ  $(ICInstallDir)Lib\n" +
                "   TargetPlatform      REG_SZ  Win32\n" +
                "   DocumentationDir    REG_SZ  C:\\Program Files\\Intel\\Compiler\\C++\\10.1.021\\Docs\n" +
                "   CompilerInfo        REG_SZ\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\\101.021\\IA32\\VSNet2003\n" +
                "   DefaultOptions      REG_SZ  /Qvc7.1 /Qlocation,link,\"$(VCInstallDir)bin\"\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\\101.021\\IA32\\VSNet2005\n" +
                "   DefaultOptions      REG_SZ  /Qvc8 /Qlocation,link,\"$(VCInstallDir)bin\"\n" +
                "\n" +
                "HKEY_LOCAL_MACHINE\\software\\INTEL\\Compilers\\C++\\101.021\\IA32\\VSNet2008\n" +
                "   DefaultOptions      REG_SZ  /Qvc9 /Qlocation,link,\"$(VCInstallDir)bin\"\n" +
                "\n";
    }
}
