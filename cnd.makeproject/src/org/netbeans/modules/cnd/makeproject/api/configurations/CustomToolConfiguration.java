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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.openide.util.NbBundle;

public class CustomToolConfiguration implements ConfigurationBase, Cloneable {
    // Custom tool
    private StringConfiguration commandLine;
    private StringConfiguration description;
    private StringConfiguration outputs;
    private StringConfiguration additionalDependencies;

    public CustomToolConfiguration() {
        // Custom Tool
        commandLine = new StringConfiguration(null, ""); // NOI18N
        description = new StringConfiguration(null, getString("PerformingStepTxt"));
        outputs = new StringConfiguration(null, ""); // NOI18N
        additionalDependencies = new StringConfiguration(null, ""); // NOI18N
    }

    @Override
    public boolean getModified() {
        return commandLine.getModified() || description.getModified() || outputs.getModified() || additionalDependencies.getModified();
    }

    public void setCommandLine(StringConfiguration commandLine) {
        this.commandLine = commandLine;
    }

    public StringConfiguration getCommandLine() {
        return commandLine;
    }

    public void setDescription(StringConfiguration description) {
        this.description = description;
    }

    public StringConfiguration getDescription() {
        return description;
    }

    public void setOutputs(StringConfiguration outputs) {
        this.outputs = outputs;
    }

    public StringConfiguration getOutputs() {
        return outputs;
    }

    public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    public StringConfiguration getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void assign(CustomToolConfiguration conf) {
        getCommandLine().assign(conf.getCommandLine());
        getDescription().assign(conf.getDescription());
        getOutputs().assign(conf.getOutputs());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
    }

    @Override
    public CustomToolConfiguration clone() {
        CustomToolConfiguration i = new CustomToolConfiguration();
        i.setCommandLine(getCommandLine().clone());
        i.setDescription(getDescription().clone());
        i.setOutputs(getOutputs().clone());
        i.setAdditionalDependencies(getAdditionalDependencies().clone());
        return i;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CustomToolConfiguration.class, s);
    }
}
