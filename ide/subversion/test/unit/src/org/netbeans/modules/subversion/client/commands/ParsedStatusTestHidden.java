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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class ParsedStatusTestHidden extends AbstractCommandTestCase {
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public ParsedStatusTestHidden(String testName) throws Exception {
        super(testName);
    }

    // XXX check with javahl
    public void testGetStatusWrongAmount() throws Exception {                                
        File folder = createFolder("folder");        
        File folder1 = createFolder(folder, "folder1");        
        File folder2 = createFolder(folder, "folder2");        
        File file1 = createFolder(folder2, "file1");        
        
        add(folder);
        add(folder1);
        add(folder2);
        add(file1);
        commit(getWC());
                
        ISVNStatus[] s1 = getNbClient().getStatus(folder, true, false);

        // returns crap (4 entries) only for the cli which was intentionaly implemted that way
        // to stay compatible with svnClientAdapter's commandline client.
        // javahl and svnkit should return a zero length array
        // commandline no lomger parses metadata, returns the same as javahl and svnkit
        assertEquals(0, s1.length);
    }
    
    
}
