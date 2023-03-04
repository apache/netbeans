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
package org.netbeans.modules.xml.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.diff.DefaultElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DiffFinder;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * The XMLTokenIdTest tests the parsing algorithm of XMLLexer.
 * Various tests include, sanity, regression, performance etc.
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class AbstractTestCase extends NbTestCase {
    
    public AbstractTestCase(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() {
    }
        
    protected BaseDocument getDocument(String path) throws Exception {
        BaseDocument doc = getResourceAsDocument(path);
        //must set the language inside unit tests
        doc.putProperty(Language.class, XMLTokenId.language());
        return doc;
    }
                 
    protected static BaseDocument getResourceAsDocument(String path) throws Exception {
        InputStream in = AbstractTestCase.class.getResourceAsStream(path);
        BaseDocument sd = new BaseDocument(true, "text/xml"); //NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    protected boolean compare(Document d1, Document d2) throws IOException {
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();        
        
        DefaultElementIdentity eID = new DefaultElementIdentity();
        DiffFinder diffEngine = new DiffFinder(eID);
        List<Difference> diffList = diffEngine.findDiff(m1.getDocument(), m2.getDocument());
        
        if( diffList!= null && diffList.size() == 0)
            return true;
        
        return false;        
    }

    protected XMLSyntaxSupport getSyntaxSupport(String path) throws Exception {
        BaseDocument doc = getResourceAsDocument(path);
        //must set the language inside unit tests
        doc.putProperty(Language.class, XMLTokenId.language());
        return XMLSyntaxSupport.getSyntaxSupport(doc);
    }

    /**
     * Converts expected result data into a string. See result*.txt files.
     */
    protected String getExpectedResultAsString(String resultFile) throws IOException {
        StringBuilder expectedResult = new StringBuilder();
        try (InputStream in = AbstractTestCase.class.getResourceAsStream(resultFile);
                Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.trim().startsWith("#")) {
                    expectedResult.append(line);
                    expectedResult.append("\n");
                }
            }
        }
        return expectedResult.toString();        
    }

}
