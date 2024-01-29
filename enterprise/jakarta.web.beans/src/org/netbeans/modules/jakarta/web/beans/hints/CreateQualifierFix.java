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

import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
final class CreateQualifierFix extends CreateAnnotationFix {

    CreateQualifierFix( CompilationInfo compilationInfo, String name , 
            String packageName , FileObject fileObject)
    {
       super(compilationInfo, name, packageName, fileObject);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#getText()
     */
    @Override
    public String getText() {
        if ( getPackage() == null || getPackage().length() == 0 ){
            return NbBundle.getMessage(CreateQualifierFix.class, 
                    "LBL_FixCreateQualifierDefaultPackage");            // NOI18N
        }
        return NbBundle.getMessage(CreateQualifierFix.class, 
                "LBL_FixCreateQualifier" , getName() , getPackage() ); // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.hints.CreateAnnotationFix#getUsageLogMessage()
     */
    @Override
    protected String getUsageLogMessage() {
        return "USG_CDI_CREATE_QUALIFIER_FIX";      // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.hints.CreateAnnotationFix#getTemplate()
     */
    @Override
    protected String getTemplate() {
        return "Templates/CDI_JakartaEE/Qualifier.java";          // NOI18N
    }

}
