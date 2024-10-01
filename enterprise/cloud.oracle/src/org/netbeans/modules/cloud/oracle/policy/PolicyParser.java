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

import com.oracle.bmc.identity.model.Policy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Dusan Petrovic
 */
public class PolicyParser {
    
    private static final List<String> OVER_PERMISSIVE_KEYWORDS = List.of(
            "all-resources", "in tenancy", "manage policies", "manage groups",
            "manage identity", "manage virtual-network-family",
            "manage database-family"
    );
    
    public static List<String> getAllStatementsFrom(List<Policy> policies) {
        return policies
                    .stream()
                    .flatMap(i -> i.getStatements().stream())
                    .collect(Collectors.toList());
    }

    public static List<String> filterOverpermissiveStatements(List<String> statements) {
        List<String> overpermissive = new ArrayList();
        for (String statement: statements) {
            for (String keyword: OVER_PERMISSIVE_KEYWORDS) {
                if (prepareForComparing(statement).contains(prepareForComparing(keyword))) {
                    overpermissive.add(statement);
                }
            }
        }
        
        return overpermissive;
    }
    
    public static List<String> removeWhitespacesFromStatements(List<String> statements) {
        return statements
                    .stream()
                    .map(PolicyParser::prepareForComparing)
                    .collect(Collectors.toList());
    }
    
    public static String prepareForComparing(String statementPart) {
        return statementPart.replaceAll("\\s+", "").toLowerCase();
    }
}
