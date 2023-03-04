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

package org.netbeans.modules.web.core.jsploader;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** JSP compilation utilities
*
* @author Petr Jiricka
*/
public class JspCompileUtil {

    /** Finds a relative context path between rootFolder and relativeObject.
     * Similar to <code>FileUtil.getRelativePath(FileObject, FileObject)</code>, only
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + FileUtil.getRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }
    
    /** Returns whether a given file is a JSP file, or possibly a JSP segment.
     * The recognition happens based on file extension (not on actual inclusion in other files).
     * @param fo the file to examine
     * @param acceptSegment whether segments should be accepted
     */
    public static boolean isJspFile(FileObject fo, boolean acceptSegment) {
        String ext = fo.getExt().toLowerCase();
        if ("jsp".equals(ext) || "jspx".equals(ext)) { // NOI18N
            return true;
        }
        if ("jspf".equals(ext) && acceptSegment) { // NOI18N
            return true;
        }
        return false;
    }
    
    /** Returns whether a given file is a tag file, or possibly a tag segment.
     * The recognition happens based on file extension (not on actual inclusion in other files).
     * @param fo the file to examine
     * @param acceptSegment whether segments should be accepted
     */
    public static boolean isTagFile(FileObject fo, boolean acceptSegment) {
        String ext = fo.getExt().toLowerCase();
        if ("tag".equals(ext) || "tagx".equals(ext)) { // NOI18N
            return true;
        }
        if ("tagf".equals(ext) && acceptSegment) { // NOI18N
            return true;
        }
        return false;
    }
    
}

