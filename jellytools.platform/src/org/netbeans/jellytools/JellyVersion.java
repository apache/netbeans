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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.jellytools;

import java.util.MissingResourceException;
import java.util.jar.Manifest;

/**
 * Class to obtain version of jellytools and to check whether user run tests
 * on correct Jemmy version.
 * <BR>
 * Information about versions is stored in org/netbeans/jellytools/version_info
 * file. To check jemmy version in runtime we use JemmyProperties.getVersion().
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class JellyVersion {

    /** Flag to check Jemmy version only once. */
    private static boolean jemmyVersionChecked = false;
    
    /** Writes version of jellytools to standard output.
     * @param argv not used
     */
    public static void main(String[] argv) {
        System.out.println("Jellytools version : " + getVersion()+ // NOI18N
        " (Build "+getBuild()+" on "+getIDEVersion()+" and Jemmy "+getJemmyVersion()+")"); // NOI18N
    }

    /** Checks if version of Jemmy which is used to test execution is the same 
     * or greater than version of Jemmy which jellytools were build on. If not,
     * it prints a message to Jemmy error output.
     */
    public static synchronized void checkJemmyVersion() {
        if(!jemmyVersionChecked) {
            jemmyVersionChecked = true;
            String jemmyVersion = null;
            try {
                jemmyVersion = org.netbeans.jemmy.JemmyProperties.getVersion();
            } catch (Exception e) {
                // jemmy version is not available
                return;
            }
            String builtOnJemmyVersion = getJemmyVersion();
            if(jemmyVersion.compareTo(builtOnJemmyVersion) < 0) {
                String line = "\n##############################################\n"; // NOI18N
                String message = "Need to upgrade Jemmy to version: "+ // NOI18N
                builtOnJemmyVersion+"\nCurrent Jemmy version: "+jemmyVersion; // NOI18N
                org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printError(line+message+line);
            }
        }
    }
    
    /** Creates instance of Manifest for file org/netbeans/jellytools/version_info
     * where information about version is stored in manifest-like format.
     */
    private static Manifest getManifest() {
        String info = "org/netbeans/jellytools/version_info"; // NOI18N
        try {
            return new Manifest(new JellyVersion().getClass().getClassLoader().getResourceAsStream(info));
        } catch (Exception e) {
            throw new MissingResourceException("Version info not available.", null, null);// NOI18N
        }
    }
    
    /** Returns Jellytools major version number.
     * @return Jellytools major version number
     */
    public static String getMajorVersion() {
        return getManifest().getMainAttributes().getValue("Jellytools-MajorVersion"); // NOI18N
    }
    
    /** Returns Jellytools minor version number.
     * @return Jellytools minor version number
     */
    public static String getMinorVersion() {
        return getManifest().getMainAttributes().getValue("Jellytools-MinorVersion"); // NOI18N
    }
    
    /** Returns Jellytools version.
     * @return Jellytools version
     */
    public static String getVersion() {
        return (getMajorVersion()+"."+getMinorVersion()); // NOI18N
    }
    
    /** Returns Jellytools build number.
     * @return Jellytools build number
     */
    public static String getBuild() {
        return getManifest().getMainAttributes().getValue("Jellytools-Build"); // NOI18N
    }
    
    /** Returns IDE version which Jellytools were build on.
     * @return IDE version which Jellytools were build on
     */
    public static String getIDEVersion() {
        return getManifest().getMainAttributes().getValue("Jellytools-IDEVersion"); // NOI18N
    }
    
    /** Returns Jemmy major version which Jellytools were build on.
     * @return Jemmy major version which Jellytools were build on
     */
    public static String getJemmyMajorVersion() {
        return getManifest().getMainAttributes().getValue("Jemmy-MajorVersion"); // NOI18N
    }
    
    /** Returns Jemmy minor version which Jellytools were build on.
     * @return Jemmy minor version which Jellytools were build on
     */
    public static String getJemmyMinorVersion() {
        return getManifest().getMainAttributes().getValue("Jemmy-MinorVersion"); // NOI18N
    }
    
    /** Returns Jemmy version which Jellytools were build on.
     * @return Jemmy version which Jellytools were build on
     */
    public static String getJemmyVersion() {
        return (getJemmyMajorVersion()+"."+getJemmyMinorVersion()); // NOI18N
    }
}

