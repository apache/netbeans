/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class PreBuildConfiguration implements Cloneable {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration preBuildCommandWorkingDir;
    private StringConfiguration preBuildCommand;
    private BooleanConfiguration preBuildFirst;
    
    private static final RequestProcessor RP = new RequestProcessor("MakeConfiguration", 1); // NOI18N
    
    // Constructors
    public PreBuildConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        preBuildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        preBuildCommand = new StringConfiguration(null, ""); // NOI18N
        preBuildFirst = new BooleanConfiguration(false);
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    public void setPreBuildFirst(BooleanConfiguration preBuildFirst){
        this.preBuildFirst = preBuildFirst;
    }
    
    public BooleanConfiguration getPreBuildFirst(){
        return preBuildFirst;
    }
    
    // Working Dir
    public StringConfiguration getPreBuildCommandWorkingDir() {
        return preBuildCommandWorkingDir;
    }
    
    // Working Dir
    public String getPreBuildCommandWorkingDirValue() {
        if (preBuildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return preBuildCommandWorkingDir.getValue();
        }
    }
    
    public void setPreBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.preBuildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Pre-Build Command
    public StringConfiguration getPreBuildCommand() {
        return preBuildCommand;
    }
    
    public void setPreBuildCommand(StringConfiguration buildCommand) {
        this.preBuildCommand = buildCommand;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsPreBuildCommandFileObject() {        
        String path = getAbsPreBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsPreBuildCommandWorkingDir() {
        String wd;
        if (getPreBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getPreBuildCommandWorkingDirValue())) {
            wd = getPreBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getPreBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }

    // Clone and assign
    public void assign(PreBuildConfiguration conf) {
        getPreBuildCommandWorkingDir().assign(conf.getPreBuildCommandWorkingDir());
        getPreBuildCommand().assign(conf.getPreBuildCommand());
        getPreBuildFirst().assign(conf.getPreBuildFirst());
    }

    @Override
    public PreBuildConfiguration clone() {
        PreBuildConfiguration clone = new PreBuildConfiguration(getMakeConfiguration());
        clone.setPreBuildCommandWorkingDir(getPreBuildCommandWorkingDir().clone());
        clone.setPreBuildCommand(getPreBuildCommand().clone());
        clone.setPreBuildFirst(getPreBuildFirst().clone());
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
