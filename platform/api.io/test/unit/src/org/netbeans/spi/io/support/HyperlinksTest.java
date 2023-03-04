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
package org.netbeans.spi.io.support;

import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.api.intent.Intent;
import org.netbeans.api.io.Hyperlink;

/**
 *
 * @author jhavlin
 */
public class HyperlinksTest {

    @Test
    public void testOnClickLink() {

        final boolean[] invoked = new boolean[1];
        Hyperlink h = Hyperlink.from(new Runnable() {

            @Override
            public void run() {
                invoked[0] = true;
            }
        });
        assertTrue(Hyperlinks.getType(h) == HyperlinkType.FROM_RUNNABLE);
        assertFalse(Hyperlinks.isImportant(h));
        assertNotNull(Hyperlinks.getRunnable(h));
        Hyperlinks.getRunnable(h).run();
        assertTrue("The passed code should be invoked", invoked[0]);
    }

    @Test
    public void testOnClickLinkImportant() {
        Hyperlink h = Hyperlink.from(new Runnable() {
            @Override
            public void run() {
            }
        }, true);
        assertTrue(Hyperlinks.isImportant(h));
    }

    @Test
    public void testIntentHyperlink() throws URISyntaxException {
        Intent i = new Intent(Intent.ACTION_EDIT, new URI("scheme://abc"));
        Hyperlink h = Hyperlink.from(i);
        assertEquals(HyperlinkType.FROM_INTENT, Hyperlinks.getType(h));
        assertEquals(i, Hyperlinks.getIntent(h));
    }
}
