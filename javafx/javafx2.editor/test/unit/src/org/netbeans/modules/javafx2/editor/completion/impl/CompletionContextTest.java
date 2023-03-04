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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.Manager;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.modules.javafx2.editor.completion.impl.CompletionContext;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class CompletionContextTest extends FXMLCompletionTestBase {
    
    private static final String MARKER = "\\|([A-Z_]+?),([a-zA-Z]+?)\\|"; // NOI18N
    
    private String text;
    private int offset;
    private String state;
    private CompletionContext.Type stateVal;

    public CompletionContextTest(String testName) {
        super(testName);
    }
    
    public CompletionContextTest(String testName, String text, String expectedState, int offset) {
        super(testName);
        this.state = expectedState;
        this.offset = offset;
        this.text = text;
    }
    
    private void prepareData(String tokenName, String fname) throws Exception  {
        File dataDir = getDataDir();
        File f = new File(dataDir, CompletionContextTest.class.getPackage().getName().replaceAll("\\.", "/") + 
                "/" + fname);
        InputStream stream = new FileInputStream(f);
        InputStreamReader rd = new InputStreamReader(stream, StandardCharsets.UTF_8);
        
        StringBuffer sb = new StringBuffer();
        CharBuffer cb = CharBuffer.allocate(10000);
        
        while (rd.read(cb) != -1) {
            cb.flip();
            sb.append(cb.toString());
            cb.rewind();
        }
        rd.close();
        
        String text = sb.toString();
        
        // strip all occurrences of markers:
        String pristine = text.replaceAll(MARKER, "");
        
        Pattern p = Pattern.compile(MARKER);
        Matcher m = p.matcher(text);
        TestSuite ts = new TestSuite(CompletionContextTest.class.getName());
        
        while (m.find()) {
            String val = m.group(1);
            String name = m.group(2);
            
            if (!name.equals(tokenName)) {
                continue;
            }
            
            int offset = m.start();
            String cleanBefore = text.substring(0, offset).replaceAll(MARKER, "");
            
            this.text = pristine;
            this.state = val;
            this.stateVal = CompletionContext.Type.valueOf(val);
            this.offset = cleanBefore.length();
            return;
        }
        throw new IllegalArgumentException("Token " + tokenName + " not found in the template");
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    private FileObject sourceFO;
    
    private void writeSourceFile() throws Exception {
        File f = new File(getWorkDir(), "source.fxml");
        FileObject dirFo = FileUtil.toFileObject(getWorkDir());
        OutputStream ostm = dirFo.createAndOpen("source.fxml");
        OutputStreamWriter wr = new OutputStreamWriter(ostm);
        wr.write(text);
        wr.flush();
        wr.close();
        
        sourceFO = dirFo.getFileObject("source.fxml");
    }
    
    private void runNamedTest(String name) throws Exception {
        runNamedTest(name, "Simple.fxml");
    }
    
    private void runNamedTest(String name, String file) throws Exception {
        prepareData(name, file);
        writeSourceFile();
        Source s = Source.create(sourceFO);
        ParserManager.parse(Collections.singleton(s), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                FxmlParserResult res = (FxmlParserResult)resultIterator.getParserResult();
                CompletionContext ctx = new CompletionContext(res, offset, CompletionProvider.COMPLETION_QUERY_TYPE);
                CompletionContext.Type type = CompletionContext.Type.valueOf(state);
                assertEquals(type, ctx.getType());
            }
            
        });
    }
    
//    public static TestSuite suite() {
//        TestSuite ts = new TestSuite();
//        ts.addTest(new CompletionContextTest("testPropertyValueEquals"));
//        //testWhitespaceAfterAttribute"
//        return ts;
//    }

    // processing instructions
    public void testStartOfInstruction() throws Exception {
        runNamedTest("startOfInstruction");
    }

    
    public void testDataInTheMiddle() throws Exception {
        runNamedTest("dataInTheMiddle");
    }

    public void testDataSuffix() throws Exception {
        runNamedTest("dataSuffix");
    }

    public void testInstructionEnd() throws Exception {
        runNamedTest("instructionEnd");
    }

    public void testAfterInstruction() throws Exception {
        runNamedTest("afterInstruction");
    }

    public void testBeginOfTargetName() throws Exception {
        runNamedTest("beginOfTargetName");
    }

    public void testWitespaceAfterInstruction() throws Exception {
        runNamedTest("whitespaceAfterInstruction");
    }
    
    public void testWitespaceAfterInstructionB() throws Exception {
        runNamedTest("whitespaceAfterInstructionB");
    }

    // elements

    public void testExistingClassNameStart() throws Exception {
        runNamedTest("existingClassNameStart");
    }

    public void testExistingPropertyElementStart() throws Exception {
        runNamedTest("existingPropertyElementStart");
    }
    
    public void testPropertyElementEnd() throws Exception {
        runNamedTest("propertyElementEnd");
    }
    
    public void testWhitespaceAfterTag() throws Exception {
        runNamedTest("whitespaceAfterTag");
    }
    
    public void testEndOfTagName() throws Exception {
        runNamedTest("endOfTagName");
    }
    
    public void testendOfPropertyTagName() throws Exception {
        runNamedTest("endOfPropertyTagName");
    }
    
    public void testWhittespaceAfterPropertyTagName() throws Exception {
        runNamedTest("whitespaceAfterPropertyTagName");
    }
    
    // attributes
    public void testPropertyValueEquals() throws Exception {
        runNamedTest("propertyValueEquals");
    }

    public void testExistingPropertyStart() throws Exception {
        runNamedTest("existingPropertyStart");
    }

    public void testPropertyValueStart() throws Exception {
        runNamedTest("existingClassNameStart");
    }

    public void testPropertyValueMiddle() throws Exception {
        runNamedTest("propertyValueMiddle");
    }
    
    public void testWhitespaceAfterAttribute() throws Exception {
        runNamedTest("whitespaceAfterAttribute");
    }
    
    // attribute values

    public void testVariableStart() throws Exception {
        runNamedTest("variableStart");
    }

    public void testVariableMiddle() throws Exception {
        runNamedTest("variableMiddle");
    }

    public void testBundleReference() throws Exception {
        runNamedTest("bundleReference");
    }
    
    public void testBinding() throws Exception {
        runNamedTest("binding");
    }
    
    public void testResourceReference() throws Exception {
        runNamedTest("resourceReference");
    }
    
    public void testMiddlePropertyName() throws Exception {
        runNamedTest("middlePropertyName");
    }
    
    public void testAfterPropertyNameBeforeSign() throws Exception {
        runNamedTest("afterPropertyNameBeforeSign");
    }
    
    public void testMiddleStaticPropertyName() throws Exception {
        runNamedTest("middleStaticPropertyName");
    }
    
    public void testNoRootElement() throws Exception {
        runNamedTest("noRootElement", "Empty.fxml");
    }
}
