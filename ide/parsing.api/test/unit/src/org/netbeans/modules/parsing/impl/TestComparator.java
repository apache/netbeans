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

package org.netbeans.modules.parsing.impl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hanz
 */
public class TestComparator {

    private String      text;
    private boolean     failed = false;

    public TestComparator (String text) {
        this.text = text;
    }

    public void check (String line) {
        if (failed) return;
        if (!text.startsWith (line)) {
            failed = true;
            throw new IllegalArgumentException (line + "\nBut expecting:\n" + text);
        }
        text = text.substring (line.length ());
        if (text.startsWith ("\n"))
            text = text.substring (1);
    }

    private Map<Class,Map<Object,Integer>> classToObjects = new HashMap<Class,Map<Object,Integer>> ();

    public int get (Object o) {
        if (failed) return -1;
        Map<Object,Integer> objects = classToObjects.get (o.getClass ());
        if (objects == null) {
            objects = new HashMap<Object,Integer> ();
            classToObjects.put (o.getClass (), objects);
        }
        Integer i = objects.get (o);
        if (i == null) {
            i = objects.size () + 1;
            objects.put (o, i);
        }
        return i;
    }

    public String getResult () {
        return text;
    }
}


