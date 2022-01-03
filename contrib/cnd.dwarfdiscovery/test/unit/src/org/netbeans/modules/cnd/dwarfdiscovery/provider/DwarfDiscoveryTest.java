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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.openide.util.Utilities;

/**
 *
 */
public class DwarfDiscoveryTest  extends NbTestCase {

    public DwarfDiscoveryTest() {
        super("DwarfDiscoveryTest");
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        System.getProperties().put("cnd.dwarfdiscovery.trace.read.errors",Boolean.TRUE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        unzipTestData();
    }

    private void unzipTestData() throws Exception  {
        File dataDir = getDataDir();
        String zip = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/data.zip";
        ZipInputStream in = new ZipInputStream(new FileInputStream(zip));
        while (true) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }
            String outFilename = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/"+entry.getName();
            if (entry.isDirectory()) {
                File f = new File(outFilename);
                if (!f.exists()) {
                    f.mkdir();
                }
                continue;
            }
            if (new File(outFilename).exists()) {
                continue;
            }
            OutputStream out = new FileOutputStream(outFilename);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        }
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testDll_RHEL55_x64_gcc() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "libhello3lib.so", "libhello4lib.so","libstdc++.so.6", "libm.so.6", "libgcc_s.so.1", "libc.so.6");
    }

    public void testDll_Sparc64_studio() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/Subproject_sparc64/main/dist/Debug/OracleSolarisStudio-Solaris-Sparc/main",
                "libhello3lib.so", "libhello4lib.so","libCstd.so.1", "libCrun.so.1", "libm.so.2", "libc.so.1");
    }

    public void testDll_Ubuntu1010_x64_gcc() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "libhello3lib.so", "libhello4lib.so","libstdc++.so.6", "libm.so.6", "libgcc_s.so.1", "libc.so.6");
    }

    public void testDll_macosx32() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/dist/Debug/GNU-MacOSX/main",
                "libhello3lib.dylib", "libhello4lib.dylib", "/usr/lib/libstdc++.6.dylib", "/usr/lib/libSystem.B.dylib");
    }

    public void testDll_macosx64() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/dist/Debug/GNU-MacOSX/main",
                "libhello3lib.dylib", "libhello4lib.dylib", "/usr/lib/libstdc++.6.dylib", "/usr/lib/libSystem.B.dylib");
    }

    public void testDll_windowsxp_mingw() {
        if (Utilities.isMac()) {
            /* TODO: Mac test mashine does not like executable compiled on windows by mingw.
             * Not reproduced on other avaliable macs. Issue should be investigated.
             * Test temporary disabled.
             */
            return;
        }
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/main/dist/Debug/MinGW-Windows/main.exe",
                "libhello3lib.dll", "libhello4lib.dll");
    }

    public void testDll_windows7_cygwin() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe",
                "libhello3lib.dll", "libhello4lib.dll", "cygwin1.dll");
    }

    public void testApplicable_RHEL55_x64_gcc() {
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "GNU C++ 4.1.2 20080704 (Red Hat 4.1.2-48)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc", 39);
    }

    public void testApplicable_Ubuntu1010_x64_gcc() {
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "GNU C++ 4.4.5",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc", 37);
    }

    public void testApplicable_macosx32() {
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/dist/Debug/GNU-MacOSX/main",
                "GNU C++ 4.2.1 (Apple Inc. build 5664)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32", 39);
    }

    public void testApplicable_macosx64() {
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/dist/Debug/GNU-MacOSX/main",
                "GNU C++ 4.2.1 (Apple Inc. build 5664)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64", 39);
    }

    public void testApplicable_windows7_cygwin() {
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe",
                "GNU C++ 3.4.4 (cygming special, gdc 0.12, using dmd 0.125)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin", 39);
    }

    public void testApplicable_windowsxp_mingw() {
        if (Utilities.isMac()) {
            /* TODO: Mac test mashine does not like executable compiled on windows by mingw.
             * Not reproduced on other avaliable macs. Issue should be investigated.
             * Test temporary disabled.
             */
            return;
        }
        applicable("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/main/dist/Debug/MinGW-Windows/main.exe",
                "GNU C++ 3.4.5 (mingw-vista special r3)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw", 39);
    }

    public void testStatic_RHEL55_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/hello1lib/dist/Debug/GNU-Linux-x86/libhello1lib.a",
                "hello1.cc");
    }

    public void testStatic_Ubuntu1010_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/hello1lib/dist/Debug/GNU-Linux-x86/libhello1lib.a",
                "hello1.cc");
    }

    public void testStatic_macosx32() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/hello1lib/dist/Debug/GNU-MacOSX/libhello1lib.a",
                "hello1.cc");
    }

    public void testStatic_macosx64() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/hello1lib/dist/Debug/GNU-MacOSX/libhello1lib.a",
                "hello1.cc");
    }

    public void testStatic_windows7_cygwin() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/hello1lib/dist/Debug/Cygwin-Windows/libhello1lib.a",
                "hello1.cc");
    }

    public void testStatic_windowsxp_mingw() {
        if (Utilities.isMac()) {
            /* TODO: Mac test mashine does not like executable compiled on windows by mingw.
             * Not reproduced on other avaliable macs. Issue should be investigated.
             * Test temporary disabled.
             */
            return;
        }
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/hello1lib/dist/Debug/MinGW-Windows/libhello1lib.a",
                "hello1.cc");
    }

    public void testShared_RHEL55_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/hello3lib/dist/Debug/GNU-Linux-x86/libhello3lib.so",
                "hello3.cc");
    }

    public void testShared_Ubuntu1010_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/hello3lib/dist/Debug/GNU-Linux-x86/libhello3lib.so",
                "hello3.cc");
    }

    public void testShared_macosx32() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/hello3lib/dist/Debug/GNU-MacOSX/libhello3lib.dylib",
                "hello3.cc");
    }

    public void testShared_macosx64() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/hello3lib/dist/Debug/GNU-MacOSX/libhello3lib.dylib",
                "hello3.cc");
    }

    public void testShared_windows7_cygwin() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/hello3lib/dist/Debug/Cygwin-Windows/libhello3lib.dll",
                "hello3.cc");
    }

    public void testShared_windowsxp_mingw() {
        if (Utilities.isMac()) {
            /* TODO: Mac test mashine does not like executable compiled on windows by mingw.
             * Not reproduced on other avaliable macs. Issue should be investigated.
             * Test temporary disabled.
             */
            return;
        }
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/hello3lib/dist/Debug/MinGW-Windows/libhello3lib.dll",
                "hello3.cc");
    }


    public void testApplication_RHEL55_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    public void testApplication_Ubuntu1010_x64_gcc() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    public void testApplication_macosx32() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/dist/Debug/GNU-MacOSX/main",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    public void testApplication_macosx64() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/dist/Debug/GNU-MacOSX/main",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    public void testApplication_windows7_cygwin() {
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    public void testApplication_windowsxp_mingw() {
        if (Utilities.isMac()) {
            /* TODO: Mac test mashine does not like executable compiled on windows by mingw.
             * Not reproduced on other avaliable macs. Issue should be investigated.
             * Test temporary disabled.
             */
            return;
        }
        readBinary("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/main/dist/Debug/MinGW-Windows/main.exe",
                "main.cc", "hello1.cc", "hello2.cc");
    }

    private void applicable(String path, String compiler, String root, int mainLine) {
        AnalyzeExecutable provider = new AnalyzeExecutable();
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+path;
        root = dataDir.getAbsolutePath().replace('\\', '/')+root;
        assertTrue(new File(objFileName).exists());
        ProviderPropertyType.ExecutablePropertyType.setProperty(provider, objFileName);
        Applicable canAnalyze = provider.canAnalyze(new ProjectProxy() {

            @Override
            public boolean createSubProjects() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Project getProject() {
                return null;
            }

            @Override
            public String getMakefile() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getSourceRoot() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getExecutable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getWorkingFolder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean mergeProjectProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean resolveSymbolicLinks() {
                return false;
            }
        }, null);
        String compilerName = canAnalyze.getCompilerName();
        String sourceRoot = canAnalyze.getSourceRoot();
        Position mainFunctionPosition = canAnalyze.getMainFunction();
        //System.err.println(compilerName);
        //System.err.println(sourceRoot);
        //System.err.println(mainFunctionPosition);
        if (canAnalyze.getErrors() != null && canAnalyze.getErrors().size() > 0) {
            for(String error : canAnalyze.getErrors()) {
                //System.err.print(error);
            }
            assert true;
        }
        assertEquals(compiler, compilerName);
        assertEquals(root, sourceRoot);
        assertNotNull(mainFunctionPosition);
        assertTrue(mainFunctionPosition.getFilePath().replace('\\', '/').startsWith(root));
        assertEquals(mainLine, mainFunctionPosition.getLine());
        assertTrue(canAnalyze.isApplicable());
        assertTrue(canAnalyze.getDependencies().size()>=2);
    }


    private void readBinary(String path, String ... sources) {
        AnalyzeExecutable provider = new AnalyzeExecutable();
        final File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+path;
        assertTrue(new File(objFileName).exists());
        ProviderPropertyType.ExecutablePropertyType.setProperty(provider, objFileName);
        List<Configuration> analyze = provider.analyze(new ProjectProxy() {

            @Override
            public boolean createSubProjects() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Project getProject() {
                return null;
            }

            @Override
            public String getMakefile() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getSourceRoot() {
                return null;
            }

            @Override
            public String getExecutable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getWorkingFolder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean mergeProjectProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean resolveSymbolicLinks() {
                return false;
            }
        }, null, null);
        assertEquals(1, analyze.size());
        Configuration conf = analyze.get(0);
        assertEquals(sources.length, conf.getSourcesConfiguration().size());
        for (SourceFileProperties file : conf.getSourcesConfiguration()) {
            //System.err.println(file.getItemPath());
            boolean match = false;
            for(String x : sources) {
                if (x.equals(file.getItemName())) {
                    match = true;
                }
            }
            assertTrue(match);
        }
    }

    private void dumpDlls(String path, String...dlls) {
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+path;
        assertTrue(new File(objFileName).exists());
        SharedLibraries res =  null;
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            res = dump.readPubNames();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (WrongFileFormatException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        assertNotNull(res);
        //System.err.println(res);
        assertEquals(dlls.length, res.getDlls().size());
        int i = 0;
        for(String dll: res.getDlls()) {
            assertEquals(dlls[i], dll);
            i++;
        }
        //for(String searchPath: res.getPaths()) {
        //    System.err.println("Search path="+searchPath);
        //}
    }
}
