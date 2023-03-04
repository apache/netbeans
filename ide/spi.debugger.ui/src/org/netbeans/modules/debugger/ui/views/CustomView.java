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
package org.netbeans.modules.debugger.ui.views;

import java.io.Serializable;
import org.netbeans.spi.debugger.ui.ViewLifecycle.ModelUpdateListener;

/**
 * Additional view for custom model set.
 * 
 * @author Martin Entlicher
 */
public class CustomView extends View {
    
    private transient String icon;
    private transient String displayName;
    private transient String toolTip;
    
    public CustomView(String icon, String name, String helpID, String propertiesHelpID,
                      String displayName, String toolTip) {
        super(icon, name, helpID, propertiesHelpID, null, null);
        this.icon = icon;
        this.displayName = displayName;
        this.toolTip = toolTip;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getToolTipText() {
        return toolTip;
    }
    
    public static ViewModelListener createViewModelService(String name,
                                                           String propertiesHelpID,
                                                           ModelUpdateListener mul) {
        return new ViewModelListener(name, propertiesHelpID, mul);
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper(icon, name, helpID, propertiesHelpID, displayName, toolTip);
    }
     
    /**
     * The serializing class.
     */
    private static final class ResolvableHelper implements Serializable {
        
        private String[] data;
        
        private static final long serialVersionUID = 1L;
        
        ResolvableHelper(String... data) {
            this.data = data;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        public Object readResolve() {
            return new CustomView(data[0], data[1], data[2], data[3], data[4], data[5]);
        }
    }
    
}
