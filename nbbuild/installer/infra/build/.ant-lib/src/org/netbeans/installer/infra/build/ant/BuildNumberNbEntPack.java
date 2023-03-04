/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
 * This class is an ant task which parses a given input file and devises the
 * netbeans enterprise pack build number from it.
 *
 * @author Kirill Sorokin
 */
public class BuildNumberNbEntPack extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    /**
     * The properties' names prefix.
     */
    private String prefix;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the <code>file</code> attribute.
     *
     * @param path The value of the <code>file</code> attribute.
     */
    public void setFile(String path) {
        this.file = new File(path);
    }
    
    /**
     * Setter for the <code>prefix</code> attribute.
     *
     * @param prefix The value of the <code>prefix</code> attribute.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The input file is parsed and three properties identifying
     * the netbeans enterprise pack build are set.
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
            
            final Matcher matcher = PATTERN.matcher(contents);
            
            if (matcher.find()) {
                String buildNumber = matcher.group(1);                      // NOMAGI
                
                getProject().setProperty(
                        prefix + BUILD_NUMBER_REAL_SUFFIX,
                        buildNumber);
                
                if (buildNumber.indexOf('_') == -1) {
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber + "00"); // NOI18N
                } else {
                    final int index = buildNumber.indexOf('_');             // NOMAGI
                    
                    final String bnPrefix = 
                            buildNumber.substring(0, index);                // NOMAGI
                    final String bnSuffix = 
                            buildNumber.substring(index + 1);               // NOMAGI
                    
                    buildNumber = 
                            bnPrefix + 
                            (bnSuffix.length() < 2 ? "0" : "") + // NOI18N NOMAGI
                            bnSuffix;
                    
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber); // NOI18N NOMAGI
                }
            } else {
                throw new BuildException(
                        "Cannot find build number"); // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Pattern for which to look in the input file.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "ent_pack-hula-([0-9_]+)\\.zip"); // NOI18N
    
    /**
     * Build number property suffix.
     */
    private static final String BUILD_NUMBER_SUFFIX =
            ".build.number"; // NOI18N
    
    /**
     * Real build number property suffix.
     */
    private static final String BUILD_NUMBER_REAL_SUFFIX =
            ".build.number.real"; // NOI18N
}
