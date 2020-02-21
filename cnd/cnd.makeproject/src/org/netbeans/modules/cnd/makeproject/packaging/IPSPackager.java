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
package org.netbeans.modules.cnd.makeproject.packaging;

import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.util.NbBundle;

/**
 *
 */
public class IPSPackager implements PackagerDescriptor {

    public static final String PACKAGER_NAME = "IPS"; // NOI18N

    @Override
    public String getName() {
        return PACKAGER_NAME;
    }

    @Override
    public String getDisplayName() {
        return getString("IPSPackage"); // FIXUP: typo...
    }

    @Override
    public boolean hasInfoList() {
        return true;
    }

    @Override
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        List<PackagerInfoElement> infoList = new ArrayList<>();
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "name", packagingConfiguration.getOutputName(), false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "description", "Package description ...", false, true)); // NOI18N

        return infoList;
    }

    @Override
    public List<String> getOptionalInfoList() {
        List<String> entryComboBox = new ArrayList<>();

        entryComboBox.add("name"); // NOI18N
        entryComboBox.add("description"); // NOI18N
//        entryComboBox.add("DESC"); // NOI18N
//        entryComboBox.add("EMAIL"); // NOI18N
//        entryComboBox.add("HOTLINE"); // NOI18N
//        entryComboBox.add("INTONLY"); // NOI18N
//        entryComboBox.add("ISTATES"); // NOI18N
//        entryComboBox.add("MAXINST"); // NOI18N
//        entryComboBox.add("ORDER"); // NOI18N
//        entryComboBox.add("PSTAMP"); // NOI18N
//        entryComboBox.add("RSTATES"); // NOI18N
//        entryComboBox.add("SUNW_ISA"); // NOI18N
//        entryComboBox.add("SUNW_LOC"); // NOI18N
//        entryComboBox.add("SUNW_PKG_DIR"); // NOI18N
//        entryComboBox.add("SUNW_PKG_ALLZONES"); // NOI18N
//        entryComboBox.add("SUNW_PKG_HOLLOW"); // NOI18N
//        entryComboBox.add("SUNW_PKG_THISZONE"); // NOI18N
//        entryComboBox.add("SUNW_PKGLIST"); // NOI18N
//        entryComboBox.add("SUNW_PKGTYPE"); // NOI18N
//        entryComboBox.add("SUNW_PKGVERS"); // NOI18N
//        entryComboBox.add("SUNW_PRODNAME"); // NOI18N
//        entryComboBox.add("SUNW_PRODVERS"); // NOI18N
//        entryComboBox.add("ULIMIT"); // NOI18N
//        entryComboBox.add("VENDOR"); // NOI18N
//        entryComboBox.add("VSTOCK"); // NOI18N

        return entryComboBox;
    }

    @Override
    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    @Override
    public String getDefaultTool() {
        return ""; // NOI18N
    }

    @Override
    public boolean isOutputAFolder() {
        return true;
    }

    @Override
    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return null;
    }

    @Override
    public String getOutputFileSuffix() {
        return null;
    }

    @Override
    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return "/usr"; // NOI18N
    }

    @Override
    public boolean supportsGroupAndOwner() {
        return true;
    }

    @Override
    public ShellSciptWriter getShellFileWriter() {
        return new ScriptWriter();
    }

    public static class ScriptWriter implements ShellSciptWriter {

        @Override
        public void writeShellScript(BufferedWriter bw, MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) throws IOException {
            writePackagingScriptBodyIPS(bw, makeConfiguration);
        }

        private List<String> findUndefinedDirectories(PackagingConfiguration packagingConfiguration) {
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();
            HashSet<String> set = new HashSet<>();
            ArrayList<String> list = new ArrayList<>();

            // Already Defined
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    String path = packagingConfiguration.expandMacros(elem.getTo());
                    if (path.endsWith("/")) { // NOI18N
                        path = path.substring(0, path.length() - 1);
                    }
                    set.add(path);
                }
            }
            // Do all sub dirrectories
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.FILE || elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String path = CndPathUtilities.getDirName(packagingConfiguration.expandMacros(elem.getTo()));
                    String base = ""; // NOI18N
                    if (path != null && path.length() > 0) {
                        StringTokenizer tokenizer = new StringTokenizer(path, "/"); // NOI18N
                        while (tokenizer.hasMoreTokens()) {
//                            if (base.length() > 0) {
//                                base += "/"; // NOI18N
//                            }
                            base += "/" + tokenizer.nextToken(); // NOI18N
                            if (!set.contains(base)) {
                                set.add(base);
                                list.add(base);
                            }
                        }
                    }
                }
            }
            return list;
        }

        private void writePackagingScriptBodyIPS(BufferedWriter bw, MakeConfiguration conf) throws IOException {
            PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();

            bw.write("# Copy files and create directories and links\n"); // NOI18N
            for (PackagerFileElement elem : fileList) {
                bw.write("cd \"${TOP}\"\n"); // NOI18N
                if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    String toDir = CndPathUtilities.getDirName(conf.getPackagingConfiguration().expandMacros(elem.getTo()));
                    if (toDir != null && toDir.length() >= 0) {
                        if (toDir.charAt(0) == '/') {
                            toDir = toDir.substring(1);
                        }
                        bw.write("makeDirectory \"" + "${NBTMPDIR}/" + toDir + "\"\n"); // NOI18N
                    }
                    bw.write("copyFileToTmpDir \"" + elem.getFrom() + "\" \"${NBTMPDIR}/" + elem.getTo() + "\" 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    bw.write("makeDirectory " + " \"${NBTMPDIR}/" + elem.getTo() + "\"" + " 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String toDir = CndPathUtilities.getDirName(elem.getTo());
                    String toName = CndPathUtilities.getBaseName(elem.getTo());
                    if (toDir != null && toDir.length() >= 0) {
                        if (toDir.charAt(0) == '/') {
                            toDir = toDir.substring(1);
                        }
                        bw.write("makeDirectory " + "\"" + "${NBTMPDIR}/" + toDir + "\"" + "\n"); // NOI18N
                    }
                    bw.write("cd " + "\"" + "${NBTMPDIR}/" + toDir + "\"" + "\n"); // NOI18N
                    bw.write("ln -s " + "\"" + elem.getFrom() + "\"" + " " + "\"" + toName + "\"" + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.UNKNOWN) {
                    // skip ???
                } else {
                    assert false;
                }
                bw.write("\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N

            bw.write("# Create manifest file\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("MANIFEST=${NBTMPDIR}/manifest\n"); // NOI18N
            bw.write("rm -f ${MANIFEST}\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N

            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "# header" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            List<PackagerInfoElement> infoList = packagingConfiguration.getHeaderSubList(PACKAGER_NAME);
            for (PackagerInfoElement elem : infoList) {
                String value = elem.getValue();
                int i = 0;
                int j = value.indexOf("\\n"); // NOI18N
                while (j >= 0) {
                    if (i == 0) {
                        bw.write("echo \'" + elem.getName() + ": " + value.substring(i, j) + "\' >> ${MANIFEST}\n"); // NOI18N
                    } else {
                        bw.write("echo \'" + value.substring(i, j) + "\' >> ${MANIFEST}\n"); // NOI18N
                    }
                    i = j + 2;
                    j = value.indexOf("\\n", i); // NOI18N
                }
                if (i < value.length()) {
                    if (i == 0) {
                        bw.write("echo \'name=\"" + elem.getName() + "\" value=\"" + value.substring(i) + "\"\' >> ${MANIFEST}\n"); // NOI18N
                    } else {
                        bw.write("echo \'" + value.substring(i) + "\' >> ${MANIFEST}\n"); // NOI18N
                    }
                }
            }

            for (String addInfo : packagingConfiguration.getAdditionalInfo().getValue()) {
                bw.write("echo \"" + addInfo + "\" >> ${MANIFEST}\n"); // NOI18N
            }

            // Directories
            bw.write("\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "# directories" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            List<String> dirList = findUndefinedDirectories(packagingConfiguration);
            for (String dir : dirList) {
                bw.write("echo \"");// NOI18N
                bw.write("dir "); // NOI18N
                bw.write("mode=0" + MakeOptions.getInstance().getDefExePerm() + " "); // NOI18N
                bw.write("owner=" + MakeOptions.getInstance().getDefOwner() + " "); // NOI18N
                bw.write("group=" + MakeOptions.getInstance().getDefGroup() + " "); // NOI18N
                bw.write("path=" + dir); // NOI18N
                bw.write("\""); // NOI18N
                bw.write(" >> ${MANIFEST}\n"); // NOI18N

            }
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    String path = packagingConfiguration.expandMacros(elem.getTo());
                    if (path.endsWith("/")) { // NOI18N
                        path = path.substring(0, path.length() - 1);
                    }
                    bw.write("echo \"");// NOI18N
                    bw.write("dir "); // NOI18N
                    bw.write("mode=0" + elem.getPermission() + " "); // NOI18N
                    bw.write("owner=" + elem.getOwner() + " "); // NOI18N
                    bw.write("group=" + elem.getGroup() + " "); // NOI18N
                    bw.write("path=" + path); // NOI18N
                    bw.write("\""); // NOI18N
                    bw.write(" >> ${MANIFEST}\n"); // NOI18N
                }
            }

            // Files
            bw.write("\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "# files" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    String path = packagingConfiguration.expandMacros(elem.getTo());
                    bw.write("echo \"");// NOI18N
                    bw.write("file "); // NOI18N
                    bw.write(path.substring(1) + " "); // NOI18N
                    bw.write("mode=0" + elem.getPermission() + " "); // NOI18N
                    bw.write("owner=" + elem.getOwner() + " "); // NOI18N
                    bw.write("group=" + elem.getGroup() + " "); // NOI18N
                    bw.write("path=" + path); // NOI18N
                    bw.write("\""); // NOI18N
                    bw.write(" >> ${MANIFEST}\n"); // NOI18N
                }
            }

            // Links
            bw.write("\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "# links" + "\' >> ${MANIFEST}\n"); // NOI18N
            bw.write("echo \'" + "#" + "\' >> ${MANIFEST}\n"); // NOI18N
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String path = packagingConfiguration.expandMacros(elem.getTo());
                    bw.write("echo \"");// NOI18N
                    bw.write("hardlink "); // NOI18N
                    bw.write("path=" + path + " "); // NOI18N
                    bw.write("target=" + packagingConfiguration.expandMacros(elem.getFrom())); // NOI18N
                    bw.write("\""); // NOI18N
                    bw.write(" >> ${MANIFEST}\n"); // NOI18N
                }
            }

            bw.write("\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("mkdir -p  " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("cd \"${NBTMPDIR}\"\n"); // NOI18N
            bw.write("for i in *; do cd \"${TOP}\"; rm -rf " + packagingConfiguration.getOutputValue() + "/$i; done" + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("mv ${NBTMPDIR}/* " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N

            bw.write("\n"); // NOI18N
            bw.write("echo IPS: " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
//            bw.write("echo To publish:" + "\n"); // NOI18N
//            bw.write("echo cd " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
//            bw.write("echo eval 'pkgsend -s \"http://localhost:80\" open srm@1.2.9'" + "\n"); // NOI18N
//            bw.write("echo pkgsend -s \"http://localhost:80\" include manifest" + "\n"); // NOI18N
//            bw.write("echo pkgsend -s \"http://localhost:80\" close" + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(IPSPackager.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
