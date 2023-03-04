/*
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

