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
package org.netbeans.modules.versioning.system.cvss.installer.util;

import java.io.File;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.netbeans.modules.versioning.system.cvss.installer.CvsInstaller;

/**
 *
 * @author ondra
 */
public class Utils {
    
    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "CVS(\\" + File.separatorChar + ".*|$)");     //NOI18N
    
    private Utils () {
        
    }
    
    public static boolean isPartOfCVSMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
    
    public static boolean containsMetadata(File folder) {
        CvsInstaller.LOG.log(Level.FINER, " containsMetadata {0}", new Object[] { folder });
        long t = System.currentTimeMillis();
        File repository = new File(folder, CvsInstaller.FILENAME_CVS_REPOSITORY);
        File entries = new File(folder, CvsInstaller.FILENAME_CVS_ENTRIES);
        boolean ret = repository.exists() && entries.exists();
        if(CvsInstaller.LOG.isLoggable(Level.FINER)) {
            CvsInstaller.LOG.log(Level.FINER, " containsMetadata returns {0} after {1} millis", new Object[] { ret, System.currentTimeMillis() - t });
        }
        return ret;
    }
}
