/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uihandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
final class BuildInfo {
    protected static final String BUILD_INFO_FILE = "build_info"; //NOI18N
    
    private static final String[] order = {"Number", "Date", "Branding", "Branch", "Tag", "Hg ID"}; //NOI18N
    
    private static final Pattern linePattern = Pattern.compile("(.+):\\s+((.+)\\z)"); //NOI18N

    static LogRecord logBuildInfoRec(){
        LogRecord rec = new LogRecord(Level.CONFIG, BUILD_INFO_FILE);
        List<String> buildInfo = logBuildInfo();
        if (buildInfo != null){
            rec.setParameters(buildInfo.toArray());
        }
        return rec;
    }
    /** Gets build informations
     * @return list build informations in this order: number, date, branding, branch, tag, hg id, or null if the info is not available 
     */
    static List<String> logBuildInfo() {
        List<String> lr = null;
        File f = InstalledFileLocator.getDefault().locate(BUILD_INFO_FILE, null, false);
        if (f != null) {
            lr = logBuildInfo(f);
        }
        return lr;
    }

    private static List<String> logBuildInfo(File f) {
        ArrayList<String> params = null;
        FileReader fr = null;
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            Map<String, String> map = new Hashtable<String, String> ();
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = linePattern.matcher(line);
                if ((m.matches()) && (m.groupCount() > 2)) {
                    map.put(m.group(1), m.group(2));
                }
            }
            params = new ArrayList<String>();
            for (int i = 0; i < order.length; i++) {
                String param = map.get(order[i]);
                if (param != null) {
                    params.add(param);
                } else {
                    params.add("");
                }
                
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return params;
    }

}
