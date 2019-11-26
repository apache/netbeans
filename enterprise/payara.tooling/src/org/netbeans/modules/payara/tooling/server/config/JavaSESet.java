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
 */package org.netbeans.modules.payara.tooling.server.config;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container of Payara JavaSE features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaSESet extends JavaSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private final List<String> platforms;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of container of Payara JavaSE features
     * configuration.
     * <p/>
     * @param platforms Platforms retrieved from XML elements.
     * @param version   Highest JavaSE specification version implemented.
     */
    public JavaSESet(final List<String> platforms, final String version) {
        super(version);
        this.platforms = platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get platforms retrieved from XML elements.
     * <p/>
     * @return Platforms retrieved from XML elements.
     */
    public List<String> getPlatforms() {
        return platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build {@link Set} of {@link JavaSEPlatform} for known platforms
     * retrieved from XML elements.
     * <p/>
     * @return {@link Set} of {@link JavaSEPlatform} for known platforms.
     */
    public Set<JavaSEPlatform> platforms() {
        int size = platforms != null ? platforms.size() : 0;
        EnumSet<JavaSEPlatform> platformsSet
                = EnumSet.noneOf(JavaSEPlatform.class);
        if (size > 0) {
            for (String name : platforms) {
                JavaSEPlatform type = JavaSEPlatform.toValue(name);
                if (type != null) {
                    platformsSet.add(type);
                }
            }
        }
        return platformsSet;
    }

}
