/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib.drawing;

/** Draw layer applies changes to draw context during painting process.
* Each extended UI has its own set of layers.
* It can currently include changes to font bold and italic attributes,
* and foreground and background color (and probably more in future).
* These changes are made by draw layer to draw context
* in <CODE>updateContext()</CODE> method.
* Draw layers form double-linked lists. Renderer goes through
* this list every time it draws the tokens of the text.
* A layer can work either by returning the next-activity-change-offset
* or by being activated through the draw-marks that it places
* at the appropriate positions or it can mix these two approaches.
*
* @deprecated Please use Highlighting SPI instead, for details see
*   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
* 
* @author Miloslav Metelka
* @version 1.00
*/

@Deprecated
/* package */ interface DrawLayer {

    /**
     * Start of the next region with the frame.
     */
    public static final String TEXT_FRAME_START_POSITION_COMPONENT_PROPERTY
            = "text-frame-start-position"; // NOI18N
    
    /**
     * End of the next region with the frame.
     */
    public static final String TEXT_FRAME_END_POSITION_COMPONENT_PROPERTY
            = "text-frame-end-position"; // NOI18N
    
    /** Get the name of the layer. The layers that should work together
    * over one component must have the different names.
    */
    public String getName();

    /** Whether the layer wants to use the last context's background
    * till the end of the window or not.
    */
    public boolean extendsEOL();

    /** Whether the layer marks the empty line with the background by half
    * of the character.
    */
    public boolean extendsEmptyLine();

    /** Get the next position at which the activity of the layer will change.
    * It can return <tt>Integer.MAX_VALUE</tt> to mark that the activity
    * will never change or if it will change only by draw-marks.
    * When this position will be reached the <tt>isActive</tt> will be called.
    */
    public int getNextActivityChangeOffset(DrawContext ctx);

    /** Called each time the paint begins for all layers
    * in the layer chain regardless whether they are currently active
    * or not. It is intended to prepare the layer. It doesn't need
    * to set the next-activity-change-offset because <tt>isActive()</tt>
    * will be called at the begining of the drawing when this method
    * finishes.
    */
    public void init(DrawContext ctx);

    /** Return whether the layer is active or not. This method
    * is called at the begining of the drawing,
    * then each time when the draw-mark is found at the current
    * fragment offset or when drawing reaches the next-activity-change-offset
    * of this layer (mark parameter is null in this case).
    * The layer must return whether it wants to be active for the next drawing
    * or not.
    * The layer should also consider
    * changing the next-activity-change-offset because the draw-engine
    * will ask for it after this method finishes.
    * If the mark is found at the same position like next-activity-change-offset
    * is, then this method is called only once with the valid <tt>mark</tt> parameter.
    * @param ctx current context with the information about the drawing
    * @param mark draw-mark at the fragment-offset or null if called
    * because of the next-activity-change-offset.
    */
    public boolean isActive(DrawContext ctx, DrawMark mark);

    /** Update draw context by setting colors, fonts and possibly other draw
    * properties.
    * The method can use information from the context to find where the painting
    * process is currently located. It is called only if the layer is active.
    */
    public void updateContext(DrawContext ctx);

    /** Update draw context related to the drawing of line number for the given
    * line by setting colors, fonts and possibly other draw
    * properties. The method can also change the current line number by returning
    * the modified line-number than the original one. At the begining the first
    * layer gets the line-number <tt>lineOffset + 1</tt> but some layers can
    * change it. If the layer doesn't want to change the line-number it should
    * return the same value as it gets.
    * The context can be affected to change the font and colors for the line-number.
    * The context's <tt>getFragmentOffset()</tt> returns the begining of the line.
    * The following methods in the context return undefined values:
    * <tt>isEOL(), getBuffer(), getTokenID(), getTokenOffset(), getTokenLength()</tt>.
    * The process of calling this method is independent of the status
    * of the layers and is called for each layer even if it's not active.
    * @param lineNumber the number that will be drawn before the line's text.
    *   The layer can change it by returning a different value.
    * @param ctx the draw context
    */
    public int updateLineNumberContext(int lineNumber, DrawContext ctx);

    /** 
     * Abstract implementation of the draw-layer. 
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public abstract static class AbstractLayer implements DrawLayer {

        /** Name of this layer. The name of the layer must be unique among
        * layers installed into EditorUI
        */
        private String name;

        /** Next position where the layer should be notified
        * to update its state.
        */
        int nextActivityChangeOffset = Integer.MAX_VALUE;

        /** Construct new abstract layer with the known name and visibility. */
        public AbstractLayer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean extendsEOL() {
            return false;
        }

        public boolean extendsEmptyLine() {
            return false;
        }

        public int getNextActivityChangeOffset(DrawContext ctx) {
            return nextActivityChangeOffset;
        }

        public void setNextActivityChangeOffset(int nextActivityChangeOffset) {
            this.nextActivityChangeOffset = nextActivityChangeOffset;
        }

        public void init(DrawContext ctx) {
        }

        public int updateLineNumberContext(int lineNumber, DrawContext ctx) {
            return lineNumber;
        }

        public @Override String toString() {
            return "Layer " + getClass() + ", name='" + name; // NOI18N
        }

    }

}
