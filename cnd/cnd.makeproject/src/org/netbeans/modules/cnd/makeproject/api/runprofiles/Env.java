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

package org.netbeans.modules.cnd.makeproject.api.runprofiles;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Generic manipulation and management of unix-like environment variables.
 * Allows easy setup of environment variables in order to be passed to
 * Runtime.exec().
 */

public final class Env implements Cloneable {
    private Vector<String[]> environ;

    public Env() {
        environ = new Vector<>();
    }

    public void removeAll() {
        environ = new Vector<>();
    }

    /**
     * Remove the entry with the given name
     */
    public void removeByName(String name) {
        if (name == null) {
            return;
        }
        String[] entry = getenvAsPair(name);
        environ.removeElement(entry);
    }

    /**
     * Returns the whole entry in the form of <code>name=value</code>.
     */
    public String getenvEntry(String name) {
        String value = getenv(name);
        if (value != null) {
            return name + "=" + value; // NOI18N
        } else {
            return null;
        }
    } 

    /**
     * Returns the entry in the form of String[2]
     */
    public String[] getenvAsPair(String name) {
        for (String[] nameValue : environ) {
            if (nameValue[0].equals(name)) {
                return nameValue;
            }
        }
        return null;
    } 

    /**
     * Returns just the value, like getenv(3).
     */
    public String getenv(String name) {
        for (String[] nameValue : environ) {
            if (nameValue[0].equals(name)) {
                return nameValue[1];
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String[] envStrings = getenv();
        boolean addSep = false;
        StringBuilder envString = new StringBuilder();
        for (int i = 0; i < envStrings.length; i++) {
            if (addSep) {
                envString.append(";"); // NOI18N
            }
            envString.append(envStrings[i]);
            addSep = true;
        }
        return envString.toString();
    }

    public String encode() {
        return toString();
    }
    
    public void decode(String envlist) {
        StringTokenizer tokenizer = new StringTokenizer(envlist, " ;"); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            putenv(tokenizer.nextToken());
        }
    }
            
    @Override
    public boolean equals(Object o) {
        boolean eq = false;
        if (o instanceof Env) {
            Env env = (Env)o;
            eq = toString().equals(env.toString());
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Takes <code>name=value</code> format.
     */
    public void putenv(String entry) {
        int equalx = entry.indexOf('='); // NOI18N
        if (equalx == -1) {
            System.err.println("Env.putenv(): odd entry '" + entry + "'"); // NOI18N
            return;
        }
        String name = entry.substring(0, equalx);
        String value = entry.substring(equalx + 1);
        putenv(name, value);
    } 

    /**
     * Sets or creates a new environment variable
     */
    public void putenv(String name, String value) {
        String[] entry = getenvAsPair(name);
        if (entry != null) {
            entry[1] = value;
        } else {
            environ.add(new String[]{name, value});
        }
    } 

    /**
     * Convert the internal representation to an array of Strings
     * Suitable for passing to Runtime.exec.
     */
    public String[] getenv() {
        String array[] = new String[environ.size()];

        int index = 0;
        for (String[] nameValue : environ) {
            array[index++] = nameValue[0] + "=" + nameValue[1]; // NOI18N
        }
        return array;
    } 

    /**
     * Converts the internal representation to an array of variable/value pairs
     */
    public String[][] getenvAsPairs() {
        String array[][] = new String[environ.size()][2];

        int index = 0;
        for (String[] nameValue : environ) {
            array[index++] = nameValue;
        }
        return array;
    }

    /**
     * Converts the internal representation to a map of variable/value pairs
     */
    public Map<String, String> getenvAsMap() {
        Map<String, String> res = new HashMap<>(environ.size());

        environ.forEach((nameValue) -> {
            res.put(nameValue[0], nameValue[1]);
        });
        return res;
    }

    public void assign(Env env) {
        if (this != env) {
            removeAll();
            String[][] pairs = env.getenvAsPairs();
            for (int i = 0; i < pairs.length; i++) {
                putenv(pairs[i][0], pairs[i][1]);
            }
        }
    }

    /**
     * Clone the environment creating an identical copy.
     */
    @Override
    public Env clone() {
        Env clone = new Env();
        String[][] pairs = getenvAsPairs();
        for (int i = 0; i < pairs.length; i++) {
            clone.putenv(pairs[i][0], pairs[i][1]);
        }
        return clone;
    }
}
