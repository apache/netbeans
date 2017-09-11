/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
