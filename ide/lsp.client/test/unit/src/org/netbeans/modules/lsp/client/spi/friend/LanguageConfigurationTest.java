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
package org.netbeans.modules.lsp.client.spi.friend;

import org.junit.Test;
import static org.junit.Assert.*;

public class LanguageConfigurationTest {

    public LanguageConfigurationTest() {
    }

    @Test
    public void testComments() {
        LanguageConfiguration lc;

        lc = LanguageConfiguration.from("""
                                        {
                                            "comments": {
                                                "lineComment": "//"
                                            }
                                        }
                                        """);
        assertNotNull(lc.comments);
        assertNotNull(lc.comments.lineComment);
        assertEquals("//", lc.comments.lineComment.comment);
        assertFalse(lc.comments.lineComment.noIdent);
        assertNull(lc.comments.blockComment);

        lc = LanguageConfiguration.from("""
                                        {
                                            "comments": {
                                                "blockComment": ["/*", "*/"]
                                            }
                                        }
                                        """);
        assertNotNull(lc.comments);
        assertNull(lc.comments.lineComment);
        assertNotNull(lc.comments.blockComment);
        assertEquals("/*", lc.comments.blockComment.first);
        assertEquals("*/", lc.comments.blockComment.second);

        lc = LanguageConfiguration.from("""
                                        {
                                            "comments": {
                                                "lineComment": {
                                                    "comment": "//",
                                                    "noIndent": true
                                                }
                                            }
                                        }
                                        """);
        assertNotNull(lc.comments);
        assertNotNull(lc.comments.lineComment);
        assertNull(lc.comments.blockComment);
        assertEquals("//", lc.comments.lineComment.comment);
        assertTrue(lc.comments.lineComment.noIdent);
    }

}
