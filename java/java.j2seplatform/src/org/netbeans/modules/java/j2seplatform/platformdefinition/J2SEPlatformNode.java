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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.text.MessageFormat;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

class J2SEPlatformNode extends AbstractNode {

    private J2SEPlatformImpl platform;
    private String toolTip;
    private boolean broken;

    public J2SEPlatformNode (J2SEPlatformImpl platform, DataObject definition) {
        super (Children.LEAF, Lookups.fixed(new Object[] {platform, definition}));
        this.platform = platform;
        super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    public String getDisplayName () {
        return this.platform.getDisplayName();
    }

    public String getHtmlDisplayName() {
        if (!platform.isValid()) {
            return "<font color=\"#A40000\">"+this.platform.getDisplayName()+"</font>";
        }
        else {
            return null;
        }
    }

    public String getName () {
        return this.getDisplayName();
    }

    public void setName (String name) {
        this.platform.setDisplayName (name);
    }

    public void setDisplayName(String name) {
        this.setName (name);
    }
    
    public synchronized String getShortDescription() {
        if (this.toolTip == null) {
            this.toolTip = MessageFormat.format (
            NbBundle.getMessage(J2SEPlatformNode.class,"TXT_J2SEPlatformToolTip"),
            new Object[] {
                this.platform.getSpecification().getVersion()
            });
        }
        return this.toolTip;
    }

    public boolean hasCustomizer () {
        return true;
    }

    public java.awt.Component getCustomizer () {
        if (!platform.isValid()) {
            return new BrokenPlatformCustomizer (this.platform);
        }
        else {
            return new J2SEPlatformCustomizer (this.platform);
        }
    }

}
