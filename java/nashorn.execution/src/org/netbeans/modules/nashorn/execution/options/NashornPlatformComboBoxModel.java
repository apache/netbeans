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

package org.netbeans.modules.nashorn.execution.options;

import java.util.Collections;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.nashorn.execution.NashornPlatform;

/**
 * ComboBoxModel delegating to PlatformUiSupport.createPlatformComboBoxModel(),
 * but assuring, that it will not show the default platform in case it is not
 * a Nashorn platform.
 * 
 * @author Martin Entlicher
 */
class NashornPlatformComboBoxModel implements ComboBoxModel {
    
    private final ComboBoxModel delegate;
    
    public NashornPlatformComboBoxModel() {
        delegate = PlatformUiSupport.createPlatformComboBoxModel(
                null,
                Collections.singleton(NashornPlatform.getFilter()));
    }

    @Override
    public void setSelectedItem(Object anItem) {
        delegate.setSelectedItem(anItem);
    }

    @Override
    public Object getSelectedItem() {
        Object selObj = delegate.getSelectedItem();
        if (selObj == null) {
            return null;
        } else {
            if (NashornPlatform.isJsJvmPlatform(PlatformUiSupport.getPlatform(selObj))) {
                return selObj;
            } else {
                return null;
            }
        }
        
    }

    @Override
    public int getSize() {
        int size = delegate.getSize();
        if (size == 1) {
            // It's possible that the default platform is there, but is not Nashorn
            Object elm = delegate.getElementAt(0);
            if (!NashornPlatform.isJsJvmPlatform(PlatformUiSupport.getPlatform(elm))) {
                size = 0;
            }
        }
        return size;
    }

    @Override
    public Object getElementAt(int index) {
        return delegate.getElementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        delegate.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        delegate.removeListDataListener(l);
    }
}
