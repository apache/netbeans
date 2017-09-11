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
package org.netbeans.jellytools;

import org.netbeans.jellytools.actions.SaveAsTemplateAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Handle "Save As Template" dialog. It can be invoked on a node from popup
 * menu.
*/
public class SaveAsTemplateOperator extends NbDialogOperator {

    /** Components operators. */
    private JTreeOperator _tree;
    private JLabelOperator _lblSelectTheCategory;
    
    /** Creates new instance of SaveAsTemplateOperator. It waits for dialog
     * with title "Save As Template".
     */
    public SaveAsTemplateOperator() {
        super(Bundle.getString("org.openide.loaders.Bundle", "Title_SaveAsTemplate"));
    }
    
    /** Invokes Save As Template dialog on specified nodes.
     * @param nodes array of nodes to select before action call
     * @return  instance of SaveAsTemplateOperator
     */
    public static SaveAsTemplateOperator invoke(Node[] nodes) {
        new SaveAsTemplateAction().perform(nodes);
        return new SaveAsTemplateOperator();
    }
    
    /** Invokes Save As Template dialog on specified node.
     * @param node node to select before action call
     * @return  instance of SaveAsTemplateOperator
     */
    public static SaveAsTemplateOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    /** Returns operator of templates tree.
     * @return  JTreeOperator instance of templates tree
     */
    public JTreeOperator tree() {
        if(_tree == null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** Returns operator of "Select the category..." label.
     * @return  JLabelOperator instance of "Select the category..." label
     */
    public JLabelOperator lblSelectTheCategory() {
        if (_lblSelectTheCategory == null) {
            _lblSelectTheCategory = new JLabelOperator(this,
                                  Bundle.getStringTrimmed("org.openide.loaders.Bundle",
                                                          "CTL_SaveAsTemplate"));
        }
        return _lblSelectTheCategory;
    }
    
    /** Returns root node of templates tree.
     * @return  Node instance of root node of templates tree
     */
    public Node getRootNode() {
        return new Node(tree(), "");
    }
    
    /** Selects given template in templates tree.
     * @param templatePath path to template (e.g. Classes|Main)
     */
    public void selectTemplate(String templatePath) {
        if(templatePath == null) {
            throw new JemmyException("Cannot accept null parameter");
        }
        new Node(tree(), templatePath).select();
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btOK();
        btCancel();
        lblSelectTheCategory();
        tree();
    }
    
}
