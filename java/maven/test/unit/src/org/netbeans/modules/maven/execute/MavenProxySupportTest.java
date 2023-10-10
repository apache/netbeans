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
package org.netbeans.modules.maven.execute;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.EntityReplacementMap;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.execute.MavenProxySupport.LineAndColumn;
import org.netbeans.modules.maven.execute.MavenProxySupport.TagInfo;
import org.netbeans.modules.maven.execute.MavenProxySupport.TextInfo;

/**
 *
 * @author sdedic
 */
public class MavenProxySupportTest extends NbTestCase {

    public MavenProxySupportTest(String name) {
        super(name);
    }
    
    TextInfo textInfo;
    List<String> lines;
    
    private void loadProxyInfo(String file) throws IOException {
        Path settingsPath = getDataDir().toPath().resolve("exec/" + file);

        MavenProxySupport.XppDelegate del = new MavenProxySupport.XppDelegate(EntityReplacementMap.defaultEntityReplacementMap);
        try (FileInputStream in = new FileInputStream(settingsPath.toFile())) {
            del.setInput( ReaderFactory.newXmlReader( in ));
            while (del.next() != XmlPullParser.END_DOCUMENT) {
                // empty, just read
            }
            textInfo = del.getTextInfo();
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        lines = Files.readAllLines(settingsPath);
    }
    
    private void assertLineColumn(LineAndColumn lc, int l, int c) {
        assertEquals(l, lc.line);
        assertEquals(c, lc.column);
    }
    
    private void assertTagText(TagInfo ti) {
        assertEquals("Tags " + ti.tagName + " should be at the same line", ti.startTag.line, ti.endTag.line);
        
        String l = lines.get(ti.startTag.line - 1);
        String text = l.substring(ti.tagName.length() + 2 - 1 /* 1-based */ + ti.startTag.column, ti.endTag.column - 1 /* 1-based */);
        assertEquals("Content of " + ti.tagName, ti.content, text);
    }
    
    private void assertIdHostEnabled(MavenProxySupport.ProxyInfo pi, String host, String id, boolean active) {
        assertTrue(pi.tags.containsKey("host"));
        assertTrue(pi.tags.containsKey("id"));
        assertTrue(pi.tags.containsKey("active"));
        
        assertEquals("Checking id of " + id, id, pi.tags.get("id").content);
        assertTagText(pi.tags.get("id"));
        assertEquals("Checking host of " + id, host, pi.tags.get("host").content);
        assertTagText(pi.tags.get("host"));
        assertEquals("Checking active of " + id, active, Boolean.parseBoolean(pi.tags.get("active").content));
        assertTagText(pi.tags.get("active"));
    }
    
    /**
     * Checks that proxies are parsed, the entries used in the code are checked that the text located at the reported line/column
     * corresponds to the appropriate text.
     * 
     * @throws Exception 
     */
    public void testProxyBlocks() throws Exception {
        loadProxyInfo("settings-proxy.xml");
        assertEquals(2, textInfo.proxyTags.size());
        
        // opening tags:
        assertLineColumn(textInfo.proxyTags.get(0).firstTag, 3, 9);
        assertLineColumn(textInfo.proxyTags.get(1).firstTag, 10, 9);
        
        assertIdHostEnabled(textInfo.proxyTags.get(0), "proxy1.acme.com", "netbeans-default-proxy", false);
        assertIdHostEnabled(textInfo.proxyTags.get(1), "cafebabe-proxy.acme.com", "cafebabe", true);
    }
    
    public void testProxyAbsent() throws Exception {
        loadProxyInfo("settings-noproxy.xml");
        assertEquals(0, textInfo.proxyTags.size());
        assertNotNull(textInfo.firstTag);
        assertLineColumn(textInfo.firstTag, 2, 5);
    }
}
