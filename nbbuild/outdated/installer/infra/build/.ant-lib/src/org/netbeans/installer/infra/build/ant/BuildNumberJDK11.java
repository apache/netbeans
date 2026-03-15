/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 */
public class BuildNumberJDK11 extends Task {
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    // setters
    /**
     * Setter for the <code>file</code> attribute.
     * 
     * @param path The value of the <code>file</code> attribute.
     */
    public void setFile(String path) {
        this.file = new File(path);
    }
    
    // execution
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
    
    // Constants
    /**
     * Pattern for which to look in the input file.
     * ${jdk_builds_host}/artifactory/re-release-local/jdk/11.0.1/13/bundles/linux-x64/jdk-11.0.1+13_linux-x64_bin.tar.gz
     */
    private static final Pattern PATTERN = Pattern.compile(
            "jdk-([1-9][0-9]*)((\\.0)*\\.([1-9])([0-9]*))*\\+([0-9]{2})" + // NOI18N
            "_([a-z]+)-([A-Za-z0-9_-]+)_bin\\.tar\\.gz"); // NOI18N
    
}
