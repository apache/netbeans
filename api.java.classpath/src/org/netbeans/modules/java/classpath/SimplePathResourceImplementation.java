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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.classpath;


import java.io.File;
import java.net.URISyntaxException;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import java.net.URL;
import org.openide.util.BaseUtilities;


/**
 * Provides implementation of the single rooted PathResoruceImplementation
 */

public final class SimplePathResourceImplementation  extends PathResourceBase {

    /**
     * Check URL for correct syntax for a classpath root.
     * Must be folder URL, and JARs must use jar protocol.
     * @param context additional information to include in exception, or null if stack trace suffices
     */
    public static void verify(final URL root, String context) throws IllegalArgumentException {
        verify(root, context, null);
    }

    /**
     * Check URL for correct syntax for a classpath root.
     * Must be folder URL, and JARs must use jar protocol.
     * @param root to verify
     * @param context additional information to include in exception, or null if stack trace suffices
     * @param initiatedIn the root case
     */
    public static void verify(final URL root,
            String context,
            final Throwable initiatedIn) throws IllegalArgumentException {
        if (context == null) {
            context = "";
        }
        if (root == null) {
            final IllegalArgumentException iae = new IllegalArgumentException("Root cannot be null." + context);
            if (initiatedIn != null) {
                iae.initCause(initiatedIn);
            }
            throw iae;
        }
        final String rootS = root.toString();
        if (rootS.matches("file:.+[.]jar/?")) {
            File f = null;
            boolean dir = false;
            try {
                f = BaseUtilities.toFile(root.toURI());
                dir = f.isDirectory();
            } catch (URISyntaxException use) {
                //pass - handle as non dir
            }
            if (!dir) {
                boolean exists = false;
                if (f == null || (exists = f.exists())) {   //NOI18N
                    final IllegalArgumentException iae = new IllegalArgumentException(rootS + " is not a valid classpath entry; use a jar-protocol URL."    //NOI18N
                            + "[File: " + f + ", exists: " + exists +"]"    //NOI18N
                            + context);
                    if (initiatedIn != null) {
                        iae.initCause(initiatedIn);
                    }
                    throw iae;
                }
            }
        }
        if (!rootS.endsWith("/")) {
            final IllegalArgumentException iae = new IllegalArgumentException(rootS + " is not a valid classpath entry; it must end with a slash." + context);
            if (initiatedIn != null) {
                iae.initCause(initiatedIn);
            }
            throw iae;
        }
        if (rootS.contains("/../") || rootS.contains("/./")) {  //NOI18N
            final IllegalArgumentException iae = new IllegalArgumentException(rootS + " is not a valid classpath entry; it cannot contain current or parent dir reference." + context); //NOI18N
            if (initiatedIn != null) {
                iae.initCause(initiatedIn);
            }
            throw iae;
        }
    }

    private URL url;

    public SimplePathResourceImplementation (URL root) {
        verify(root, null);
        this.url = root;
    }


    public URL[] getRoots() {
        return new URL[] {url};
    }

    public ClassPathImplementation getContent() {
        return null;
    }

    public String toString () {
        return "SimplePathResource{" + url + "}"; // NOI18N
    }

    public int hashCode () {
        return url.hashCode();
    }

    public boolean equals (Object other) {
        if (other instanceof SimplePathResourceImplementation) {
            SimplePathResourceImplementation opr = (SimplePathResourceImplementation) other;
            return url.equals(opr.url);
        }
        else
            return false;
    }
}
