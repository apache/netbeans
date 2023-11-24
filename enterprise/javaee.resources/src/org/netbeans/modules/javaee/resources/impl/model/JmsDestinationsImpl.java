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
package org.netbeans.modules.javaee.resources.impl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.javaee.resources.api.JmsDestination;
import org.netbeans.modules.javaee.resources.api.JmsDestinations;
import org.netbeans.modules.javaee.resources.api.JndiResourcesDefinition;
import org.netbeans.modules.javaee.resources.api.model.Location;
import org.netbeans.modules.javaee.resources.api.model.Refreshable;

/**
 * Implementation of the JmsDestinations resource.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JmsDestinationsImpl extends PersistentObject implements JmsDestinations, Refreshable {

    private List<JmsDestination> destinations;
    private Location location;

    public JmsDestinationsImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    @Override
    public List<JmsDestination> getJmsDestinations() {
        return destinations;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean refresh(TypeElement type) {
        AnnotationMirror annotationMirror = getSpecificAnnotationMirror(getAllAnnotationTypes(getHelper(), type));
        if (annotationMirror == null) {
            return false;
        }
        parseAnnotation(annotationMirror);
        return true;
    }
    
    private void parseAnnotation(AnnotationMirror annotationMirror) {
        destinations = new ArrayList<JmsDestination>();
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectAnnotationArray("value", getHelper().resolveType(JndiResourcesDefinition.ANN_JMS_DESTINATION_JAKARTA), new ArrayValueHandler() { //NOI18N
            @Override
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    Object arrayMemberValue = arrayMember.getValue();
                    if (arrayMemberValue instanceof AnnotationMirror) {
                        destinations.add(JmsDestinationImpl.parseAnnotation(getHelper(), (AnnotationMirror) arrayMemberValue));
                    }
                }
                return null;
            }
        }, null);
        parser.expectAnnotationArray("value", getHelper().resolveType(JndiResourcesDefinition.ANN_JMS_DESTINATION), new ArrayValueHandler() { //NOI18N
            @Override
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    Object arrayMemberValue = arrayMember.getValue();
                    if (arrayMemberValue instanceof AnnotationMirror) {
                        destinations.add(JmsDestinationImpl.parseAnnotation(getHelper(), (AnnotationMirror) arrayMemberValue));
                    }
                }
                return null;
            }
        }, null);
        parser.parse(annotationMirror);
    }

    private static Map<String, ? extends AnnotationMirror> getAllAnnotationTypes(AnnotationModelHelper helper, TypeElement type) {
        return helper.getAnnotationsByType(helper.getCompilationController().getElements().getAllAnnotationMirrors(type));
    }

    private static AnnotationMirror getSpecificAnnotationMirror(Map<String, ? extends AnnotationMirror> types) {
        AnnotationMirror annotationMirror = types.get(JndiResourcesDefinition.ANN_JMS_DESTINATIONS_JAKARTA);
        if (annotationMirror == null) {
            annotationMirror = types.get(JndiResourcesDefinition.ANN_JMS_DESTINATIONS);
        }
        return annotationMirror;
    }

}
