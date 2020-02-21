/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.PackagingNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class PackagingCustomizerNode extends CustomizerNode {

    public PackagingCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet generalSheet = getGeneralSheet(getContext().getContainer(), ((MakeConfiguration) configuration).getPackagingConfiguration(), this);
        return new Sheet[]{generalSheet};
    }
    private TypePropertyChangeListener typePropertyChangeListener;

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsPackaging"); // NOI18N // FIXUP
    }

    private static String getString(String s) {
        return NbBundle.getMessage(PackagingCustomizerNode.class, s);
    }
    
    private Sheet getGeneralSheet(JPanel makeCustomizer, PackagingConfiguration conf, PackagingCustomizerNode cust) {
        IntNodeProp intNodeprop;
        OutputNodeProp outputNodeProp;
        StringNodeProp toolNodeProp;
        StringNodeProp optionsNodeProp;

        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralTxt"));
        set.setShortDescription(getString("GeneralHint"));

        IntConfiguration tmpIntConfiguration = new PackagerIntConfiguration(null, 0, PackagerManager.getDefault().getDisplayNames(), null, conf);

        set.put(intNodeprop = new PackagerIntNodeProp(tmpIntConfiguration, true, "PackageType", getString("PackageTypeName"), getString("PackageTypeHint"), conf)); // NOI18N
        set.put(outputNodeProp = new OutputNodeProp(conf.getOutput(), conf.getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
        String[] texts = new String[]{"Files", getString("FilesName"), getString("FilesHint")}; // NOI18N
        set.put(new PackagingNodeProp(conf, conf.getMakeConfiguration(), texts)); // NOI18N
        set.put(toolNodeProp = new StringNodeProp(conf.getTool(), conf.getToolDefault(), "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
        set.put(optionsNodeProp = new StringNodeProp(conf.getOptions(), conf.getOptionsDefault(), "AdditionalOptions", getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"))); // NOI18N
        set.put(new BooleanNodeProp(conf.getVerbose(), true, "Verbose", getString("VerboseName"), getString("VerboseHint"))); // NOI18N

        sheet.put(set);

        intNodeprop.getPropertyEditor().addPropertyChangeListener(cust.typePropertyChangeListener = new TypePropertyChangeListener(makeCustomizer, outputNodeProp, toolNodeProp, optionsNodeProp, conf));
        return sheet;
    }

    private static class PackagerIntConfiguration extends IntConfiguration {
        private final PackagingConfiguration conf;
        PackagerIntConfiguration(IntConfiguration master, int def, String[] names, String[] options, PackagingConfiguration conf) {
            super(master, def, names, options);
            this.conf = conf;
        }

        @Override
        public void setValue(String s) {
            if (s != null) {
                String displayName = s;
                String name = PackagerManager.getDefault().getName(displayName);
                if (name != null) {
                    conf.getType().setValue(name);
                }
                else {
                    assert false;
                }
            }
        }

        @Override
        public int getValue() {
            int i = PackagerManager.getDefault().getNameIndex(conf.getType().getValue());
            return i;
        }
    }

    private static class PackagerIntNodeProp extends IntNodeProp {
        private final PackagingConfiguration conf;
        public PackagerIntNodeProp(IntConfiguration intConfiguration, boolean canWrite, String name, String displayName, String description, PackagingConfiguration conf) {
            super(intConfiguration, canWrite, name, displayName, description);
            this.conf = conf;
        }


        @Override
        public Object getValue() {
            return PackagerManager.getDefault().getNameIndex(conf.getType().getValue());
        }

        @Override
        public void setValue(Object v) {
            String displayName = (String)v;
            String name = PackagerManager.getDefault().getName(displayName);
            if (name != null) {
                conf.getType().setValue(name);
            }
            else {
                assert false;
            }
        }
    }

    private static class TypePropertyChangeListener implements PropertyChangeListener {

        private final JPanel makeCustomizer;
        private final OutputNodeProp outputNodeProp;
        private final StringNodeProp toolNodeProp;
        private final StringNodeProp optionsNodeProp;
        private final PackagingConfiguration conf;

        TypePropertyChangeListener(JPanel makeCustomizer, OutputNodeProp outputNodeProp, StringNodeProp toolNodeProp, StringNodeProp optionsNodeProp, PackagingConfiguration conf) {
            this.makeCustomizer = makeCustomizer;
            this.outputNodeProp = outputNodeProp;
            this.toolNodeProp = toolNodeProp;
            this.optionsNodeProp = optionsNodeProp;
            this.conf = conf;
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            toolNodeProp.setCanWrite(conf.getToolDefault().length() > 0);
            optionsNodeProp.setCanWrite(conf.getToolDefault().length() > 0);
            if (!conf.getOutput().getModified()) {
                outputNodeProp.setDefaultValue(conf.getOutputDefault());
                conf.getOutput().reset();
            }
            if (!conf.getTool().getModified()) {
                toolNodeProp.setDefaultValue(conf.getToolDefault());
                conf.getTool().reset();
            }
            if (!conf.getOptions().getModified()) {
                optionsNodeProp.setDefaultValue(conf.getOptionsDefault());
                conf.getOptions().reset();
            }
            makeCustomizer.validate(); // this swill trigger repainting of the property
            makeCustomizer.repaint();
        }
    }

    private static class OutputNodeProp extends StringNodeProp {

        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }

        @Override
        public void setValue(String v) {
            if (CndPathUtilities.hasMakeSpecialCharacters(v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }
}
