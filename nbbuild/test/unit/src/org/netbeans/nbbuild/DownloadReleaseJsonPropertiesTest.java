/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tools.ant.Project;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class DownloadReleaseJsonPropertiesTest extends NbTestCase {
    
    private final DownloadReleaseJsonProperties downloadTask = new DownloadReleaseJsonProperties();

    public DownloadReleaseJsonPropertiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testExecute() throws MalformedURLException, IOException {
        Project project = new Project();
        downloadTask.setProject(project);

        URL src = new URL("https://gitbox.apache.org/repos/asf?p=netbeans-jenkins-lib.git;a=blob_plain;f=meta/netbeansrelease.json");
        downloadTask.setSrc(src);
        
        File destFolder = new File(getWorkDir().getAbsolutePath() + File.separator + "dest" + File.separator + "netbeansrelease.json");
        destFolder.mkdirs();
        
        File dest = new File(destFolder.getAbsolutePath() + File.separator + "netbeansrelease.json");
        assertFalse(dest.exists());
        downloadTask.setDest(dest);
        
        File cacheFolder = new File(getWorkDir().getAbsolutePath() + File.separator + "cache");
        cacheFolder.mkdirs();
        
        File cache = new File(cacheFolder.getAbsolutePath() + File.separator + "netbeansrelease.json");
        assertFalse(cache.exists());
        downloadTask.setCache(cache);
        
        downloadTask.execute();
        
        assertTrue(dest.exists());
        assertTrue(cache.exists());
        
    }
    
}
