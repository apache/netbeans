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

import java.util.List;

/**
 *
 * @author hanz
 */
public class Rule {

    private String  nt;
    private List    right;

    private Rule () {}

    public static Rule create (
        String      nt, 
        List        right
    ) {
        Rule r = new Rule ();
        r.nt = nt;
        r.right = right;
        return r;
    }

    public String getNT () {
        return nt;
    }

    public List getRight () {
        return right;
    }

    private String toString = null;

    @Override
    public String toString () {
        if (toString == null) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("Rule ").append (nt).append (" = ");
            int i = 0, k = right.size ();
            if (i < k) 
                sb.append (right.get (i++));
            while (i < k)
                sb.append (' ').append (right.get (i++));
            toString = sb.toString ();
        }
        return toString;
    }
}
