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
package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Task to check sha1 from named files (generally  binaries such as ZIPs)
 * from a repository.
 */
public class CheckEmbeddedBinaries extends Task {

    private File dir;

    /**
     * Location of unzippped jar folder to be tested.
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    private File shalist;

    /**
     * List of chechcksum and coordinate
     * @param shaList
     */
    public void setShaList(File shaList) {
        this.shalist = shaList;
    }

    @Override
    public void execute() throws BuildException {
        boolean success = true;

        File manifest = shalist;
        Map<String,String> shamap = new LinkedHashMap<>();
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
                    String[] hashAndFile = line.split(";", 3);
                    if (hashAndFile.length < 2) {
                        throw new BuildException("Bad line '" + line + "' in " + manifest, getLocation());
                    }

                    if (MavenCoordinate.isMavenFile(hashAndFile[1])) {
                        MavenCoordinate mc = MavenCoordinate.fromGradleFormat(hashAndFile[1]);
                        shamap.put(hashAndFile[0], mc.toArtifactFilename());
                    } else if (hashAndFile.length >= 3 && Files.isReadable(dir.toPath().resolve(hashAndFile[2]))){
                        shamap.put(hashAndFile[0], hashAndFile[2]);
                    } else{
                        throw new BuildException("Invalid manifest entry should be Maven coordinate: "+hashAndFile[1], getLocation());
                    }
                }
            }
        } catch (IOException x) {
            throw new BuildException("Could not open " + manifest + ": " + x, x, getLocation());
        }

        for (Map.Entry<String, String> entry : shamap.entrySet()) {
            log ("sha entry "+ entry.getKey()+" = "+entry.getValue());
        }
        try (Stream<Path> list = Files.list(dir.toPath()).sorted((a,b)-> a.getFileName().toString().compareTo(b.getFileName().toString()))) {
            StringBuilder errorList = new StringBuilder();
            list.forEach((t) -> {
                        String sha1 = hash(t.toFile());
                        String filename = shamap.get(sha1);
//                        log("visiting "+t.toString());
                        if (filename == null) {
                            errorList.append("No sha1 (expected ").append(sha1).append(" for file: ").append(t.getFileName()).append("\n");
                        } else if (!filename.equals(t.getFileName().toString())) {
                            errorList.append("Wrong filename for hash (expected ").append(filename).append(" but got: ").append(t.getFileName()).append("\n");
                        }
                    });
            if (! errorList.isEmpty()) {
//                log(""+errorList.toString());
                throw new BuildException("Errors while processing files:\n"+errorList, getLocation());
            }
        } catch (IOException ex) {
            throw new BuildException("Invalid manifest entry should be Maven coordinate: "+ex.getMessage(), getLocation());
        }
        if (!success) {
            throw new BuildException("Failed to download binaries - see log message for the detailed reasons.", getLocation());
        }
    }

    private String hash(File f) {
        try {
            try (FileInputStream is = new FileInputStream(f)) {
                return hash(is);
            }
        } catch (IOException x) {
            throw new BuildException("Could not get hash for " + f + ": " + x, x, getLocation());
        }
    }

    private String hash(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException x) {
            throw new BuildException(x, getLocation());
        }
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) {
            digest.update(buf, 0, r);
        }
        return String.format("%040X", new BigInteger(1, digest.digest()));
    }

}
