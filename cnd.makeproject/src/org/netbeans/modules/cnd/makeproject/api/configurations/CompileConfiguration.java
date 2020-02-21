/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;

/**
 *
 */
public class CompileConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    private ComboStringConfiguration compileCommandWorkingDir;
    private final DefaultPicklistModel compileCommandWorkingDirPicklist;
    private ComboStringConfiguration compileCommand;
    private final DefaultPicklistModel compileCommandPicklist;
    public static final String AUTO_FOLDER = "${AUTO_FOLDER}"; // NOI18N
    public static final String AUTO_COMPILE = "${AUTO_COMPILE}"; // NOI18N
    public static final String AUTO_MAKE = MakeArtifact.MAKE_MACRO;
    public static final String AUTO_ITEM_PATH = "${ITEM_PATH}"; // NOI18N
    public static final String AUTO_ITEM_NAME = "${ITEM_NAME}"; // NOI18N

    public CompileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        
        compileCommandWorkingDirPicklist = new DefaultPicklistModel(10);
        compileCommandWorkingDirPicklist.addElement(AUTO_FOLDER);
        compileCommandWorkingDirPicklist.addElement("."); // NOI18N
        compileCommandWorkingDir = new ComboStringConfiguration(null, AUTO_FOLDER, compileCommandWorkingDirPicklist);
        
        compileCommandPicklist = new DefaultPicklistModel(10);
        compileCommandPicklist.addElement(AUTO_COMPILE);
        compileCommandPicklist.addElement(AUTO_MAKE+" "+AUTO_ITEM_NAME+".o"); // NOI18N
        compileCommand = new ComboStringConfiguration(null, AUTO_COMPILE, compileCommandPicklist); // NOI18N
    }
    
    public ComboStringConfiguration getCompileCommandWorkingDir() {
        return compileCommandWorkingDir;
    }

    public void setCompileCommandWorkingDir(ComboStringConfiguration compileCommandWorkingDir) {
        this.compileCommandWorkingDir = compileCommandWorkingDir;
    }

    public ComboStringConfiguration getCompileCommand() {
        return compileCommand;
    }
    
    public void setCompileCommand(ComboStringConfiguration compileCommand) {
        this.compileCommand = compileCommand;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    @Override
    public CompileConfiguration clone() {
        CompileConfiguration clone = new CompileConfiguration(getMakeConfiguration());
        clone.setCompileCommandWorkingDir(getCompileCommandWorkingDir().clone());
        clone.setCompileCommand(getCompileCommand().clone());
        return clone;
    }

    void assign(CompileConfiguration compileConfiguration) {
         getCompileCommandWorkingDir().assign(compileConfiguration.getCompileCommandWorkingDir());
         getCompileCommand().assign(compileConfiguration.getCompileCommand());
    }
}
