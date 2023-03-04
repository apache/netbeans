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

package org.netbeans.modules.options;

import org.netbeans.spi.options.OptionsPanelController;

/**
 * Accessor for OptionsPanelController.setCurrentSubcategory
 * (see http://openide.netbeans.org/tutorial/api-design.html#design.less.friend).
 * @author Jiri Skrivanek
 */
public abstract class OptionsPanelControllerAccessor {
    public static OptionsPanelControllerAccessor DEFAULT;

    public static OptionsPanelControllerAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }
        // invokes static initializer of OptionsPanelController.class
        // that will assign value to the DEFAULT field above
        Class c = OptionsPanelController.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert DEFAULT != null : "The DEFAULT field must be initialized";
        return DEFAULT;
    }

    /** Accessor to setCurrentSubcategory */
    public abstract void setCurrentSubcategory(OptionsPanelController controller, String subpath);
}
