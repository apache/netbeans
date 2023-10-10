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
package org.netbeans.modules.jakarta.web.beans.hints;

import org.openide.text.Annotation;
import org.openide.text.Line.Part;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class CDIAnnotation extends Annotation {
    
    public enum CDIAnnotaitonType {
        INJECTION_POINT("org-netbeans-modules-jakarta-web-beans-annotations-injection-point"),
        DELEGATE_POINT("org-netbeans-modules-jakarta-web-beans-annotations-delegate-point"),
        DECORATED_BEAN("org-netbeans-modules-jakarta-web-beans-annotations-decorated-bean"),
        EVENT("org-netbeans-modules-jakarta-web-beans-annotations-event"),
        OBSERVER("org-netbeans-modules-jakarta-web-beans-annotations-observer"),
        INTERCEPTED_ELEMENT("org-netbeans-modules-editor-annotations-intercepted");
        
        private CDIAnnotaitonType( String type ){
            myType = type;
        }
        
        @Override
        public String toString(){
            return myType;
        }
        
        private final String myType;
    }
    
    CDIAnnotation( CDIAnnotaitonType type, Part part){
       myType = type; 
       myPart = part;
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public String getAnnotationType() {
        return myType.toString();
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getShortDescription()
     */
    @Override
    public String getShortDescription() {
        switch (myType) {
            case INJECTION_POINT:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_InjectionPoint"); //NOI18N
            case DELEGATE_POINT:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_DelegatePoint"); //NOI18N
            case DECORATED_BEAN:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_DecoratedBean"); //NOI18N
            case EVENT:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_Event"); //NOI18N
            case OBSERVER:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_Observer"); //NOI18N
            case INTERCEPTED_ELEMENT:
                return NbBundle.getMessage(CDIAnnotation.class, "LBL_Intercepted"); //NOI18N
            default:
                assert false;
                return null;
        }
    }
    
    public Part getPart(){
        return myPart;
    }
    
    
    private CDIAnnotaitonType myType;
    private Part myPart;
}
