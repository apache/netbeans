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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.glassfish.tooling.data.GlassFishConfig;
import org.netbeans.modules.glassfish.tooling.server.parser.ConfigReaderServer;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser;

/**
 * GlassFish configuration reader API.
 * <p/>
 * Allows to access GlassFish server features and libraries configuration
 * XML file using configuration XML file parser.
 * <p/>
 * XML configuration file reader is called only once. Any subsequent
 * configuration values access will return values cached from first attempt.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishConfigXMLImpl implements GlassFishConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish configuration XML file. */
    private final URL configFile;

    /** GlassFish configuration XML file reader. */
    private final ConfigReaderServer reader = new ConfigReaderServer();

    /** Stores information whether GlassFish configuration XML file
     *  was already read and processed */
    private volatile boolean readDone;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish configuration API.
     * <p/>
     * @param configFile GlassFish configuration XML file.
     */
    public GlassFishConfigXMLImpl(final URL configFile) {
        this.configFile = configFile;
        readDone = false;
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish libraries configuration.
     * <p/>
     * @return GlassFish libraries configuration.
     */
    @Override
    public List<LibraryNode> getLibrary() {
        readXml();
        return reader.getLibraries();
    }

    /**
     * Get GlassFish JavaEE configuration.
     * <p/>
     * @return GlassFish JavaEE configuration.
     */
    @Override
    public JavaEESet getJavaEE() {
        readXml();
        return reader.getJavaEE();
    }
    
    /**
     * Get GlassFish JavaSE configuration.
     * <p/>
     * @return GlassFish JavaSE configuration.
     */
    @Override
    public JavaSESet getJavaSE() {
        readXml();
        return reader.getJavaSE();
    }

    /**
     * Get GlassFish tools configuration.
     * <p/>
     * @return GlassFish tools configuration.
     */
    @Override
    public Tools getTools() {
        readXml();
        return reader.getTools();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Make sure GlassFish configuration XML file was read and processed.
     */
    private void readXml() {
        if (readDone)
            return;
        synchronized(reader) {
            if (!readDone) {
                TreeParser.readXml(configFile, reader);
                readDone = true;
            }
        }
    }
    
}
