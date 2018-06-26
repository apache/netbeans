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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;


/**
 * @author ads
 *
 */
class SystemEventListenerImpl extends PersistentObject implements
        Refreshable, SystemEventListener
{

    SystemEventListenerImpl( AnnotationModelHelper helper,
            TypeElement typeElement )
    {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    public boolean refresh( TypeElement type ) {
        Map<String, ? extends AnnotationMirror> types = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController()
                    .getElements().getAllAnnotationMirrors(type));
        AnnotationMirror annotationMirror = types.get(
                "javax.faces.event.ListenerFor");                       // NOI18N
        if (annotationMirror == null || 
                !ObjectProviders.SystemEventListenerProvider.
                isApplicationSystemEventListener(type, getHelper())) 
        {
            return false;
        }
        
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectClass( "systemEventClass", null);                  // NOI18N
        parser.expectClass("sourceClass", AnnotationParser.defaultValue(// NOI18N
                Void.class.getCanonicalName()));
        ParseResult parseResult = parser.parse(annotationMirror);
        myEventClass = parseResult.get( "systemEventClass" ,            // NOI18N 
                String.class );
        mySourceClass = parseResult.get( "sourceClass" ,                // NOI18N 
                String.class );
        myClass = type.getQualifiedName().toString();
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSourceClass()
     */
    public String getSourceClass() {
        return mySourceClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSystemEventClass()
     */
    public String getSystemEventClass() {
        return myEventClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSystemEventListenerClass()
     */
    public String getSystemEventListenerClass() {
        return myClass;
    }
    
    private String myClass;
    private String myEventClass;
    private String mySourceClass;

}
