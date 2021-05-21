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
package org.netbeans.modules.payara.eecommon.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

// This is J2eeModule.Type rewritten as Enum. I could not modify original class
// because it's API.
/**
 * Java EE module types.
 * <p/>
 * @author Tomas Kraus
 */
public enum JavaEEModule {
    
    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Client application archive. */
    CAR(ModuleType.CAR),
    /** Enterprise application archive */
    EAR(ModuleType.EAR),
    /** Enterprise Java bean archive. */
    EJB(ModuleType.EJB),
    /** Connector archive. */
    RAR(ModuleType.RAR),
    /** Web application archive. */
    WAR(ModuleType.WAR);

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara Java EE common module Logger. */
    private static final Logger LOGGER = Logger.getLogger("payara-eecommon");

    /** JavaEEModule version enumeration length. */
    public static final int length = JavaEEModule.values().length;

    /** Name of web application configuration directory. */
    public static final String WEB_INF = "WEB-INF";

    /** Name of java archive manifest directory. */
    public static final String META_INF = "META-INF";

    /** {@link J2eeModule.Type} to {@link JavaEEModule} conversion map. */
    private static final Map<J2eeModule.Type, JavaEEModule> j2eeModuleTypeToValue
            = new HashMap<J2eeModule.Type, JavaEEModule>(2*length);

    // Initialize J2eeModule.Type to JavaEEModule conversion map.
    static {
        j2eeModuleTypeToValue.put(J2eeModule.Type.CAR, CAR);
        j2eeModuleTypeToValue.put(J2eeModule.Type.EAR, EAR);
        j2eeModuleTypeToValue.put(J2eeModule.Type.EJB, EJB);
        j2eeModuleTypeToValue.put(J2eeModule.Type.RAR, RAR);
        j2eeModuleTypeToValue.put(J2eeModule.Type.WAR, WAR);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

   /**
     * Get Java EE module configuration directory (e.g. {@code "META-INF"}).
     * This is just {@link J2eeModule.Type} shortcut.
     * <p/>
     * @param type {@link J2eeModule.Type} instance of Java EE module type.
     * @return Java EE module configuration directory for known Java EE
     *         module type or {@code null} when provided Java EE module type
     *         is not known.
     */
    public static final String getConfigDir(J2eeModule.Type type) {
        JavaEEModule configDir = JavaEEModule.toValue(type);
        return type != null ? configDir.getConfigDir() : null;
    }

    /**
     * Convert {@link J2eeModule.Type} to {@link JavaEEModule}.
     * <p/>
     * @param type {@link ModuleType} value to be converted.
     * @return {@link J2eeModule.Type} value corresponding to provided
     *         {@link ModuleType} value.
     */
    @SuppressWarnings("deprecation")
    public static JavaEEModule toValue(final J2eeModule.Type type) {
        return j2eeModuleTypeToValue.get(type);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE module type. */
    private final ModuleType moduleType;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE module type.
     * <p/>
     * @param moduleType Java EE module type.
     */
    private JavaEEModule(final ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get stored {@link ModuleType} value.
     * <p/>
     * @return Stored {@link ModuleType} value.
     */
    private ModuleType getModuleType() {
        return moduleType;
    }

    /**
     * Get Java EE module configuration directory (e.g. {@code "META-INF"}).
     * <p/>
     * @return Java EE module configuration directory.
     */
    public String getConfigDir() {
        switch (this) {
            case CAR:
            case EAR:
            case EJB:
            case RAR:
                return META_INF;
            case WAR:
                return WEB_INF;
            default:
                throw new IllegalArgumentException("Unknown Java EE module type.");
        }
    }

}
