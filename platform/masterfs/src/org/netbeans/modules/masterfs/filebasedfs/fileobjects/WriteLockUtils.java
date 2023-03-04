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
