/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Fetch GlassFish log from server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class FetchLog {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLog.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server for fetching server log. */
    GlassFishServer server;

    /** Input stream which will provide access to log retrieved from server. */
    final InputStream in;

    /** Request to skip to the end of log. */
    final boolean skip;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback which will initialize log <code>InputStream</code>.
     * <p/>
     * @return <code>InputStream</code> where log lines received from server
     *         will be available to read.
     */
    abstract InputStream initInputStream();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an empty instance of GlassFish server log fetcher using
     * provided input stream.
     * <p/>
     * <code>InputStream</code> is set using constructor argument. Child class
     * <code>initInputStream</code> method is ignored.
     * <p/>
     * @param in     Input stream used to read server log.
     * @param skip   Skip to the end of the log file.
     */
    FetchLog(InputStream in, boolean skip) {
        this.server = null;
        this.in = in;
        this.skip = skip;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher.
     * <p/>
     * <code>InputStream</code> is set using child
     * <code>initInputStream</code> method.
     * <p/>
     * @param server GlassFish server for fetching server log.
     * @param skip   Skip to the end of the log file.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    FetchLog(GlassFishServer server, boolean skip) {
        this.server = server;
        this.in = initInputStream();
        this.skip = skip;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get input stream for reading lines from server log file.
     * <p/>
     * @return Input stream for reading lines from server log file.
     */
    public InputStream getInputStream() {
        return this.in;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Close input stream used to access log lines received from server.
     * <p/>
     * This should be overridden in child classes to handle all streams and
     * threads properly.
     */
    public void close() {
        final String METHOD = "close";
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, METHOD, "cantClose", ioe);
            }
        } else {
            LOGGER.log(Level.INFO, METHOD, "isNull");
        }
    }

}
