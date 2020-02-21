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
package org.netbeans.modules.subversion.remote.client.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 
 */
public class Parser {

    private static Parser instance;

    private  Parser() {
    }        
    
    public static synchronized Parser getInstance() {
        if(instance == null) {
            instance = new Parser();            
        }
        return instance;
    }
    
    public Line parse(String value) {
        for (LinePattern linePattern : patterns) {
            Line line = linePattern.parse(value);
            if(line != null) {
                return line;
            }
        }
        return null;
    }
    
    public static class Line {
        private final String path;
        private final long revision;
        public Line(String path, long revision) {
            this.path = path;
            this.revision = revision;
        }
        public String getPath() {
            return path;
        }
        public long getRevision() {
            return revision;
        }
    }         
            
    private static class LinePattern {
        private final int pathIdx;
        private final int revisionIdx;
        private final Pattern pattern;
        public LinePattern(String re) {
            this(re, -1, -1);
        }        
        public LinePattern(String re, int path, int revision) {
            pattern = Pattern.compile(re);
            this.pathIdx = path; 
            this.revisionIdx = revision;
        }        
        Line parse(String value) {
            Matcher m = pattern.matcher(value);
            if(m.matches()) {
                String path = null;
                long rev = -1;
                if(pathIdx > -1) {
                    path = m.group(pathIdx + 1);
                }            
                if(revisionIdx > -1) {
                    rev = Long.parseLong(m.group(revisionIdx + 1));                    
                }            
                return new Line(path, rev);
            }
            return null;
        }        
    }    
    
    private LinePattern[] patterns = {
        new LinePattern("[ADUCGE ][ADUCG ][BC ][C ] ([^ ].+)",                   0, -1), //NOI18N
        new LinePattern("([CGU ])([CGU ])   (.+)",                               2, -1), //NOI18N
        new LinePattern("D    ([^ ].+)",                                         0, -1), //NOI18N
        new LinePattern("A    ([^ ].+)",                                         0, -1), //NOI18N
        new LinePattern("A  \\(bin\\)  ([^ ].+)",                                0, -1), //NOI18N
        new LinePattern("A         ([^ ].+)",                                    0, -1), //NOI18N
        new LinePattern("D         ([^ ].+)",                                    0, -1), //NOI18N             
        new LinePattern("Sending        (.+)",                                   0, -1), //NOI18N
        new LinePattern("Adding  \\(bin\\)  (.+)",                               0, -1), //NOI18N
        new LinePattern("Adding         (.+)",                                   0, -1), //NOI18N
        new LinePattern("Deleting       (.+)",                                   0, -1), //NOI18N       
        new LinePattern("Updated to revision (\\d+)\\.",                        -1,  0), //NOI18N        
        new LinePattern("Update complete\\."), //NOI18N
        new LinePattern("Updated external to revision (\\d+)\\.",               -1,  0), //NOI18N
        new LinePattern("Committed revision (\\d+)\\.",                         -1,  0), //NOI18N
        new LinePattern("External update complete\\."), //NOI18N
        new LinePattern("Checked out revision (\\d+)\\.",                       -1,  0), //NOI18N
        new LinePattern("Checkout complete\\."), //NOI18N
        new LinePattern("Checked out external at revision (\\d+)\\.",           -1,  0), //NOI18N
        new LinePattern("Restored '(.+)'",                                       0, -1), //NOI18N
        new LinePattern("Reverted '(.+)'",                                       0, -1), //NOI18N
        new LinePattern("Failed to revert '(.+)' -- try updating instead\\.",    0, -1), //NOI18N
        new LinePattern("Resolved conflicted state of '(.+)'",                   0, -1), //NOI18N
        new LinePattern("Skipped missing target: '(.+)'",                        0, -1), //NOI18N
        new LinePattern("Skipped '(.+)'",                                        0, -1), //NOI18N
        new LinePattern("Fetching external item into '(.+)'",                    0, -1), //NOI18N
        new LinePattern("Exported external at revision (\\d+)\\.",              -1,  0), //NOI18N
        new LinePattern("Exported revision (\\d+)\\.",                          -1,  0), //NOI18N
        new LinePattern("External at revision (\\d+)\\.",                       -1,  0), //NOI18N
        new LinePattern("At revision (\\d+)\\.",                                -1,  0), //NOI18N
        new LinePattern("External export complete\\."), //NOI18N
        new LinePattern("Export complete\\."), //NOI18N
        new LinePattern("External checkout complete\\."), //NOI18N
        new LinePattern("Performing status on external item at '(.+)'",          0, -1), //NOI18N
        new LinePattern("Status against revision:  *(\\d+)",                    -1,  0), //NOI18N
        new LinePattern("Replacing      (.+)",                                   0, -1), //NOI18N
        new LinePattern("Transmitting file data \\.*"), //NOI18N
        new LinePattern("'(.+)' locked by user.*",                               0, -1), //NOI18N
        new LinePattern("'(.+)' unlocked.*",                                     0, -1)  //NOI18N
    };    
    
}
