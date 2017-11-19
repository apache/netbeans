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

package org.netbeans.nbbuild;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;

/** Expand the comma-separated list of properties to 
 *  their values and assing it to single property
 * 
 * @author Michal Zlamal
 */
public class ResolveList extends Task {
    
    private List<String> properties;
    private String name;
    private Mapper mapper;

    /** Comma-separated list of properties to expand */
    public void setList (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        properties = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            properties.add(tok.nextToken ());
    }

    /** New property name */
    public void setName(String s) {
        name = s;
    }

    /** Mapper to be applied to each property in the list before its
     * value is taken
     */
    public void addMapper(Mapper m) {
        this.mapper = m;
    }

    @Override
    public void execute () throws BuildException {
        if (name == null) throw new BuildException("name property have to be set", getLocation());
        String value = "";
        String prefix = "";
        for (String property: properties) {
            String[] props;
            if (mapper != null) {
                props = mapper.getImplementation().mapFileName(property);
            } else {
                props = new String[] { property };
            }

            for (String p : props) {
                String oneValue = getProject().getProperty( p );
                if (oneValue != null && oneValue.length() > 0) {
                    value += prefix + oneValue;
                    prefix = ",";
                }
            }
        }
        
        getProject().setNewProperty(name,value);
    }        
}
