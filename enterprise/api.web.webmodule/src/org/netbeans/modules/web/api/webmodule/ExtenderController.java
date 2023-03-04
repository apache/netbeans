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
package org.netbeans.modules.web.api.webmodule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Parameters;

/**
 * This class allows a {@link org.netbeans.modules.web.spi.webmodule.WebModuleExtender}
 * to communicate with its environment.
 *
 * @author Andrei Badea
 *
 * @since 1.9
 */
public class ExtenderController {

    private final Properties properties = new Properties();
    private String errorMessage;

    /**
     * Creates a new controller.
     *
     * @return a new controller.
     */
    public static ExtenderController create() {
        return new ExtenderController();
    }

    private ExtenderController() {
    }

    /**
     * Gets the error message for this controller.
     *
     * @return the error message or null if there is no error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets an error message for this controller, which creator of the controller
     * may use to e.g. display it in the UI.
     *
     * @param  errorMessage the error message; can be null if there is no error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the properties of this controller. This allows the creator
     * of the controller to communicate with the extenders. Usually
     * the creator of the controller will set some properties and then call
     * {@link org.netbeans.modules.web.spi.webmodule.WebModuleExtender#update}
     * to let the extender know that the properties have changed.
     *
     * @return an instance of {@link Properties}; never null.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Encapsulates the properties of an {@link ExtenderController}, which are
     * arbitrary <code>Object</code> values with a <code>String</code> name.
     */
    public static class Properties {

        private final Map<String, Object> properties = new HashMap<String, Object>();

        /**
         * Return the value of a given property.
         *
         * @param  name the property name; never null.
         * @return the property value; can be null.
         * @throws NullPointerException if the <code>name</code> parameter is null.
         */
        public Object getProperty(String name) {
            Parameters.notNull("name", name); // NOI18N
            return properties.get(name);
        }

        /**
         * Sets the value of a property.
         *
         * @param  name the property name; never null.
         * @param  value the property value; can be null.
         * @throws NullPointerException if the <code>name</code> parameter is null.
         */
        public void setProperty(String name, Object value) {
            Parameters.notNull("name", name); // NOI18N
            if (value != null) {
                properties.put(name, value);
            } else {
                properties.remove(name);
            }
        }

        /**
         * Returns a {@link java.util.Map} containing all properties.
         *
         * @return a map containinig all properties; never null.
         */
        public Map<String, Object> getProperties() {
            return Collections.unmodifiableMap(new HashMap<String, Object>(properties));
        }
    }
}
