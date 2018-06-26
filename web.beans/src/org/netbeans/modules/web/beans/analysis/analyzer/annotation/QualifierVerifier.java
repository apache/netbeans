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
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;


/**
 * @author ads
 *
 */
public class QualifierVerifier implements TargetVerifier {
    
    
    private static final QualifierVerifier INSTANCE = new QualifierVerifier( false );
    
    private static final QualifierVerifier EVENT_INSTANCE = 
            new QualifierVerifier( true );
    
    private QualifierVerifier(boolean event){
        isEvent = event;
    }
    
    public static QualifierVerifier getInstance( boolean event ){
        if ( event ){
            return EVENT_INSTANCE;
        }
        else {
            return INSTANCE;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetVerifier#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target,
            Set<ElementType> targetTypes )
    {
        boolean hasRequiredTarget = false;
//        if ( isEvent() ){ TODO: any reason why it was different, ins specification event qualifier is just the same as usual, and example have 4 targets but it's just example? see #225556
        //especially it was updated for evvents before but specification is new for all qualifiers. keep for now commented
            boolean hasFieldParameterTarget = targetTypes.contains(
                    ElementType.FIELD) &&
                        targetTypes.contains(ElementType.PARAMETER);
            if ( !hasFieldParameterTarget){
                hasRequiredTarget = (targetTypes.size() == 1 && 
                                        (targetTypes.contains(ElementType.TYPE) || 
                                         targetTypes.contains(ElementType.METHOD) ||
                                         targetTypes.contains(ElementType.FIELD))) ||
                                    (targetTypes.size() == 2 && 
                                         targetTypes.contains(ElementType.METHOD) &&
                                         targetTypes.contains(ElementType.FIELD));//see #244059
            }
            else {
                if ( targetTypes.size() == 2 ){
                    hasRequiredTarget = true;
                }
                else {
                    hasRequiredTarget = 
                            (targetTypes.size() == 4 && 
                                targetTypes.contains( ElementType.METHOD) &&
                                targetTypes.contains( ElementType.TYPE)) || 
                            (targetTypes.size() == 3 && 
                                targetTypes.contains( ElementType.METHOD));
                }
            }
//        }
//        else {
//            hasRequiredTarget = targetTypes.size() == 4 && 
//                targetTypes.contains( ElementType.METHOD) &&
//                        targetTypes.contains(ElementType.FIELD) &&
//                            targetTypes.contains(ElementType.PARAMETER)&&
//                                targetTypes.contains( ElementType.TYPE);
//        }
        
        return hasRequiredTarget;
    }
    
    private boolean isEvent(){
        return isEvent;
    }
    
    private boolean isEvent;
}
