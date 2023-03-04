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

package org.netbeans.modules.web.core.syntax.completion;

import java.util.Map;
import java.util.TreeMap;

/**
 * Associate the short names (prefixes) of most often used tag libraries
 * with their URI. This is used to guess and insert missing tag lib import
 * directive.
 *
 * TODO: perhaps the functionality of this class could be replaced with
 * a call to JspSyntaxSupport.getTagLibraryMappings() and then
 * parsing all the TLDs. At this stage it seems inefficient and not
 * necessary though.
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class StandardTagLibraryPrefixes {
    private static Map<String, String> standardPrefixes = new TreeMap<String, String>();

    static {
        standardPrefixes.put("c", "http://java.sun.com/jsp/jstl/core");
        standardPrefixes.put("x", "http://java.sun.com/jsp/jstl/xml");
        standardPrefixes.put("fmt", "http://java.sun.com/jsp/jstl/fmt");
        standardPrefixes.put("sql", "http://java.sun.com/jsp/jstl/sql");
    }

    public static String get(String prefix){
        return standardPrefixes.get(prefix);
    }
}
