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

package org.netbeans.modules.java.api.common;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class TestRoots extends Roots {

    public static final String TYPE_TEST = "test"; //NOI18N

    private final Map<String,String> props = new HashMap<String, String>();
    private final AntProjectHelper helper;

    public TestRoots (final AntProjectHelper helper) {
        super(true,true,TYPE_TEST,null);
        this.helper=helper;
    }

    public void addRoot(final String prop, final FileObject root, String name) {
        props.put(prop, name);
        final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty(prop, root.getName());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        firePropertyChange(SourceRoots.PROP_ROOTS, null, null);
    }

    public void removeRoot(final String prop) {
        props.remove(prop);
        final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.remove(prop);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        //fire by putProperties
    }

    @Override
    public String[] getRootDisplayNames() {
        return props.values().toArray(new String[props.size()]);
    }

    @Override
    public String[] getRootProperties() {
        return props.keySet().toArray(new String[props.size()]);
    }

}
