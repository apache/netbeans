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
package org.netbeans.modules.maven.model.settings.impl;

import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsQName;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class MirrorImpl extends SettingsComponentImpl implements Mirror {

    private static final Class<? extends SettingsComponent>[] ORDER = new Class[] {
    };

    public MirrorImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public MirrorImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().MIRROR));
    }

    // attributes

    // child elements


    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getName() {
        return getChildElementText(getModel().getSettingsQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getSettingsQNames().NAME.getName(), name,
                getModel().getSettingsQNames().NAME.getQName());
    }



    @Override
    public String getUrl() {
        return getChildElementText(getModel().getSettingsQNames().URL.getQName());
    }

    @Override
    public void setUrl(String url) {
        setChildElementText(getModel().getSettingsQNames().URL.getName(), url,
                getModel().getSettingsQNames().URL.getQName());
    }

    @Override
    public String getMirrorOf() {
        return getChildElementText(getModel().getSettingsQNames().MIRROROF.getQName());
    }

    @Override
    public void setMirrorOf(String mirrorof) {
        setChildElementText(getModel().getSettingsQNames().MIRROROF.getName(), mirrorof,
                getModel().getSettingsQNames().MIRROROF.getQName());
    }

    @Override
    public String getId() {
        return getChildElementText(getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public void setId(String id) {
        setChildElementText(getModel().getSettingsQNames().ID.getName(), id,
                getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public String getLayout() {
        final SettingsQName MIRROR_LAYOUT = getModel().getSettingsQNames().MIRROR_LAYOUT_110;
        if (MIRROR_LAYOUT != null) {
            return getChildElementText(MIRROR_LAYOUT.getQName());
        }
        return null;
    }

    @Override
    public void setLayout(String layout) {
        final SettingsQName MIRROR_LAYOUT = getModel().getSettingsQNames().MIRROR_LAYOUT_110;
        if (MIRROR_LAYOUT != null) {
           setChildElementText(MIRROR_LAYOUT.getName(), layout, 
                MIRROR_LAYOUT.getQName()); 
        } else {
            //log somehow?
        }
    }

    @Override
    public String getMirrorOfLayouts() {
        final SettingsQName MIRROR_OF_LAYOUTS = getModel().getSettingsQNames().MIRROR_OF_LAYOUTS_110;
        if (MIRROR_OF_LAYOUTS != null) {
            return getChildElementText(MIRROR_OF_LAYOUTS.getQName());
        }
        return null;
    }

    @Override
    public void setMirrorOfLayouts(String mirrorOf) {
        final SettingsQName MIRROR_OF_LAYOUTS = getModel().getSettingsQNames().MIRROR_OF_LAYOUTS_110;
        if (MIRROR_OF_LAYOUTS != null) {
           setChildElementText(MIRROR_OF_LAYOUTS.getName(), mirrorOf, 
                MIRROR_OF_LAYOUTS.getQName()); 
        } else {
            //log somehow?
        }
    }


    public static class List extends ListImpl<Mirror> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().MIRROR, Mirror.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().MIRRORS));
        }
    }


}
