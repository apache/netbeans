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
package org.netbeans.modules.xml.catalog.lib;

/**
 * Classifying utilities.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class Categorizer {


    /** Returns true if the string is a valid URL. */
    public static boolean isURL(String str) {
        try {
            new java.net.URL(str);
            return true;
        }
        catch (java.net.MalformedURLException e) {
            // assume the worst
        }
        return false;
    }
}
