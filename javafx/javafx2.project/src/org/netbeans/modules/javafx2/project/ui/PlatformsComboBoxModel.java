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
package org.netbeans.modules.javafx2.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;

/**
 * JavaFX enabled Java Platform ComboBoxModel decorator
 * 
 * @author Anton Chechel
 */
public class PlatformsComboBoxModel implements ComboBoxModel {
    private ComboBoxModel delegate;

    public PlatformsComboBoxModel(ComboBoxModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        delegate.setSelectedItem(anItem);
    }

    @Override
    public Object getSelectedItem() {
        return delegate.getSelectedItem();
    }

    @Override
    public int getSize() {
        return getData().size();
    }

    @Override
    public Object getElementAt(int index) {
        return getData().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        delegate.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        delegate.removeListDataListener(l);
    }
    
    private List<Object> getData() {
        List<Object> data = new ArrayList<Object>();
        int origSize = delegate.getSize();
        for (int i = 0; i < origSize; i++) {
            final Object element = delegate.getElementAt(i);
            final JavaPlatform platform = PlatformUiSupport.getPlatform(element);
            if (JavaFXPlatformUtils.isJavaFXEnabled(platform)) {
                data.add(element);
            }
        }
        return data;
    }
    
}
