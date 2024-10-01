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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cloud.oracle.assets.SuggestedItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 * Creates OCI policies that will allow access to Cloud Assets.
 * 
 * @author Jan Horvath
 */
public class PolicyGenerator {
        
    public static List<String> getPolicyStatementsFor(Collection<OCIItem> items) {   
        OCIItem execution = getExecutionEnvironment(items);
        if (execution == null) {
            return List.of();
        }
        
        String principalType = getPrincipalType(execution);
        List<String> result = new ArrayList();
        for (OCIItem item : items) {
            switch (item.getKey().getPath()) {
                case "Databases": //NOI18N
                    result.add("Allow any-user to manage autonomous-database-family" //NOI18N
                            + " in compartment id " + item.getCompartmentId() //NOI18N
                            + " where ALL {" //NOI18N
                            + " request.principal.type = '" + principalType + "'," //NOI18N
                            + " request.principal.compartment.id = '" + execution.getCompartmentId() + "' }"); //NOI18N
                    break;
                case "Bucket": //NOI18N
                    result.add("Allow any-user to manage object-family" //NOI18N
                            + " in compartment id " + item.getCompartmentId() //NOI18N
                            + " where ALL {" //NOI18N
                            + " request.principal.type = '" + principalType + "'," //NOI18N
                            + " request.principal.compartment.id = '" + execution.getCompartmentId() + "' }"); //NOI18N
                    break;
                case "Vault": //NOI18N
                    result.add("Allow any-user to read secret-family" //NOI18N
                            + " in compartment id " + item.getCompartmentId() //NOI18N
                            + " where ALL {" //NOI18N
                            + " request.principal.type = '" + principalType + "'," //NOI18N
                            + " request.principal.compartment.id = '" + execution.getCompartmentId() + "' }"); //NOI18N
                    break;
            }
        }
        return result;
    }
    
    private static String getPrincipalType(OCIItem executionEnvironment) {
        if ("ComputeInstance".equals(executionEnvironment.getKey().getPath())) { //NOI18N
            return "instance"; //NOI18N
        }
        return "cluster"; //NOI18N
    }
    
    private static OCIItem getExecutionEnvironment(Collection<OCIItem> items) {
        OCIItem execution = null;
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
        return execution;
    }

    public static String createPolicies(Collection<OCIItem> items) {
        List<String> statements = getPolicyStatementsFor(items);
        if (statements.isEmpty()) {
            return "# Resolve execution environment suggestion"; //NOI18N
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prettyPrintStatements(statements));
        sb.append(resolveSuggestions(items));
        return sb.toString();
    }

    private static String prettyPrintStatements(List<String> statements) {
        String[] newLineBefore = {"in ", "where ", "}"}; // NOI18N
        String[] newLineAfter = {"\\{", ",", "}"}; // NOI18N

        String result = String.join("\n", statements); // NOI18N
        for (String keyword : newLineBefore) {
            result = result.replaceAll(keyword, "\n" + keyword); // NOI18N
        }
        for (String keyword : newLineAfter) {
            result = result.replaceAll(keyword, keyword + "\n"); // NOI18N
        }
        
        return result;
    }

    private static String resolveSuggestions(Collection<OCIItem> items) {
        StringBuilder sb = new StringBuilder();
        items.stream()
                .filter(item -> "Suggested".equals(item.getKey().getPath())) // NOI18N
                .forEach(item -> sb
                        .append("# Resolve suggestion:") // NOI18N
                        .append(((SuggestedItem) item).getPath())
                        .append("\n")); // NOI18N
        return sb.toString();
    }
}
