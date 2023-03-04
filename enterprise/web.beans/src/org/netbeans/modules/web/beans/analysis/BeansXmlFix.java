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
package org.netbeans.modules.web.beans.analysis;

import java.util.Collection;
import java.util.Iterator;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class BeansXmlFix implements Fix {

    BeansXmlFix( Project project , FileObject fileObject , 
            CdiEditorAwareJavaSourceTaskFactory factory ) 
    {
        myProject = project;
        myFileObject = fileObject;
        myFactory = factory;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#getText()
     */
    @Override
    public String getText() {   
        return NbBundle.getMessage( BeansXmlFix.class, "MSG_HintCreateBeansXml");    // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        CdiUtil util = myProject.getLookup().lookup( CdiUtil.class);
        Collection<FileObject> infs;
        if ( util == null ){
            infs= CdiUtil.getBeansTargetFolder(myProject, true);
        }
        else {
            infs = util.getBeansTargetFolder(true);
        }
        for (FileObject inf : infs) {
            if (inf != null) {
                FileObject beansXml = inf
                        .getFileObject(CdiUtil.BEANS_XML);
                if (beansXml != null) {
                    return null;
                }
                DDHelper.createBeansXml(Profile.JAVA_EE_6_FULL, inf,
                        CdiUtil.BEANS);
                CdiUtil logger = myProject.getLookup().lookup(CdiUtil.class);
                if ( logger!= null ){
                    logger.log("USG_CDI_BEANS_FIX", BeansXmlFix.class, 
                            new Object[]{myProject.getClass().getName()}, true );
                }
                if (myFactory != null) {
                    myFactory.restart(myFileObject);
                }
                return null;
            }
        }
        return null;
    }
    
    private Project myProject;
    private CdiEditorAwareJavaSourceTaskFactory myFactory;
    private FileObject myFileObject;
}
