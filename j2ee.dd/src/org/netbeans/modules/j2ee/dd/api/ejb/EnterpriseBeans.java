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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.FindCapability;

public interface EnterpriseBeans extends CommonDDBean, FindCapability {

        public static final String SESSION = "Session";	// NOI18N
	public static final String ENTITY = "Entity";	// NOI18N
	public static final String MESSAGE_DRIVEN = "MessageDriven";	// NOI18N
        
        public void setSession(int index, Session value);
        
        public void setSession(Session[] value);
        
        public Session getSession(int index);       

        public Session[] getSession();
        
	public int addSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);

	public int removeSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);
        
        public int sizeSession();
        
        public Session newSession();
                
        public void setEntity(int index, Entity value);
        
        public void setEntity(Entity[] value);
        
        public Entity getEntity(int index);       

        public Entity[] getEntity();
        
	public int removeEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);

	public int addEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);
        
        public int sizeEntity();
	
        public Entity newEntity();
        
        public void setMessageDriven(int index, MessageDriven value);

        public MessageDriven getMessageDriven(int index);

        public void setMessageDriven(MessageDriven[] value);

        public MessageDriven[] getMessageDriven();
        
	public int addMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

	public int sizeMessageDriven();

	public int removeMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

        public MessageDriven newMessageDriven();
        
        public Ejb[] getEjbs();
        
        public void removeEjb( Ejb value);
        
}


