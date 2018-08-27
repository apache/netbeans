/*
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

package org.netbeans.projectopener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 *
 * @author Milan Kubec
 */
public class UserdirScanner {
    
    private static Logger LOGGER = WSProjectOpener.LOGGER;
    
    UserdirScanner() {}
    
    public static NBInstallation[] suitableNBInstallations(File homeDir, String minVersion, Comparator comp) {
        File nbUserHome = new File(homeDir, ".netbeans");
        List list = allNBInstallations(nbUserHome);
        LOGGER.info("All found NetBeans installations: " + list);
        
        NBInstallation devNbi = null;
        // find dev NBInstallation
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            NBInstallation nbi = (NBInstallation) iter.next();
            // 1.0 version means no version number exists
            if (nbi.numVersion().equals("1.0") && 
                    nbi.releaseType().equals("dev") && 
                    nbi.releaseVersion().equals("")) {
                devNbi = nbi;
            }
        }
        if (minVersion.equals("dev")) {
            if (devNbi != null) {
                return new NBInstallation[] { devNbi };
            }
            return new NBInstallation[] { };
        }
        
        Collections.sort(list, comp);
        for (ListIterator listIter = list.listIterator(); listIter.hasNext(); ) {
            NBInstallation nbi = (NBInstallation) listIter.next();
            if (Utils.compareVersions(minVersion, nbi.numVersion()) > 0) { // in case we don't want dev builds -> || nbi.releaseType().equals("dev")) {
                listIter.remove();
            }
        }
        Collections.reverse(list);
        // add dev to the end of the list here
        if (devNbi != null) {
            list.add(devNbi);
        }
        return (NBInstallation[]) list.toArray(new NBInstallation[list.size()]);
    }
    
    // returns all valid installations of NB found in ${HOME}/.netbeans
    private static List allNBInstallations(File nbUserHome) {
        File files[] = nbUserHome.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        List list = new ArrayList();
        // files might be null here, e.g. if there is no .netbeans folder
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // creating NB installation is based on userdir
                NBInstallation nbi = new NBInstallation(files[i]);
                if (nbi.isValid()) {
                    list.add(nbi);
                }
            }
        }
        return list;
    }
    
}
