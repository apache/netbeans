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
package org.netbeans.modules.rust.cargo.impl.nodes;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A Node for RustPackages
 *
 * @author antonio
 */
@NbBundle.Messages({
    "NAME=Name",
    "MAJOR=Major version",
    "MINOR=Minor version",
    "PATCH=Patch version",
    "NAME_DESC=The name of this package",
    "MAJOR_DESC=The major version of this package",
    "MINOR_DESC=The minor version of this package",
    "PATCH_DESC=The patch version of this package",})
public final class RustPackageNode extends AbstractNode {

    public enum PROPS {
        NAME,
        MAJOR,
        MINOR,
        PATCH;
    }

    private final RustPackage rustPackage;

    public RustPackageNode(RustPackage rustPackage) {
        this(rustPackage, new InstanceContent());
    }

    private RustPackageNode(RustPackage rustPackage, InstanceContent ic) {
        super(Children.LEAF, new AbstractLookup(ic));
        this.rustPackage = rustPackage;
        setIconBaseWithExtension(RustProjectAPI.ICON);
        setDisplayName(String.format("%s (%s)", rustPackage.getName(), rustPackage.getVersion()));
        ic.add(rustPackage);
        ic.add(rustPackage.getCargotoml());
    }

    private static final String I18N(String key) {
        return NbBundle.getMessage(RustPackageNode.class, key);
    }

    @Override
    protected Sheet createSheet() {

        // Create an empty sheet
        Sheet sheet = Sheet.createDefault();
        // Create a set of properties
        Sheet.Set set = Sheet.createPropertiesSet();

        // The Name read-only property
        Property name = new PropertySupport.ReadOnly<String>(PROPS.NAME.name(), String.class, I18N("NAME"), I18N("NAME_DESC")) {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return rustPackage.getName();
            }
        };
        set.put(name);

        Property major
                = new PropertySupport.ReadOnly<String>(PROPS.MAJOR.name(), String.class, I18N("MAJOR"), I18N("MAJOR_DESC")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return Long.toString(rustPackage.getSemver().getMajor());
            }

        };
        set.put(major);

        Property minor
                = new PropertySupport.ReadOnly<String>(PROPS.MINOR.name(), String.class, I18N("MINOR"), I18N("MINOR_DESC")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return Long.toString(rustPackage.getSemver().getMinor());
            }

        };
        set.put(minor);

        Property patch
                = new PropertySupport.ReadOnly<String>(PROPS.PATCH.name(), String.class, I18N("PATCH"), I18N("PATCH_DESC")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return Long.toString(rustPackage.getSemver().getMinor());
            }

        };
        set.put(patch);

        // Add the set of properties to the sheet
        sheet.put(set);

        return sheet;
    }

}
