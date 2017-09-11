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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.util.Exceptions;


/**
 * @author Radek Matous
 */


public class WriteLockUtils {
    static final String PREFIX = ".LCK";
    static final String SUFFIX = "~";


    private WriteLockUtils(){}

    public static synchronized boolean hasActiveLockFileSigns(final String filename) {
        return filename.startsWith(WriteLockUtils.PREFIX) && filename.endsWith(WriteLockUtils.SUFFIX);
    }
    
    public static synchronized boolean isActiveLockFile(final File file) {
        final String name = file.getName();
        boolean isActiveLockFile = hasActiveLockFileSigns(name);
        if (isActiveLockFile) {
            final String newName = name.substring(WriteLockUtils.PREFIX.length(), (name.length() - WriteLockUtils.SUFFIX.length()));
            isActiveLockFile = FileChangedManager.getInstance().exists(new File(file.getParentFile(), newName));
        }
        
        return isActiveLockFile;
    }
    
    public static File getAssociatedLockFile(File file)  {
        try {
            file = file.getCanonicalFile();
        } catch (IOException iex) {
            Exceptions.printStackTrace(iex);            
        }
        
        final File parentFile = file.getParentFile();
        final StringBuilder sb = new StringBuilder();
        
        sb.append(WriteLockUtils.PREFIX);//NOI18N
        sb.append(file.getName());//NOI18N
        sb.append(WriteLockUtils.SUFFIX);//NOI18N
        
        final String lckName = sb.toString();
        final File lck = new File(parentFile, lckName);
        return lck;
    }
    
    static String getContentOfLckFile(File lckFile, FileChannel channel) throws IOException {
        final byte[] readContent = new byte[(int) lckFile.length()];
        channel.read(ByteBuffer.wrap(readContent));
        
        final String retVal = new String(readContent);
        return (FileChangedManager.getInstance().exists(new File(retVal))) ? retVal : null;
    }
    
    static String writeContentOfLckFile(final File lck, FileChannel channel) throws IOException {
        final String absolutePath = lck.getAbsolutePath();
        final ByteBuffer buf = ByteBuffer.wrap(absolutePath.getBytes());
        channel.write(buf);
        return absolutePath;
    }    
}
