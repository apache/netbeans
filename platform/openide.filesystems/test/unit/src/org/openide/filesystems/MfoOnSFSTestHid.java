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

package org.openide.filesystems;

import java.io.IOException;

public class MfoOnSFSTestHid extends TestBaseHid {
    FileSystem sfs = null;

    public MfoOnSFSTestHid(String testName) {
        super(testName);
        sfs = this.testedFS;
    }

    /** not necessary  */
    protected String[] getResources (String testName) {
        return new String[] {};
    }


    public void testMove () throws IOException{
        String whatRes = "Actions/System/org-openide-actions-GarbageCollectAction.instance";
        String whereRes = "Menu/Tools";                        
                
        FileObject what = sfs.findResource (whatRes);
        if (what == null) {
            what = FileUtil.createData(sfs.getRoot(), whatRes);
        }
        fsAssert("Expected in SystemFileSystem: " + whatRes,what != null);        
        FileObject where = sfs.findResource (whereRes);
        if (where == null) {
            where = FileUtil.createFolder(sfs.getRoot(), whereRes);
        }
        
        fsAssert("Expected in SystemFileSystem: " + whereRes,where != null);        
        
        FileLock flock = what.lock();
        try {
            FileObject moveResult = what.move (flock,where,what.getName(),what.getExt());
            fsAssert("Move error",moveResult != null);
            fsAssert("Move error",sfs.findResource (whatRes) == null);
            fsAssert("Move error",sfs.findResource (whereRes) != null);            
        } finally {
            flock.releaseLock();
        }
        
    }    

    
    @Override
    protected void setUp() throws Exception {
        this.testedFS = sfs = FileUtil.getConfigRoot().getFileSystem();
    }
    
}
