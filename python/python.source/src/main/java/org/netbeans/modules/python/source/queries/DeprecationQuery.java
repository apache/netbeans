/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.source.queries;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ralph Ruijs
 */
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

        /*
        al.rst:    The :mod:`al` module has been deprecated for removal in Python 3.0.
        al.rst:   The :mod:`AL` module has been deprecated for removal in Python 3.0.
        bsddb.rst:    The :mod:`bsddb` module has been deprecated for removal in Python 3.0.
        cd.rst:    The :mod:`cd` module has been deprecated for removal in Python 3.0.
        dbhash.rst:    The :mod:`dbhash` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`fl` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`FL` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`flp` module has been deprecated for removal in Python 3.0.
        fm.rst:   The :mod:`fm` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`gl` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`DEVICE` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`GL` module has been deprecated for removal in Python 3.0.
        imgfile.rst:   The :mod:`imgfile` module has been deprecated for removal in Python 3.0.
        jpeg.rst:   The :mod:`jpeg` module has been deprecated for removal in Python 3.0.
        statvfs.rst:   The :mod:`statvfs` module has been deprecated for removal in Python 3.0.
        sunaudio.rst:   The :mod:`sunaudiodev` module has been deprecated for removal in Python 3.0.
        sunaudio.rst:   The :mod:`SUNAUDIODEV` module has been deprecated for removal in Python 3.0.
        tarfile.rst:      The :class:`TarFileCompat` class has been deprecated for removal in Python 3.0.
         */

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
