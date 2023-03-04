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
package org.openide.filesystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class MIMESupportHid {

    static void assertNonDeclarativeResolver(String msg, MIMEResolver expected, MIMEResolver... arr) {
        assertNonDeclarativeResolver(msg, new MIMEResolver[] { expected }, arr);
    }
    static void assertNonDeclarativeResolver(String msg, MIMEResolver[] expected, MIMEResolver... arr) {
        Assert.assertNotNull(msg + " result was computed", arr);
        List<MIMEResolver> filter = new ArrayList<MIMEResolver>();
        for (MIMEResolver m : arr) {
            if (!MIMEResolverImpl.isDeclarative(m)) {
                filter.add(m);
            }
        }
        List<MIMEResolver> exp = Arrays.asList(expected);
        Assert.assertTrue(msg + " Expected " + exp + " present: " + filter, exp.equals(filter));
    }
    
}
