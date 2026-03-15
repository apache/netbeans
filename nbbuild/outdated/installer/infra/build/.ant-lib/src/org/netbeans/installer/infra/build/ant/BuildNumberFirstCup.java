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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which parses a given input file and devises the
 * glassfish milestone number, build type (ea, beta, rc, etc) and build number from
 * it.
 *
 * @author Dmitry Lipin
 */
public class BuildNumberFirstCup extends Task {
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    /**
     * The properties' names prefix.
     */
    private String prefix;
    
    // setters
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
    
    // execution
    /**
     * Executes the task. The input file is parsed and three properties identifying
     * the glassfish build are set.
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
                final String macroNumber =
                        matcher.group(1);                              // NOMAGI
                final String microNumber =
                        matcher.group(2);                              // NOMAGI
                final String milestoneNumber =
                        matcher.group(3);                              // NOMAGI
                
                final String buildNumber = FORMAT_OUT.format(
                        FORMAT_IN.parse(matcher.group(4))); // NOMAGI
                
                getProject().setProperty(
                        prefix + MACRO_BUILD_SUFFIX,
                        macroNumber);
                getProject().setProperty(
                        prefix + MICRO_BUILD_SUFFIX,
                        microNumber);
                
                getProject().setProperty(
                        prefix + MILESTONE_NUMBER_SUFFIX,
                        milestoneNumber);
                getProject().setProperty(
                        prefix + BUILD_NUMBER_SUFFIX,
                        buildNumber);
            } else {
                throw new BuildException(
                        "Cannot parse the input file."); // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (ParseException e) {
            throw new BuildException(e);
        }
    }
    
    // Constants
    /**
     * Pattern for which to look in the input file.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "firstcup-([0-9]+)_([0-9]+)-bin-b([0-9]+)-([A-Za-z0-9_]+).zip");//NOI18N
    
    /**
     * Date format used in the input file.
     */
    private static final DateFormat FORMAT_IN =
            new SimpleDateFormat("dd_MMM_yyyy", Locale.US); // NOI18N
    
    /**
     * Date format to use in the output properties.
     */
    private static final DateFormat FORMAT_OUT =
            new SimpleDateFormat("yyyyMMdd", Locale.US); // NOI18N
    
    /**
     * Milestone number property suffix.
     */
    private static final String MILESTONE_NUMBER_SUFFIX =
            ".milestone.number"; // NOI18N
    
    /**
     * Build type property suffix.
     */
    private static final String BUILD_TYPE_SUFFIX =
            ".build.type"; // NOI18N
    
    /**
     * Macro Build number property suffix.
     */
    private static final String MACRO_BUILD_SUFFIX =
            ".macro.number"; // NOI18N
    
    /**
     * Macro Build number property suffix.
     */
    private static final String MICRO_BUILD_SUFFIX =
            ".micro.number";
    /**
     * Build number property suffix.
     */
    private static final String BUILD_NUMBER_SUFFIX =
            ".build.number"; // NOI18N
}
