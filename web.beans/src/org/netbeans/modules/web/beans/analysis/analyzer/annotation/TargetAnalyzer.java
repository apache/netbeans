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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;


/**
 * @author ads
 *
 */
public abstract class TargetAnalyzer extends RuntimeRetentionAnalyzer 
    implements TargetVerifier 
{

    public boolean hasTarget() {
        Map<String, ? extends AnnotationMirror> types = getHelper()
                .getAnnotationsByType(getElement().getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            handleNoTarget();
        }
        return hasReqiredTarget( target , getDeclaredTargetTypes( getHelper(), 
                target ));
    }
    
    public Set<ElementType> getDeclaredTargetTypes() {
        Map<String, ? extends AnnotationMirror> types = getHelper()
                .getAnnotationsByType(getElement().getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            return Collections.emptySet();
        }
        return getDeclaredTargetTypes( getHelper(), target );
    }
    
    public static Set<ElementType> getDeclaredTargetTypes( 
            AnnotationHelper helper, TypeElement element ) 
    {
        Map<String, ? extends AnnotationMirror> types = helper
                .getAnnotationsByType(element.getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            return Collections.emptySet();
        }
        return getDeclaredTargetTypes( helper, target );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetVerifier#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target, 
            Set<ElementType> set ) 
    {
        return getTargetVerifier().hasReqiredTarget(target, set );
    }
    
    protected abstract TargetVerifier getTargetVerifier();

    protected abstract void handleNoTarget();
    
    private static Set<ElementType> getDeclaredTargetTypes(
            AnnotationHelper helper, AnnotationMirror target)
    {
        AnnotationParser parser = AnnotationParser.create(helper);
        final Set<String> elementTypes = new HashSet<String>();
        parser.expectEnumConstantArray( AnnotationUtil.VALUE, 
                helper.resolveType(
                ElementType.class.getCanonicalName()), 
                new ArrayValueHandler() {
                    
                    @Override
                    public Object handleArray( List<AnnotationValue> arrayMembers ) {
                        for (AnnotationValue arrayMember : arrayMembers) {
                            String value = arrayMember.getValue().toString();
                            elementTypes.add(value);
                        }
                        return null;
                    }
                } , null);
        
        parser.parse( target );
        Set<ElementType> result = new HashSet<ElementType>();
        for (String type : elementTypes) {
            ElementType elementType = ElementType.valueOf(ElementType.class, type);
            result.add( elementType );
        }
        return result;
    }

}
