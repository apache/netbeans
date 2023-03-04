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

package org.netbeans.modules.beans.resources.templates;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.WizardDescriptor;

public class Templates {

    @TemplateRegistrations({
        @TemplateRegistration(folder="Beans", position=100, content="Bean.java.template", displayName="org.netbeans.modules.beans.Bundle#Templates/Beans/Bean.java", iconBase=JavaTemplates.JAVA_ICON, description="Bean.html", category="java-beans", scriptEngine="freemarker"),
        @TemplateRegistration(folder="Beans", position=200, content="BeanInfo.java.template", displayName="org.netbeans.modules.beans.Bundle#Templates/Beans/BeanInfo.java", iconBase=JavaTemplates.JAVA_ICON, description="BeanInfo.html", category="java-beans", scriptEngine="freemarker"),
        @TemplateRegistration(folder="Beans", position=300, content="NoIconBeanInfo.java.template", displayName="org.netbeans.modules.beans.Bundle#Templates/Beans/NoIconBeanInfo.java", iconBase=JavaTemplates.JAVA_ICON, description="BeanInfo.html", category="java-beans", scriptEngine="freemarker"),
        @TemplateRegistration(folder="Beans", position=400, content={"Customizer.java.template", "Customizer.form.template"}, displayName="org.netbeans.modules.beans.Bundle#Templates/Beans/Customizer.java", description="Customizer.html", category="java-beans", scriptEngine="freemarker"),
        @TemplateRegistration(folder="Beans", position=600, content="PropertyEditor.java.template", displayName="org.netbeans.modules.beans.Bundle#Templates/Beans/PropertyEditor.java", iconBase=JavaTemplates.JAVA_ICON, description="PropertyEditor.html", category="java-beans", scriptEngine="freemarker")
    })
    public static WizardDescriptor.InstantiatingIterator<?> wizard() {
        return JavaTemplates.createJavaTemplateIterator();
    }

    private Templates() {}

}
