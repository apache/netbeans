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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CreatePersistenceUnit implements Fix {
    private Project project;
    
    /** Creates a new instance of CreatePersistenceUnit */
    public CreatePersistenceUnit(Project project) {
        this.project = project;
    }
    
    public ChangeInfo implement(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                try{
                    Util.createPersistenceUnitUsingWizard(project, null);
                } catch(InvalidPersistenceXmlException e){
                    JPAProblemFinder.LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
        
        return null;
    }
    
    public int hashCode(){
        return this.getClass().getName().hashCode();
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_CreatePersistenceUnitHint");
    }
}
