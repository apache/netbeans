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

package org.netbeans.modules.project.ant.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.spi.project.support.ant.CRC32Calculator;

/**
 * Ant task to show CRCs of files such as <samp>build.xml</samp> as
 * computed by {@link GeneratedFilesHelper}.
 * @author Jesse Glick
 */
public final class ShowCRCTask extends Task {

    private File f;
    public void setFile(File f) {
        this.f = f;
    }
    
    /** Standard constructor. */
    public ShowCRCTask() {}
    
    public void execute() throws BuildException {
        if (f == null) {
            throw new BuildException("No 'file' attr");
        }
        try {
            InputStream is = new FileInputStream(f);
            try {
                String crc = CRC32Calculator.computeCrc32(is);
                log("CRC32 for " + f + ": " + crc);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
