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

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.util.List;
import org.apache.maven.index.ArtifactInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.maven.indexer.api.AbstractTestQueryProvider.TestIndexer1;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.util.test.TestFileUtils;

public class NexusRepositoryIndexerImplTest extends NexusTestBase {

    public NexusRepositoryIndexerImplTest(String n) {
        super(n);
    }
    
    public void testFilterPluginGroupIdsRepositoryQueries() throws Exception {
        // add an alternative QueryProvider _not_ handlig our request
        MockServices.setServices(TestIndexer1.class); 
        
        install(File.createTempFile("whatever", ".txt", getWorkDir()), "test", "spin", "1.1", "txt");
        nrii.indexRepo(info);
        // RepositoryQueries should handle the query via our NexusRepositoryIndexerImpl
        RepositoryQueries.Result<String> res = RepositoryQueries.getArtifactsResult("test", List.of(info));
        assertEquals("[spin]", res.getResults().toString());
    }

    public void testFind() throws Exception {
        installPOM("test", "plugin", "0", "maven-plugin");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "plugin.jar"), "META-INF/maven/plugin.xml:<plugin><goalPrefix>stuff</goalPrefix></plugin>"), "test", "plugin", "0", "maven-plugin");
        nrii.indexRepo(info);
        QueryField qf = new QueryField();
        qf.setField(ArtifactInfo.PLUGIN_PREFIX);
        qf.setValue("stuff");
        qf.setOccur(QueryField.OCCUR_MUST);
        qf.setMatch(QueryField.MATCH_EXACT);
        assertEquals("[test:plugin:0:test]", nrii.find(List.of(qf), List.of(info)).getResults().toString());
    }

    public void testLastUpdated() throws Exception { // #197670
        installPOM("test", "art", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "art.jar"), "stuff:whatever"), "test", "art", "0", "jar");
        File empty = TestFileUtils.writeFile(new File(getWorkDir(), "empty"), "# placeholder\n");
        install(empty, "test", "art", "0", "pom.lastUpdated");
        install(empty, "test", "art", "0", "jar.lastUpdated");
        nrii.indexRepo(info);
        List<NBVersionInfo> versions = nrii.getVersions("test", "art", List.of(info)).getResults();
        assertEquals(1, versions.size());
        NBVersionInfo v = versions.get(0);
        assertEquals("test:art:0:test", v.toString());
        assertEquals("jar", v.getPackaging());
        assertEquals("jar", v.getType());
    }

//    @Override
//    protected int timeOut() {
//        return Integer.MAX_VALUE;
//    }
//    
//    public void testUnpack() throws DuplicateRealmException, NoSuchRealmException, PlexusContainerException, ComponentLookupException, URISyntaxException, IOException {
//        // HEY! probably needs test.timeout=[very many minutes] to be set in project.properties 
//        System.setProperty("maven.indexing.diag", "true");
//        System.setProperty("maven.diag.index.properties", "/work/tmp/nexus-maven-repository-index.properties1115421058305181525");
//        System.setProperty("maven.diag.index.gz", "/work/tmp/nexus-maven-repository-index.gz7121431417826212064");
//        int c = 3;
//        List<Long> l = new LinkedList<Long>();
//        NexusRepositoryIndexerImpl impl = new NexusRepositoryIndexerImpl();
//        for (int i = 0; i < c; i++) {
//            delete(new File(getWorkDir(), "var"));
//            System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
//            long t = System.currentTimeMillis();
//            impl.indexRepo(new RepositoryInfo("central", "central", null, "http://repo.maven.apache.org/maven2/"));
//            long tt = System.currentTimeMillis() - t;
//            l.add(tt);
//            System.out.println(" " + tt);
//        }
//        
//        System.out.println("--------------------------");
//        System.out.println("Times: ");
//        long s = 0;
//        for (Long t : l) {
//            System.out.println(" " + t);
//            s+=t;
//        }
//        System.out.println("--------------------------");
//        System.out.println("average: " + (s/c));
//    }
//    
//    private void delete(File f) {
//        if(f.isFile()) {
//            f.delete();
//            return;
//        }
//        File[] fs = f.listFiles();
//        if(fs == null) {
//            return;
//        }
//        for(File f1 : fs) {
//            delete(f1);
//        }
//    }
}
