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
package org.netbeans.modules.java.source.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author Tomas Zezula
 */
public class ProxyFileManagerTest extends NbTestCase {
    
    public ProxyFileManagerTest(@NonNull final String name) {
        super(name);
    }

    public void testRepeatableIterator() {
        final List<String> options = Arrays.asList(new String[] {
            "--patch-module",       //NOI18N
            "a=apatch.jar",         //NOI18N
            "--patch-module",       //NOI18N
            "b=bpatch.jar",         //NOI18N
        });
        final Map<String,List<String>> expected = new HashMap<>();
        expected.put("a",Collections.singletonList("apatch.jar"));  //NOI18N
        expected.put("b",Collections.singletonList("bpatch.jar"));  //NOI18N
        final PatchProcessor[] processors = new PatchProcessor[2];
        for (int i=0; i< processors.length; i++) {
            processors[i] = new PatchProcessor();
        }
        final ProxyFileManager.RepeatableIterator<String> it =
                ProxyFileManager.RepeatableIterator.create(options.iterator());
        for (int i=0; i< processors.length; i++) {
            final Map<String, List<String>> r = processors[i].apply(it);
            assertEquals(expected, r);
            it.reset();
        }
    }
    
    
    private static final class PatchProcessor implements Function<Iterator<String>,Map<String,List<String>>> {

        @Override
        public Map<String, List<String>> apply(Iterator<String> t) {
            final Map<String,List<String>> res = new HashMap<>();
            boolean inPatch = false;
            final Pattern p = Pattern.compile("(\\S+)=(\\S+)"); //NOI18N
            while (t.hasNext()) {
                String current = t.next();
                if (inPatch) {
                    final Matcher m = p.matcher(current);
                    if (m.matches() && m.groupCount() == 2) {
                        String module = m.group(1);
                        String[] path = m.group(2).split(":");  //NOI18N
                        res.put(module, Arrays.asList(path));
                    }
                }
                inPatch = "--patch-module".equals(current); //NOI18N
            }
            return res;
        }
        
    }
}
