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
package org.netbeans.lib.editor.codetemplates.storage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.StorageImpl;

/**
 *
 * @author Vita Stejskal
 */
public class CodeTemplateSettingsImplTest extends NbTestCase {

    public CodeTemplateSettingsImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();

        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/lib/editor/codetemplates/storage/test-layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/lib/editor/codetemplates/resources/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testSimple() {
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(MimePath.EMPTY);
        Map<String, CodeTemplateDescription> map = ctsi.getCodeTemplates();
        
        assertNotNull("CodeTemplates map should not be null", map);
        assertEquals("Wrong number of code templates", 3, map.size());
        
        checkCodeTemplate(map, "sout", "System Out Print Line", "System.out.println(${cursor});", "not-a-real-uuid", "aaa", "bbb");
        checkCodeTemplate(map, "hello", null, "  Hello World!  ", null);
        checkCodeTemplate(map, "xyz", "xyz", "<xyz>${cursor}</xyz>", null, "xyz");
    }

    public void testUserChangesOverrideDefaults() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        Map<String, CodeTemplateDescription> map = ctsi.getCodeTemplates();
        
        assertNotNull("CodeTemplates map should not be null", map);
        assertEquals("Wrong number of code templates", 2, map.size());
        
        checkCodeTemplate(map, "module1", null, "module1", null);
        checkCodeTemplate(map, "user1", null, "user1", null);
        
        CodeTemplateDescription ct = map.get("module2");
        assertNull("'module2' code template should be removed", ct);
    }
    
    public void testSimpleWrite() throws IOException {
        String abbrev = "ct";
        String desc = "Code Template";
        String text = "<code-template>${cursor}";
        String uuid = "test-uuid";
        List<String> contexts = Arrays.asList(new String [] { "ct-ctx1" });
        CodeTemplateDescription ct = new CodeTemplateDescription(abbrev, desc, text, contexts, uuid);
        
        MimePath mimePath = MimePath.parse("text/x-empty");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        
        // Write the code template
        ctsi.setCodeTemplates(Collections.singletonMap(abbrev, ct));
        
        // Force loading from the files
        //Map<String, CodeTemplateDescription> loadedMap = CodeTemplatesStorage.load(mimePath, false);
        StorageImpl<String, CodeTemplateDescription> storage = new StorageImpl<String, CodeTemplateDescription>(new CodeTemplatesStorage(), null);
        Map<String, CodeTemplateDescription> loadedMap = storage.load(mimePath, null, false);
        
        assertNotNull("Can't load the map", loadedMap);
        assertEquals("Wrong number of code templates", 1, loadedMap.size());
        checkCodeTemplate(loadedMap, abbrev, desc, text, uuid, contexts.toArray(new String [0]));
    }

    public void testEndOfLines() throws IOException {
        String abbrev = "tt";
        String desc = "Multi-line Code Template";
        String text = "<table>\n" +
                      "    <tr>\n" +
                      "        <td>${cursor}</td>\n" +
                      "    </tr>\n" +
                      "</table>\n";
        String uuid = "test-uuid-1";
        List<String> contexts = Arrays.asList(new String [] { "ct-ctx-tt" });
        CodeTemplateDescription ct = new CodeTemplateDescription(abbrev, desc, text, contexts, uuid);
        
        MimePath mimePath = MimePath.parse("text/x-multi");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        
        // Write the code template
        ctsi.setCodeTemplates(Collections.singletonMap(abbrev, ct));
        
        // Force loading from the files
        //Map<String, CodeTemplateDescription> loadedMap = CodeTemplatesStorage.load(mimePath, false);
        StorageImpl<String, CodeTemplateDescription> storage = new StorageImpl<String, CodeTemplateDescription>(new CodeTemplatesStorage(), null);
        Map<String, CodeTemplateDescription> loadedMap = storage.load(mimePath, null, false);
        
        assertNotNull("Can't load the map", loadedMap);
        assertEquals("Wrong number of code templates", 1, loadedMap.size());
        checkCodeTemplate(loadedMap, abbrev, desc, text, uuid, contexts.toArray(new String [0]));
    }

    public void testRemoveAll() throws IOException {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        Map<String, CodeTemplateDescription> map = ctsi.getCodeTemplates();
        
        assertNotNull("CodeTemplates map should not be null", map);
        assertTrue("Code templates map should not be empty", map.size() > 0);
        
        // Remove all code templates
        ctsi.setCodeTemplates(Collections.<String, CodeTemplateDescription>emptyMap());
        
        // Force loading from the files
        //Map<String, CodeTemplateDescription> loadedMap = CodeTemplatesStorage.load(mimePath, false);
        StorageImpl<String, CodeTemplateDescription> storage = new StorageImpl<String, CodeTemplateDescription>(new CodeTemplatesStorage(), null);
        Map<String, CodeTemplateDescription> loadedMap = storage.load(mimePath, null, false);
        
        assertNotNull("Can't load the map", loadedMap);
        assertEquals("Some template were not removed", 0, loadedMap.size());
    }
    
    public void testLoadLegacy() {
        MimePath mimePath = MimePath.parse("text/x-legacy");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        Map<String, CodeTemplateDescription> map = ctsi.getCodeTemplates();
        
        assertNotNull("CodeTemplates map should not be null", map);
        assertEquals("Wrong number of code templates", 1, map.size());
        
        checkCodeTemplate(map, "tglb", null, "<%@taglib uri=\"${cursor}\"%>", null);
    }
    
    public void testMimeLookup() {
        MimePath mimePath = MimePath.parse("text/x-type-B");
        CodeTemplateSettingsImpl ctsi = CodeTemplateSettingsImpl.get(mimePath);
        Map<String, CodeTemplateDescription> map = ctsi.getCodeTemplates();
        
        assertNotNull("CodeTemplates should not be null", map);
        assertEquals("Wrong number of code templates", 2, map.size());
        
        checkCodeTemplate(map, "module1", null, "module1", null);
        checkCodeTemplate(map, "user1", null, "user1", null);
        
        CodeTemplateDescription ct = map.get("module2");
        assertNull("'module2' code template should be removed", ct);
    }
    
    private void checkCodeTemplate(
        Map<String, CodeTemplateDescription> map, 
        String abbrev, 
        String desc, 
        String text, 
        String uuid,
        String... contexts
    ) {
        CodeTemplateDescription ct = map.get(abbrev);
        
        assertNotNull("Can't find code template for '" + abbrev + "'", ct);
        assertEquals("Wrong abbreviation", abbrev, ct.getAbbreviation());
        assertEquals("Wrong description", desc, ct.getDescription());
        assertEquals("Wrong text", text, ct.getParametrizedText());
        assertEquals("Wrong unique id", uuid, ct.getUniqueId());
        
        List<String> ctxs = ct.getContexts();
        assertEquals("Wrong number of contexts", contexts.length, ctxs.size());
        
        for(String c : contexts) {
            assertTrue("Code template does not contain context '" + c + "'", ctxs.contains(c));
        }
    }
    
    private static Map<String, CodeTemplateDescription> toMap(Collection<CodeTemplateDescription> c) {
        Map<String, CodeTemplateDescription> m = new HashMap<String, CodeTemplateDescription>();
        for(CodeTemplateDescription ct : c) {
            m.put(ct.getAbbreviation(), ct);
        }
        return m;
    }
}
