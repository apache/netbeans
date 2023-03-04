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
package org.netbeans.modules.maven.embedder.impl;

import java.io.IOException;
import java.net.URL;
import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;


public class MavenProtocolHandlerTest extends NbTestCase {

    public MavenProtocolHandlerTest(String name) {
        super(name);
    }

    @Test
    public void testResolveM2Url() throws IOException {
        org.netbeans.ProxyURLStreamHandlerFactory.register();
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        Artifact a = MavenProtocolHandler.resolveM2Url(new URL("m2:/org.openjfx:javafx-base:17:jar:linux"), online);
        assertEquals("org.openjfx", a.getGroupId());
        assertEquals("javafx-base", a.getArtifactId());
        assertEquals("17", a.getVersion());
        assertEquals("jar", a.getType());
        assertEquals("linux", a.getClassifier());
        Artifact b = MavenProtocolHandler.resolveM2Url(new URL("m2:/com.dukescript.nbjavac:nb-javac:jdk-17%2B35:jar"), online);
        assertEquals("com.dukescript.nbjavac", b.getGroupId());
        assertEquals("nb-javac", b.getArtifactId());
        assertEquals("jdk-17+35", b.getVersion());
        assertEquals("jar", b.getType());
    }

}
