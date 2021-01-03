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
package org.netbeans.modules.python.source.queries;

import java.util.HashMap;
import java.util.Map;

public final class DeprecationQuery {
    private static final Map<String, String> DEPRECATED = new HashMap<>();


    static {
        for (String module : new String[]{"cl", "sv", "timing"}) {
            DEPRECATED.put(module, "Listed as obsolete in the library documentation");
        }

        for (String module : new String[]{
                    "addpack", "cmp", "cmpcache", "codehack", "dircmp", "dump", "find", "fmt",
                    "grep", "lockfile", "newdir", "ni", "packmail", "Para", "poly",
                    "rand", "reconvert", "regex", "regsub", "statcache", "tb", "tzparse",
                    "util", "whatsound", "whrandom", "zmod"}) {
            DEPRECATED.put(module, "Obsolete module, removed in Python 2.5");

        }

        for (String module : new String[]{"gopherlib", "rgbimg", "macfs"}) {
            DEPRECATED.put(module, "Obsolete module, removed in Python 2.6");
        }


        DEPRECATED.put("posixfile",
                "Locking is better done by fcntl.lockf().");

        DEPRECATED.put("gopherlib",
                "The gopher protocol is not in active use anymore.");

        DEPRECATED.put("rgbimgmodule",
                "");

        DEPRECATED.put("pre",
                "The underlying PCRE engine doesn't support Unicode, and has been unmaintained since Python 1.5.2.");

        DEPRECATED.put("whrandom",
                "The module's default seed computation was inherently insecure; the random module should be used instead.");

        DEPRECATED.put("rfc822",
                "Supplanted by Python 2.2's email package.");

        DEPRECATED.put("mimetools",
                "Supplanted by Python 2.2's email package.");

        DEPRECATED.put("MimeWriter",
                "Supplanted by Python 2.2's email package.");

        DEPRECATED.put("mimify",
                "Supplanted by Python 2.2's email package.");

        DEPRECATED.put("rotor",
                "Uses insecure algorithm.");

        DEPRECATED.put("TERMIOS.py",
                "The constants in this file are now in the 'termios' module.");

        DEPRECATED.put("statcache",
                "Using the cache can be fragile and error-prone; applications should just use os.stat() directly.");

        DEPRECATED.put("mpz",
                "Third-party packages provide similiar features and wrap more of GMP's API.");


        DEPRECATED.put("xreadlines",
                "Using 'for line in file', introduced in 2.3, is preferable.");

        DEPRECATED.put("multifile",
                "Supplanted by the email package.");

        DEPRECATED.put("sets",
                "The built-in set/frozenset types, introduced in Python 2.4, supplant the module.");

        DEPRECATED.put("buildtools",
                "");

        DEPRECATED.put("cfmfile",
                "");

        DEPRECATED.put("macfs",
                "");

        DEPRECATED.put("md5",
                "Replaced by the 'hashlib' module.");

        DEPRECATED.put("sha",
                "Replaced by the 'hashlib' module.");
    }

    public static boolean isDeprecatedModule(String module) {
        return DEPRECATED.containsKey(module);
    }
    
    public static String getDeprecatedModuleDescription(String module) {
        return DEPRECATED.get(module);
    }

    private DeprecationQuery() {
    }
}
