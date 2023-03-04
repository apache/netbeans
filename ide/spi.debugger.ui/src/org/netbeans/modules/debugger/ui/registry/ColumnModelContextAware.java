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

package org.netbeans.modules.debugger.ui.registry;

import java.beans.PropertyEditor;
import java.util.Map;

import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ColumnModel;

/**
 *
 * @author Martin Entlicher
 */
public class ColumnModelContextAware extends ColumnModel implements ContextAwareService<ColumnModel> {

    private String serviceName;
    private ContextProvider context;
    private ColumnModel delegate;

    private ColumnModelContextAware(String serviceName) {
        this.serviceName = serviceName;
    }

    private ColumnModelContextAware(String serviceName, ContextProvider context) {
        this.serviceName = serviceName;
        this.context = context;
    }

    private synchronized ColumnModel getDelegate() {
        if (delegate == null) {
            delegate = (ColumnModel) ContextAwareSupport.createInstance(serviceName, context);
        }
        return delegate;
    }

    public ColumnModel forContext(ContextProvider context) {
        if (context == this.context) {
            return this;
        } else {
            return new ColumnModelContextAware(serviceName, context);
        }
    }

    @Override
    public String getID() {
        return getDelegate().getID();
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public Class getType() {
        return getDelegate().getType();
    }

    @Override
    public int getColumnWidth() {
        return getDelegate().getColumnWidth();
    }

    @Override
    public int getCurrentOrderNumber() {
        return getDelegate().getCurrentOrderNumber();
    }

    @Override
    public Character getDisplayedMnemonic() {
        return getDelegate().getDisplayedMnemonic();
    }

    @Override
    public String getNextColumnID() {
        return getDelegate().getNextColumnID();
    }

    @Override
    public String getPreviuosColumnID() {
        return getDelegate().getPreviuosColumnID();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return getDelegate().getPropertyEditor();
    }

    @Override
    public String getShortDescription() {
        return getDelegate().getShortDescription();
    }

    @Override
    public boolean isSortable() {
        return getDelegate().isSortable();
    }

    @Override
    public boolean isSorted() {
        return getDelegate().isSorted();
    }

    @Override
    public boolean isSortedDescending() {
        return getDelegate().isSortedDescending();
    }

    @Override
    public boolean isVisible() {
        return getDelegate().isVisible();
    }

    @Override
    public void setColumnWidth(int newColumnWidth) {
        getDelegate().setColumnWidth(newColumnWidth);
    }

    @Override
    public void setCurrentOrderNumber(int newOrderNumber) {
        getDelegate().setCurrentOrderNumber(newOrderNumber);
    }

    @Override
    public void setSorted(boolean sorted) {
        getDelegate().setSorted(sorted);
    }

    @Override
    public void setSortedDescending(boolean sortedDescending) {
        getDelegate().setSortedDescending(sortedDescending);
    }

    @Override
    public void setVisible(boolean visible) {
        getDelegate().setVisible(visible);
    }

    @Override
    public String toString() {
        return super.toString() + " with service = "+serviceName+" and delegate = "+delegate;
    }

    
    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
        return new ColumnModelContextAware(serviceName);
    }

}
