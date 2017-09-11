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
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.SvnUtils;

/**
 *
 * @author Ed Hillmann 
 */
public class SvnWcUtils {

    static final String ENTRIES = "entries";      // NOI18N    

    private static final String PROPS = "props";
    private static final String PROPS_BASE = "prop-base";
           
    public static File getSvnFile(File file, String svnFileName) {
        File svnFile = new File(file, SvnUtils.SVN_ADMIN_DIR + "/" + svnFileName);
        if(svnFile.canRead()) {
            return svnFile;
        }
        return null;                
    }
    
    public static File getPropertiesFile(File file, boolean base) {
        if(file.isFile()) {            
            if (base) {
                return getSvnFile(file.getParentFile(), PROPS_BASE + "/" + file.getName() + getPropFileNameSuffix(base));
            } else {
                return getSvnFile(file.getParentFile(), PROPS + "/" + file.getName() + getPropFileNameSuffix(base));
            }            
        } else {            
            return getSvnFile(file, base ? "/dir-prop-base" : "/dir-props");
        }        
    }

    private static String getPropFileNameSuffix(boolean base) {
        if (base) {
            return ".svn-base";
        } else {
            return ".svn-work";
        }        
    }
    
    public static File getTextBaseFile(File file) throws IOException {
        return getSvnFile(file.getParentFile(), "text-base/" + file.getName() + ".svn-base");
    }

    public static Date parseSvnDate(String inputValue) throws ParseException {
        Date returnValue = null;
        if (inputValue != null) {              
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            int idx = inputValue.lastIndexOf(".");            
            if(idx > 0) {
                idx = (idx + 4 > inputValue.length()) ? inputValue.length() : idx + 4; // parse as mili-, not microseconds
                inputValue = inputValue.substring(0, idx) + "Z";            
            }
            returnValue = dateFormat.parse(inputValue);
        }
        return returnValue;        
    }

    static File getEntriesFile(File file) throws IOException {
        return getSvnFile(!file.isDirectory() ? file.getParentFile() : file, ENTRIES);        
    }
    
}
