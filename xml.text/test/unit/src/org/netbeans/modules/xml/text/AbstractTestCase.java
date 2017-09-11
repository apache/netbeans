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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
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
    protected String getExpectedResultAsString(String resultFile) {
        StringBuilder expectedResult = new StringBuilder();
        InputStream in = AbstractTestCase.class.getResourceAsStream(resultFile);
        Scanner scanner = new Scanner(in);
        try {
            while(scanner.hasNextLine()) {
                expectedResult.append(scanner.nextLine());
            }
        } finally {
            scanner.close();
            try {
                in.close();
            } catch (IOException ex) {
                //stupid catch
            }
        }
        return expectedResult.toString();        
    }

}
