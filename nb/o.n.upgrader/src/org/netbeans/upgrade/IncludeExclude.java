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

package org.netbeans.upgrade;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;
import org.openide.util.Union2;



/** A test that is initialized based on includes and excludes.
 *
 * @author Jaroslav Tulach
 */
final class IncludeExclude extends AbstractSet<String> {
    /** List<Boolean and Pattern>
     */
    private final List<Union2<Boolean, Pattern>> patterns = new ArrayList<>();

    private IncludeExclude () {
    }

    /** Reads the include/exclude set from a given reader.
     * @param r reader
     * @return set that accepts names based on include exclude from the file
     */
    public static IncludeExclude create (Reader r) throws IOException {
        IncludeExclude set = new IncludeExclude ();
        
        BufferedReader buf = new BufferedReader (r);
        for (;;) {
            String line = buf.readLine ();
            if (line == null) break;
            
            line = line.trim ();
            if (line.length () == 0 || line.startsWith ("#")) {
                continue;
            }
            
            Boolean plus;
            if (line.startsWith ("include ")) {
                line = line.substring (8);
                plus = Boolean.TRUE;
            } else {
                if (line.startsWith ("exclude ")) {
                    line = line.substring (8);
                    plus = Boolean.FALSE;
                } else {
                    throw new java.io.IOException ("Wrong line: " + line);
                }
            }
            
            Pattern p = Pattern.compile (line);
            
            set.patterns.add (Union2.<Boolean,Pattern>createFirst(plus));
            set.patterns.add (Union2.<Boolean,Pattern>createSecond(p));
        }
        
        return set; 
    }
    
    
    @Override
    public Iterator<String> iterator() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Stream<String> stream() {
        throw new UnsupportedOperationException("not implemented");
    }
    
    @Override
    public int size() {
        throw new UnsupportedOperationException("not implemented");
    }
    
    @Override
    public boolean contains (Object o) {
        String s = (String)o;
        
        boolean yes = false;
        
        Iterator<Union2<Boolean,Pattern>> it = patterns.iterator ();
        while (it.hasNext ()) {
            Boolean include = it.next ().first();
            Pattern p = it.next ().second();
            
            Matcher m = p.matcher (s);
            if (m.matches ()) {
                yes = include;
                if (!yes) {
                    // exclude matches => immediately return
                    return false;
                }
            }
        }
        
        return yes;
    }
    
}
