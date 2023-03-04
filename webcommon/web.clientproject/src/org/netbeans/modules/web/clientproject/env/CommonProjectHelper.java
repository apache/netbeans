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
package org.netbeans.modules.web.clientproject.env;

import java.io.File;
import java.io.IOException;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.w3c.dom.Element;

/**
 */
public abstract class CommonProjectHelper {
    public static final Object PRIVATE_PROPERTIES_PATH = new Object();
    public static final Object PROJECT_PROPERTIES_PATH = new Object();

    public abstract EditableProperties getProperties(Object path);

    public abstract void putProperties(Object path, EditableProperties privateProps);

    public abstract SharabilityQueryImplementation2 createSharabilityQuery2(Values evaluator, String[] toArray, String[] string);

    public abstract Values getStandardPropertyEvaluator();

    public abstract Object getXmlSavedHook();

    public abstract File resolveFile(String licensePath);

    public abstract FileObject getProjectDirectory();

    public abstract FileObject resolveFileObject(String sourceFolder);

    public abstract void notifyDeleted();

    public abstract AuxiliaryConfiguration createAuxiliaryConfiguration();

    public abstract void registerCallback(Callback l);

    public abstract Element getPrimaryConfigurationData(boolean b);

    public abstract Object createCacheDirectoryProvider();

    public abstract Object createAuxiliaryProperties();

    public abstract void putPrimaryConfigurationData(Element data, boolean b);

    public interface Callback {
        public void projectXmlSaved() throws IOException;
        public void configurationXmlChanged();
        public void propertiesChanged();
    }
}
