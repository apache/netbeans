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
package org.netbeans.modules.htmlui.impl;

import java.net.URL;
import java.net.URLStreamHandlerFactory;
import org.openide.util.Lookup;
import static org.testng.Assert.assertNotNull;

/**
 *
 * @author Jaroslav Tulach
 */
final class NbResloc {
    static {
        URLStreamHandlerFactory f = Lookup.getDefault().lookup(URLStreamHandlerFactory.class);
        assertNotNull(f, "Factory found");
        URL.setURLStreamHandlerFactory(f);
    }
    static void init() {
    }
}
