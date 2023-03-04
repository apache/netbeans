/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.netbeans.modules.javaee.wildfly.ide;

import java.util.regex.Pattern;

/**
 * A parser for log lines from a Wildfly server.<br>
 * The purpose of this class is to detect whether a Wildfly server is starting or has completely started.
 */
public class WildflyStartLineParser {

    private static final Pattern JBOSS_7_STARTED_ML = Pattern.compile(".*JBoss AS 7(\\..*)* \\d+ms .*");

    private static final Pattern WILDFLY_8_STARTED_ML = Pattern.compile(".*JBAS015874: WildFly 8(\\..*)* .* started in \\d+ms .*");

    private static final Pattern WILDFLY_8_STARTING_ML = Pattern.compile(".*JBAS015899: WildFly 8(\\..*)* .* starting");

    private static final Pattern WILDFLY_9_STARTED_ML = Pattern.compile(".*WFLYSRV0050: WildFly Full \\d+(\\..*)* .* started in \\d+ms .*");

    private static final Pattern WILDFLY_STARTING_ML = Pattern.compile(".*WFLYSRV0049: WildFly .* \\d+(\\..*)* .* starting");

    private static final Pattern WILDFLY_10_STARTED_ML = Pattern.compile(".*WFLYSRV0025: WildFly .* \\d+(\\..*)* .* started in \\d+ms .*");

    private static final Pattern EAP6_STARTED_ML = Pattern.compile(".*JBAS015874: JBoss EAP 6\\.[0-9]{0,2}?.[0-9]{0,2}?\\.GA .* \\d+ms .*");

    private static final Pattern EAP6_STARTING_ML = Pattern.compile(".*JBAS015899: JBoss EAP 6\\.[0-9]{0,2}?.[0-9]{0,2}?\\.GA .*");

    private static final Pattern EAP7_STARTED_ML = Pattern.compile(".*WFLYSRV0025: JBoss EAP 7\\.[0-9]{0,2}?.[0-9]{0,2}?\\.GA .* \\d+\\s?ms .*");

    private static final Pattern EAP7_STARTING_ML = Pattern.compile(".*WFLYSRV0049: JBoss EAP 7\\.[0-9]{0,2}?.[0-9]{0,2}?\\.GA .*");

    /**
     * Check whether the given line indicates the wildfly server is starting.
     * @param line The line to check.
     * @return {@code true} if this line indicates a starting Wildfly server, {@code false} else.
     */
    public static boolean isStarting(String line) {
        return line.contains("Starting JBoss (MX MicroKernel)") // JBoss 4.x message // NOI18N
                || line.contains("Starting JBoss (Microcontainer)") // JBoss 5.0 message // NOI18N
                || line.contains("Starting JBossAS") // JBoss 6.0 message // NOI18N
                || WILDFLY_8_STARTING_ML.matcher(line).matches()
                || WILDFLY_STARTING_ML.matcher(line).matches()
                || EAP6_STARTING_ML.matcher(line).matches()
                || EAP7_STARTING_ML.matcher(line).matches();
    }

    /**
     * Check whether the given line indicates the wildfly server has completely started.
     * @param line The line to check.
     * @return {@code true} if this line indicates a started Wildfly server, {@code false} else.
     */
    public static boolean isStarted(String line) {
        return ((line.contains("JBoss (MX MicroKernel)") // JBoss 4.x message // NOI18N
                || line.contains("JBoss (Microcontainer)") // JBoss 5.0 message // NOI18N
                || line.contains("JBossAS") // JBoss 6.0 message // NOI18N
                || line.contains("JBoss AS"))// JBoss 7.0 message // NOI18N
                && (line.contains("Started in")) // NOI18N
                || line.contains("started in") // NOI18N
                || line.contains("started (with errors) in")) // JBoss 7 with some errors (include wrong deployments) // NOI18N
                || JBOSS_7_STARTED_ML.matcher(line).matches()
                || WILDFLY_8_STARTED_ML.matcher(line).matches()
                || WILDFLY_9_STARTED_ML.matcher(line).matches()
                || WILDFLY_10_STARTED_ML.matcher(line).matches()
                || EAP6_STARTED_ML.matcher(line).matches()
                || EAP7_STARTED_ML.matcher(line).matches();
    }
}
