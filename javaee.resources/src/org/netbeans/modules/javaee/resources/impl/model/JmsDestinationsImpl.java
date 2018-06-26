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
        AnnotationMirror annotationMirror = types.get(JndiResourcesDefinition.ANN_JMS_DESTINATIONS);
        return annotationMirror;
    }

}
