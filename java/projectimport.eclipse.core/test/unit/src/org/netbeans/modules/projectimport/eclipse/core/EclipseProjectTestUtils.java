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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;

/**
 *
 * @author david
 */
public class EclipseProjectTestUtils {

    public static EclipseProject createEclipseProject(File proj, DotClassPath cp) throws IOException {
        return createEclipseProject(proj, cp, null, null);
    }
    
    public static EclipseProject createEclipseProject(File proj, DotClassPath cp, Workspace w, String name) throws IOException {
        return createEclipseProject(proj, cp, w, name, new ArrayList<Link>());
    }
    
    public static EclipseProject createEclipseProject(File proj, DotClassPath cp, Workspace w, String name, List<Link> links) throws IOException {
        EclipseProject ep = new EclipseProject(proj);
        if (w != null) {
            ep.setName(name);
            ep.setWorkspace(w);
            ep.setLinks(links);
            w.addProject(ep);
        }
        ep.setClassPath(cp);
        ep.resolveContainers(new ArrayList<String>(), false);
        return ep;
    }
    
    public static DotClassPathEntry createDotClassPathEntry(String ... keyvalue) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i=0; i<keyvalue.length; i = i +2) {
            map.put(keyvalue[i], keyvalue[i+1]);
        }
        return new DotClassPathEntry(map, null);
    }
    
    public static Workspace createWorkspace(File workspace, Workspace.Variable ... variables) {
        Workspace w = new Workspace(workspace);
        for (Workspace.Variable v : variables) {
            w.addVariable(v);
        }
        return w;
    }
}
