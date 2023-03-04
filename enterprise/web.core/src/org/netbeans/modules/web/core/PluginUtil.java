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

package org.netbeans.modules.web.core;

import java.io.File;

/** This class contains static utility methods, which are anticipated
 * to be used by most server plugins, as well as the core web module.
 * This is the only class from the web module that server plugins are
 * allowed to import. Plugins should not use any org.netbeans.* code except
 * for the integration APIs and this class, and likewise, they should not use
 * any org.openide.* classes.
 *
 * @author  pjiricka
 * @version
 */
public class PluginUtil {

    /** Replacement of java.io.File.mkdirs(), as that may fail 
     *  on Solaris mounted disks when invoked from NT. 
     *  Returns true if the dir exists when we finish.
     */
    public static boolean myMkdirs(File f) {
        if (f.exists()) return true;
        if (!f.isAbsolute())
            f = f.getAbsoluteFile();
        String par = f.getParent();
        if (par == null) return false;
        if (!myMkdirs(new File(par))) return false;
        f.mkdir();
        return f.exists();
    }


}
