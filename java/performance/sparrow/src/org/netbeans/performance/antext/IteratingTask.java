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
package org.netbeans.performance.antext;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import java.util.*;
import java.io.File;

/** Calls a target repeatedly for the number of times specified
 *  by the property <code>count</code>.  Sets a property for the
 *  specified target called <code>index</code>.
 * @author Jesse Glick, Tim Boudreau
 * @see CallTask
 */
public class IteratingTask extends Task {

    private String subTarget;
    public void setTarget(String t) {
        subTarget = t;
    }
    
    public static class Param {
        private String name, value;
        public String getName() {return name;}
        public void setName(String s) {name = s;}
        public String getValue() {return value;}
        public void setValue(String s) {value = s;}
        public void setLocation(File f) {value = f.getAbsolutePath();}
    }
    private List properties = new LinkedList(); // List<Param>
    public void addParam(Param p) {
        properties.add(p);
    }
    
    private int count;
    public void setCount(String count) {
        this.count = Integer.parseInt(count);
    }
    
    public void createProperty (Property p) {
        properties.add(p);
    }
    
    public void execute() throws BuildException {
        if (count == 0) throw new BuildException("Count not set or was set to 0");
        if (subTarget == null) throw new BuildException("No subtarget set.");
        int index = 0;
        while (index < count) {
            Ant callee = (Ant)project.createTask("ant");
            callee.setOwningTarget(target);
            callee.setTaskName(getTaskName());
            callee.setLocation(location);
            callee.init();

            project.getProperties().put ("index", Integer.toString(index));
            /*
            Property p=callee.createProperty();
            p.setName("index");
            p.setValue(Integer.toString(index));
             */
            
            System.out.println("Iteration " + Integer.toString(index));
            Iterator props = properties.iterator();
            while (props.hasNext()) {
                Param p1 = (Param)props.next();
                Property p2 = callee.createProperty();
                p2.setName(p1.getName());
                p2.setValue(p1.getValue());
            }
            
            callee.setDir(project.getBaseDir());
            callee.setAntfile(project.getProperty("ant.file"));
            callee.setTarget(subTarget);
            //callee.setInheritAll(true);
            callee.execute();
            index++;
        }
    }
    
}
