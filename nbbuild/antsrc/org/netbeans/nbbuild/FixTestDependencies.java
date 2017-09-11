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

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

// XXX should use DOM, not text manipulation

/**
 * Moves classpath from properties to project.xml
 * @author pzajac
 */
public class FixTestDependencies extends Task {
    /**  entries for unit testing in order to avoid scanning modules
     */
    Set<ModuleListParser.Entry> cachedEntries ;
    
    /** Creates a new instance of FixTestClassPath */
    public FixTestDependencies() {
    }
    
    
    String cnb;
    
    File projectXmlFile;
    public void setProjectXml(File projectXml) {
        projectXmlFile = projectXml;
        
    }
    File propertiesFile;
    public void setPropertiesFile(File properties) {
        this.propertiesFile = properties;
    }
    public void execute() throws BuildException {
        // read project xml
        try {
            // test mode doesn't override project.xml
            boolean testFix = getProject().getProperty("test.fix.dependencies") != null;
            if (projectXmlFile == null || !projectXmlFile.isFile()) {
                throw new BuildException("project.xml file doesn't exist.");
            }
            byte xmlBytes [] = new byte[(int)projectXmlFile.length()];
            FileInputStream prjFis = new FileInputStream(projectXmlFile);
            try {
                prjFis.read(xmlBytes);
            } finally {
                prjFis.close();
            }
            String xml = new String(xmlBytes);
            String oldXsd = "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2";
            int xsdIndex = xml.indexOf(oldXsd);
            if (xsdIndex == -1) {
                xsdIndex = xml.indexOf("<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3");
            }
            if (xsdIndex != -1 || testFix) {
                // increase schema version
                String part1 = xml.substring(0,xsdIndex + oldXsd.length() - 1);
                String part2 = xml.substring(xsdIndex + oldXsd.length(), xml.length());
                xml = part1 + "3" + part2;
                
                ModuleType projectType = ModuleType.NB_ORG;
                if (xml.contains("<suite-component/>")) {
                    projectType = ModuleType.SUITE;
                } else if (xml.contains("<standalone/>")) {
                    projectType = ModuleType.STANDALONE;
                } 
                //grrr
                int typeStart = xml.indexOf("<code-name-base>");
                int typeEnd = xml.indexOf("</code-name-base>");
                if (typeStart <= 0 || typeEnd <= 0 || typeEnd <= typeStart) {
                    throw new BuildException("Parsing of project.xml failed.");
                }
                cnb = xml.substring(typeStart + "<code-name-base>".length(), typeEnd).trim();
                if (cnb.length() <= 0) {
                    throw new BuildException("Invalid codename base:" + cnb);
                }
                final boolean td = xml.contains("<test-dependencies>");
                // test if project.xml contains test-deps
                if (td && !testFix) {
                    // yes -> exit
                    xml = fixOpenideUtil(fixCoreStartup(xml));
                    PrintStream ps = new PrintStream(projectXmlFile);
                    ps.print(xml);
                    ps.close();                  
                    return ;
                }
                Set<ModuleListParser.Entry> entries = getModuleList(projectType);
                Set<String> allCnbs = getCNBsFromEntries(entries);
                // read properties

                // remove modules and test from properties and put it to project.xml

                // unittest
                //
                Set<String> compileCNB = new TreeSet<String>();
                Set<String> compileTestCNB = new TreeSet<String>();
                Set<String> runtimeCNB = new TreeSet<String>();
                Set<String> runtimeTestCNB = new TreeSet<String>();

                Properties projectProperties = getTestProperties();
                boolean found;
                found = readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.unit.cp",allCnbs,entries);
                found |= readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.unit.cp.extra",allCnbs,entries);
                found |= readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.unit.run.cp",allCnbs,entries);
                found |= readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.unit.run.cp.extra",allCnbs,entries);

                if (!found && !td) {
                    return;
                }

                updateProperties(projectProperties,new String[]{"test.unit.cp","test.unit.cp.extra","test.unit.run.cp","test.unit.run.cp.extra"});

                StringWriter writer = new StringWriter();
                PrintWriter buffer = new PrintWriter(writer);
                buffer.println("");
                buffer.println("          <test-dependencies>");
                buffer.println("              <test-type>");
                buffer.println("                  <name>unit</name>");
                addDependency(buffer,cnb,true,true,false);

                runtimeCNB.removeAll(compileCNB);
                //compileCNB.removeAll(runtimeCNB);
                compileCNB.addAll(compileTestCNB);
                runtimeTestCNB.removeAll(compileTestCNB);
                runtimeCNB.addAll(runtimeTestCNB);
                addDependencies(buffer,compileCNB,compileTestCNB,true,false);
                addDependencies(buffer,runtimeCNB,runtimeTestCNB,false,false);
                buffer.println("              </test-type>");

                // qa functional tests
                compileCNB.clear();
                runtimeCNB.clear();
                compileTestCNB.clear();
                runtimeTestCNB.clear();

                readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.qa-functional.cp",allCnbs,entries);
                readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.qa-functional.cp.extra",allCnbs,entries);

                readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.qa-functional.runtime.cp",allCnbs,entries);
                readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.qa-functional.runtime.extra",allCnbs,entries);
                if (!compileTestCNB.isEmpty() || !compileCNB.isEmpty() || !runtimeTestCNB.isEmpty() || !runtimeCNB.isEmpty()) {
                    buffer.println("              <test-type>");
                    buffer.println("                  <name>qa-functional</name>");

                    addDependencies(buffer,compileCNB,compileTestCNB,true,false);
                    addDependencies(buffer,runtimeCNB,runtimeTestCNB,false,false);
                    buffer.println("              </test-type>");
                }
                
                buffer.println("            </test-dependencies>");
                updateProperties(projectProperties,new String[]{"test.qa-functional.cp","test.qa-functional.cp","test.qa-functional.runtime.cp","test.qa-functional.runtime.extra"});

                // merge project properties
                String MODULE_DEP_END = "</module-dependencies>";
                int moduleDepEnd = xml.indexOf(MODULE_DEP_END);
                if (moduleDepEnd == -1) {
                    MODULE_DEP_END = "<module-dependencies/>";
                    moduleDepEnd = xml.indexOf(MODULE_DEP_END);
                }
                if (moduleDepEnd == -1) {
                    throw new BuildException("No module dependency found.");
                }
                moduleDepEnd += MODULE_DEP_END.length();
                StringBuffer resultXml = new StringBuffer();
                resultXml.append(xml.substring(0,moduleDepEnd));
                resultXml.append(writer.toString());
                // windows
                if (xml.charAt(moduleDepEnd) == '\r') {
                    moduleDepEnd++;
                }
                resultXml.append(xml.substring(moduleDepEnd + 1, xml.length()));
                if (!testFix) {
                   PrintStream ps = new PrintStream(projectXmlFile);
                   ps.print(fixOpenideUtil(
                           fixCoreStartup(
                                   resultXml.toString()
                           )));
                   ps.close();
                } else {
                    System.out.println(resultXml);
                }
            }
            
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
        // store project.properties and project.xml
    }

    private Set<ModuleListParser.Entry> getModuleList(final ModuleType projectType) throws IOException {
        if (cachedEntries == null ) {
          // scan for all modules
            ModuleListParser listParser = new ModuleListParser(getProject().getProperties(), projectType, getProject());
            return  listParser.findAll();
        } else {
            // used by FixTestDependenciesTest
            return cachedEntries;
        }
    }
    
    private Set<String> getCNBsFromEntries(Set<ModuleListParser.Entry> entries) {
        Set<String> cnbs = new HashSet<String>();
        for (ModuleListParser.Entry e : entries) {
            cnbs.add(e.getCnb());
        }
        return cnbs;
    }
    /** parses all codenamebases from path
     */
     boolean readCodeNameBases(Set<String> compileCNB,
            Set <String> testsCNB,
            Properties projectPropertis,
            String property,
            Set <String> allCnbs,
            Set <ModuleListParser.Entry> entries) {
        String prop = projectPropertis.getProperty(property);
        StringBuffer newProp = new StringBuffer();
        if (prop !=   null) {
            StringTokenizer tokenizer = new StringTokenizer(prop,";:\\");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                boolean found = false;
                if (token.length() > 1) {
                    int lastSlash = token.lastIndexOf("/");
                    int lastDot = token.lastIndexOf(".");
                    // check if the file is module
                    //
                    // the boot.jar is org.netbeans.bootstrap
                    if (token.endsWith("lib/boot.jar")) {
                         compileCNB.add("org.netbeans.bootstrap");
                         found = true;
                    } else if (token.endsWith("core/core.jar")) {
                        compileCNB.add("org.netbeans.core.startup");
                        compileCNB.add("org.netbeans.core.startup.base");
                        found = true;
                    } else if (lastSlash != -1 && lastDot != -1 && lastSlash + 1< lastDot ) {
                        String codeBaseName = token.substring(lastSlash + 1, lastDot);
                        codeBaseName = codeBaseName.replace('-','.');
                        if (allCnbs.contains(codeBaseName)) {
                            compileCNB.add(codeBaseName);
                            found = true;
                        } else  {
                            String name = token.substring(lastSlash + 1, token.length());
                            // check if the file is wrapped library
                            String wrapCNB = null;
                            for (ModuleListParser.Entry entry : entries) {
                                  File extensions [] = entry.getClassPathExtensions();
                                  if (extensions != null) {
                                      for (File f : extensions) {
                                          if (f.getPath().endsWith( name)) {
                                              if (wrapCNB != null) {
                                                // collision
                                                  found = false;
                                                  System.out.println("wrapped? " + entry.getCnb() + " -> " + token + " = " + f);
                                              } else {
                                                  wrapCNB = entry.getCnb();
                                                  found = true;
                                              }
                                          }
                                      }    
                                  }

                            }
                            if (found && wrapCNB != null && allCnbs.contains(wrapCNB)) {
                                  compileCNB.add(wrapCNB);
                            }
                         }
                        // check if the dependency is dependency on test
                    } else {
                        int prjEnd = token.indexOf("/build/test/unit/class");
                        if (prjEnd != -1) {
                            // get the project folder
                            int firstSlash = token.indexOf("/");
                            if (firstSlash != -1 && firstSlash + 1 < prjEnd) {
                                String  prjFolder = token.substring(firstSlash + 1, prjEnd);
                                String  codebaseName = getCNBForFolder(prjFolder,entries);
                                if (codebaseName == null) {
                                    log("No code name base found for file " + token);
                                } else {
                                    testsCNB.add(codebaseName);
                                    found = true;
                                }
                            }
                        }
                    }
                    // check if the file is file
                    //
                    if (found == false) {
                        if (newProp.length() > 0) {
                            newProp.append(":");
                        }
                        // windows platform
                        token = token.replace(File.separatorChar,'/');
                        newProp.append(token);
                    }
                }
            } // while
            projectPropertis.setProperty(property,newProp.toString());
            return true;
        }
        return false;
    }
    
