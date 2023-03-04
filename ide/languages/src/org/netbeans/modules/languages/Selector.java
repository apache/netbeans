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

package org.netbeans.modules.languages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jan Jancura
 */
public class Selector {
    
    public static Selector create (String selector) {
        List<String> path = new ArrayList<String> ();
        int s = 0, e = selector.indexOf ('.');
        while (e >= 0) {
            path.add (selector.substring (s, e));
            s = e + 1;
            e = selector.indexOf ('.', s);
        }
        path.add (selector.substring (s));
        return new Selector (path);
    }
    
    private List<String> path;
    
    private Selector (List<String> path) {
        this.path = path;
    }
    
    List<String> getPath () {
        return path;
    }
    
    private String asText;
    
    public String getAsString () {
        if (asText == null) {
            Iterator<String> it = path.iterator ();
            StringBuilder sb = new StringBuilder ();
            if (it.hasNext ())
                sb.append (it.next ());
            while (it.hasNext ())
                sb.append ('.').append (it.next ());
            asText = sb.toString ();
        }
        return asText;
    }
    
    public String toString () {
        return getAsString ();
    }
}
