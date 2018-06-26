/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import org.netbeans.modules.glassfish.tooling.admin.CommandSetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultMap;
import org.netbeans.modules.glassfish.tooling.admin.CommandGetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.spi.GlassfishModule;

/**
 *
 * @author vkraemer
 */
public class EnableComet implements Runnable {
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(CommonServerSupport.class);

    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

     /** GlassFish server instance to be modified. */
    private final GlassfishInstance instance;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Comet support enable handler.
     * @param instance GlassFish server instance to be modified.
     */
    public EnableComet(GlassfishInstance instance) {
        this.instance = instance;
    }

    /**
     * Thread execution method.
     */
    @Override
    public void run() {
        String propertiesPattern = "*.comet-support-enabled";
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    instance, propertiesPattern,
                    CommonServerSupport.PROPERTIES_FETCH_TIMEOUT);
            if (result.getState() == TaskState.COMPLETED) {
                String newValue
                        = instance.getProperty(GlassfishModule.COMET_FLAG);
                if (null == newValue || newValue.trim().length() < 1) {
                    newValue = "false"; // NOI18N
                }
                for (Entry<String, String> entry
                        : result.getValue().entrySet()) {
                    String key = entry.getKey();
                    // do not update the admin listener....
                    if (null != key && !key.contains("admin-listener")) {
                        CommandSetProperty command
                                = GlassfishInstanceProvider.getProvider()
                                .getCommandFactory().getSetPropertyCommand(
                                key, newValue);
                        ResultString setResult = CommandSetProperty.setProperty(
                                instance, command,
                                CommonServerSupport.PROPERTIES_FETCH_TIMEOUT);  
                    }
                }
                
            }
        } catch (GlassFishIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not get comment-support-enabeld value.", gfie);
        }
    }

}
