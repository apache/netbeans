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
import java.util.function.Function;
import javax.swing.Action;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.cargo.impl.nodes.actions.dependencies.RustRemoveDependencyAction;
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
    "VERSION=Version",
    "DESCRIPTION=Description",
    "GIT=Git repository",
    "BRANCH=Git repository branch",
    "NAME_DESC=The name of this package",
    "VERSION_DESC=The version of this package",
    "DESCRIPTION_DESC=The description of this package",
    "GIT_DESC=A git repository where this package is downloaded from",
    "BRANCH_DESC=The branch used to fetch this package source from git",
})
public final class RustPackageNode extends AbstractNode {

    public enum PROPS {
        NAME,
        VERSION,
        DESCRIPTION,
        GIT,
        BRANCH;
    }

    private final RustPackage rustPackage;
    private final RustProjectDependenciesNode.DependencyType dependencyType;

    public RustPackageNode(RustPackage rustPackage, RustProjectDependenciesNode.DependencyType dependencyType) {
        this(rustPackage, dependencyType, new InstanceContent());
    }

    private RustPackageNode(RustPackage rustPackage, RustProjectDependenciesNode.DependencyType dependencyType, InstanceContent ic) {
        super(Children.LEAF, new AbstractLookup(ic));
        this.rustPackage = rustPackage;
        this.dependencyType = dependencyType;
        setIconBaseWithExtension(RustProjectAPI.ICON);
        if (rustPackage != null) {
            setDisplayName(String.format("%s (%s)", rustPackage.getName(), rustPackage.getVersion()));
            if (rustPackage.getDescription() != null) {
                setShortDescription(rustPackage.getDescription());
            }
            ic.add(rustPackage);
            ic.add(rustPackage.getCargotoml());
        }
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

        set.put(getNameProperty());
        set.put(getDescriptionProperty());
        set.put( getVersionProperty());
        set.put( getGitProperty());
        set.put( getBranchProperty());

        // Add the set of properties to the sheet
        sheet.put(set);

        return sheet;
    }

    public Property<String> getNameProperty() {
        return getStringProperty(PROPS.NAME, RustPackage::getName);
    }

    public Property<String> getVersionProperty() {
        return getStringProperty(PROPS.VERSION, RustPackage::getVersion);
    }

    public Property<String> getDescriptionProperty() {
        return getStringProperty(PROPS.DESCRIPTION, RustPackage::getDescription);
    }

    public Property<String> getGitProperty() {
        return getStringProperty(PROPS.GIT, RustPackage::getGit);
    }

    public Property<String> getBranchProperty() {
        return getStringProperty(PROPS.BRANCH, RustPackage::getBranch);
    }

    private Property<String> getStringProperty(PROPS props, Function<RustPackage, String> value) {
        Property<String> property = new PropertySupport.ReadOnly<String>(props.name(), String.class, I18N(props.name()), I18N(props.name() + "_DESC")) {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return value.apply(rustPackage);
            }
        };
        return property;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new RustRemoveDependencyAction(rustPackage.getCargotoml(), rustPackage, dependencyType)
        };
    }

}
