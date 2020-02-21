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

import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;

public class MakefileConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    
    private StringConfiguration buildCommandWorkingDir;
    private StringConfiguration buildCommand;
    private StringConfiguration cleanCommand;
    private StringConfiguration output;
    
    // Constructors
    public MakefileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        buildCommand = new StringConfiguration(null, MakeArtifact.MAKE_MACRO); // NOI18N
        cleanCommand = new StringConfiguration(null, MakeArtifact.MAKE_MACRO+" clean"); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    // Working Dir
    public StringConfiguration getBuildCommandWorkingDir() {
        return buildCommandWorkingDir;
    }
    
    // Working Dir
    public String getBuildCommandWorkingDirValue() {
        if (buildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return buildCommandWorkingDir.getValue();
        }
    }
    
    public void setBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.buildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Build Command
    public StringConfiguration getBuildCommand() {
        return buildCommand;
    }
    
    public void setBuildCommand(StringConfiguration buildCommand) {
        this.buildCommand = buildCommand;
    }
    
    // Build Command
    public StringConfiguration getCleanCommand() {
        return cleanCommand;
    }
    
    public void setCleanCommand(StringConfiguration cleanCommand) {
        this.cleanCommand = cleanCommand;
    }
    
    // Output
    public StringConfiguration getOutput() {
        return output;
    }
    
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }
    
    // Extra
    public boolean canBuild() {
        return getBuildCommand().getValue().length() > 0;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsBuildCommandFileObject() {        
        String path = getAbsBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsBuildCommandWorkingDir() {
        String wd;
        if (getBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getBuildCommandWorkingDirValue())) {
            wd = getBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }
    
    public boolean canClean() {
        return getCleanCommand().getValue().length() > 0;
    }
    
    public String getAbsOutput() {
        if (getOutput().getValue().length() == 0) {
            return ""; // NOI18N
        } else if (CndPathUtilities.isPathAbsolute(getOutput().getValue())) {
            return getOutput().getValue();
        } else {
            return getMakeConfiguration().getBaseDir() + "/" + getOutput().getValue(); // NOI18N
        }
    }
    
    // Clone and assign
    public void assign(MakefileConfiguration conf) {
        // MakefileConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getBuildCommandWorkingDir().assign(conf.getBuildCommandWorkingDir());
        getBuildCommand().assign(conf.getBuildCommand());
        getCleanCommand().assign(conf.getCleanCommand());
        getOutput().assign(conf.getOutput());
    }

    @Override
    public MakefileConfiguration clone() {
        MakefileConfiguration clone = new MakefileConfiguration(getMakeConfiguration());
        clone.setBuildCommandWorkingDir(getBuildCommandWorkingDir().clone());
        clone.setBuildCommand(getBuildCommand().clone());
        clone.setCleanCommand(getCleanCommand().clone());
        clone.setOutput(getOutput().clone());
        return clone;
    }

    private ExecutionEnvironment getSourceExecutionEnvironment() {
        ExecutionEnvironment env = null;
        MakeConfiguration mc = this.getMakeConfiguration();
        if (mc != null) {
            return FileSystemProvider.getExecutionEnvironment(mc.getBaseFSPath().getFileSystem());
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }
}
