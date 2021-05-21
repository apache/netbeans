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
package org.netbeans.modules.payara.tooling.server.config;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.payara.tooling.server.parser.ConfigReaderServer;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser;
import org.netbeans.modules.payara.tooling.data.PayaraConfig;

/**
 * Payara configuration reader API.
 * <p/>
 * Allows to access Payara server features and libraries configuration
 * XML file using configuration XML file parser.
 * <p/>
 * XML configuration file reader is called only once. Any subsequent
 * configuration values access will return values cached from first attempt.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PayaraConfigXMLImpl implements PayaraConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara configuration XML file. */
    private final URL configFile;

    /** Payara configuration XML file reader. */
    private final ConfigReaderServer reader = new ConfigReaderServer();

    /** Stores information whether Payara configuration XML file
     *  was already read and processed */
    private volatile boolean readDone;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara configuration API.
     * <p/>
     * @param configFile Payara configuration XML file.
     */
    public PayaraConfigXMLImpl(final URL configFile) {
        this.configFile = configFile;
        readDone = false;
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara libraries configuration.
     * <p/>
     * @return Payara libraries configuration.
     */
    @Override
    public List<LibraryNode> getLibrary() {
        readXml();
        return reader.getLibraries();
    }

    /**
     * Get Payara JavaEE configuration.
     * <p/>
     * @return Payara JavaEE configuration.
     */
    @Override
    public JavaEESet getJavaEE() {
        readXml();
        return reader.getJavaEE();
    }
    
    /**
     * Get Payara JavaSE configuration.
     * <p/>
     * @return Payara JavaSE configuration.
     */
    @Override
    public JavaSESet getJavaSE() {
        readXml();
        return reader.getJavaSE();
    }

    /**
     * Get Payara tools configuration.
     * <p/>
     * @return Payara tools configuration.
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
     * Make sure Payara configuration XML file was read and processed.
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
