/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which parses a given input file and 
 * get version, update, build, etc
 * 
 * @author Jiri Rechtacek
 */
public class BuildNumberJDK11 extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the <code>file</code> attribute.
     * 
     * @param path The value of the <code>file</code> attribute.
     */
    public void setFile(String path) {
        this.file = new File(path);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The input file is parsed and three properties identifying
     * the JDK build are set.
     * 
     * @throws org.apache.tools.ant.BuildException if the input file cannot be 
     *      parsed for whatever reason.
     */
    @Override
    public void execute() throws BuildException {
        try {
            final FileInputStream in = new FileInputStream(file);
            final CharSequence contents = Utils.read(in);
            
            in.close();
            
            Matcher matcher = PATTERN.matcher(contents);
            if (matcher.find()) {
                String jdkVersion = matcher.group(1);
                String jdkUpdate = matcher.group(4);
                if(jdkUpdate == null) {
                    jdkUpdate = "0";// NOI18N
                }
                String jdkBuildType = "0";//NOI18N
                String jdkBuildNumber =  matcher.group(6);
                String jdkEaText = ""; // NOI18N

                getProject().setProperty("jdk.version.number", jdkVersion); // NOI18N
                getProject().setProperty("jdk.update.number", jdkUpdate); // NOI18N
                getProject().setProperty("jdk.update.number.long", jdkUpdate.length() == 1 ? "0" + jdkUpdate : jdkUpdate); // NOI18N
                getProject().setProperty("jdk.ea.text", jdkEaText); // NOI18N
                getProject().setProperty("jdk.build.number", jdkBuildNumber); // NOI18N                
            } else { 
                    throw new BuildException(
                            "Cannot parse the input file " + file); // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Pattern for which to look in the input file.
     * ${jdk_builds_host}/artifactory/re-release-local/jdk/11.0.1/13/bundles/linux-x64/jdk-11.0.1+13_linux-x64_bin.tar.gz
     */
    private static final Pattern PATTERN = Pattern.compile(
            "jdk-([1-9][0-9]*)((\\.0)*\\.([1-9])([0-9]*))*\\+([0-9]{2})" + // NOI18N
            "_([a-z]+)-([A-Za-z0-9_-]+)_bin\\.tar\\.gz"); // NOI18N
    
}
