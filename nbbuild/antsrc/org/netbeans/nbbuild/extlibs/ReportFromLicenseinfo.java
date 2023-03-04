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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.nbbuild.extlibs.licenseinfo.CommentType;
import org.netbeans.nbbuild.extlibs.licenseinfo.Fileset;
import org.netbeans.nbbuild.extlibs.licenseinfo.Licenseinfo;

public class ReportFromLicenseinfo extends Task {

    private File nball;
    public void setNball(File nball) {
        this.nball = nball;
    }

    private File report;
    public void setReport(File report) {
        this.report = report;
    }

    public @Override void execute() throws BuildException {
        Properties licenseNames = new Properties();
        try (InputStream is = new FileInputStream(new File(nball, "nbbuild/licenses/names.properties"))) {
            licenseNames.load(is);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
        try (FileOutputStream fos = new FileOutputStream(report);
                OutputStreamWriter osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
                PrintWriter pw = new PrintWriter(osw)) {

            try(FileInputStream fis = new FileInputStream(new File(nball, "nbbuild/rat-licenseinfo-stub.txt"));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader br = new BufferedReader(isr)) {
                for(String line = br.readLine(); line != null; line = br.readLine()) {
                    pw.write(line);
                    pw.write("\n");
                }
            }
            
            Path nballPath = nball.toPath();

            List<File> licenseinfofiles;

            try (Stream<Path> walk = Files.walk(nballPath)) {
                licenseinfofiles = walk
                        .filter(p -> p.endsWith("licenseinfo.xml"))
                        .map(p -> p.toFile())
                        .collect(Collectors.toList());
            }
            
            TreeMap<LicenseGroup,List<File>> licenseInfo = new TreeMap<>();
            
            for (File licenseInfoFile: licenseinfofiles) {
                Licenseinfo li = Licenseinfo.parse(licenseInfoFile);
                for(Fileset fs: li.getFilesets()) {
                    LicenseGroup lg = new LicenseGroup(
                            fs.getLicenseRef(),
                            fs.getLicenseInfo(),
                            fs.getCommentType(),
                            fs.getComment()
                    );
                    if(! licenseInfo.containsKey(lg)) {
                        licenseInfo.put(lg, new ArrayList<>());
                    }
                    for(File f: fs.getFiles()) {
                        licenseInfo.get(lg).add(f);
                    }
                }
                
            }
            
            pw.print("======================================  Per File Information  ======================================\n\n");
            
            for(Entry<LicenseGroup,List<File>> e: licenseInfo.entrySet()) {
                LicenseGroup lg = e.getKey();
                String licenseName = licenseNames.getProperty(lg.getLicenseRef());
                
                if(licenseName != null) {
                    pw.printf("#### %s: %s\n\n", lg.getLicenseRef(), licenseName);
                } else {
                    pw.printf("#### %s\n\n", lg.getLicenseRef());
                }
                if(lg.getCommentType() != null || lg.getComment() != null) {
                    if(lg.getCommentType() != null) {
                        pw.printf("%s\n\n", lg.getCommentType().getOutputComment());
                    }
                    if(lg.getComment() != null) {
                        pw.printf("%s\n\n", lg.getComment().trim());
                    }
                }
                pw.printf("-- Files --\n");
                for(File f: e.getValue()) {
                    Path relativePath = nball.toPath().relativize(f.toPath());
                    pw.printf("%s\n", relativePath.toString());
                }
                pw.printf("\n");
            }
            
            pw.print("=========================================  Other excludes  =========================================\n\n");
            
            try (FileInputStream fis = new FileInputStream(new File(nball, "nbbuild/rat-exclusions.txt"));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader br = new BufferedReader(isr)) {
                
                boolean beginSkipped = false;
                
                for(String line = br.readLine(); line != null; line = br.readLine()) {
                    if (beginSkipped) {
                        pw.write(line);
                        pw.write("\n");
                    }
                    
                    if(line.contains("###### BEGIN OF EXCLUSIONS")) {
                        beginSkipped = true;
                    }
                }
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    
    private static class LicenseGroup implements Comparable<LicenseGroup> {
        private static final String ALV2 = "Apache-2.0";
        
        private final String licenseRef;
        private final CommentType commentType;
        private final String comment;

        public LicenseGroup(String licenseRef, String licenseInfo, CommentType commentType, String comment) {
            this.licenseRef = licenseRef;
            this.commentType = commentType;
            this.comment = comment;
        }

        public String getLicenseRef() {
            return licenseRef;
        }

        public CommentType getCommentType() {
            return commentType;
        }

        public String getComment() {
            return comment;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.licenseRef);
            hash = 89 * hash + Objects.hashCode(this.commentType);
            hash = 89 * hash + Objects.hashCode(this.comment);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LicenseGroup other = (LicenseGroup) obj;
            if (!Objects.equals(this.licenseRef, other.licenseRef)) {
                return false;
            }
            if (!Objects.equals(this.comment, other.comment)) {
                return false;
            }
            if (this.commentType != other.commentType) {
                return false;
            }
            return true;
        }

        
        
        @Override
        public int compareTo(LicenseGroup o) {
            String licenseNameA = nvl(getLicenseRef(), "");
            String licenseNameB = nvl(o.getLicenseRef(), "");
            
            if(ALV2.equals(licenseNameA) && (! ALV2.equals(licenseNameB))) {
                return -1;
            } else if(ALV2.equals(licenseNameB) && (! ALV2.equals(licenseNameA))) {
                return 1;
            }
            
            int result = licenseNameA.compareTo(licenseNameB);
            if(result != 0) {
                return result;
            }
            
            String commentTypeA = getCommentType() != null ? getCommentType().name() : "";
            String commentTypeB = o.getCommentType() != null ? o.getCommentType().name() : "";
            result = commentTypeA.compareTo(commentTypeB);
            if (result != 0) {
                return result;
            }
            
            String commentA = nvl(getComment(), "");
            String commentB = nvl(o.getComment(), "");
            result = commentA.compareTo(commentB);
            return result;
        }
        
        private static String nvl(String input, String replacement) {
            if(input == null) {
                return replacement;
            } else {
                return input;
            }
        }
    }
}
