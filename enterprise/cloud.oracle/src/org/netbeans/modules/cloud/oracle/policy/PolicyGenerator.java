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
package org.netbeans.modules.cloud.oracle.policy;

import java.util.List;
import org.netbeans.modules.cloud.oracle.assets.SuggestedItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 *
 * @author Jan Horvath
 */
public class PolicyGenerator {

    public static String createPolicies(List<OCIItem> items) {
        OCIItem execution = null;
        String principalType;
        for (OCIItem item : items) {
            if ("Cluster".equals(item.getKey().getPath()) //NOI18N
                    || "ComputeInstance".equals(item.getKey().getPath())) { //NOI18N
                if (execution != null) {
                    throw new IllegalStateException("More than one execution environment found"); //NOI18N
                } else {
                    execution = item;
                }
            }
        }
        if (execution == null) {
            return "# Resolve execution environment suggestion"; //NOI18N
        }
        if ("ComputeInstance".equals(execution.getKey().getPath())) { //NOI18N
            principalType = "instance"; //NOI18N
        } else {
            principalType = "cluster"; //NOI18N
        }
        StringBuilder result = new StringBuilder();
        for (OCIItem item : items) {
            switch (item.getKey().getPath()) {
                case "Databases": //NOI18N
                    result.append("Allow any-user to manage autonomous-database-family \n" //NOI18N
                            + "in compartment id " + item.getCompartmentId() //NOI18N
                            + "\n" //NOI18N
                            + "where ALL { \n" //NOI18N
                            + "    target.autonomous-database.id = '" + item.getKey().getValue() + "',\n" //NOI18N
                            + "    request.principal.type = '" + principalType + "',\n" //NOI18N
                            + "    request.principal.compartment.id = '" + execution.getCompartmentId() + "'\n" //NOI18N
                            + "}\n\n"); //NOI18N
                    break;
                case "Bucket": //NOI18N
                    result.append("Allow any-user to manage object-family \n" //NOI18N
                            + "in compartment id " + item.getCompartmentId() //NOI18N
                            + "\n" //NOI18N
                            + "where ALL {\n" //NOI18N
                            + "    request.principal.type = '" + principalType + "',\n" //NOI18N
                            + "    request.principal.compartment.id = '" + execution.getCompartmentId() + "'\n" //NOI18N
                            + "}\n\n"); //NOI18N
                    break;
                case "Vault": //NOI18N
                    result.append("Allow any-user to read secret-family \n" //NOI18N
                            + "in compartment id " + item.getCompartmentId() //NOI18N
                            + "\n" //NOI18N
                            + "where ALL {\n" //NOI18N
                            + "    request.principal.type = '" + principalType + "',\n" //NOI18N
                            + "    request.principal.compartment.id = '" + execution.getCompartmentId() + "'\n" //NOI18N
                            + "}"); //NOI18N
                    break;
                case "Suggested": //NOI18N
                    result.append("# Resolve suggestion:" + ((SuggestedItem) item).getPath() + "'\n"); //NOI18N
                    break;
            }

        }
        return result.toString();
    }

}
