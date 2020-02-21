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
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.openide.util.NbBundle;

/**
 *
 */
public class TarPackager implements PackagerDescriptor {

    public static final String PACKAGER_NAME = "Tar"; // NOI18N

    @Override
    public String getName() {
        return PACKAGER_NAME;
    }

    @Override
    public String getDisplayName() {
        return getString("Tar");
    }

    @Override
    public boolean hasInfoList() {
        return false;
    }

    @Override
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return null;
    }

    @Override
    public List<String> getOptionalInfoList() {
        return null;
    }

    @Override
    public String getDefaultOptions() {
        return "-v"; // NOI18N
    }

    @Override
    public String getDefaultTool() {
        return "tar"; // NOI18N
    }

    @Override
    public boolean isOutputAFolder() {
        return false;
    }

    @Override
    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return packagingConfiguration.getOutputName();
    }

    @Override
    public String getOutputFileSuffix() {
        return "tar";  // NOI18N
    }

    @Override
    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        String topDir = CndPathUtilities.getBaseName(packagingConfiguration.getOutputValue());

        int i = topDir.lastIndexOf("."); // NOI18N
        if (i > 0) {
            topDir = topDir.substring(0, i);
        }
        return topDir;
    }

    @Override
    public boolean supportsGroupAndOwner() {
        return false;
    }

    @Override
    public ShellSciptWriter getShellFileWriter() {
        return new ScriptWriter();
    }

    public static class ScriptWriter implements ShellSciptWriter {

        @Override
        public void writeShellScript(BufferedWriter bw, MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) throws IOException {
            writePackagingScriptBodyTarZip(bw, makeConfiguration);
        }

        private void writePackagingScriptBodyTarZip(BufferedWriter bw, MakeConfiguration conf) throws IOException {
            PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            String output = packagingConfiguration.getOutputValue();
            output = CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, output));
            String outputRelToTmp = CndPathUtilities.isPathAbsolute(output) ? output : "../../../../" + output; // NOI18N

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

            bw.write("# Generate tar file\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("rm -f " + output + "\n"); // NOI18N
            bw.write("cd ${NBTMPDIR}\n"); // NOI18N
            String options = packagingConfiguration.getOptionsValue() + "cf"; // NOI18N
            if (options.charAt(0) != '-') { // NOI18N
                options = "-" + options; // NOI18N
            }
            bw.write(packagingConfiguration.getToolValue() + " " + options + " " + outputRelToTmp + " *\n"); // NOI18N

            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(TarPackager.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
