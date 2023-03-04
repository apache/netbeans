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

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * Represents the schema annotation component, <xs:annotation>.
 * An example:
 *    <xs:annotation>
 *       <xs:documentation>A type for experts only</xs:documentation>
 *       <xs:appinfo>
 *         <fn:specialHandling>checkForPrimes</fn:specialHandling>
 *       </xs:appinfo>
 *    </xs:annotation>
 * See http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html#cAnnotations.
 *
**/
public interface Annotation extends SchemaComponent {
	
	public static final String DOCUMENTATION_PROPERTY = "documentation";
	public static final String APPINFO_PROPERTY = "appinfo";
        
    /**
     * Adds the given Documentation to this Annotation
     * @param documentation the documentation to add to this Annotation
    **/
    public void addDocumentation(Documentation documentation);

    /**
     * Removes the given Documentation from this Annotation
     * @param documentation the Documentation to remove
    **/
    public void removeDocumentation(Documentation documentation);
    
    /**
     * Returns an enumeration of all documentation elements for this Annotation
     * @return an enumeration of all documentation elements for this Annotation
    **/
    public Collection<Documentation> getDocumentationElements();
    
    Collection<AppInfo> getAppInfos();
    void addAppInfo(AppInfo appInfo);
    void removeAppInfo(AppInfo appInfo);
    
} //-- Annotation
