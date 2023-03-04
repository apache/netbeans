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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.form.RADProperty;

/**
 * This class is used internally to provide default support for any layout
 * manager class. The layout manager is handled as a JavaBean, no component
 * constraints are supported, as well as no drag&drop and no arranging
 * features.
 *
 * @author Tomas Pavek
 */

class DefaultLayoutSupport extends AbstractLayoutSupport {

    private Class layoutClass;

    public DefaultLayoutSupport(Class layoutClass) {
        this.layoutClass = layoutClass;
    }

    @Override
    public Class getSupportedClass() {
        return layoutClass;
    }

    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        // for better robustness catch exceptions that might occur because
        // the default support does not deal with constraints
        try {
            super.addComponentsToContainer(container,
                                           containerDelegate,
                                           components,
                                           index);
        }
        catch (RuntimeException ex) { // just ignore
            ex.printStackTrace();
        }
    }

    /** Cloning method - creates a new instance of this layout support, just
     * not initialized yet.
     * @return new instance of this layout support
     */
    @Override
    protected AbstractLayoutSupport createLayoutSupportInstance() {
        return new DefaultLayoutSupport(layoutClass);
    }
}
