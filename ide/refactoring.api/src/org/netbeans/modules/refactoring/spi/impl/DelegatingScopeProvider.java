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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

public class DelegatingScopeProvider extends ScopeProvider implements DelegatingScopeInformation {

    private final Map<?, ?> map;
    private final String id;
    private final String displayName;
    private final int position;
    private Icon icon;
    private ScopeProvider delegate;

    public static DelegatingScopeProvider create(Map<?, ?> map) {
        return new DelegatingScopeProvider(map);
    }

    public DelegatingScopeProvider(ScopeProvider delegate, String id, String displayName, int position, Icon image) {
        this.icon = image;
        this.id = id;
        this.displayName = displayName;
        this.position = position;
        this.delegate = delegate;
        map = null;
    }

    private DelegatingScopeProvider(Map<?, ?> map) {
        this.map = map;
        String path = (String) map.get("iconBase"); //NOI18N
        icon = path != null && !path.equals("") ? ImageUtilities.loadImageIcon(path, false) : null;
        id = (String) map.get("id"); //NOI18N
        displayName = (String) map.get("displayName"); //NOI18N
        position = (Integer) map.get("position"); //NOI18N
    }

    public ScopeProvider getDelegate() {
        if (delegate == null) {
            assert map != null;
            delegate = (ScopeProvider) map.get("delegate"); // NOI18N
        }
        return delegate;
    }
    
    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        ScopeProvider d = getDelegate();
        return d != null ? d.initialize(context, cancel) : null;
    }

    @Override
    public Scope getScope() {
        ScopeProvider d = getDelegate();
        return d != null ? d.getScope() : null;
    }

    @Override
    public Problem getProblem() {
        ScopeProvider d = getDelegate();
        return d != null ? d.getProblem(): null;
    }
    
    @Override
    public Icon getIcon() {
        Icon delegateIcon = null;
        ScopeProvider d = getDelegate();
        if(d != null) {
            delegateIcon = d.getIcon();
        }
        return delegateIcon == null? icon : delegateIcon;
    }

    @Override
    public String getDisplayName() {
        String detail = null;
        ScopeProvider d = getDelegate();
        if(d != null) {
            detail = d.getDetail();
        }
        return detail == null? displayName : displayName + " (" + detail + ")";
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getId() {
        return id;
    }
}
