/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.spellchecker;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 *
 * @author lahvac
 */
public class Utilities {

    public static Locale name2Locale(String name) {
        StringTokenizer tok = new StringTokenizer(name, "_");

        String language = "";
        String country  = "";
        String variant  = "";

        if (tok.hasMoreTokens()) {
            language = tok.nextToken();

            if (tok.hasMoreTokens()) {
                country = tok.nextToken();

                if (tok.hasMoreTokens())
                    variant = tok.nextToken();
            }
        }

        return new Locale(language, country, variant);
    }
}
