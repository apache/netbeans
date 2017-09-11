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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Color;
import java.awt.Font;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;

/** This interface provides methods for
* getting and setting various drawing attributes.
* During painting draw layer receives draw context
* and it is expected to either leave draw parameters
* as they are or change them.
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface DrawContext {

    /** Get current foreground color */
    public Color getForeColor();

    /** Set current foreground color */
    public void setForeColor(Color foreColor);

    /** Get current background color */
    public Color getBackColor();

    /** Set current background color */
    public void setBackColor(Color backColor);

    /** Get current underline color */
    public Color getUnderlineColor();

    /** Set current underline color */
    public void setUnderlineColor(Color underlineColor);

    /** Get current wave underline color */
    public Color getWaveUnderlineColor();

    /** Set current wave underline color */
    public void setWaveUnderlineColor(Color waveUnderlineColor);

    /** Get current strike-through color */
    public Color getStrikeThroughColor();

    /** Set current underline color */
    public void setStrikeThroughColor(Color strikeThroughColor);

    /** Get current top border color 
     * @since 1.22
     */
    public Color getTopBorderLineColor();

    /** Set current top border color 
     * @since 1.22
     */
    public void setTopBorderLineColor(Color topBorderLineColor);

    /** Get current right border color 
     * @since 1.22
     */
    public Color getRightBorderLineColor();

    /** Set current right border color 
     * @since 1.22
     */
    public void setRightBorderLineColor(Color rightBorderLineColor);

    /** Get current bottom border color 
     * @since 1.22
     */
    public Color getBottomBorderLineColor();

    /** Set current bottom border color 
     * @since 1.22
     */
    public void setBottomBorderLineColor(Color bottomBorderLineColor);

    /** Get current left border color 
     * @since 1.22
     */
    public Color getLeftBorderLineColor();

    /** Set current left border color 
     * @since 1.22
     */
    public void setLeftBorderLineColor(Color leftBorderLineColor);

    /** Get current font */
    public Font getFont();

    /** Set current font */
    public void setFont(Font font);

    /** Get start position of the drawing. This value
    * stays unchanged during the line-number drawing.
    */
    public int getStartOffset();

    /** Get end position of the drawing. This value
    * stays unchanged during the line-number drawing.
    */
    public int getEndOffset();

    /** Is current drawing position at the begining of the line?
    * This flag is undefined for the line-number drawing.
    */
    public boolean isBOL();

    /** Is current drawing position at the end of the line
    * This flag is undefined for the line-number drawing.
    */
    public boolean isEOL();

    /** Get draw info for the component that is currently drawn. */
    public EditorUI getEditorUI();

    /** Get token type number according to the appropriate
    * syntax scanner */
    public TokenID getTokenID();

    /** Get the token-context-path for the token */
    public TokenContextPath getTokenContextPath();

    /** Get starting position in the document of the token being drawn */
    public int getTokenOffset();

    /** Get length of the token text */
    public int getTokenLength();

    /** Get the starting position in the document of the
     * fragment of the token being drawn.
     */
    public int getFragmentOffset();

    /** Get the length of the fragment of the token
     * being drawn
     */
    public int getFragmentLength();

    /** Get the buffer with the characters being drawn. No changes can
    * be done to characters in the buffer.
    */
    public char[] getBuffer();

    /** Get the position in the document where the buffer starts.
     * The area between <tt>getDrawStartOffset()</tt> and <tt>getDrawEndOffset</tt>
     * will contain valid characters. However the first token
     * can start even under <tt>getDrawStartOffset()</tt>. In this case
     * the valid area starts at <tt>getTokenOffset()</tt> of the first
     * token.
     */
    public int getBufferStartOffset();

}
