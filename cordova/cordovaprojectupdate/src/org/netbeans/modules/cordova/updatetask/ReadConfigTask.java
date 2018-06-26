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

package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * 
 * @author Jan Becicka
 */
public class ReadConfigTask extends CordovaTask {

    @Override
    public void execute() throws BuildException {
        try {
            File configFile = getConfigFile();
            SourceConfig config = new SourceConfig(configFile);
            final String id = config.getId();
            String pkg = id.substring(0, id.lastIndexOf(".")); // NOI18N
            setProperty("android.project.package", pkg); //NOI18N       
            setProperty("android.project.package.folder", pkg.replace(".", "/"));//NOI18N
            setProperty("project.name", config.getName()); // NOI18N
            setProperty("cordova.command",PluginTask.getCordovaCommand());
            final String path = PluginTask.isWin()?"env.Path":"env.PATH";
            final String pathKey = PluginTask.isWin()?"Path":"PATH";
            setProperty("cordova.path.key", pathKey);
            setProperty("cordova.path.value", PluginTask.isMac()?getMacPath(path):getProperty(path));
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private String getMacPath(String path) {
        ExecTask exec = (ExecTask) getProject().createTask("exec");
        exec.setExecutable("/bin/bash");
        exec.createArg().setLine("-lc env");
        exec.setOutputproperty("mac.path");
        exec.execute();
        String property = getProperty("mac.path");
        if (property != null) {
            for (String line : property.split(System.getProperty("line.separator"))) {
                if (line.startsWith("PATH=")) {
                    return line.substring(5);
                }
            }
        }
        return getProperty(path);
    }
}
