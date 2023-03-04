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

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;

public class FileRecognitionPanelTest extends NbTestCase {

    public FileRecognitionPanelTest(String n) {
        super(n);
    }

    public void testCheckValidity() throws Exception {
        assertValidity(Result.VALID, "text/x-foo", "", "foo", false);
        assertValidity(Result.INFO, "", "", "foo", false);
        assertValidity(Result.ERROR, "some-type", "", "foo", false);
        assertValidity(Result.INFO, "text/x-foo", "", "", false);
        assertValidity(Result.ERROR, "text/x-foo", "", "bad/ext", false);
        assertValidity(Result.INFO, "text/x-foo", "", "", true);
        assertValidity(Result.ERROR, "text/docbook", "whatever", "", true);
        assertValidity(Result.VALID, "text/docbook+xml", "whatever", "", true);
        assertValidity(Result.VALID, "text/x-docbook+xml", "whatever", "", true);
    }
    enum Result {VALID, INFO, ERROR}
    private static void assertValidity(Result expected, String mimeType, String namespace, String extension, boolean byElement) {
        AtomicBoolean error = new AtomicBoolean();
        String msg = FileRecognitionPanel.checkValidity(error, mimeType, namespace, extension, byElement);
        assertEquals(msg, expected, msg != null ? (error.get() ? Result.ERROR : Result.INFO) : Result.VALID);
    }

}
