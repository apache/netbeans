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

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;

public class InsertModuleAllTargetsTest extends NbTestCase {
    private Project p;
    private File nball;
    
    public InsertModuleAllTargetsTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        String prop = System.getProperty("nb_all");
        assertNotNull("${nb_all} defined", prop);
        nball = new File(prop);
        
        File baseDir = new File(getWorkDir(), "basedir");
        baseDir.mkdirs();
        
        File destDir = new File(getWorkDir(), "destdir");
        destDir.mkdirs();
        
        p = new Project();
        p.init();
        p.setBaseDir(nball);
        p.setProperty("netbeans.dest.dir", destDir.getAbsolutePath());
        p.setProperty("nb_all", nball.getAbsolutePath());
        
        File clusters = new File(new File(nball, "nbbuild"), "cluster.properties");
        assertTrue("cluster.properties file exists", clusters.exists());
        
        Properties clusterProps = new Properties();
        final FileInputStream is = new FileInputStream(clusters);
        clusterProps.load(is);
        is.close();
        
        for (Entry<Object, Object> en : clusterProps.entrySet()) {
            p.setProperty(en.getKey().toString(), en.getValue().toString());
        }
    }
    
    

    public void testInstallAllTargetWithClusters() {
        
        InsertModuleAllTargets insert = new InsertModuleAllTargets();
        insert.setProject(p);
        
        insert.execute();

        Object obj = p.getTargets().get("all-java.source.queries");
        assertNotNull("Target found", obj);
        Target t = (Target)obj;
        
        Set<String> s = depsToNames(t.getDependencies());
        assertEquals("One dep: " + s, 1, s.size());
        assertEquals("Just dep on init", "init", s.iterator().next());
        
        int callTargets = 0;
        for (Task task : t.getTasks()) {
            if (task instanceof CallTarget) {
                callTargets++;
            }
        }
        assertEquals("One call target to build super cluster", 1, callTargets);
    }
    
    
    public void testInstallAllTargetWithoutClusters() {
        InsertModuleAllTargets insert = new InsertModuleAllTargets();
        insert.setProject(p);
        insert.setUseClusters(false);
        insert.execute();

        Object obj = p.getTargets().get("all-java.source.queries");
        assertNotNull("Target found", obj);
        Target t = (Target)obj;
        
        Set<String> s = depsToNames(t.getDependencies());
        assertEquals("Three dependencies: " + s, 5, s.size());
        assertTrue("on init", s.contains("init"));
        assertTrue("on all-openide.util", s.contains("all-openide.util"));
        assertTrue("on all-openide.util.lookup", s.contains("all-openide.util.lookup"));
        assertTrue("on all-api.annotations.common", s.contains("all-api.annotations.common"));
        assertTrue("on all-openide.dialogs", s.contains("all-openide.dialogs"));
        
        int callTargets = 0;
        for (Task task : t.getTasks()) {
            if (task instanceof CallTarget) {
                callTargets++;
            }
        }
        assertEquals("No call targes", 0, callTargets);
    }

    private Set<String> depsToNames(Enumeration en) {
        Set<String> set = new HashSet<String>();
        while (en.hasMoreElements()) {
            String dep = en.nextElement().toString();
            set.add(dep);
        }
        return set;
    }
}
