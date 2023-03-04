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
