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
package org.netbeans.modules.maven.workspace.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.codehaus.plexus.logging.Logger;

/**
 * netbeansProjectMappings comma separated list of <GAV>=<path> where gav is G:A:V
 * @author mkleint
 */
public class AbstractIDEWorkspaceReader {

    private Logger logger;
    
    private final Map<String, File> mappings;

    public AbstractIDEWorkspaceReader() {
        mappings = new HashMap<String, File>();
        String mapp = System.getenv("netbeansProjectMappings");
        if (mapp != null) {
            StringTokenizer st = new StringTokenizer(mapp, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(tok, "=");
                if (st2.hasMoreTokens()) {
                    String gav = st2.nextToken();
                    if (st2.hasMoreElements()) {
                        String file = st2.nextToken();
                        File f = new File(file);
                        if (f.exists()) {
                            mappings.put(gav, new File(file));
                        }
                    }
                }
            }
        }

    }


    public File findArtifact(String groupId, String artifactId, String baseVersion, String extension, String classifier) {
        File f = mappings.get(groupId + ":" + artifactId + ":" + baseVersion);
        if (f != null) {
            if ("pom".equals(extension)) {
                logger.debug("[NETBEANS] linking artifact to workspace POM:" + new File(f, "pom.xml"));
                return new File(f, "pom.xml");
            }
            if ("jar".equals(extension) && "".equals(classifier)) {
                logger.debug("[NETBEANS] linking artifact to workspace output folder:" + new File(f, "target/classes"));
                return new File(new File(f, "target"), "classes");
            }
            if ("jar".equals(extension) && "tests".equals(classifier)) {
                logger.debug("[NETBEANS] linking artifact to workspace output folder:" + new File(f, "target/test-classes"));
                return new File(new File(f, "target"), "test-classes");
            }
        }
        return null;
    }

    public List<String> findVersions(String groupId, String artifactId) {
        String id = groupId + ":" + artifactId + ":";
        List<String> toRet = new ArrayList<String>();
        for (String s : mappings.keySet()) {
            if (s.startsWith(id)) {
                toRet.add(s.substring(id.length()));
            }
        }
        return toRet;
    }
}
