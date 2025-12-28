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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Dusan Petrovic
 */
public class ApplicationPropertiesGenerator {

    private static final String COMMENT_START = "# "; //NOI18N
    private static final String NEW_LINE = "\n"; //NOI18N

    final PropertiesGenerator propertiesGenerator;

    public ApplicationPropertiesGenerator(PropertiesGenerator propertiesGenerator) {
        this.propertiesGenerator = propertiesGenerator;
    }

    public String getApplicationPropertiesString() {
        StringBuilder sb = new StringBuilder();
        appendApplicationProperties(sb);
        separatePropertiesSections(sb);
        appendBootstrapProperties(sb);
        return sb.toString();
    }

    private void separatePropertiesSections(StringBuilder sb) {
        if (!this.propertiesGenerator.getApplication().isEmpty()) {
            sb.append(NEW_LINE);
        }
    }

    private void appendApplicationProperties(StringBuilder sb) {
        appendComments(sb,
                "Generated application.properties", //NOI18N
                "Uncomment following line when running inside Oracle Cloud", //NOI18N
                "oci.config.instance-principal.enabled=true" //NOI18N
                );
        appendDateComment(sb);
        appendProperties(sb, new TreeMap<>(this.propertiesGenerator.getApplication()));
    }

    private void appendBootstrapProperties(StringBuilder sb) {
        appendComments(sb, "Generated bootstrap.properties"); //NOI18N
        appendProperties(sb, new TreeMap<>(this.propertiesGenerator.getBootstrap()));
    }

    private void appendComments(StringBuilder sb, String ...comments) {
        for (String comment : comments) {
            sb.append(COMMENT_START)
                    .append(comment)
                    .append(NEW_LINE);
        }
    }

    private void appendDateComment(StringBuilder sb) {
        sb.append(COMMENT_START)
                .append(new Date())
                .append(NEW_LINE);
    }

    private void appendProperties(StringBuilder sb, Map<String, String> sortedProperties) {
        String groupName = null;
        for (Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            if (entry.getKey() == null) continue;

            String currentGroupName = getCurrentPropertyGroupName(entry);
            if (!currentGroupName.equals(groupName)) {
                groupName = currentGroupName;
                sb.append(NEW_LINE);
            }

            sb.append(entry.getKey())
                    .append("=")  //NOI18N
                    .append(entry.getValue())
                    .append(NEW_LINE);
        }
    }

    private String getCurrentPropertyGroupName(Map.Entry<String, String> entry) {
        int dotIndex = entry.getKey().indexOf('.'); //NOI18N
        return dotIndex > -1 ? entry.getKey().substring(0, dotIndex) : entry.getKey();
    }
}
