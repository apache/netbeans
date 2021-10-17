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
package org.netbeans.modules.java.disco.archive;

import com.google.common.base.MoreObjects;
import java.io.File;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openide.windows.InputOutput;

public class JDKCommonsUnzip extends CommonsUnzip {

    private final static File[] EMPTY_FILES = new File[0];

    @Override
    public void uncompress(File zip, File targetDir, InputOutput io) throws IOException {
        super.uncompress(zip, targetDir, io);

        //TODO: This is hack to set permissions until I see why Apache Compress does not read them.
        File bin = findBin(targetDir);

        if (bin != null) {
            io.getOut().println("Making sure java is executable.");
            for (File exe : MoreObjects.firstNonNull(bin.listFiles(), EMPTY_FILES)) {
                if (exe.isFile())
                    exe.setExecutable(true);
            }
        }
        io.getOut().println("Done.");
    }

    public static @Nullable File findBin(File outputDir) {
        for (File f : MoreObjects.firstNonNull(outputDir.listFiles(), EMPTY_FILES)) {
            if (f.isDirectory() && f.getName().equals("bin"))
                return f;
        }
        for (File f : MoreObjects.firstNonNull(outputDir.listFiles(), EMPTY_FILES)) {
            if (f.isDirectory()) {
                File sub = findBin(f);
                if (sub != null)
                    return sub;
            }
        }
        return null;
    }

}
