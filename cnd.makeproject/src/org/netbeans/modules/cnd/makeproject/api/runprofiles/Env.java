/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
