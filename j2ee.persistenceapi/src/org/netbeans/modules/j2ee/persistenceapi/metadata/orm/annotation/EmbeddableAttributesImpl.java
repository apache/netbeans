/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.AttributesHelper.PropertyHandler;

public class EmbeddableAttributesImpl implements EmbeddableAttributes, PropertyHandler {

    private final EmbeddableImpl embeddable;
    private final AttributesHelper attrHelper;

    private final List<Basic> basicList = new ArrayList<Basic>();

    public EmbeddableAttributesImpl(EmbeddableImpl embeddable) {
        this.embeddable = embeddable;
        attrHelper = new AttributesHelper(embeddable.getRoot().getHelper(), embeddable.getTypeElement(), this);
        attrHelper.parse();
    }

    public boolean hasFieldAccess() {
        return attrHelper.hasFieldAccess();
    }

    public void handleProperty(Element element, String propertyName) {
        AnnotationModelHelper helper = embeddable.getRoot().getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        if (EntityMappingsUtilities.isTransient(annByType, element.getModifiers())) {
            return;
        }

        AnnotationMirror columnAnnotation = annByType.get("javax.persistence.Column"); // NOI18N
        AnnotationMirror temporalAnnotation = annByType.get("javax.persistence.Temporal"); // NOI18N
        String temporal = temporalAnnotation != null ? EntityMappingsUtilities.getTemporalType(helper, temporalAnnotation) : null;
        Column column = new ColumnImpl(helper, columnAnnotation, propertyName.toUpperCase()); // NOI18N
        AnnotationMirror basicAnnotation = annByType.get("javax.persistence.Basic"); // NOI18N
        basicList.add(new BasicImpl(helper, basicAnnotation, propertyName, column, temporal));
    }

    public void setBasic(int index, Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic getBasic(int index) {
        return basicList.get(index);
    }

    public int sizeBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setBasic(Basic[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic[] getBasic() {
        return basicList.toArray(new Basic[basicList.size()]);
    }

    public int addBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic newBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(int index, Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient getTransient(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(Transient[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient[] getTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient newTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
