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
 * @author Jiri Rechtacek
 */
public class BuildNumberJDK extends Task {
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
                System.out.println("###: GROUP COUNT: " + matcher.groupCount());
                String jdkVersion = matcher.group(1);
                String jdkUpdate = matcher.group(2);
                String jdkBuildType = matcher.group(3);
                String jdkBuildNumber = matcher.group(4);
                String jdkEaText = "fcs".equals(jdkBuildType) ? "" : "ea-"; // NOI18N

                getProject().setProperty("jdk.version.number", jdkVersion); // NOI18N
                getProject().setProperty("jdk.update.number", jdkUpdate); // NOI18N
                getProject().setProperty("jdk.update.number.long", jdkUpdate.length() == 1 ? "0" + jdkUpdate : jdkUpdate); // NOI18N
                getProject().setProperty("jdk.ea.text", jdkEaText); // NOI18N
                getProject().setProperty("jdk.build.number", jdkBuildNumber); // NOI18N                
            } else {
                // In case there is no update number                
                matcher = PATTERN_NO_UPDATE.matcher(contents);
                
                if (matcher.find()) {
                    System.out.println("###: GROUP COUNT: " + matcher.groupCount());
                    String jdkVersion = matcher.group(1);               
                    String jdkBuildType = matcher.group(2);
                    String jdkBuildNumber = matcher.group(3);
                    String jdkEaText = "fcs".equals(jdkBuildType) ? "" : "ea-"; // NOI18N

                    getProject().setProperty("jdk.version.number", jdkVersion); // NOI18N
                    getProject().setProperty("jdk.ea.text", jdkEaText); // NOI18N
                    getProject().setProperty("jdk.build.number", jdkBuildNumber); // NOI18N    
                    getProject().setProperty("jdk.update.number", "0"); // NOI18N
                } else {          
                    throw new BuildException(
                            "Cannot parse the input file " + file); // NOI18N
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    // Constants
    /**
     * Pattern for which to look in the input file.
     * ${jdk_builds_host}/java/re/jdk/7u25/promoted/latest/bundles/linux-x64/jdk-7u25-fcs-bin-b15-linux-x64-05_jun_2013.tar.gz
     */
    private static final Pattern PATTERN = Pattern.compile(
            "jdk-([0-9]+)u([0-9]+)-([a-z]+)-bin-" + // NOI18N
            "b(([0-9]+)+)-([A-Za-z0-9_-]+)\\.tar.gz"); // NOI18N
    
    /**
     * Pattern for which to look in the input file. No update number
     * ${jdk_builds_host}/java/re/jdk/8/promoted/latest/bundles/linux-x64/jdk-8-fcs-bin-b127-linux-x64-29_jan_2014.tar.gz 
     */
    private static final Pattern PATTERN_NO_UPDATE = Pattern.compile(
            "jdk-([0-9]+)-([a-z]+)-bin-" + // NOI18N
            "b(([0-9]+)+)-([A-Za-z0-9_-]+)\\.tar.gz"); // NOI18N
    
}
