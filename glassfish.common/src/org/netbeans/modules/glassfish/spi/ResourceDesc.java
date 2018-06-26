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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.admin.CommandListResources;
import org.netbeans.modules.glassfish.tooling.admin.ResultList;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;

/**
 * Resource description.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public class ResourceDesc implements Comparable<ResourceDesc> {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(ResourceDesc.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Fetch list of resource descriptions of given resource type from given
     * GlassFish instance.
     * <p/>
     * @param instance GlassFish instance from which to retrieve
     *                 resource descriptions.
     * @param type     Resource type to search for (<code>jdbc-resource</code>,
     *                 <code>jdbc-connection-pool</code>, ...).
     * @return List of resource descriptions retrieved from GlassFish server.
     */
    public static List<ResourceDesc> getResources(
            GlassfishInstance instance, String type) {
        List<ResourceDesc> resourcesList;
        List<String> values;
        ResultList<String> result
                = CommandListResources.listResources(instance, type, null);
        if (result != null && result.getState() == TaskState.COMPLETED) {
            values = result.getValue();
        } else {
            values = null;
        }
        if (values != null && values.size() > 0) {
            resourcesList = new ArrayList<ResourceDesc>(values.size());
            for (String value : values) {
                resourcesList.add(new ResourceDesc(value, type));
            }
        } else {
            resourcesList = Collections.emptyList();
        }
        return resourcesList;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Resource name. */
    private final String name;

    /** Command type (<code>jdbc-resource</code>,
     *  <code>jdbc-connection-pool</code>, ...). */
    private final String cmdType;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of resource description.
     * <p/>
     * @param name    Resource name.
     * @param cmdType Command type.
     */
    public ResourceDesc(final String name, final String cmdType) {
        this.name = name;
        this.cmdType = cmdType;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get resource name from resource description.
     * <p/>
     * @return Resource name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get command type from resource description.
     * <p/>
     * @return Command type.
     */
    public String getCommandType() {
        return cmdType;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compare this resource description with another one.
     * <p/>
     * @param resourceDesc Resource description to be compared with this object.
     * @return The value <code>0</code> if resource name and command type
     *         <code>String</code> values of this and provided objects
     *         are equal. 
     *         The value <code>&gt;0</code> if this resource name (or command
     *         type when resource names are equal) <code>String</code> value
     *         is lexicographically less than in provided object.
     *         The value <code>&lt;0</code> if this resource name (or command
     *         type when resource names are equal) <code>String</code> value
     *         is lexicographically greater than in provided object.
     */
    @Override
    public int compareTo(ResourceDesc resourceDesc) {
        int result = name.compareTo(resourceDesc.name);
        if(result == 0) {
            result = cmdType.compareTo(resourceDesc.cmdType);
        }
        return result;
    }
    
}
