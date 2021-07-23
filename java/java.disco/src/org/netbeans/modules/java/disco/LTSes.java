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
package org.netbeans.modules.java.disco;

import io.foojay.api.discoclient.pkg.TermOfSupport;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LTSes {

    @NonNull
    public static String text(@NonNull Integer value, @NonNull TermOfSupport support) {
        final String ui;
        switch (support) {
            case LTS:
                ui = "LTS";
                break;
            case MTS:
                ui = "MTS";
                break;
            case STS:
                ui = "STS";
                break;
            case NONE:
            case NOT_FOUND:
            default:
                ui = "";
                break;
        }
        return String.format("%s (%s)", value, ui);
    }

    static int latest(Map<Integer, TermOfSupport> lts) {
        return lts.entrySet().stream()
                .filter(e -> e.getValue()==TermOfSupport.LTS)
                .mapToInt(e -> e.getKey())
                .max().getAsInt();
    }

}
