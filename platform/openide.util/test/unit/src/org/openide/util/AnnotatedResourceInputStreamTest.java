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
package org.openide.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AnnotatedResourceInputStreamTest {

    //--------------------------------------------------------------------------
    private static Properties annotatedResourceFrom(final String s,
            final boolean localizable) throws IOException {

        Properties result = new Properties();
        try (final InputStream in = new AnnotatedResourceInputStream(
                new ByteArrayInputStream(s.getBytes("ISO-8859-1")), 17, localizable)) {
            result.load(in);
        }
        return result;
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_properlyAnnotatesResource_whenLocalizableIsTrue()
            throws Exception {

        Properties props;

        props = annotatedResourceFrom("k1=val\n", true);

        assertEquals(1, props.size());
        assertEquals("val (17:1)", props.get("k1"));

        props = annotatedResourceFrom("k1:val\n", true);

        assertEquals(1, props.size());
        assertEquals("val (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsKeysWithEmptyValues()
            throws Exception {

        Properties props;

        props = annotatedResourceFrom("k1=  \n", true);

        assertEquals(1, props.size());
        assertEquals("", props.get("k1"));

        props = annotatedResourceFrom("k1=\n", true);

        assertEquals(1, props.size());
        assertEquals("", props.get("k1"));

        props = annotatedResourceFrom("k1:  \n", true);

        assertEquals(1, props.size());
        assertEquals("", props.get("k1"));

        props = annotatedResourceFrom("k1:\n", true);

        assertEquals(1, props.size());
        assertEquals("", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_doesNotAnnotatesResource_whenLocalizableIsFalse()
            throws Exception {

        Properties props = annotatedResourceFrom("k1=val\n", false);

        assertEquals(1, props.size());
        assertEquals("val", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_ignoresComments()
            throws Exception {

        Properties props = annotatedResourceFrom("# some comment\nk1=val\n", true);

        assertEquals(1, props.size());
        assertEquals("val (17:2)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsMultilineValues_skippingNewLines()
            throws Exception {

        Properties props = annotatedResourceFrom("k1=vee \\\none\n", true);

        assertEquals(1, props.size());
        assertEquals("vee one (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_ignoresWhitespaceAtABegginningOfValue()
            throws Exception {

        Properties props;

        props = annotatedResourceFrom("k1= v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));

        props = annotatedResourceFrom("k1=vee \\\n    one\n", true);

        assertEquals(1, props.size());
        assertEquals("vee one (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_ignoresWhitespaceArraoundKey()
            throws Exception {

        Properties props = annotatedResourceFrom(" k1 =v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_keepsWhitespaceAfterValue()
            throws Exception {

        Properties props = annotatedResourceFrom("k1=v1 \n", true);

        assertEquals(1, props.size());
        assertEquals("v1  (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsMissingTrailingNewline()
            throws Exception {

        Properties props = annotatedResourceFrom("k1=v1", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsComplexResources()
            throws Exception {

        Properties props = annotatedResourceFrom(
                " k1 = vee \\\n    one\r\r\n k2  =  vee \\\n    two ", true);

        assertEquals(2, props.size());
        assertEquals("vee one (17:1)", props.get("k1"));
        assertEquals("vee two  (17:4)", props.get("k2"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsI18N()
            throws Exception {

        Properties props = annotatedResourceFrom("#I18N\nk1=v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:2)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsNOI18N()
            throws Exception {

        Properties props = annotatedResourceFrom("#NOI18N\nk1=v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsPARTI18N()
            throws Exception {

        Properties props = annotatedResourceFrom("#PARTI18N\nk1=v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:2)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsBackslassesInKeys()
            throws Exception {

        Properties props = annotatedResourceFrom("k\\1=v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsBackslassesBeforeKeys()
            throws Exception {

        Properties props = annotatedResourceFrom("\\k1=v1\n", true);

        System.out.println(props);
        assertEquals(1, props.size());
        assertEquals("v1 (17:0)", props.get("k1")); // is should probably be "v1 (17:1)"
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsEmptyKeys()
            throws Exception {

        Properties props = annotatedResourceFrom("k1\n", true);

        System.out.println(props);
        assertEquals(1, props.size());
        assertEquals("", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsBackslassesInValues()
            throws Exception {

        Properties props;

        props = annotatedResourceFrom("k1=\\v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));

        props = annotatedResourceFrom("k1=v\\1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("k1"));

        props = annotatedResourceFrom("k1=v1\\\n", true);

        assertEquals(1, props.size());
        assertEquals("v1(17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void stream_supportsUnicodeEscapes()
            throws Exception {

        Properties props;

        props = annotatedResourceFrom("k\u00411=v1\n", true);

        assertEquals(1, props.size());
        assertEquals("v1 (17:1)", props.get("kA1"));

        props = annotatedResourceFrom("k1=v\u00411\n", true);

        assertEquals(1, props.size());
        assertEquals("vA1 (17:1)", props.get("k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void close_closesUnderlyingStream()
            throws Exception {

        FakeStream stream = new FakeStream();

        new AnnotatedResourceInputStream(stream, 0, true).close();

        assertTrue(stream.isClosed);
    }

    //--------------------------------------------------------------------------
    private static class FakeStream extends InputStream {

        boolean isClosed = false;

        //----------------------------------------------------------------------
        @Override
        public void close() throws IOException {

            this.isClosed = true;
        }

        //----------------------------------------------------------------------
        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
