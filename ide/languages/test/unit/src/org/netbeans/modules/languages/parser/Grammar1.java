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

package org.netbeans.modules.languages.parser;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.NBSLanguageReader;
import org.netbeans.modules.languages.TestUtils;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan Jancura
 */
public class Grammar1 extends TestCase {
    
    public Grammar1 (String testName) {
        super (testName);
    }

    public void test1 () throws ParseException {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/parser/Grammar1.nbs");
        try {
            NBSLanguageReader reader = NBSLanguageReader.create (is, "Grammar1.nbs", "test/mimeType");
            LanguageImpl l = TestUtils.createLanguage (reader);
            l.read ();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
}
