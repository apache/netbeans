/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.resources.impl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.javaee.resources.api.JmsDestination;
import org.netbeans.modules.javaee.resources.api.JndiResourcesDefinition;
import org.netbeans.modules.javaee.resources.api.model.Location;
import org.netbeans.modules.javaee.resources.api.model.Refreshable;

/**
 * Implementation of the JmsDestination resource.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JmsDestinationImpl extends PersistentObject implements JmsDestination, Refreshable {

    private SimpleImpl holder;

    protected JmsDestinationImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    @Override
    public String getDescription() {
        return holder.description;
    }

    @Override
    public String getName() {
        return holder.name;
    }

    @Override
    public String getClassName() {
        return holder.className;
    }

    @Override
    public String getInterfaceName() {
        return holder.interfaceName;
    }

    @Override
    public String getDestinationName() {
        return holder.destinationName;
    }

    @Override
    public String getResourceAdapterName() {
        return holder.resourceAdapterName;
    }

    @Override
    public String[] getProperties() {
        return holder.properties;
    }

    @Override
    public Location getLocation() {
        return holder.location;
    }

    @Override
    public boolean refresh(TypeElement type) {
        AnnotationMirror annotationMirror = getSpecificAnnotationMirror(getAllAnnotationTypes(getHelper(), type));
        if (annotationMirror == null) {
            return false;
        }
        holder = parseAnnotation(getHelper(), annotationMirror);
        return true;
    }

    protected static SimpleImpl parseAnnotation(AnnotationModelHelper helper, AnnotationMirror annotationMirror) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("className", parser.defaultValue("")); //NOI18N
        parser.expectString("interfaceName", null); //NOI18N
        parser.expectString("description", parser.defaultValue("")); //NOI18N
        parser.expectString("destinationName", parser.defaultValue("")); //NOI18N
        parser.expectString("name", null); //NOI18N
        List<String> props = new ArrayList<String>();
        parser.expectStringArray("properties", new JndiResourcesValueHandlers.PropertiesArrayValueHandler(props),
                parser.defaultValue(new String[0])); //NOI18N
        parser.expectString("resourceAdapterName", parser.defaultValue("")); //NOI18N
        ParseResult result = parser.parse(annotationMirror);

        String description = result.get("description", String.class);
        String destinationName = result.get("destinationName", String.class);
        String className = result.get("className", String.class);
        String interfaceName = result.get("interfaceName", String.class);
        String name = result.get("name", String.class);
        String resourceAdapterName = result.get("resourceAdapterName", String.class);
        String[] properties = result.get("properties", String[].class);
        Location location = LocationHelper.getClassLocation(helper, className);
        return new SimpleImpl(description, name, className, interfaceName, destinationName, resourceAdapterName, properties, location);
    }

    private static Map<String, ? extends AnnotationMirror> getAllAnnotationTypes(AnnotationModelHelper helper, TypeElement type) {
        return helper.getAnnotationsByType(helper.getCompilationController().getElements().getAllAnnotationMirrors(type));
    }

    private static AnnotationMirror getSpecificAnnotationMirror(Map<String, ? extends AnnotationMirror> types) {
        AnnotationMirror annotationMirror = types.get(JndiResourcesDefinition.ANN_JMS_DESTINATION);
        return annotationMirror;
    }

    protected static class SimpleImpl implements JmsDestination {
        private final String description;
        private final String name;
        private final String className;
        private final String interfaceName;
        private final String destinationName;
        private final String resourceAdapterName;
        private final String[] properties;
        private final Location location;

        public SimpleImpl(String description, String name, String className, String interfaceName,
                String destinationName, String resourceAdapterName, String[] properties, Location location) {
            this.description = description;
            this.name = name;
            this.className = className;
            this.interfaceName = interfaceName;
            this.destinationName = destinationName;
            this.resourceAdapterName = resourceAdapterName;
            this.properties = properties;
            this.location = location;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public String getInterfaceName() {
            return interfaceName;
        }

        @Override
        public String getDestinationName() {
            return destinationName;
        }

        @Override
        public String getResourceAdapterName() {
            return resourceAdapterName;
        }

        @Override
        public String[] getProperties() {
            return properties;
        }

        @Override
        public Location getLocation() {
            return location;
        }
    }

}
