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

package org.netbeans.modules.payara.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.admin.CommandListResources;
import org.netbeans.modules.payara.tooling.admin.ResultList;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;

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
            = PayaraLogger.get(ResourceDesc.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Fetch list of resource descriptions of given resource type from given
     * Payara instance.
     * <p/>
     * @param instance Payara instance from which to retrieve
     *                 resource descriptions.
     * @param type     Resource type to search for (<code>jdbc-resource</code>,
     *                 <code>jdbc-connection-pool</code>, ...).
     * @return List of resource descriptions retrieved from Payara server.
     */
    public static List<ResourceDesc> getResources(
            PayaraInstance instance, String type) {
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
