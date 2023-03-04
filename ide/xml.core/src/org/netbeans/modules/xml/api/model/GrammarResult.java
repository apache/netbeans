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
