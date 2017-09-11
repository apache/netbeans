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

package org.netbeans.modules.j2ee.jpa.verification;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;

/**
 * @see ProblemContext
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAProblemContext extends ProblemContext {
    private boolean entity;
    private boolean embeddable;
    private boolean idClass;
    private boolean mappedSuperClass;
    private AccessType accessType;
    private EntityMappingsMetadata metadata;
    private Set<CancelListener> cListeners;
    private final Object cListenersLock = new Object();
    
    public boolean isEntity(){
        return entity;
    }
    
    public void setEntity(boolean entity){
        this.entity = entity;
    }
    
    public boolean isEmbeddable(){
        return embeddable;
    }
    
    public void setEmbeddable(boolean embeddable){
        this.embeddable = embeddable;
    }
    
    public boolean isIdClass(){
        return idClass;
    }
    
    public void setIdClass(boolean idClass){
        this.idClass = idClass;
    }
    
    public boolean isMappedSuperClass(){
        return mappedSuperClass;
    }
    
    public void setMappedSuperClass(boolean mappedSuperClass){
        this.mappedSuperClass = mappedSuperClass;
    }
    
    public AccessType getAccessType(){
        return accessType;
    }
    
    public void setAccessType(AccessType accessType){
        this.accessType = accessType;
    }
    
    public EntityMappingsMetadata getMetaData(){
        return metadata;
    }
    
    public void setMetaData(EntityMappingsMetadata metadata){
        this.metadata = metadata;
    }
    
    public boolean isJPAClass(){
        return entity || embeddable || idClass || mappedSuperClass;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        if(cancelled && cListeners != null) {
            synchronized(cListenersLock) {
                for(CancelListener cl:cListeners) {
                    cl.cancelled();
                }
            }
        }
    }

    public void addCancelListener(CancelListener aThis) {
        if(cListeners == null) {
            cListeners = new HashSet();
        }
        synchronized(cListenersLock) {
            cListeners.add(aThis);
        }
    }
    
    public void removeCancelListener(CancelListener cl) {
        if(cListeners != null) {
            synchronized(cListenersLock) {
                cListeners.remove(cl);
            }
        }
    }
}
