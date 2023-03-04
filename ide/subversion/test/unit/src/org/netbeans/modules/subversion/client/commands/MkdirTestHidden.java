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
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class MkdirTestHidden extends AbstractCommandTestCase {
    
    public MkdirTestHidden(String testName) throws Exception {
        super(testName);
    }
            
    public void testMkdirUrl() throws Exception {
        testMkdirUrl("mrkvadir");
    }

    public void testMkdirUrlWithAtSign() throws Exception {
        testMkdirUrl("@mrkvadir");
        testMkdirUrl("mrkv@adir");
        testMkdirUrl("mrkvadir@");
    }

    private void testMkdirUrl(String urlLastPathSegment) throws Exception {
                        
        ISVNClientAdapter c = getNbClient();        
        SVNUrl url = getRepoUrl().appendPath(urlLastPathSegment);
        c.mkdir(url, "trkvadir");

        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        assertNotifiedFiles(new File[] {});        
    }
    
    public void testMkdirFile() throws Exception {
        testMkdirFile("folder");
    }

    public void testMkdirFileWithAtSign() throws Exception {
        testMkdirFile("@folder");
        testMkdirFile("fol@der");
        testMkdirFile("folder@");
    }

    private void testMkdirFile(String folderName) throws Exception {
                        
        File folder = new File(getWC(), folderName);
        
        ISVNClientAdapter c = getNbClient();                
        c.mkdir(folder);       

        ISVNInfo info = getInfo(folder);
        assertNotNull(info);        
        assertStatus(SVNStatusKind.ADDED, folder);
        assertNotifiedFiles(new File[] {folder});        
    }
    
    public void testMkdirUrlParents() throws Exception {
        testMkdirUrlParents("mrkvadira", "mrkvadirb", "mrkvadirc", "mrkvadird");
    }

    public void testMkdirUrlParentsWithAtSigns1() throws Exception {
        testMkdirUrlParents("@mrkvadira", "mrk@vadirb", "mrkvad@irc", "mrkvadird@");
    }

    public void testMkdirUrlParentsWithAtSigns2() throws Exception {
        testMkdirUrlParents("mrkvadira@", "mrkvad@irb", "mrk@vadirc", "@mrkvadird");
    }

    private void testMkdirUrlParents(String... urlPathSegments) throws Exception {
        ISVNClientAdapter c = getNbClient();        
        SVNUrl url = getRepoUrl();
        for (String pathSegment : urlPathSegments) {
            url = url.appendPath(pathSegment);
        }
        c.mkdir(url, true, "trkvadir");       

        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        assertNotifiedFiles(new File[] {});        
    }            
    
}
