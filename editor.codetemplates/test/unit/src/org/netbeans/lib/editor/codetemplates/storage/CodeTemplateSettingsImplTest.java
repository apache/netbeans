/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        checkCodeTemplate(loadedMap, abbrev, desc, text, uuid, contexts.toArray(new String [contexts.size()]));
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
        checkCodeTemplate(loadedMap, abbrev, desc, text, uuid, contexts.toArray(new String [contexts.size()]));
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
