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

package org.netbeans.nbbuild.testdist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * It scans test distribution ant sets to property with name defined by param 
 * 'testListProperty'path with filtered test. The TestDistFilter is used for running tests 
 * in test distribution.
 * <br>
 * 
 * Parameters :
 * <ul>
 *    <li>testtype - unit|qa-functional|etc. (required)
 *    <li>testlistproperty  - store to property path with test folders (separated by ':')
 *    <li>testdistdir - root folder with test distribution
 *    <li>requiredmodules - list of module names required on runtime classpath example:
 *             org-netbeans-modules-masterfs.jar,org-openide-loaders.jar. Only tests 
 *             which contains masterfs and loaders will be stored to testlistproperty value.
 * </ul>
 */
public class TestDistFilter extends Task {
    private File testDistDir;
    Set<TestConf> possibleTests = new HashSet<TestConf>();
    private String testtype;
    private String testListProperty;
    private String requiredModules;
    // TODO customize method names to match custom task
    // property and type (handled by inner class) names
    
    /** represents a test directory
     */
    private static class TestConf {
        File moduleDir;
        TestConf(File moduleDir) {
            this.moduleDir = moduleDir;
        }

        public int hashCode() {
            return moduleDir.hashCode();
        }
        public boolean  equals(Object obj) {
            return  (obj instanceof TestConf) && moduleDir.equals(((TestConf)obj).moduleDir); 
        }
      
        
        File getModuleDir() {
            return moduleDir;
        }
        
    }

    
    public void execute() throws BuildException {
          possibleTests.clear();
          if (getTestListProperty() == null) {
              throw new BuildException("Param testlistproperty is not defined.");
          }
          if (getTestDistDir() == null || !getTestDistDir().exists()) {
              throw new BuildException("Param testdistdir is not defined.");
          }
          String tt = getTesttype();
          findCodeTests(tt);
          define(getTestListProperty(),getTestList());
    }
    /** get path with test dirs separated by :
     */
    private String getTestList() {
        StringBuffer path = new StringBuffer();
        for (Iterator it = possibleTests.iterator() ; it.hasNext() ; ) {
            TestConf tc = (TestConf)it.next();
            if (!matchRequiredModule(tc.getModuleDir())) {
                continue;
            }
            if (path.length() > 0) {
                path.append(':');
            }
            path.append(tc.getModuleDir().getAbsolutePath());
        }
        return path.toString();
    }
    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }


    public String getTesttype() {
        return testtype;
    }

    public void setTesttype(String testtype) {
        this.testtype = testtype;
    }

    public String getTestListProperty() {
        return testListProperty;
    }

    public void setTestListProperty(String testListProperty) {
        this.testListProperty = testListProperty;
    }

    private void findCodeTests(String type) {
          for (TestConf test : getTestList(type)) {
              possibleTests.add(test);
          }
    }

    private List<TestConf> getTestList(String testtype) {
        File root = new File (getTestDistDir(),testtype);
        List <TestConf> testList = new ArrayList<TestConf>();
        if (!root.exists()) {
            return Collections.emptyList();
        }
        File clusters[] = root.listFiles();
        for (int c = 0 ; c < clusters.length ; c++) {
            File cluster = clusters[c];
            if (cluster.isDirectory()) {
                File modules[] = cluster.listFiles();
                for (int m = 0 ; m < modules.length ; m++) {
                    File module = modules[m];
                    if (new File(module, "tests.jar").isFile()) {
                        testList.add(new TestConf(module));
                    }
                }
            }
        }
        return testList;
    }

    
    public File getTestDistDir() {
        return testDistDir;
    }

    public void setTestDistDir(File testDistDir) {
        this.testDistDir = testDistDir;
    }

    public String getRequiredModules() {
        return requiredModules;
    }

    public void setRequiredModules(String requiredModules) {
        this.requiredModules = requiredModules;
    }

    private boolean matchRequiredModule(File path) {
       if (requiredModules == null || requiredModules.trim().length() == 0) {
           return true;
       }
       File pfile = new File(path,"test.properties");
       if (pfile.exists()) {
           Properties props = new Properties();
            try {
                FileInputStream fis = new FileInputStream(pfile);
                try { 
                  props.load(fis);
                  
                  String runCp = props.getProperty("test.unit.run.cp");
                  if (runCp != null) {
                      String paths[] = runCp.split(":");
                      Set reqModules = getRequiredModulesSet();
                      if (reqModules.size() == 0) {
                          return true;
                      }
                      for (int i = 0 ; i < paths.length ; i++) {
                          String p = paths[i];
                          int lastSlash = p.lastIndexOf('/');
                          if (lastSlash != -1) {
                              p = p.substring(lastSlash + 1);
                          } 
                          if (reqModules.contains(p)) {
                              return true;
                          }
                      }
                  }
                } finally {
                  fis.close();  
                }
            } catch(IOException ioe){
                throw new BuildException(ioe);
            }
       }
       return false;        
    }

    private Set<String> getRequiredModulesSet() {
        String names[] = getRequiredModules().split(",");
        return new HashSet<String>(Arrays.asList(names));
    }
}
