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

package org.netbeans.nbbuild;

import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtilTest extends NbTestCase {

    public XMLUtilTest(String n) {
        super(n);
    }

    public void testHandlerLeak() throws Exception {
        ErrorHandler handler = new DefaultHandler();
        EntityResolver resolver = new DefaultHandler();
        Reference<?> handlerRef = new WeakReference<Object>(handler);
        Reference<?> resolverRef = new WeakReference<Object>(resolver);
        XMLUtil.parse(new InputSource(new StringReader("<hello/>")), false, false, handler, resolver);
        handler = null;
        resolver = null;
        assertGC("can collect handler", handlerRef);
        assertGC("can collect resolver", resolverRef);
    }

}