    private void addDependencies(PrintWriter buffer, Set<String> moduleCNB, Set<String> testCNB, boolean compile, boolean recursive) {
        for (String cnb : moduleCNB) {
            addDependency(buffer,cnb,compile,recursive,testCNB.contains(cnb));
        }
    }
    
    private void addDependency(PrintWriter buffer, String cnb, boolean compile, boolean recursive, boolean test) {
        buffer.println("                  <test-dependency>");
        buffer.println("                      <code-name-base>" + cnb + "</code-name-base>");
        if (recursive) {
            buffer.println("                      <recursive/>");
        }
        if (compile) {
            buffer.println("                      <compile-dependency/>");
        }
        if (test) {
            buffer.println("                      <test/>");
        }
        buffer.println("                  </test-dependency>");
        
    }
    
    private Properties getTestProperties() throws IOException {
        if (propertiesFile == null || !propertiesFile.isFile()) {
            return new Properties();
        }
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        try {
            props.load(fis);
        } finally {
            fis.close();
        }
        return props;
    }
    
    /** @return codeNameBase of project for relative folder prjFolder
     */
    private String getCNBForFolder(String prjFolder, Set<ModuleListParser.Entry> entries) {
        for (ModuleListParser.Entry elem : entries) {
            if (prjFolder.equals(elem.getNetbeansOrgPath())) {
                return elem.getCnb();
            }
        }
        return null;
    }
    private void updateProperties(Properties projectProperties,String names[]) {
        if (propertiesFile == null) {
            return;
        }
        try {
            List<String> lines = new ArrayList<String>();
            if (propertiesFile.isFile()) {
                // read properties
                BufferedReader reader = new BufferedReader (new FileReader(propertiesFile));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
            }
            
            // merge properties
            for (String propName : names) {
               String value = projectProperties.getProperty(propName);
               lines = replaceProperty(propName,value,lines);    
            }
            if (lines.isEmpty() && !propertiesFile.isFile()) {
                return;
            }

            // store properties
            PrintStream ps = new PrintStream(propertiesFile);
            for (String l : lines) {
                ps.println(l);
            }
            ps.close();
            
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
        
    }

    private List<String> replaceProperty(String name, String value, List<String> lines) {
        List<String> retLines = new ArrayList<String>();
        for (int i = 0 ; i < lines.size() ; i++) {
            String line = lines.get(i);
            String trimmedLine = line.trim();
            int eqIdx = trimmedLine.indexOf("=");
            if (eqIdx != -1) {
                String pName = line.substring(0,eqIdx).trim();
                if (pName.equals(name)) {
                    // skip property
                    for (; i < lines.size() && lines.get(i).trim().endsWith("\\") ; i++) ;
                    // append new property 
                    if (value != null && !value.trim().equals("")) {
                        retLines.add(name + "=" + value);
                    }
                    continue;
                }
            }
            // either empty line, comment or other property
            retLines.add(line);
        }
        return retLines;
    }
    
    private String fixCoreStartup(String xml) {
        Pattern l = Pattern.compile(
                "^ *<test-dependency>[^/]*" +
                "<code-name-base>org.netbeans.core.startup.base</code-name-base>", Pattern.MULTILINE);
        if (l.matcher(xml).find()) {
            return xml;
        }

        Pattern p = Pattern.compile(
                "^ *<test-dependency>[^/]*" +
                "<code-name-base>org.netbeans.core.startup</code-name-base>", Pattern.MULTILINE);

        Matcher m = p.matcher(xml);
        if (m.find()) {
            final String txt = "</test-dependency>";
            final int s = m.start();
            int end = xml.indexOf(txt, s);
            if (end == -1) {
                throw new BuildException("No end of dependency " + xml);
            }
            final int e = end + txt.length();
            String dep = xml.substring(s, e);
            return xml.substring(0, s) + dep + '\n' + dep.replace("org.netbeans.core.startup", "org.netbeans.core.startup.base") + xml.substring(e);
        }
        return xml;
    }

    private String fixOpenideUtil(String xml) {
        Pattern l = Pattern.compile(
                "^ *<test-dependency>[^/]*" +
                "<code-name-base>org.openide.util.lookup</code-name-base>", Pattern.MULTILINE);
        if (l.matcher(xml).find()) {
            return xml;
        }

        Pattern p = Pattern.compile(
                "^ *<test-dependency>[^/]*" +
                "<code-name-base>org.openide.util</code-name-base>", Pattern.MULTILINE);

        Matcher m = p.matcher(xml);
        if (m.find()) {
            final String txt = "</test-dependency>";
            final int s = m.start();
            int end = xml.indexOf(txt, s);
            if (end == -1) {
                throw new BuildException("No end of dependency " + xml);
            }
            final int e = end + txt.length();
            String dep = xml.substring(s, e);
            return xml.substring(0, s) + dep + '\n' + dep.replace("org.openide.util", "org.openide.util.lookup") + xml.substring(e);
        }
        return xml;
    }

}
