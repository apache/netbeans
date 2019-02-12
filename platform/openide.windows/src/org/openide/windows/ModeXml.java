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
package org.openide.windows;

/**
 * Utility class to help clients get the XML configuration of a Mode.
 * 
 * @see http://wiki.apidesign.org/wiki/ExtendingInterfaces
 * 
 * @author Mark Phipps
 */
public final class ModeXml {

    private ModeXml() {
    }

    /**
     * Expose the Mode's configuration as XML.
     * 
     * @param mode the {@link Mode} whose XML configuration is required.
     * @return the XML of the Mode's configuration or {@code null} if not supported.
     */
    public final static String toXml(Mode mode) {
        return mode instanceof Mode.Xml
                ? ((Mode.Xml) mode).toXml()
                : null;
    }
}
