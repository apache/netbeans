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

package org.netbeans.api.editor.settings;

/**
 * Style Constants for Fonts and Colors AttributeSets.
 *
 * @author Martin Roskanin
 */
public final class EditorStyleConstants {

    private String representation;

    private EditorStyleConstants(String representation) {
        this.representation = representation;
    }

    /**
     * Name of the wave underline color attribute.
     */
    public static final Object WaveUnderlineColor = new EditorStyleConstants ("wave underline color"); //NOI18N

    /**
     * Name of the display name attribute.
     */
    public static final Object DisplayName = new EditorStyleConstants ("display name"); //NOI18N
    
    /**
     * Name of the default fonts and colots attribute.
     */
    public static final Object Default = new EditorStyleConstants ("default"); //NOI18N

    /**
     * Name of the tooltip attribute. It's value can be either <code>String</code> or
     * <code>HighlightAttributeValue</code> returning <code>String</code>.
     * 
     * <p>The tooltip text can either be plain text or HTML. If using HTML the
     * text must be enclosed in the case insensitive &lt;html&gt;&lt/html&gt; tags.
     * The only HTML tags guaranteed to work are those defined in <code>HtmlRenderer</code>.
     * 
     * @see org.openide.awt.HtmlRenderer
     * @since 1.12
     */
    public static final Object Tooltip = new EditorStyleConstants ("tooltip"); //NOI18N
    
    /**
     * Name of the top border line color.
     * @since 1.16
     */
    public static final Object TopBorderLineColor = new EditorStyleConstants("top border line color"); //NOI18N
    /**
     * Name of the right hand side border line color.
     * @since 1.16
     */
    public static final Object RightBorderLineColor = new EditorStyleConstants("right border line color"); //NOI18N
    /**
     * Name of the bottom border line color.
     * @since 1.16
     */
    public static final Object BottomBorderLineColor = new EditorStyleConstants("bottom border line color"); //NOI18N
    /**
     * Name of the left hand side border line color.
     * @since 1.16
     */
    public static final Object LeftBorderLineColor = new EditorStyleConstants("left border line color"); //NOI18N
    
    /**
     * Name of the rendering hints <code>Map&lt;?,?&gt;</code> that are used
     * for rendering text in the editor.
     * @since 1.20
     */
    public static final Object RenderingHints = new EditorStyleConstants("rendering hints"); //NOI18N
    
    public @Override String toString() {
        return representation;
    }

}
