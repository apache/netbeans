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
package org.netbeans.api.lsp;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lsp.SignatureInformationCollector;

/**
 *
 * @author Dusan Balek
 */
public class SignatureInformationTest extends NbTestCase {

    public SignatureInformationTest(String name) {
        super(name);
    }

    @Override
    public void setUp () throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances (MimePath.get ("text/foo"), new FooSignatureInformationCollector());
    }

    public void testSignatureInformationCollect() {
        Document doc = createDocument("text/foo", "");
        int offset = 0;
        // @start region="testSignatureInformationCollect"

        // Compute and collect signature information for a document at a given offset
        SignatureInformation.collect(doc, offset, null, signature -> {

            // signature should never be 'null'
            assertNotNull(signature);

            // getting signature 'label'
            String label = signature.getLabel();
            assertEquals("label", label);

            // check if the signature is active
            assertTrue(signature.isActive());

            // getting signature 'parameters'
            List<SignatureInformation.ParameterInformation> params = signature.getParameters();
            // check number of parameters
            assertEquals(2, params.size());
            for (int i = 0; i < params.size(); i++) {
                SignatureInformation.ParameterInformation param = params.get(i);
                // getting parameter 'label'
                String paramLabel = param.getLabel();
                assertEquals("param" + i, paramLabel);
                // check if the parameter is active
                if (i == 1) {
                    assertTrue(param.isActive());
                } else {
                    assertFalse(param.isActive());
                }
                // getting optional parameter 'documentation'
                String paramDocumentation = param.getDocumentation();
                assertEquals("param" + i + " documentation", paramDocumentation);
            }

            // getting optional signature 'documentation'
            String documentation = signature.getDocumentation();
            assertEquals("documentation", documentation);
        });

        // @end region="testSignatureInformationCollect"
    }

    private Document createDocument(String mimeType, String contents) {
        Document doc = new DefaultStyledDocument();
        doc.putProperty("mimeType", mimeType);
        try {
            doc.insertString(0, contents, null);
            return doc;
        } catch (BadLocationException ble) {
            throw new IllegalStateException(ble);
        }
    }

    private static class FooSignatureInformationCollector implements SignatureInformationCollector {

        @Override
        public void collectSignatureInformation(Document doc, int offset, SignatureInformation.Context context, Consumer<SignatureInformation> consumer) {
            // @start region="builder"

            // Create a builder for creating 'SignatureInformation' instance providing its 'label' and 'isActive' flag
            SignatureInformation si = SignatureInformationCollector.newBuilder("label", true)

                    // add signature parameters
                    .addParameter("param0", false, "param0 documentation")
                    .addParameter("param1", true, "param1 documentation")

                    // set signature documentation
                    .documentation("documentation")

                    // create a new 'SignatureInformation' instance
                    .build();

            // @end region="builder"
            consumer.accept(si);
        }
    }
}
