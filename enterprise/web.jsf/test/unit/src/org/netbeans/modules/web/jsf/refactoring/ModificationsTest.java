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

package org.netbeans.modules.web.jsf.refactoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Position.Bias;
import junit.framework.TestCase;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import org.netbeans.modules.web.jsf.refactoring.Modifications.Difference;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;

/**
 *
 * @author Petr Pisl
 */
public class ModificationsTest extends NbTestCase {
    
    public ModificationsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices (MockXMLFileEncodingQuery.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInputStream() throws Exception {
        File file = new File(getDataDir(), "faces-config1.xml");
        FileObject fileObject = FileUtil.toFileObject(file);
        CloneableEditorSupport editor
                            = JSFEditorUtilities.findCloneableEditorSupport(DataObject.find(fileObject));
        Modifications modifications = new Modifications();
        Modifications.Difference difference = new Modifications.Difference(
                Modifications.Difference.Kind.CHANGE, 
                editor.createPositionRef(549, Bias.Forward), 
                editor.createPositionRef(549+21, Bias.Backward), 
                "org.ncl.backing.Login", "org.ncl.forward.Login", "");
        File outFile = new File(getWorkDir(), "faces-modification.xml");
        FileWriter fWriter = new FileWriter(outFile);
        modifications.addDifference(fileObject, difference);
        List<Difference> differences = new LinkedList<>();
        differences.add(difference);
        modifications.commit(fileObject, differences, fWriter);
        fWriter.close();
        assertFile(outFile, getGoldenFile("gold-modifications.xml"));
       
        
    }
    
    public static class MockXMLFileEncodingQuery extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            return XmlFileEncodingQueryImpl.singleton().getEncoding(file);
        }
        
    }
}
