/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
import java.text.DateFormat;
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
 * @author Kirill Sorokin
 */
public class BuildNumberGlassFish extends Task {
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
            
            Matcher matcher = PATTERN.matcher(contents);
            boolean found = false;
            if (matcher.find()) {
                found = true;
            } else {
                matcher = PATTERN_V3.matcher(contents);
            }
            if (found || matcher.find()) {
                found = true;
            } else {
                matcher = PATTERN_V3_P02.matcher(contents);
            }
            if (found || matcher.find()) {
                String buildType = "";
                String releaseNumber = "";
                int counter = 1;
                if(matcher.groupCount() > 3) { //V2
		    buildType = matcher.group(counter++);                           // NOMAGI
                } else { // V3.X
                    releaseNumber = matcher.group(counter++);                       // NOMAGI
                }
                // no milestones in GF 4
                final String milestoneNumberReal = "0";
                final String milestoneNumber = "0";

                String buildNumber = matcher.group(counter++);                      // NOMAGI
                
                getProject().setProperty(
                        prefix + BUILD_TYPE_SUFFIX, 
                        buildType.toLowerCase());
                getProject().setProperty(
                        prefix + MILESTONE_NUMBER_REAL_SUFFIX, 
                        milestoneNumberReal);
                getProject().setProperty(
                        prefix + MILESTONE_NUMBER_SUFFIX, 
                        milestoneNumber);
                getProject().setProperty(
                        prefix + BUILD_NUMBER_SUFFIX, 
                        buildNumber);
                getProject().setProperty(
                        prefix + RELEASE_NUMBER_SUFFIX, 
                        releaseNumber);
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
     */
    private static final Pattern PATTERN = Pattern.compile(
            "sjsas-9_1_02-([a-z0-9]+)-bin-" + // NOI18N
            "b(([0-9]+)[a-z]?)-linux-([A-Za-z0-9_]+)\\.bin"); // NOI18N
    private static final Pattern PATTERN_V3 = Pattern.compile(            
            "glassfish-([0-9]+[\\.0-9]+)-b(([0-9]+)[a-z]?)\\.zip"); // NOI18N
    
    // glassfish-3.1.2-2-b03.zip
    private static final Pattern PATTERN_V3_P02 = Pattern.compile(            
            "glassfish-([0-9]+[\\.0-9]+)-[0-9]-b(([0-9]+)[a-z]?)\\.zip"); // NOI18N
    
    /**
     * Date format used in the input file.
     */
    private static final DateFormat FORMAT_IN = 
            new SimpleDateFormat("dd_MMM_yyyy", Locale.US); // NOI18N
    private static final DateFormat FORMAT_IN_V3 = 
            new SimpleDateFormat("M_dd_yyyy", Locale.US); // NOI18N
    
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
     * Milestone number property suffix.
     */
    private static final String MILESTONE_NUMBER_REAL_SUFFIX = 
            ".milestone.number.real"; // NOI18N
    
    /**
     * Build type property suffix.
     */
    private static final String BUILD_TYPE_SUFFIX = 
            ".build.type"; // NOI18N
    
    /**
     * Build number property suffix.
     */
    private static final String BUILD_NUMBER_SUFFIX = 
            ".build.number"; // NOI18N

    /**
     * Release number property suffix.
     */
    private static final String RELEASE_NUMBER_SUFFIX = 
            ".release.number"; // NOI18N
}
