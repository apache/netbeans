/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.project.ant.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 */
public class VariablesModel {

    private static final String VARIABLE_PREFIX = "var."; // NOI18N
       
    private List<Variable> vars;
    
    VariablesModel() {
        vars = readVariables();
    }
    
    public List<Variable> getVariables() {
        return vars;
    }

    public Variable find(String name) {
        return find(name, vars);
    }
    
    private static Variable find(String name, List<Variable> vs) {
        for (Variable v : vs) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    void remove(Variable var) {
        vars.remove(var);
    }

    void save() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties ep = PropertyUtils.getGlobalProperties();
                boolean change = false;
                List<Variable> old = readVariables();
                for (Variable var : vars) {
                    Variable oldVar = find(var.getName(), old);
                    if (oldVar == null || !oldVar.getValue().equals(var.getValue())) {
                        ep.put(VARIABLE_PREFIX+var.getName(), var.getValue().getAbsolutePath());
                        change = true;
                    }
                    if (oldVar != null) {
                        old.remove(oldVar);
                    }
                }
                for (Variable v : old) {
                    ep.remove(VARIABLE_PREFIX+v.getName());
                    change = true;
                }
                if (change) {
                    try {
                        PropertyUtils.putGlobalProperties(ep);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }
    
    private List<Variable> readVariables() {
        List<Variable> vs = new ArrayList<Variable>();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        for (Map.Entry<String, String> entry : ep.entrySet()) {
            if (entry.getKey().startsWith(VARIABLE_PREFIX)) {
                vs.add(new Variable(entry.getKey().substring(VARIABLE_PREFIX.length()), FileUtil.normalizeFile(new File(entry.getValue()))));
            }
        }
        return vs;
    }
    
    public void add(String name, File location) {
        assert find(name) == null : name;
        vars.add(new Variable(name, location)); 
    }
    
    public String getRelativePath(File path, boolean forDisplay) {
        for (Variable v : vars) {
            if (path.getAbsolutePath().startsWith(v.getValue().getAbsolutePath())) {
                String s;
                if (forDisplay) {
                    s = v.name;
                } else {
                    s = "${var."+v.name+"}"; // NOI18N
                }
                String p = path.getAbsolutePath().substring(v.getValue().getAbsolutePath().length()).replace('\\', '/'); // NOI18N
                if (p.startsWith("/")) { // NOI18N
                    p = p.substring(1);
                }
                return s + "/" + p; // NOI18N
            }
        }
        return null;
    }
    
    
    public static class Variable {
        
        private String name;
        private File value;

        public Variable(String name, File value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public File getValue() {
            return value;
        }

        @Override
        public boolean equals(Object arg0) {
            return name.equals(((Variable)arg0).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        public void setValue(File loc) {
            this.value = loc;
        }

        @Override
        public String toString() {
            return "var["+getName()+"="+getValue().getAbsolutePath()+"]"; // NOI18N
        }
        
        
    }
    
}
