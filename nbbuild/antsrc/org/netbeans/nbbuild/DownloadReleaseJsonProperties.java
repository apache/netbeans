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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Get;

/**
 *
 * @author Hector Espert
 */
public class DownloadReleaseJsonProperties extends Get {

    private File dest;

    private File cache;

    private long refresh = 86400;

    private boolean force = false;

    @Override
    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    public void setRefresh(long refresh) {
        this.refresh = refresh;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
    public void execute() throws BuildException {
        log("Cached release properties file path: " + cache.getAbsolutePath() + ", file exists: " + cache.exists(), Project.MSG_INFO);

        // Download file if cache file doesn't exists.
        boolean downloadFile = !cache.exists() || force;

        // Force download if file is older
        if (cache.exists()) {
            try {
                BasicFileAttributes attr = Files.readAttributes(cache.toPath(), BasicFileAttributes.class);
                Instant fileInstant = attr.creationTime().toInstant();
                if (Instant.now().minusSeconds(refresh).isAfter(fileInstant)) {
                    downloadFile = true;
                }
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }

        log("Download release properties file: " + downloadFile, Project.MSG_INFO);
        if (downloadFile) {
            super.setDest(cache);
            super.execute();
        }

        if (cache.exists()) {
            try {
                Files.copy(cache.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        } else {
            throw new BuildException("Unable to obtain the release properties file");
        }

    }

}
