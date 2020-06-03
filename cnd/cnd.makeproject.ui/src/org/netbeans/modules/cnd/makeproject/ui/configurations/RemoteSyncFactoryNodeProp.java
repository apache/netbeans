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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
public class RemoteSyncFactoryNodeProp extends Node.Property<String> implements PropertyChangeListener {

    private RemoteSyncFactory factory;

    public RemoteSyncFactoryNodeProp(MakeConfiguration makeConfiguration) {
        super(String.class);
        factory = makeConfiguration.getRemoteSyncFactory();
        makeConfiguration.getDevelopmentHost().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof  DevelopmentHostConfiguration) {
            ExecutionEnvironment execEnv = ((DevelopmentHostConfiguration) evt.getNewValue()).getExecutionEnvironment();
            RemoteSyncFactory newFactory = ServerList.get(execEnv).getSyncFactory();
            if (!Objects.equals(newFactory, factory)) {
                factory = newFactory;
                PropertyEditor ed = getPropertyEditor();
                if (ed instanceof PropertyEditorSupport) {
                    ((PropertyEditorSupport) ed).firePropertyChange();
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "RemoteSyncFactoryTxt");
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "RemoteSyncFactoryHint");
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return (factory == null) ? NbBundle.getMessage(getClass(), "RemoteSyncFactoryNoFactory") : factory.getDisplayName();
    }

    @Override
    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canAutoComplete")) { //NOI18N
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }

}
