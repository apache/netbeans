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
package org.netbeans.modules.payara.tooling.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * JAR file utilities.
 * <p/>
 * This class is a stream wrapper. {@link #close} method should be called
 * before class instance is abandoned like when working with streams.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Jar {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** JManifest attribute containing version string. */
    public static final String MANIFEST_BUNDLE_VERSION = "Bundle-Version";

     ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    /** JAR file input stream. */
    private final JarInputStream jar;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of JAR file.
     * <p/>
     * @param jarfile JAR file to be opened.
     */
    public Jar(File jarfile) {
        JarInputStream jarStream = null;
        try {
        jarStream = new JarInputStream(new FileInputStream(jarfile));
        } catch (IOException ioe) {
            jar = null;
            throw new JarException(JarException.OPEN_ERROR, ioe);
        }
        jar = jarStream;
    }

    /**
     * Creates an instance of JAR file.
     * <p/>
     * @param jarfile JAR file to be opened.
     */
    public Jar(String jarfile) {
        JarInputStream jarStream = null;
        try {
        jarStream = new JarInputStream(new FileInputStream(jarfile));
        } catch (IOException ioe) {
            jar = null;
            throw new JarException(JarException.OPEN_ERROR, ioe);
        }
        jar = jarStream;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the <code>Manifest</code> for this JAR file, or
     * <code>null</code> if none.
     * <p/>
     * @return The <code>Manifest</code> for this JAR file, or
     *         <code>null</code> if none.
     */
    public Manifest getManifest() {
        return jar.getManifest();
    }

    /**
     * Returns the bundle version string from Manifest file.
     * <p/>
     * @return Bundle version string from Manifest file or <code>null</code>
     *         when no such attribute exists.
     */
    public String getBundleVersion() {
        Manifest manifest = jar.getManifest();
        Attributes attrs = manifest != null
                ? manifest.getMainAttributes() : null;
        return attrs != null ? attrs.getValue(MANIFEST_BUNDLE_VERSION) : null;
    }

    /**
     * Close JAR file and release all allocated resources.
     * <p/>
     * This method should be called when this object is being released to avoid
     * memory leaks.
     */
    public void close() {
        if (jar != null) {
            try {
                jar.close();
            } catch (IOException ioe) {
                throw new JarException(JarException.CLOSE_ERROR, ioe);
            }
        }
    }


}
