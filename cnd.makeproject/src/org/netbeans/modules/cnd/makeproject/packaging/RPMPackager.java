/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.packaging;

import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.util.NbBundle;

/**
 *
 */
public class RPMPackager implements PackagerDescriptor {

    public static final String PACKAGER_NAME = "RPM"; // NOI18N

    @Override
    public String getName() {
        return PACKAGER_NAME;
    }

    @Override
    public String getDisplayName() {
        return getString("RPM");
    }

    @Override
    public boolean hasInfoList() {
        return true;
    }

    @Override
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        List<PackagerInfoElement> infoList = new ArrayList<>();
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Summary", "Summary...", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Name", packagingConfiguration.getOutputName(), true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Version", "1.0", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Release", "1", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Group", "Applications/System", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "License", "BSD-type", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "%description", "Description...", true, true)); // NOI18N

        return infoList;
    }

    @Override
    public List<String> getOptionalInfoList() {
        List<String> entryComboBox = new ArrayList<>();

        entryComboBox.add("Patch"); // NOI18N
        entryComboBox.add("%changelog"); // NOI18N
        entryComboBox.add("%pre"); // NOI18N
        entryComboBox.add("%post"); // NOI18N
        entryComboBox.add("%preun"); // NOI18N
        entryComboBox.add("%postun"); // NOI18N

        return entryComboBox;
    }

    @Override
    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    @Override
    public String getDefaultTool() {
        return "rpmbuild"; // NOI18N
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
            writePackagingScriptBodyRPM(bw, makeConfiguration);
        }

        private void writePackagingScriptBodyRPM(BufferedWriter bw, MakeConfiguration conf) throws IOException {
            PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();
            String output = packagingConfiguration.getOutputValue();
            String tmprpmdir = "/tmp/cnd/rpms"; // NOI18N

            bw.write("# Copy files and create directories and links\n"); // NOI18N
            for (PackagerFileElement elem : fileList) {
                bw.write("cd \"${TOP}\"\n"); // NOI18N
                if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    String toDir = CndPathUtilities.getDirName(conf.getPackagingConfiguration().expandMacros(elem.getTo()));
                    if (toDir != null && toDir.length() >= 0) {
                        bw.write("makeDirectory \"" + "${NBTMPDIR}/" + toDir + "\"\n"); // NOI18N
                    }
                    bw.write("copyFileToTmpDir \"" + elem.getFrom() + "\" \"${NBTMPDIR}/" + elem.getTo() + "\" 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    bw.write("makeDirectory " + " \"${NBTMPDIR}/" + elem.getTo() + "\"" + " 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String toDir = CndPathUtilities.getDirName(elem.getTo());
                    String toName = CndPathUtilities.getBaseName(elem.getTo());
                    if (toDir != null && toDir.length() >= 0) {
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

            bw.write("# Ensure proper rpm build environment\n"); // NOI18N
            bw.write("RPMMACROS=~/.rpmmacros\n"); // NOI18N
            bw.write("NBTOPDIR=" + tmprpmdir + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("if [ ! -f ${RPMMACROS} ]\n"); // NOI18N
            bw.write("then\n"); // NOI18N
            bw.write("    touch ${RPMMACROS}\n"); // NOI18N
            bw.write("fi\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("TOPDIR=`grep _topdir ${RPMMACROS}`\n"); // NOI18N
            bw.write("if [ \"$TOPDIR\" == \"\" ]\n"); // NOI18N
            bw.write("then\n"); // NOI18N
            bw.write("    echo \"**********************************************************************************************************\"\n"); // NOI18N
            bw.write("    echo Warning: rpm build environment updated:\n"); // NOI18N
            bw.write("    echo \\\"%_topdir ${NBTOPDIR}\\\" added to ${RPMMACROS}\n"); // NOI18N
            bw.write("    echo \"**********************************************************************************************************\"\n"); // NOI18N
            bw.write("    echo %_topdir ${NBTOPDIR} >> ${RPMMACROS}\n"); // NOI18N
            bw.write("fi  \n"); // NOI18N
            bw.write("mkdir -p ${NBTOPDIR}/RPMS\n"); // NOI18N
            bw.write("\n"); // NOI18N

            bw.write("# Create spec file\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("SPEC_FILE=${NBTMPDIR}/../${OUTPUT_BASENAME}.spec\n"); // NOI18N
            bw.write("rm -f ${SPEC_FILE}\n"); // NOI18N
            bw.write("\n"); // NOI18N        
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("echo " + "BuildRoot: ${TOP}/${NBTMPDIR} >> ${SPEC_FILE}\n"); // NOI18N
            List<PackagerInfoElement> infoList = packagingConfiguration.getHeaderSubList(PACKAGER_NAME);
            for (PackagerInfoElement elem : infoList) {
                if (elem.getName().startsWith("%")) { // NOI18N
                    bw.write("echo \'" + elem.getName() + "\' >> ${SPEC_FILE}\n"); // NOI18N 
                    String value = elem.getValue();
                    int i = 0;
                    int j = value.indexOf("\\n"); // NOI18N 
                    while (j >= 0) {
                        bw.write("echo \'" + value.substring(i, j) + "\' >> ${SPEC_FILE}\n"); // NOI18N 
                        i = j + 2;
                        j = value.indexOf("\\n", i); // NOI18N 
                    }
                    if (i < value.length()) {
                        bw.write("echo \'" + value.substring(i) + "\' >> ${SPEC_FILE}\n"); // NOI18N 
                    }
                    bw.write("echo " + " >> ${SPEC_FILE}\n"); // NOI18N 
                } else {
                    bw.write("echo \'" + elem.getName() + ": " + packagingConfiguration.expandMacros(elem.getValue()) + "\' >> ${SPEC_FILE}\n"); // NOI18N
                }
            }
            bw.write("echo \'%files\' >> ${SPEC_FILE}\n"); // NOI18N 
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.FILE || elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    bw.write("echo " + "\\\"/" + elem.getTo() + "\\\" >> ${SPEC_FILE}\n"); // NOI18N
                }
            }
            bw.write("echo \'%dir\' >> ${SPEC_FILE}\n"); // NOI18N 
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    bw.write("echo " + "\\\"/" + elem.getTo() + "\\\" >> ${SPEC_FILE}\n"); // NOI18N
                }
            }

            bw.write("\n"); // NOI18N
            bw.write("# Create RPM Package\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("LOG_FILE=${NBTMPDIR}/../${OUTPUT_BASENAME}.log\n"); // NOI18N
            if (packagingConfiguration.getOptionsValue().contains("--buildroot")) { // NOI18N
                bw.write(packagingConfiguration.getToolValue() + " " + packagingConfiguration.getOptionsValue() + " -bb ${SPEC_FILE} > ${LOG_FILE}\n"); // NOI18N
            } else {
                bw.write(packagingConfiguration.getToolValue() + " --buildroot ${TOP}/${NBTMPDIR} " + packagingConfiguration.getOptionsValue() + " -bb ${SPEC_FILE} > ${LOG_FILE}\n"); // NOI18N
            }
            bw.write("makeDirectory " + "\"" + "${NBTMPDIR}" + "\"" + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("cat ${LOG_FILE}\n"); // NOI18N
            bw.write("RPM_PATH=`cat $LOG_FILE | grep '\\.rpm' | tail -1 |awk -F: '{ print $2 }'`\n"); // NOI18N
            bw.write("RPM_NAME=`basename ${RPM_PATH}`\n"); // NOI18N
            bw.write("mv ${RPM_PATH} " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("echo RPM: " + packagingConfiguration.getOutputValue() + "/" + "${RPM_NAME}" + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(RPMPackager.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
