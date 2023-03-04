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

package org.netbeans.modules.subversion.client.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Stupka
 */
public class Parser {

    private static Parser instance;

    private  Parser() {
    }        
    
    public static Parser getInstance() {
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
    
    public class Line {
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
            
    private class LinePattern {
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
        new LinePattern("[ADUCGE ][ADUCG ][BC ][C ] ([^ ].+)",                   0, -1),
        new LinePattern("([CGU ])([CGU ])   (.+)",                               2, -1),
        new LinePattern("D    ([^ ].+)",                                         0, -1),
        new LinePattern("A    ([^ ].+)",                                         0, -1),
        new LinePattern("A  \\(bin\\)  ([^ ].+)",                                0, -1),
        new LinePattern("A         ([^ ].+)",                                    0, -1),
        new LinePattern("D         ([^ ].+)",                                    0, -1),                
        new LinePattern("Sending        (.+)",                                   0, -1),
        new LinePattern("Adding  \\(bin\\)  (.+)",                               0, -1),
        new LinePattern("Adding         (.+)",                                   0, -1),
        new LinePattern("Deleting       (.+)",                                   0, -1),       
        new LinePattern("Updated to revision (\\d+)\\.",                        -1,  0),        
        new LinePattern("Update complete\\."),        
        new LinePattern("Updated external to revision (\\d+)\\.",               -1,  0),
        new LinePattern("Committed revision (\\d+)\\.",                         -1,  0),        
        new LinePattern("External update complete\\."),        
        new LinePattern("Checked out revision (\\d+)\\.",                       -1,  0),        
        new LinePattern("Checkout complete\\."),        
        new LinePattern("Checked out external at revision (\\d+)\\.",           -1,  0),        
        new LinePattern("Restored '(.+)'",                                       0, -1),
        new LinePattern("Reverted '(.+)'",                                       0, -1),
        new LinePattern("Failed to revert '(.+)' -- try updating instead\\.",    0, -1),
        new LinePattern("Resolved conflicted state of '(.+)'",                   0, -1),
        new LinePattern("Skipped missing target: '(.+)'",                        0, -1),
        new LinePattern("Skipped '(.+)'",                                        0, -1),
        new LinePattern("Fetching external item into '(.+)'",                    0, -1),
        new LinePattern("Exported external at revision (\\d+)\\.",              -1,  0),
        new LinePattern("Exported revision (\\d+)\\.",                          -1,  0),
        new LinePattern("External at revision (\\d+)\\.",                       -1,  0),
        new LinePattern("At revision (\\d+)\\.",                                -1,  0),
        new LinePattern("External export complete\\."),
        new LinePattern("Export complete\\."),
        new LinePattern("External checkout complete\\."),
        new LinePattern("Performing status on external item at '(.+)'",          0, -1),
        new LinePattern("Status against revision:  *(\\d+)",                    -1,  0),
        new LinePattern("Replacing      (.+)",                                   0, -1),
        new LinePattern("Transmitting file data \\.*"),
        new LinePattern("'(.+)' locked by user.*",                               0, -1),
        new LinePattern("'(.+)' unlocked.*",                                     0, -1) 
    };    
    
}
