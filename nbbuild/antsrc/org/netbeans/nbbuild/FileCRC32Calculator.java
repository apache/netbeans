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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.zip.CRC32;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Calculate the CRC32 checksum for the given file. The resulting long value
 * is converted to a string and placed in the property denoted with the name
 * of {@code property}.
 */
public class FileCRC32Calculator extends Task {

    private File file;
    public void setFile(File file) {
        this.file = file;
    }

    private String property;
    public void setProperty(String property) {
        this.property = property;
    }

    public @Override void execute() throws BuildException {

        try {
            CRC32 crc32 = new CRC32();
            try(FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
                MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                crc32.update(mbb);
            }
            getProject().setProperty(property, Long.toString(crc32.getValue()));
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
}
