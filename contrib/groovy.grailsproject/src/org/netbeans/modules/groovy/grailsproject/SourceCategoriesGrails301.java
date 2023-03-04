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
package org.netbeans.modules.groovy.grailsproject;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;

/**
 * This class registers the Source Categories for Grails version 3
 * @author schmidtm
 * @author Martin Adamek
 */
public class SourceCategoriesGrails301 implements SourceCategoriesInterface {

    public static final GrailsPlatform.Version MIN_VERSION = GrailsPlatform.Version.valueOf("3.0.1");    
    
    private static final Map<SourceCategoryType, SourceCategory> SOURCE_CATEGORIES
            = new HashMap<>();

    static {
        /*
        Registers the Source Categories for each type supported by this version.
        */
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_CONF, new SourceCategory("grails-app/conf", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_CONTROLLERS, new SourceCategory("grails-app/controllers", "create-controller", "Controller.groovy"));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_DOMAIN, new SourceCategory("grails-app/domain", "create-domain-class", null));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_I18N, new SourceCategory("grails-app/i18n", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_SERVICES, new SourceCategory("grails-app/services", "create-service", "Service.groovy"));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_TAGLIB, new SourceCategory("grails-app/taglib", "create-taglib", "TagLib.groovy"));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_UTILS, new SourceCategory("grails-app/utils", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.GRAILSAPP_VIEWS, new SourceCategory("grails-app/views", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.PLUGINS, new SourceCategory("plugins", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.TEST_INTEGRATION, new SourceCategory("test/integration", "create-integration-test", "Tests.groovy"));
        SOURCE_CATEGORIES.put(SourceCategoryType.TEST_UNIT, new SourceCategory("test/unit", "create-unit-test", "Tests.groovy"));
        SOURCE_CATEGORIES.put(SourceCategoryType.SCRIPTS, new SourceCategory("scripts", "create-script", null));
        SOURCE_CATEGORIES.put(SourceCategoryType.SRC_JAVA, new SourceCategory("src/java", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.SRC_GWT, new SourceCategory("src/gwt", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.SRC_GROOVY, new SourceCategory("src/groovy", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.TEMPLATES, new SourceCategory("src/templates", "install-templates", null));
        SOURCE_CATEGORIES.put(SourceCategoryType.WEBAPP, new SourceCategory("web-app", null, null));
        SOURCE_CATEGORIES.put(SourceCategoryType.LIB, new SourceCategory("lib", null, null));
    }

    /**
     * Returns the source category for the given type.
     * @param type Source category type to lookup
     * @return the source category, null if not present.
     */
    @Override
    public SourceCategory getSourceCategory(SourceCategoryType type) {
        return SourceCategoriesGrails301.SOURCE_CATEGORIES.get(type);
    }

}
