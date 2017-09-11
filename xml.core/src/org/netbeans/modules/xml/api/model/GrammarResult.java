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

package org.netbeans.modules.xml.api.model;

import org.w3c.dom.Node;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * It represents additonal properties of a result option.
 * <p>
 * It enriches DOM Node with information useful for presenting
 * query result option to a user. Also all children or siblings of this
 * result must be <code>GrammarResult</code> instances.
 * <p>
 * It can have children representing mandatory content.
 * However it is up to client if it uses the mandatory content.
 * <p>
 * As in whole package provide only readonly DOM level 1 implementation. 
 *
 * @author  Petr Kuzel
 * @stereotype description 
 */
public interface GrammarResult extends Node {

    //Node getNode() use instead of extends Node
    //boolean isDefault()
    //boolean isImplied()
    //boolean isRequired()
    //boolean isFixed()
    
    /**
     * @return name that is presented to user or <code>null</code> if
     * <code>getNodeName()</code> is enough.
     */
    String getDisplayName();

    /**
     * Returns contents of a description, text suitable for displaying as a tooltip
     * that simplifies decision. {@code null} may be returned if no description is available.
     * The decription is interpreted as HTML markup. If the markup contains relative
     * links or special URIs, implement also {@link DescriptionSource} to resolve 
     * those links.
     * <p/>
     * If {@link DescriptionSource} is implemented on the same object, this method
     * may return null to indicate the content should be <b>loaded by the infrastructure</b>
     * from the URL returned by {@link DescriptionSource#getContentURL()}. If both
     * {@code getDescription()} and {@code DescriptionSource.getContentURL()} return
     * null, no description is displayed in the tooltip.
     * <p/>
     * Implementors may prefer implementing the {@code DescriptionSource} and
     * loading from the {@link DescriptionSource#getContentURL()} if the
     * description resides in a separate file included in the JAR or in the XML layer.
     * 
     * @return provide additional information simplifing decision
     * (suitable for tooltip) or {@code null}.
     * 
     * @since 1.28 - DescriptionSource extension
     */
    @CheckForNull
    String getDescription();

    /**
     * @param kind icon kind as given by BeanInfo
     * @return an icon - a visual hint or <code>null</code>
     */
    Icon getIcon(int kind);

    /**
     * For elements provide hint whether element has empty content model.
     * @return true element has empty content model (no childs) and can
     * be completed in empty element form i.e. <code>&lt;ement/></code>.
     * @since 6th Aug 2004
     */
    boolean isEmptyElement();
}
