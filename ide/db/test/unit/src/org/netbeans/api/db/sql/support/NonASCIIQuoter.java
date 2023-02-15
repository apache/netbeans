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

package org.netbeans.api.db.sql.support;

import java.util.regex.Pattern;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;

/**
 *
 * @author Andrei Badea
 */
public class NonASCIIQuoter extends Quoter {

    private static final Pattern ASCII_IDENTIFIER = Pattern.compile("[a-zA-z]\\w+");

    public NonASCIIQuoter(String quoteString) {
        super(quoteString);
    }

    @Override
    public String quoteIfNeeded(String identifier) {
        if (!alreadyQuoted(identifier) && !ASCII_IDENTIFIER.matcher(identifier).matches()) {
            return quoteString + identifier + quoteString;
        } else {
            return identifier;
        }
    }

    @Override
    public String quoteAlways(String identifier) {
        if (!alreadyQuoted(identifier)) {
            return quoteString + identifier + quoteString;
        } else {
            return identifier;
        }
    }
}
