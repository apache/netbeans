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

/**
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public abstract class GetAllEjbs extends EnclosingBean {
    
    public GetAllEjbs(java.util.Vector comps, Version version) {
        super(comps, version);
    }
    
    public abstract Entity[] getEntity();
    public abstract MessageDriven[] getMessageDriven();
    public abstract Session[] getSession();
    
    public abstract int sizeSession();
    public abstract int sizeEntity();
    public abstract int sizeMessageDriven();
    public abstract int removeSession(Session s);
    public abstract int removeEntity(Entity e);
    public abstract int removeMessageDriven(MessageDriven m);
    
    public void removeEjb(Ejb value){
        
        if(value instanceof Entity){
            removeEntity((Entity) value);
        }
        else  if(value instanceof Session){
            removeSession((Session) value);
        }
        else  if(value instanceof MessageDriven){
            removeMessageDriven((MessageDriven) value);
        }
        
        
    }
    public Ejb[] getEjbs(){
        int sizeEntity = sizeEntity();
        int sizeSession = sizeSession();
        int sizeMessageDriven = sizeMessageDriven();
        int size = sizeEntity + sizeSession + sizeMessageDriven;
        
        Ejb[] ejbs = new Ejb[size];
        Entity[] enBeans = getEntity();
        Session[] ssbeans = getSession();
        MessageDriven[] mdbeans = getMessageDriven();
        int addindex=0;
        for(int i=0; i<sizeEntity ; i++){
            ejbs[addindex] = (Ejb)enBeans[i];
            addindex++;
        }
        for(int j=0; j<sizeSession ; j++){
            ejbs[addindex] = (Ejb)ssbeans[j];
            addindex++;
        }
        
        for(int j=0; j<sizeMessageDriven ; j++){
            ejbs[addindex] = (Ejb)mdbeans[j];
            addindex++;
        }
        return ejbs;
    }
    
}
