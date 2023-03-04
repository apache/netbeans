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
package org.netbeans.modules.css.editor.module.spi;

import java.net.URL;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class HelpResolver {
    
    /**
     * Returns the help content in the html code form for the given property.
     */
    public abstract String getHelp(FileObject context, PropertyDefinition property);
    
    /**
     * Resolves a link (relative or absolute) from within the property help content
     */
    public abstract URL resolveLink(FileObject context, PropertyDefinition property, String link);
    
    /**
     * Return a reasonable number representing a sort priority of the help resolver.
     * Lower number means higher priority - the help content will appear higher in the 
     * documentation window if there are more help resolvers returning a content for the 
     * given source object.
     */
    public abstract int getPriority();
    
}
