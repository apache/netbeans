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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.LocatorTest;
import org.netbeans.modules.editor.settings.storage.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vita Stejskal
 */
public class CodeTemplatesLocatorTest extends NbTestCase {
    
    private static final String CT_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE codetemplates PUBLIC \"-//NetBeans//DTD Editor Code Templates settings 1.0//EN\" \"http://www.netbeans.org/dtds/EditorCodeTemplates-1_0.dtd\">\n" +
        "<codetemplates></codetemplates>";
    
    
    /** Creates a new instance of LocatorTest */
    public CodeTemplatesLocatorTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
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

    public void testFullCodeTemplatesMixedLayout() throws Exception {
        String writableUserFile = "Editors/" + LocatorTest.getWritableFileName(CodeTemplatesStorage.ID, null, null, null, false);
        String [] files = new String [] {
            "Editors/Defaults/abbreviations.xml",
            "Editors/CodeTemplates/Defaults/zz.xml",
            "Editors/CodeTemplates/Defaults/dd.xml",
            "Editors/CodeTemplates/Defaults/kk.xml",
            "Editors/CodeTemplates/Defaults/aa.xml",
            "Editors/abbreviations.xml",
            "Editors/CodeTemplates/papap.xml",
            "Editors/CodeTemplates/kekeke.xml",
            "Editors/CodeTemplates/dhdhdddd.xml",
            writableUserFile
        };
        
        
        LocatorTest.createOrderedFiles(files, CT_CONTENTS);
//        TestUtilities.createFile(writableUserFile, CT_CONTENTS);
//        LocatorTest.orderFiles("Editors/CodeTemplates/dhdhdddd.xml", writableUserFile);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        LocatorTest.scan(CodeTemplatesStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get(null);
        LocatorTest.checkProfileFiles(files, writableUserFile, profileFiles, null);
    }

    public void testFullCodeTemplatesLegacyLayout() throws Exception {
        String [] files = new String [] {
            "Editors/Defaults/abbreviations.xml",
            "Editors/abbreviations.xml",
        };
        
        LocatorTest.createOrderedFiles(files, CT_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        LocatorTest.scan(CodeTemplatesStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get(null);
        LocatorTest.checkProfileFiles(files, null, profileFiles, null);
    }
}
