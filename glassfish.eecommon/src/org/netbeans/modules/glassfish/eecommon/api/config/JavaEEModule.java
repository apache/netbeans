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
package org.netbeans.modules.glassfish.eecommon.api.config;

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

    /** GlassFish Java EE common module Logger. */
    private static final Logger LOGGER = Logger.getLogger("glassfish-eecommon");

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
