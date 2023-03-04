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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/** Expand the comma-separated list of properties to 
 *  their values and assing it to single property
 * 
 * @author Michal Zlamal
 */
public class ResolveList extends Task {
    
    private List<String> properties;
    private String name;
    private Mapper mapper;
    private File dir;
    private String path;
    private Set<String> modules;
    private boolean ignoreMissing;

    /** Comma-separated list of properties to expand */
    public void setList (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        properties = new ArrayList<>();
        while (tok.hasMoreTokens ())
            properties.add(tok.nextToken ());
    }

    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new HashSet<>();
        while (tok.hasMoreTokens ())
            modules.add(tok.nextToken ());
    }
    
    public void setIgnoreMissing(boolean m) {
        this.ignoreMissing = m;
    }

    /** New property name */
    public void setName(String s) {
        name = s;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    /** Mapper to be applied to each property in the list before its
     * value is taken
     */
    public void addMapper(Mapper m) {
        this.mapper = m;
    }

    /** Name of the path to fill with fully qualified paths to projects.
     */
    public void setPath(String pathName) {
        this.path = pathName;
    }

    @Override
    public void execute () throws BuildException {
        if (name == null) throw new BuildException("name property have to be set", getLocation());

        if (path != null && dir != null) {
            seekHarderExecute();
            return;
        }

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

    private void seekHarderExecute() {
        StringBuilder value = new StringBuilder();
        Path full = new Path(getProject());
        String prefix = "";
        for (String property: properties) {
            String[] props;
            if (mapper != null) {
                props = mapper.getImplementation().mapFileName(property);
            } else {
                props = new String[] { property };
            }
            final FileUtils fileUtils = FileUtils.getFileUtils();

            String clusterDir = getProject().getProperty(property + ".dir");
            if (clusterDir == null) {
                throw new BuildException(property + ".dir is not defined");
            }
            File cluster = fileUtils.resolveFile(dir, clusterDir);

            for (String p : props) {
                String pval = getProject().getProperty(p);
                if (pval == null) {
                    if (ignoreMissing) {
                        continue;
                    } else {
                        throw new BuildException("Missing definition for " + p);
                    }
                    
                }
                String[] pValues = pval.split(",");

                for (String oneValue : pValues) {
                    if (modules != null && !modules.contains(oneValue)) {
                        continue;
                    }
                    File oneFile = fileUtils.resolveFile(dir, oneValue);
                    if (!nbProjectExists(oneFile)) {
                        File sndFile = fileUtils.resolveFile(cluster, oneValue);
                        if (!nbProjectExists(sndFile)) {
                            throw new BuildException("Cannot resolve " + oneValue + ". Neither one exist:\n  " + oneFile + "\n  " + sndFile);
                        }
                        oneValue = clusterDir + "/" + oneValue;
                        oneFile = sndFile;
                    }

                    if (oneValue != null && oneValue.length() > 0) {
                        value.append(prefix).append(oneValue);
                        full.createPathElement().setLocation(oneFile);
                        prefix = ",";
                    }
                }
            }
        }

        getProject().setNewProperty(name, value.toString());
        getProject().setNewProperty(path, full.toString());
    }

    private static boolean nbProjectExists(File d) {
        File projectXml = new File(d, "nbproject" + File.separatorChar + "project.xml");
        return projectXml.exists();
    }
}
