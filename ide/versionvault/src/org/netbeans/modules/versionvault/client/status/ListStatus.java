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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.client.status;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.ClearcaseException;
import org.netbeans.modules.versionvault.client.Arguments;
import org.netbeans.modules.versionvault.client.ClearcaseCommand;
import org.netbeans.modules.versionvault.client.ExecutionUnit;

/**
 * Retrieves a files {@link FileEntry} (or entries if directory and {@link #handleChildren} true) 
 * by calling 'ct ls' first and 'ct lsco' to get additional info for checkedout files eventually.
 * 
 * @author Tomas Stupka
 */
public class ListStatus extends ExecutionUnit {

    private static String EXTENDED_NAMING_SYMBOL = Clearcase.getInstance().getExtendedNamingSymbol();
    private static String RULE_PREFIX = "Rule:"; 
           
    private static Pattern typePattern = Pattern.compile("(" + 
        FileEntry.TYPE_VERSION +"|" + FileEntry.TYPE_DIRECTORY_VERSION + "|" + FileEntry.TYPE_FILE_ELEMENT + "|" + 
        FileEntry.TYPE_DIRECTORY_ELEMENT + "|" + FileEntry.TYPE_VIEW_PRIVATE_OBJECT+ "|" + FileEntry.TYPE_DERIVED_OBJECT + "|" + 
        FileEntry.TYPE_DERIVED_OBJECT_VERSION + "|" + FileEntry.TYPE_SYMBOLIC_LINK + ")" + 
        "( +)(.*)");
   
    private static Pattern annotationPattern = Pattern.compile("(.*?)(\\[.*\\])");
    private static Pattern checkedoutPattern = Pattern.compile("(.*?\\" + File.separator + "CHECKEDOUT)( +from +(.+?))?");

    private static String OUTPUT_DELIMITER = "<~=~>";
    private static String RESERVED = "reserved";
    
    /** The file to get the FileEntry(s) for **/
    private final File file;        
    
    /** Sets the --directory switch for the cleartool commands **/
    private final boolean directory;
    
    /** Structured output from the commands **/ 
    private Map<File, FileEntry> output = new HashMap<File, FileEntry>();
    
    /**
     * Creates a ListStatus instance
     * @param file to get the {@link FileEntry}(s) for
     * @param directory Sets the --directory switch for the cleartool commands
     */
    public ListStatus(File file, boolean directory) {        
        this.file = file;
        this.directory = directory;
        
        add(new ListCommand());        
    }
      
    /**
     * Returns structured cleartool commands (ls, lsco) output
     * 
     * @return
     * @see FileEntry 
     */
    public Collection<FileEntry> getOutput() {
        return output.values();
    }

    private void listCheckouts() {
        add(new LSCOCommand());
    }
        
    private class ListCommand extends ClearcaseCommand {                
        @Override
        public void prepareCommand(Arguments arguments) throws ClearcaseException {
            arguments.add("ls");
            arguments.add("-long");
            if(file.isDirectory() && directory) {
                arguments.add("-directory");
            }
            arguments.add(file.getAbsoluteFile());
        }

        @Override
        protected boolean isErrorMessage(String s) {
            s = s.toLowerCase();
            return !(s.startsWith("cleartool: error: unable to access") && s.endsWith("no such file or directory."));            
        }
               
