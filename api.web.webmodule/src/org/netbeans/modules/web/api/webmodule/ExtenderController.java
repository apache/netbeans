/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
