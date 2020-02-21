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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.util.Date;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanReverseNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StateCA;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StateCANodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class ItemNodeFactory {
    private static final boolean SHOW_HEADER_EXCLUDE = CndUtils.getBoolean("cnd.makeproject.showHeaderExclude", true); // NOI18N

    private ItemNodeFactory() {
    }

    public static Node createRootNodeItem(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        CustomizerNode descriptions[];

        PredefinedToolKind tool = context.getItemsTool();
        
        // IG -> context.isProCfile()

        boolean procFile = context.isProc();

        int count = 1; //general
        if (tool != PredefinedToolKind.UnknownTool){
            count++;
            if (procFile) {
                count++;
            }
        }
        descriptions = new CustomizerNode[count];
        int index = 0;
        descriptions[index++] = createGeneralItemDescription(lookup);
        if (tool != PredefinedToolKind.UnknownTool) {
            if (tool == PredefinedToolKind.CCompiler) {
                descriptions[index++] = createCCompilerDescription(lookup);
                if(procFile) {
                    descriptions[index++] = createCustomBuildItemDescription(lookup);
                }
            } else if (tool == PredefinedToolKind.CCCompiler) {
                descriptions[index++] = createCCCompilerDescription(lookup);
                if(procFile) {
                    descriptions[index++] = createCustomBuildItemDescription(lookup);
                }
            } else if (tool == PredefinedToolKind.FortranCompiler) {
                descriptions[index++] = createFortranCompilerDescription(lookup);
            } else if (tool == PredefinedToolKind.Assembler) {
                descriptions[index++] = createAssemblerDescription(lookup);
            } else if (tool == PredefinedToolKind.CustomTool) {
                descriptions[index++] = createCustomBuildItemDescription(lookup);
            } else {
                descriptions[index++] = createCustomBuildItemDescription(lookup); // FIXUP
            }
        }

        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions, lookup); // NOI18N

        return new PropertyNode(rootDescription);
    }

    private static CustomizerNode createGeneralItemDescription(Lookup lookup) {
        return new GeneralItemCustomizerNode(
                "GeneralItem", getString("LBL_Config_General"), null, lookup); // NOI18N
    }

    // Fortran Compiler Node
    public static CustomizerNode createFortranCompilerDescription(Lookup lookup) {
        String compilerName = "fortran"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.FortranCompiler.getDisplayName();
        CustomizerNode fortranCompilerCustomizerNode = new FortranCompilerCustomizerNode(
                compilerName,  compilerDisplayName, null, lookup);
        return fortranCompilerCustomizerNode;
    }

    // Assembler Compiler Node
    public static CustomizerNode createAssemblerDescription(Lookup lookup) {
        String compilerName = "as"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.Assembler.getDisplayName();
        CustomizerNode assemblerCustomizerNode = new AssemblerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return assemblerCustomizerNode;
    }

    // CC Compiler Node
    public static CustomizerNode createCCCompilerDescription(Lookup lookup) {
        String compilerName = "cpp"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.CCCompiler.getDisplayName();
        CustomizerNode ccCompilerCustomizerNode = new CCCompilerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return ccCompilerCustomizerNode;
    }

    public static CustomizerNode createCustomBuildItemDescription(Lookup lookup) {
        return new CustomBuildItemCustomizerNode(
                "Custom Build Step", getString("LBL_Config_Custom_Build"), null, lookup); // NOI18N
    }

    // C Compiler Node
    public static CustomizerNode createCCompilerDescription(Lookup lookup) {
        String compilerName = "c"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.CCompiler.getDisplayName();
        CustomizerNode cCompilerCustomizerNode = new CCompilerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return cCompilerCustomizerNode;
    }

    private static String getString(String s) {
        return NbBundle.getBundle(ItemNodeFactory.class).getString(s);
    }
    
    public static Sheet getGeneralSheet(ItemConfiguration ic) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("Item"); // NOI18N
        set.setDisplayName(getString("ItemTxt"));
        set.setShortDescription(getString("ItemHint"));
        set.put(new StringRONodeProp(getString("NameTxt"), CndPathUtilities.getBaseName(ic.getItem().getPath())));
        set.put(new StringRONodeProp(getString("FilePathTxt"), ic.getItem().getPath()));
        String mdate = ""; // NOI18N
        String fullPath;
        FileObject itemFO;
        MakeConfiguration mc = (MakeConfiguration) ic.getConfiguration();
        FileSystem sourceFS = mc.getSourceFileSystem();
        if (sourceFS == null) {
            sourceFS = CndFileUtils.getLocalFileSystem();
        }
        final String baseDir = mc.getBaseDir();
        FileObject baseDirFO = sourceFS.findResource(baseDir);
        if (baseDirFO != null && baseDirFO.isValid()) {
            fullPath = CndPathUtilities.toAbsolutePath(baseDirFO, ic.getItem().getPath());
            itemFO = sourceFS.findResource(FileSystemProvider.normalizeAbsolutePath(fullPath, sourceFS));
        } else {
            fullPath = CndPathUtilities.toAbsolutePath(sourceFS, baseDir, ic.getItem().getPath());
            itemFO = null;
        }
        if (itemFO != null && itemFO.isValid()) {
            Date lastModified = itemFO.lastModified();
            mdate = DateFormat.getDateInstance().format(lastModified);
            mdate += " " + DateFormat.getTimeInstance().format(lastModified); // NOI18N
        }
        set.put(new StringRONodeProp(getString("FullFilePathTxt"), fullPath));
        set.put(new StringRONodeProp(getString("LastModifiedTxt"), mdate));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("ItemConfiguration"); // NOI18N
        set.setDisplayName(getString("ItemConfigurationTxt"));
        set.setShortDescription(getString("ItemConfigurationHint"));

        set.put(new StateCANodeProp(StateCA.getState(ic.getConfiguration(), ic.getItem(), ic),
                getString("CodeAssistanceTxt"), getString("CodeAssistanceHint"))); //NOI18N
        if (SHOW_HEADER_EXCLUDE || !MIMENames.isHeader(ic.getItem().getMIMEType())) {
            if ((ic.getConfiguration() instanceof MakeConfiguration) &&
                    ((MakeConfiguration) ic.getConfiguration()).isMakefileConfiguration()) {
                set.put(new BooleanReverseNodeProp(ic.getExcluded(), true, "IncludedInCodeAssistance", getString("IncludedInCodeAssistanceTxt"), getString("IncludedInCodeAssistanceHint"))); // NOI18N
            } else {
                set.put(new BooleanNodeProp(ic.getExcluded(), true, "ExcludedFromBuild", getString("ExcludedFromBuildTxt"), getString("ExcludedFromBuildHint"))); // NOI18N
            }
        }
        set.put(new ToolNodeProp(ic));
        sheet.put(set);

        return sheet;
    }

    private static class ToolNodeProp extends Node.Property<PredefinedToolKind> {
        private final ItemConfiguration ic;

        public ToolNodeProp(ItemConfiguration ic) {
            super(PredefinedToolKind.class);
            this.ic = ic;
        }

        @Override
        public String getName() {
            return getString("ToolTxt1");
        }

        @Override
        public PredefinedToolKind getValue() {
            return ic.getTool();
        }

        @Override
        public void setValue(PredefinedToolKind v) {
            ic.setTool(v);
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new ToolEditor(ic);
        }
    }

    private static class ToolEditor extends PropertyEditorSupport {
        private final ItemConfiguration ic;

        public ToolEditor(ItemConfiguration ic) {
            this.ic = ic;
        }

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            ToolKind val = (ToolKind) getValue();
            return val.getDisplayName();
//            CompilerSet set = CompilerSetManager.getDefault(((MakeConfiguration)configuration).getDevelopmentHost().getName()).getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
//            return set.getTool(val).getGenericName();
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
//            setValue(text);
            setValue(PredefinedToolKind.getTool(text));
        }

        @Override
        public String[] getTags() {
            return ic.getToolNames();
        }
    }

    private static class StringRONodeProp extends PropertySupport<String> {

        private final String value;

        public StringRONodeProp(String name, String value) {
            super(name, String.class, name, name, true, false);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String v) {
        }
    }

}
