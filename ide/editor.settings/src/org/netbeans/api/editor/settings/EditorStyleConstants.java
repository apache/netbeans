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
