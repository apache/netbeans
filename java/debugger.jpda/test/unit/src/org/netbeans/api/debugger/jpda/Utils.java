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

package org.netbeans.api.debugger.jpda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;

/**
 * Test utilities.
 *
 * @author Martin Entlicher
 */
public class Utils {
    
    private static final String LINE_BREAKPOINT = "LBREAKPOINT";
    private static final String STOP_POSITION = "STOP";
    
    /**
     * Get the string representation of the URL for the given path.
     */
    public static String getURL(String path) {
        File file = new File(path);
        try {
            return file.toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return path;
        }
    }

    /*public static List<LineBreakpoint> getBreakpoints(URL source) throws Exception{
        List<LineBreakpoint> breakpoints = new ArrayList<LineBreakpoint>();
        BufferedReader r = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            String line;
            int lineNo = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.endsWith(LINE_BREAKPOINT)) {
                    breakpoints.add(LineBreakpoint.create(source.toString(), lineNo));
                }
            }
        } finally {
            r.close();
        }
        return breakpoints;
    }*/
    
    public static BreakPositions getBreakPositions(URL source) throws Exception{
        BreakPositions breakPositions = new BreakPositions(source);
        BufferedReader r = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            String line;
            int lineNo = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.endsWith(LINE_BREAKPOINT)) {
                    breakPositions.addBreakpoint(LineBreakpoint.create(source.toString(), lineNo));
                }
                if (line.indexOf(STOP_POSITION) > 0) {
                    String stepID = line.substring(line.indexOf(STOP_POSITION) + STOP_POSITION.length()).trim();
                    int index = 0;
                    while (index < stepID.length() && !Character.isWhitespace(stepID.charAt(index))) {
                        index++;
                    }
                    if (index < stepID.length()) {
                        stepID = stepID.substring(0, index);
                    }
                    breakPositions.addStopPosition(lineNo, stepID);
                }
            }
        } finally {
            r.close();
        }
        return breakPositions;
    }
    
    public static BreakPositions getBreakPositions(String path) throws Exception {
        return getBreakPositions(new URL(getURL(path)));
    }
    
    public static final class BreakPositions {
        
        private URL source;
        private List<Breakpoint> breakpoints;
        private List<LineBreakpoint> lineBreakpoints;
        private Map<String, Integer> stops;
        
        BreakPositions(URL source) {
            this.source = source;
            breakpoints = new ArrayList<Breakpoint>();
            lineBreakpoints = new ArrayList<LineBreakpoint>();
            stops = new HashMap<String, Integer>();
        }
        
        void addBreakpoint(Breakpoint brkp) {
            breakpoints.add(brkp);
            if (brkp instanceof LineBreakpoint) {
                lineBreakpoints.add((LineBreakpoint) brkp);
            }
        }
        
        public List<Breakpoint> getBreakpoints() {
            return breakpoints;
        }
        
        public List<LineBreakpoint> getLineBreakpoints() {
            return lineBreakpoints;
        }
        
        void addStopPosition(int line, String stepID) {
            stops.put(stepID, line);
        }
        
        public int getStopLine(String stepID) {
            Integer line = stops.get(stepID);
            if (line == null) {
                return -1;
            } else {
                return line.intValue();
            }
        }
    }
    
}
