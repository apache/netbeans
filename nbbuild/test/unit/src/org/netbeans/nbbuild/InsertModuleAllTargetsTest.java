/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
