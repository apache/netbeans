
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
 *//*
 * HandlerChain.java
 *
 * Created on March 19, 2006, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class HandlerChain {
    private org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain;
    /** Creates a new instance of HandlerChain */
    public HandlerChain(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain) {
        this.chain=chain;
    }
    
    Object getOriginal() {
        return chain;
    }
    
    public String getHandlerChainName() {
        return chain.getHandlerChainName();
    }
    
    public Handler[] getHandlers() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        Handler[] newHandlers = new Handler[handlers.length];
        for (int i=0;i<handlers.length;i++) {
            newHandlers[i]=new Handler(handlers[i]);
        }
        return newHandlers;
    }
    
    public void setHandlerChainName(String value) {
        chain.setHandlerChainName(value);
    }
    
    public Handler newHandler() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        return new Handler(handler);
    }
    
    public void addHandler(String handlerName, String handlerClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        handler.setHandlerName(handlerName);
        handler.setHandlerClass(handlerClass);
        chain.addHandler(handler);
    }
    
    public boolean removeHandler(String handlerNameOrClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        for (int i=0;i<handlers.length;i++) {
            if (handlerNameOrClass.equals(handlers[i].getHandlerName()) || 
                    handlerNameOrClass.equals(handlers[i].getHandlerClass())) {
                chain.removeHandler(handlers[i]);
                return true;
            }
        }
        return false;
    }
    
    public Handler findHandlerByName(String handlerName) {
        Handler[] handlers = getHandlers();
        for (int i=0;i<handlers.length;i++) {
            if (handlerName.equals(handlers[i].getHandlerName())) return handlers[i];
        }
        return null;
    }
}
