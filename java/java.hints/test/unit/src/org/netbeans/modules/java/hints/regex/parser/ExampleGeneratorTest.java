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
package org.netbeans.modules.java.hints.regex.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.regex.parser.RegexConstructs.RegEx;

/**
 *
 * @author SANDEEMI
 */
public class ExampleGeneratorTest {

    public ExampleGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of generate method, of class ExampleGenerator.
     */
    @Test
    public void testGenerate() throws IOException {

        int n = 50;

        List<String> regexps = Arrays.asList("z*",
                "z+",
                "z?",
                "z{1}",
                "z{1,2}",
                "z{1,}",
                "z?+t",
                "a|b",
                "ab",
                "[^a-z]",
                "\\d\\w\\s",
                "\n\r\f\\\\",
                "a|(b|c|d)|e",
                "z*[1-9d-f](ab|bc|cd)\\d+",
                "a*|b{2,4}",
                "[\\s]",
                "\\d+(\\.\\d\\d)?",
                "rege(x(es)?|xps?)",
                "[2-9]|[12]\\d|3[0-6]",
                "([0-2][1-9]|3[01])-(0[1-9]|1[12])-(\\d{4})",
                "[a-z_1-9]|\\Qabcd!@#$%^&*()1234[]?'\\E|(ab)+.",
                "[a-z[q-s]]",
                "[a-z1-9&&[^a-z]]",
                "\\p{Lower}\\p{Punct}\\p{Graph}",
                "^[a-z0-9_-]{3,15}$",
                "(ab(cd(ef)))\\2\\3{2}\\1",
                "(?<new>ab)(ba)\\k<new>{2}\\2\\1+",
                "(ab{2,8}[^abcd])",
                "ab(?!ef)",
                "(ab(cd(?:ef(gh))))\\3",
                "(?<!ab)cd",
                "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
                "([^\\s]+(\\.(jpg|png|gif|bmp))$)",
                "(1[012]|[1-9]):[0-5][0-9](\\s)?(am|pm)",
                "([01]?[0-9]|2[0-3]):[0-5][0-9]",
                "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)",
                "<(\"[^\"]*\"|'[^']*'|[^'\">])*>",
                "<a([^>]+)>(.+?)</a>",
                "\\s*href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+));");
        
        for (String regex : regexps) {
            if (regex.charAt(0) != '#') {
                Pattern pat = Pattern.compile(regex, 0);
                Matcher m;
                RegExParser r = new RegExParser(regex);
                RegEx p = r.parse();
                assertNotNull(p);

                ExampleGenerator eg = new ExampleGenerator(p);
                ArrayList<String> generate = eg.generate(n);
                assertNotNull(generate);
                assertEquals(generate.size(), n);

                for (String examples : generate) {
                        m = pat.matcher(examples);
                        assertTrue(m.matches()); 
                }
            }
        }
    }
}
