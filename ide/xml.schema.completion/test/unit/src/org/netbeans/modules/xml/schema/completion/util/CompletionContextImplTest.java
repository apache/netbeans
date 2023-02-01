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
package org.netbeans.modules.xml.schema.completion.util;

import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.junit.Test;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.schema.completion.Util;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;

import static org.junit.Assert.assertEquals;

public class CompletionContextImplTest {
    @Test
    public void testDetectedPaths() throws Exception {
        String TNSA = "http://xml.netbeans.org/schema/TNSA";
        String path = "/org/netbeans/modules/xml/schema/completion/resources/CompletionContext.xml";
        FileObject fo = Util.getResourceAsFileObject(path);
        Document doc = Util.getResourceAsDocument(path);
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        doc.putProperty(Language.class, XMLTokenId.language());

        assertEquals(Arrays.asList(), pathFromRoot(fo, support, 862));
        assertEquals(Arrays.asList(new QName(TNSA, "rootA2")), pathFromRoot(fo, support, 872));
        assertEquals(Arrays.asList(new QName(TNSA, "rootA2"), new QName(TNSA, "rootA3")), pathFromRoot(fo, support, 1052));
        assertEquals(Arrays.asList(new QName(TNSA, "rootA2"), new QName(TNSA, "rootA3"), new QName(TNSA, "A31")), pathFromRoot(fo, support, 1064));
        assertEquals(Arrays.asList(new QName(TNSA, "rootA2"), new QName(TNSA, "rootA3")), pathFromRoot(fo, support, 1066));
        assertEquals(Arrays.asList(new QName(TNSA, "rootA2")), pathFromRoot(fo, support, 1081));
    }

    private static List<QName> pathFromRoot(FileObject fo, XMLSyntaxSupport support, int pos) {
        CompletionContextImpl cci = new CompletionContextImpl(fo, support, pos);
        cci.initContext();
        return cci.getPathFromRoot();
    }
}
