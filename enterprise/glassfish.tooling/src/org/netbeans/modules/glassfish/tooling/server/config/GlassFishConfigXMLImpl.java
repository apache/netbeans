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

    // Instance attributes                                                    //
    /** GlassFish configuration XML file. */
    private final URL configFile;

    /** GlassFish configuration XML file reader. */
    private final ConfigReaderServer reader = new ConfigReaderServer();

    /** Stores information whether GlassFish configuration XML file
     *  was already read and processed */
    private volatile boolean readDone;

    // Constructors                                                           //
    /**
     * Creates an instance of GlassFish configuration API.
     * <p/>
     * @param configFile GlassFish configuration XML file.
     */
    public GlassFishConfigXMLImpl(final URL configFile) {
        this.configFile = configFile;
        readDone = false;
    }
   
    // Getters and setters                                                    //
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
    
    // Methods                                                                //
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
