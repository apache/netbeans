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
