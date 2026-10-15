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
package org.netbeans.modules.php.dbgp.packets;

import java.util.Locale;

/**
 * @author ads
 *
 */
public class FeatureGetCommand extends DbgpCommand {

    public enum Feature {
        LANGUAGE_SUPPORTS_THREADS,
        LANGUAGE_NAME,
        LANGUAGE_VERSION,
        ENCODING,
        PROTOCOL_VERSION,
        SUPPORTS_ASYNC,
        DATA_ENCODING,
        BREAKPOINT_LANGUAGES,
        BREAKPOINT_TYPES,
        BREAKPOINT_DETAILS,
        MULTIPLE_SESSIONS,
        MAX_CHILDREN,
        MAX_DATA,
        MAX_DEPTH,
        SUPPORTS_POSTMORTEM,
        SHOW_HIDDEN,
        NOTIFY_OK,
        RESOLVED_BREAKPOINTS,
        /*
         * additional commands that could be supported
         */
        BREAK, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        EVAL, // at the time of writing ( protocol version 2.0.0 ) this command is supported
        EXPR, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        EXEC; // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

        public Feature forString(String str) {
            Feature[] features = Feature.values();
            for (Feature feature : features) {
                if (str.equals(feature.toString())) {
                    return feature;
                }
            }
            return null;
        }

    }
    static final String FEATURE_GET = "feature_get"; // NOI18N
    private static final String NAME_ARG = "-n "; // NOI18N
    private String myName;

    public FeatureGetCommand(String transactionId) {
        this(FEATURE_GET, transactionId);
    }

    protected FeatureGetCommand(String command, String transactionId) {
        super(command, transactionId);
    }

    public void setFeature(Feature feature) {
        myName = feature.toString();
    }

    public void setFeature(String name) {
        myName = name;
    }

    public String getFeature() {
        return myName;
    }

    @Override
    protected String getArguments() {
        return NAME_ARG + myName;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

}
