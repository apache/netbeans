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

package org.netbeans.modules.languages.features;

import org.netbeans.modules.languages.*;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTNode;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.api.languages.ParseException;
import org.openide.ErrorManager;


/**
 * @author Dan Prusa
 */
public class GenericAction extends BaseAction {
    
    String performerName = null;
    String enablerName = null;
    Feature performer = null;
    Feature enabler = null;
    
    public GenericAction(String name, String performerName, String enablerName) {
        super(name);
        this.performerName = performerName;
        this.enablerName = enablerName;
    }
    
    private Feature getPerformer() {
        if (performer == null) {
            performer = Feature.createMethodCallFeature (null, null, performerName);
        }
        return performer;
    }
    
    private Feature getEnabler() {
        if (enablerName == null) {
            return null;
        }
        if (enabler == null) {
            enabler = Feature.createMethodCallFeature (null, null, enablerName);
        }
        return enabler;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        return ParserManagerImpl.getImpl (comp.getDocument ()).getAST();
    }
    
    
    public void actionPerformed(ActionEvent e, JTextComponent comp) {
        ASTNode node = getASTNode(comp);
        if (node != null) {
            getPerformer().getValue (new Object[] {node, comp});
        }
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        ASTNode node = getASTNode(comp);
        if (node == null)
            return false;
        Feature em = getEnabler();
        if (em == null) {
            return super.isEnabled();
        }
        Object result = em.getValue (new Object[] {node, comp});
        return result != null && ((Boolean)result).booleanValue();
    }
    
}
