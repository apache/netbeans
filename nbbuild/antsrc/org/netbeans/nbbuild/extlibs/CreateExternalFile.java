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

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Create .external files which represent external dependencies, that can't be
 * distributed with the NBM but are downloaded at installation time of the
 * module. This is relevant if a license needs to be accepted by the user first
 * or where the file can't be distributed with the NBN directly (GPLv2 + ALv2
 * for example).
 *
 * <p><strong>At this point only dependencies downloaded from maven central
 * are supported!</strong></p>
 */
public class CreateExternalFile extends Task {
    private File manifest;

    public File getManifest() {
        return manifest;
    }

    public void setManifest(File manifest) {
        this.manifest = manifest;
    }

    @Override
    public void execute() throws BuildException {
        log("Scanning: " + manifest, Project.MSG_VERBOSE);
        try {
            try (InputStream is = new FileInputStream(manifest)) {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    String[] hashAndFile = line.split(" ", 2);
                    if (hashAndFile.length < 2) {
                        throw new BuildException("Bad line '" + line + "' in " + manifest, getLocation());
                    }

                    if (MavenCoordinate.isMavenFile(hashAndFile[1])) {
                        MavenCoordinate mc = MavenCoordinate.fromGradleFormat(hashAndFile[1]);
                        File artifactFile = new File(manifest.getParentFile(), mc.toArtifactFilename());
                        File externalFile = new File(artifactFile.getAbsolutePath() + ".external");
                        CRC32 crc = new CRC32();
                        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

                        try(FileInputStream fis = new FileInputStream(artifactFile)) {
                            byte[] buffer = new byte[1024 * 100];
                            int read;
                            while((read = fis.read(buffer)) >= 0) {
                                crc.update(buffer, 0, read);
                                sha256.update(buffer, 0, read);
                                sha512.update(buffer, 0, read);
                            }
                        }

                        try (PrintWriter pw = new PrintWriter(externalFile, "UTF-8")) {
                            pw.printf("CRC: %d\n", crc.getValue());
                            pw.printf("URL: %s\n", mc.toM2Url());
                            pw.printf("MessageDigest: SHA-256 %064x\n", new BigInteger(1, sha256.digest()));
                            pw.printf("MessageDigest: SHA-512 %0128x\n" , new BigInteger(1, sha512.digest()));
                        }
                    } else {
                        throw new BuildException("Only maven coordinates are supported");
                    }
                }
            }
        } catch (IOException x) {
            throw new BuildException("Could not open " + manifest + ": " + x, x, getLocation());
        } catch (NoSuchAlgorithmException ex) {
            throw new BuildException("Failed to create hash", ex);
        }
    }

}