        @Override
        public void outputText(String line) {
            FileEntry fe = parseLSOutput(line); 
            if(fe != null) {
                if(fe.isCheckedout()) {
                    listCheckouts();
                }    
                if(fe.isEclipsed()) {
                    // there may be two entries for an eclipsed file
                    // we wan't the one with the [eclipsed] annotation 
                    output.put(fe.getFile(), fe);                       
                } else {
                    FileEntry oldEntry = output.get(fe.getFile());
                    // don't store the entry if there is already one
                    // for the same file and it is eclipsed,
                    // as we know that eclipsed files may return two entries
                    if(oldEntry == null || !oldEntry.isEclipsed() ) { 
                        output.put(fe.getFile(), fe);                       
                    }     
                }                
            }            
        }        
        private FileEntry parseLSOutput(String outputLine) {        
            if(outputLine == null) {
                return null;
            }
            try {
                Matcher typeMatcher = typePattern.matcher(outputLine);
                if(typeMatcher.matches()) {
                    String type = typeMatcher.group(1);
                    String fileDesc = typeMatcher.group(3);
                    String rule = "";

                    String filePath = null;       
                    String annotation = null;
                    FileVersionSelector version = null;
                    FileVersionSelector originVersion = null;

                    int idxAt = fileDesc.lastIndexOf(EXTENDED_NAMING_SYMBOL);

                    if(idxAt > -1) {                

                        // rip of the Rule part - "Rule: ... " 
                        int idxRule = fileDesc.lastIndexOf(RULE_PREFIX);                        
                        if(idxRule > -1) {
                            rule = fileDesc.substring(idxRule + RULE_PREFIX.length()).trim();
                            fileDesc = fileDesc.substring(0, idxRule).trim();                            
                        }                
                        filePath = fileDesc.substring(0, idxAt).trim();
                        String extendedPathPart = fileDesc.substring(idxAt + EXTENDED_NAMING_SYMBOL.length()).trim();

                        // rip of the Annotation - e.g [hijacked]
                        Matcher annotationMatcher = annotationPattern.matcher(extendedPathPart);
                        if(annotationMatcher.matches()) {
                           annotation = annotationMatcher.group(2).trim();
                           extendedPathPart = annotationMatcher.group(1).trim();
                        } 

                        String branch;
                        String originBranch = null;
                        Matcher checkedoutMatcher = checkedoutPattern.matcher(extendedPathPart);
                        if(checkedoutMatcher.matches()) {
                            branch = checkedoutMatcher.group(1);
                            originBranch = checkedoutMatcher.group(3);                                        
                        } else {
                            branch = extendedPathPart;
                        }

                        // XXX originVersion should be populated even if the file isn't checked out
                        originVersion = originBranch != null ? FileVersionSelector.fromString(originBranch) : null;
                        version = FileVersionSelector.fromString(branch);

                    } else {
                        filePath = fileDesc.trim();
                    }           
                    return new FileEntry(type, new File(filePath), originVersion, version, annotation, rule, false, null);
                } else {
                    Clearcase.LOG.warning("Unknownn file classification: \"" + outputLine + "\"");                    
                    return null; 
                }
            } catch (Exception e) {
                Clearcase.LOG.log(Level.SEVERE, "Error while parsing [" + outputLine + "]", e);                
            }                    
            return null;
        }           
    }
    
    private class LSCOCommand extends ClearcaseCommand {            
        @Override
        public void prepareCommand(Arguments arguments) throws ClearcaseException {
            arguments.add("lsco");       
            arguments.add("-fmt");
            arguments.add("\"%En" + OUTPUT_DELIMITER + "%u" + OUTPUT_DELIMITER + "%Rf\\n\"");
            if(file.isDirectory() && directory) {
                arguments.add("-directory");
            }
            arguments.add("-me");
            arguments.add("-cview");            
            arguments.add(file.getAbsoluteFile());
        }

        @Override
        protected boolean isErrorMessage(String s) {
            s = s.toLowerCase();
            return !s.startsWith("cleartool: error: pathname not found:");
        }        
        
        @Override
        public void outputText(String line) {
            parseOutput(line);
        }
    
        private void parseOutput(String outputLine) {        
            try {
                String st[] = outputLine.split(OUTPUT_DELIMITER);        
                if(st.length < 3) {
                    // this might happen - e.g. the file got cheked in just between the
                    // 'ct ls' and the 'ct lsco' commands
                    // XXX mark the file as invalid and rerun the status for it
                    return;
                }
                File file = new File(st[0]);
                String user = st[1];
                boolean reserved = st[2].equals(RESERVED);
                
                FileEntry fe = output.get(file);
                if(fe != null) {
                    output.put(
                        file, 
                        new FileEntry(
                            fe.getType(), 
                            fe.getFile(), 
                            fe.getOriginVersion(), 
                            fe.getVersion(), 
                            fe.getAnnotation(), 
                            fe.getRule(),
                            reserved,
                            user));
                }               
            } catch (Exception e) {
                Clearcase.LOG.log(Level.INFO, "Error while parsing [" + outputLine + "]", e);                
            }        
        }
    }

}
