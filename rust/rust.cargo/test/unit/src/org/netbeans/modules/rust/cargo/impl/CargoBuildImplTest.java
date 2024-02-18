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
package org.netbeans.modules.rust.cargo.impl;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.modules.rust.cargo.api.RustPackage;

/**
 *
 * @author antonio
 */
public class CargoBuildImplTest {

    public CargoBuildImplTest() {
    }

    @Test
    public void testShouldFilterCargoSearchResultsCorrectly() {
        // Given a set of lines result of "cargo search ..."
        String[] lines = {
            "regex = \"1.7.1\"                    # An implementation of regular expressions for Rust. This implementation uses finite automata …\n",
            "lazy-regex = \"2.4.1\"               # lazy static regular expressions checked at compile time\n",
            "proc-macro-regex = \"1.1.0\"         # A proc macro regex library\n",
            "regex-automata = \"0.2.0\"           # Automata construction and matching using regular expressions.\n",
            "easy-regex = \"0.11.7\"              # Make long regular expressions like pseudocodes\n",
            "readable-regex = \"0.1.0-alpha1\"    # Regex made for humans. Wrapper to build regexes in a verbose style.\n",
            "human_regex = \"0.2.3\"              # A regex library for humans\n",
            "regex_static = \"0.1.1\"             # Compile-time validated regex, with convenience functions for lazy and static regexes.\n",
            "webforms = \"0.2.2\"                 # Provides form validation for web forms\n",
            "hashtag-regex = \"0.1.1\"            # A simple regex matching hashtags accoding to the unicode spec: http://unicode.org/reports/tr…\n",
            "safe-regex = \"0.2.5\"               # Safe regular expression library\n",
            "pleaser = \"0.5.4\"                  # please, a polite regex-first sudo alternative\n",
            "rand_regex = \"0.15.1\"              # Generates random strings and byte strings matching a regex\n",
            "regex-macro = \"0.2.0\"              # A macro to generate a lazy regex expression\n",
            "regex-syntax = \"0.6.28\"            # A regular expression parser.\n",
            "... and 930 crates more (use --limit N to see more)"
        };
        // ... when we filter them...
        List<RustPackage> packages = CargoCLIImpl.filterLines(null, Arrays.asList(lines));
        // .. then 15 packages should have been detected
        Assert.assertEquals(15, packages.size());
    }
}
