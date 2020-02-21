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
package org.netbeans.modules.cnd.makefile.parser;

import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.makefile.MakefileElement;
import org.netbeans.modules.cnd.api.makefile.MakefileInclude;
import org.netbeans.modules.cnd.api.makefile.MakefileMacro;
import org.netbeans.modules.cnd.api.makefile.MakefileRule;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;

/**
 *
 */
public class MakefileParserTest extends NbTestCase {

    public MakefileParserTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MimePath mimePath = MimePath.parse(MIMENames.MAKEFILE_MIME_TYPE);
        MockMimeLookup.setInstances(mimePath, MakefileTokenId.language());
    }

    public void testSample() throws Exception {
        MakefileParseResult result = parseFile(new File(getDataDir(), "Makefile1"));
        assertNotNull(result);
        List<MakefileElement> elements = result.getElements();
        assertNotNull(elements);


        MakefileMacro rm = (MakefileMacro) elements.get(0);
        assertEquals(MakefileElement.Kind.MACRO, rm.getKind());
        assertEquals("RM", rm.getName());
        assertEquals("rm", rm.getValue());

        MakefileMacro cc = (MakefileMacro) elements.get(1);
        assertEquals(MakefileElement.Kind.MACRO, cc.getKind());
        assertEquals("CC", cc.getName());
        assertEquals("gcc", cc.getValue());

        MakefileInclude include = (MakefileInclude) elements.get(2);
        assertEquals(MakefileElement.Kind.INCLUDE, include.getKind());
        assertEquals(Arrays.asList("Makefile", "${FOO}.mk"), include.getFileNames());

        MakefileRule buildConf = (MakefileRule) elements.get(3);
        assertEquals(MakefileElement.Kind.RULE, buildConf.getKind());
        assertEquals(Collections.singletonList(".build-conf"), buildConf.getTargets());
        assertEquals(Arrays.asList("$(BUILD_SUBPROJECTS)", "dist/Debug/GNU-Solaris-x86/quote_1"), buildConf.getPrerequisites());

        MakefileRule cleanConf = (MakefileRule) elements.get(4);
        assertEquals(MakefileElement.Kind.RULE, cleanConf.getKind());
        assertEquals(Collections.singletonList(".clean-conf"), cleanConf.getTargets());
        assertEquals(Collections.emptyList(), cleanConf.getPrerequisites());

        MakefileRule done = (MakefileRule) elements.get(5);
        assertEquals(MakefileElement.Kind.RULE, done.getKind());
        assertEquals(Collections.singletonList(".DONE"), done.getTargets());
        assertEquals(Collections.emptyList(), done.getPrerequisites());

        MakefileMacro foo = (MakefileMacro) elements.get(6);
        assertEquals(MakefileElement.Kind.MACRO, foo.getKind());
        assertEquals("FOO", foo.getName());
        assertEquals("BAR=$(BAZ)\na: b\n\techo endef\n#dummy comment", foo.getValue());

        assertEquals(7, elements.size());
    }

    private MakefileParseResult parseFile(File file) throws ParseException {
        FileObject fobj = CndFileUtils.toFileObject(file);
        MakefileParser parser = new MakefileParser();
        parser.parse(Source.create(fobj).createSnapshot(), null, null);
        return parser.getResult(null);
    }
}
