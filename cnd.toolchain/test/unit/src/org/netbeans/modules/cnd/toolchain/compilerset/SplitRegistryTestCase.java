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
package org.netbeans.modules.cnd.toolchain.compilerset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 */
public class SplitRegistryTestCase extends NbTestCase {

    public static final String CONFIG_FOLDER = "CND/ToolChain"; // NOI18N

    public SplitRegistryTestCase(String testName) {
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

    public void testWrtiteDescriptor() throws Exception {
        //ToolchainManagerImpl.getImpl().getAllToolchains();
        //ToolchainManagerImpl.getImpl().writeToolchains();
        ArrayList<FileContent> result = new ArrayList<FileContent>();
        FileObject folder = FileUtil.getConfigFile(CONFIG_FOLDER);
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for (FileObject file : files) {
                FileContent content = readToolchain(file);
                result.add(content);
            }
        }

        System.out.println("<folder name=\"Tool\">");
        for (FileContent content : result) {
            FileContent parent;
            System.out.println("    <file name=\""+content.name+"_flavor\" url=\"resources/toolchaindefinition/"+content.name+"_flavor.xml\">");
            if (content.base != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+content.base+"_flavor\"/>");
            }
            System.out.println("    </file>");
            if (content.c.length()>0) {
            parent = countBase(findBase(content, result), result, 2);
            System.out.println("    <file name=\""+content.name+"_c\" url=\"resources/toolchaindefinition/"+content.name+"_c.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_c\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.cpp.length()>0) {
            parent = countBase(findBase(content, result), result, 3);
            System.out.println("    <file name=\""+content.name+"_cpp\" url=\"resources/toolchaindefinition/"+content.name+"_cpp.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_cpp\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.fortran.length()>0) {
            parent = countBase(findBase(content, result), result, 4);
            System.out.println("    <file name=\""+content.name+"_fortran\" url=\"resources/toolchaindefinition/"+content.name+"_fortran.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_fortran\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.assembler.length()>0) {
            parent = countBase(findBase(content, result), result, 5);
            System.out.println("    <file name=\""+content.name+"_assembler\" url=\"resources/toolchaindefinition/"+content.name+"_assembler.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_assembler\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.scanner.length()>0) {
            parent = countBase(findBase(content, result), result,6);
            System.out.println("    <file name=\""+content.name+"_scanner\" url=\"resources/toolchaindefinition/"+content.name+"_scanner.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_scanner\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.linker.length()>0) {
            parent = countBase(findBase(content, result), result,7);
            System.out.println("    <file name=\""+content.name+"_linker\" url=\"resources/toolchaindefinition/"+content.name+"_linker.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_linker\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.make.length()>0) {
            parent = countBase(findBase(content, result), result,8);
            System.out.println("    <file name=\""+content.name+"_make\" url=\"resources/toolchaindefinition/"+content.name+"_make.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_make\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.debugger.length()>0) {
            parent = countBase(findBase(content, result), result,9);
            System.out.println("    <file name=\""+content.name+"_debugger\" url=\"resources/toolchaindefinition/"+content.name+"_debugger.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_debugger\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.qmake.length()>0) {
            parent = countBase(findBase(content, result), result,10);
            System.out.println("    <file name=\""+content.name+"_qmake\" url=\"resources/toolchaindefinition/"+content.name+"_qmake.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_qmake\"/>");
            }
            System.out.println("    </file>");
            }
            if (content.cmake.length()>0) {
            parent = countBase(findBase(content, result), result,11);
            System.out.println("    <file name=\""+content.name+"_cmake\" url=\"resources/toolchaindefinition/"+content.name+"_cmake.xml\">");
            if (parent != null) {
            System.out.println("        <attr name=\"extends\" stringvalue=\""+parent.name+"_cmake\"/>");
            }
            System.out.println("    </file>");
            }
        }
        System.out.println("</folder>");

        System.out.println("<folder name=\"ToolChains\">");
        for (FileContent content : result) {
            FileContent parent;
            System.out.println("    <folder name=\""+content.name+"\">");
            System.out.println("        <attr name=\"position\" intvalue=\""+content.position+"\"/>");
            System.out.println("        <file name=\"flavor\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+content.name+"_flavor\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 2);
            System.out.println("        <file name=\"c\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_c\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 3);
            System.out.println("        <file name=\"cpp\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_cpp\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 4);
            System.out.println("        <file name=\"fortran\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_fortran\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 5);
            System.out.println("        <file name=\"assembler\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_assembler\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 6);
            System.out.println("        <file name=\"scanner\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_scanner\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 7);
            System.out.println("        <file name=\"linker\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_linker\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 8);
            System.out.println("        <file name=\"make\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_make\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 9);
            System.out.println("        <file name=\"debugger\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_debugger\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 10);
            System.out.println("        <file name=\"qmake\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_qmake\"/>");
            System.out.println("        </file>");
            parent = countBase(content, result, 11);
            System.out.println("        <file name=\"cmake\">");
            System.out.println("            <attr name=\"originalFile\" stringvalue=\"CND/Tool/"+parent.name+"_cmake\"/>");
            System.out.println("        </file>");
            System.out.println("    </folder>");
        }
        System.out.println("</folder>");


        for (FileContent content : result) {
            print(content, result);
        }
    }

    private FileContent readToolchain(FileObject file) throws FileNotFoundException, IOException {
        FileContent content = new FileContent();
        content.name = file.getName();
        content.position = ((Integer) file.getAttribute("position")); // NOI18N
        content.base = (String) file.getAttribute("extends"); // NOI18N
        BufferedReader stream = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line = null;
        int state = 0;
        while ((line = stream.readLine()) != null) {
            String trim = line.trim();
            if (trim.startsWith("<toolchaindefinition")) {
                state = 1;
            } else if (trim.startsWith("<c>")) {
                state = 2;
            } else if (trim.startsWith("<cpp>")) {
                state = 3;
            } else if (trim.startsWith("<fortran>")) {
                state = 4;
            } else if (trim.startsWith("<assembler>")) {
                state = 5;
            } else if (trim.startsWith("<scanner")) {
                state = 6;
            } else if (trim.startsWith("<linker>")) {
                state = 7;
            } else if (trim.startsWith("<make>")) {
                state = 8;
            } else if (trim.startsWith("<debugger>")) {
                state = 9;
            } else if (trim.startsWith("<qmake>")) {
                state = 10;
            } else if (trim.startsWith("<cmake>")) {
                state = 11;
            } else if (trim.startsWith("</toolchaindefinition")) {
                state = 12;
            }
            switch (state) {
                case 0:
                    content.header.append(line).append('\n');
                    break;
                case 1:
                    content.flavor.append(line).append('\n');
                    break;
                case 2:
                    content.c.append(line).append('\n');
                    break;
                case 3:
                    content.cpp.append(line).append('\n');
                    break;
                case 4:
                    content.fortran.append(line).append('\n');
                    break;
                case 5:
                    content.assembler.append(line).append('\n');
                    break;
                case 6:
                    content.scanner.append(line).append('\n');
                    break;
                case 7:
                    content.linker.append(line).append('\n');
                    break;
                case 8:
                    content.make.append(line).append('\n');
                    break;
                case 9:
                    content.debugger.append(line).append('\n');
                    break;
                case 10:
                    content.qmake.append(line).append('\n');
                    break;
                case 11:
                    content.cmake.append(line).append('\n');
                    break;
            }
        }
        stream.close();
        content.flavor.append("</toolchaindefinition>").append('\n');
        addHeader(content.c);
        addHeader(content.cpp);
        addHeader(content.fortran);
        addHeader(content.assembler);
        addHeader(content.scanner);
        addHeader(content.linker);
        addHeader(content.make);
        addHeader(content.debugger);
        addHeader(content.qmake);
        addHeader(content.cmake);
        return content;
    }

    private void print(FileContent content, ArrayList<FileContent> all) {
        saveFile(content.flavor, content.name + "_flavor.xml", content.header);
        if (content.c.length()>0) {
        saveFile(content.c, content.name + "_c.xml", content.header);
        }
        if (content.cpp.length()>0) {
        saveFile(content.cpp, content.name + "_cpp.xml", content.header);
        }
        if (content.fortran.length()>0) {
        saveFile(content.fortran, content.name + "_fortran.xml", content.header);
        }
        if (content.assembler.length()>0) {
        saveFile(content.assembler, content.name + "_assembler.xml", content.header);
        }
        if (content.scanner.length()>0) {
        saveFile(content.scanner, content.name + "_scanner.xml", content.header);
        }
        if (content.linker.length()>0) {
        saveFile(content.linker, content.name + "_linker.xml", content.header);
        }
        if (content.make.length()>0) {
        saveFile(content.make, content.name + "_make.xml", content.header);
        }
        if (content.debugger.length()>0) {
        saveFile(content.debugger, content.name + "_debugger.xml", content.header);
        }
        if (content.qmake.length()>0) {
        saveFile(content.qmake, content.name + "_qmake.xml", content.header);
        }
        if (content.cmake.length()>0) {
        saveFile(content.cmake, content.name + "_cmake.xml", content.header);
        }
    }

    private void saveFile(StringBuilder content, String fileName, StringBuilder header) {
        System.out.println("================== " + fileName + " ==================");
        System.out.println(content.toString());
        File file = getXml(fileName);
        try {
            PrintStream writer = new PrintStream(file);
            writer.print(header);
            writer.print(content);
            writer.close();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private File getXml(String name) {
        return new File(getDataDir(), name);
    }

    private FileContent findBase(FileContent content, ArrayList<FileContent> all) {
        if (content.base == null) {
            return null;
        }
        for (FileContent fc : all) {
            if (fc.name.equals(content.base)) {
                return fc;
            }
        }
        return null;
    }

    private FileContent countBase(FileContent content, ArrayList<FileContent> all, int kind) {
        if (content == null) {
            return null;
        }
        switch (kind) {
            case 1:
                while(true) {
                    if (content.flavor.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 2:
                while(true) {
                    if (content.c.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 3:
                while(true) {
                    if (content.cpp.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 4:
                while(true) {
                    if (content.fortran.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 5:
                while(true) {
                    if (content.assembler.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 6:
                while(true) {
                    if (content.scanner.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 7:
                while(true) {
                    if (content.linker.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 8:
                while(true) {
                    if (content.make.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 9:
                while(true) {
                    if (content.debugger.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 10:
                while(true) {
                    if (content.qmake.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
            case 11:
                while(true) {
                    if (content.cmake.length() != 0) {
                        return content;
                    }
                    content = findBase(content, all);
                    if (content == null) {
                        return null;
                    }
                }
        }
        return null;
    }

    private void addHeader(StringBuilder buf) {
        if (buf.length() > 0) {
            buf.insert(0, "<toolchaindefinition xmlns=\"http://www.netbeans.org/ns/cnd-toolchain-definition/1\">\n");
            buf.append("</toolchaindefinition>").append('\n');
        }
    }

    private static final class FileContent {

        private int position;
        private String name;
        private String base;
        private StringBuilder header = new StringBuilder();
        private StringBuilder flavor = new StringBuilder();
        private StringBuilder c = new StringBuilder();
        private StringBuilder cpp = new StringBuilder();
        private StringBuilder fortran = new StringBuilder();
        private StringBuilder assembler = new StringBuilder();
        private StringBuilder scanner = new StringBuilder();
        private StringBuilder linker = new StringBuilder();
        private StringBuilder make = new StringBuilder();
        private StringBuilder debugger = new StringBuilder();
        private StringBuilder qmake = new StringBuilder();
        private StringBuilder cmake = new StringBuilder();
    }
}
