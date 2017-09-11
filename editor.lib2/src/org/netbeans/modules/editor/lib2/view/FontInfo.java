/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;

/**
 * Info about font measurements.
 */
final class FontInfo {

    // -J-Dorg.netbeans.modules.editor.lib2.view.FontInfo.level=FINE
    private static final Logger LOG = Logger.getLogger(FontInfo.class.getName());

    /**
     * Font that incorporates a possible text zoom passed to constructor.
     */
    final Font renderFont; // 4 bytes

    /**
     * Maximum ascent of all text layouts created so far with the contained font.
     */
    float ascent; // 8 bytes
    
    /**
     * Integer ascent which is float ascent rounded up to an integer.
     * Used for rendering to adhere to integer pixels since text renders around baseline
     * determined by the ascent.
     */
    int ascentInt; // 12 bytes
    
    /**
     * Maximum descent of all text layouts created so far with the contained font.
     */
    float descent; // 16 bytes
    
    /**
     * Leading of the font.
     */
    float leading; // 20 bytes
    
    /**
     * Integer row height is computed as an integer ascent plus descent and leading.
     * Used for rendering to compute total height rounded up to an integer.
     */
    int rowHeightInt; // 24 bytes
    
    float charWidth; // 28 bytes
    
    /**
     * Array of
     * <ol>
     *     <li>Underline offset.</li>
     *     <li>Underline thickness.</li>
     *     <li>Strike-through offset.</li>
     *     <li>Strike-through thickness.</li>
     * </ol>
     */
    final float[] underlineAndStrike = new float[4]; // 32 bytes

    FontInfo(Font origFont, JTextComponent textComponent, FontRenderContext frc, float rowHeightCorrection, int textZoom) {
        renderFont = (textZoom != 0)
                ? new Font(origFont.getName(), origFont.getStyle(), Math.max(origFont.getSize() + textZoom, 1))
                : origFont;
        char defaultChar = 'A';
        String defaultCharText = String.valueOf(defaultChar);
        TextLayout defaultCharTextLayout = new TextLayout(defaultCharText, renderFont, frc); // NOI18N
        TextLayout rowHeightTextLayout = new TextLayout("A_|B", renderFont, frc);
        // Round the ascent to eliminate long mantissa without any visible effect on rendering.
        updateRowHeight(rowHeightTextLayout, rowHeightCorrection);
        // Ceil fractions to whole numbers since this measure may be used for background rendering
        charWidth = (float) Math.ceil(defaultCharTextLayout.getAdvance());
        LineMetrics lineMetrics = renderFont.getLineMetrics(defaultCharText, frc);
        underlineAndStrike[0] = lineMetrics.getUnderlineOffset() * rowHeightCorrection;
        underlineAndStrike[1] = lineMetrics.getUnderlineThickness();
        underlineAndStrike[2] = lineMetrics.getStrikethroughOffset() * rowHeightCorrection;
        underlineAndStrike[3] = lineMetrics.getStrikethroughThickness();
        if (LOG.isLoggable(Level.FINE)) {
            FontMetrics fm = textComponent.getFontMetrics(origFont); // From original font
            LOG.fine("Orig Font=" + origFont + // NOI18N
                    "\n  " + this + ", charWidth=" + charWidth + ", textZoom=" + textZoom + // NOI18N
                    "\n  rowHeightCorrection=" + rowHeightCorrection + // NOI18N
                    ", underlineO/T=" + underlineAndStrike[0] + "/" + underlineAndStrike[1] + // NOI18N
                    ", strikethroughO/T=" + underlineAndStrike[2] + "/" + underlineAndStrike[3] + // NOI18N
                    "\n  FontMetrics (for comparison; without-RHC): fm-line-height=" + fm.getHeight() + // NOI18N
                    ", fm-ascent,descent,leading=" + fm.getAscent() + "," + fm.getDescent() + "," + fm.getLeading() + // NOI18N
                    "\n"); // NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "FontInfo creation stacktrace", new Exception()); // NOI18N
            }
        }
    }
    
    boolean updateRowHeight(TextLayout textLayout, float rowHeightCorrection) {
        return updateRowHeight(textLayout.getAscent(), textLayout.getDescent(), textLayout.getLeading(),
                rowHeightCorrection);
    }
    
    /**
     * Possibly update descent and leading checking if the line height changes.
     * This method should follow {@link #updateAscent(float) } call.
     *
     * @param textAscent ascent from a text layout with a font from this font info.
     * @param textDescent descent from a text layout with a font from this font info.
     * @param textLeading leading from a text layout with a font from this font info.
     * @param rowHeightCorrection 1.0f or a particular line height correction.
     * @return true for a line height change or false for no change.
     */
    boolean updateRowHeight(float textAscent, float textDescent, float textLeading, float rowHeightCorrection) {
        textAscent *= rowHeightCorrection;
        textDescent *= rowHeightCorrection;
        textLeading *= rowHeightCorrection;

        boolean change = (textAscent > ascent);
        if (change) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("FontInfo: Ascent change from " + ascent + " to " + textAscent + " for " + this + "\n"); // NOI18N
            }
            ascent = textAscent;
            if (ascent > ascentInt) {
                ascentInt = (int) Math.ceil(ascent);
            }
        }
        if (textDescent > descent) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("FontInfo: Descent change from " + descent + " to " + textDescent + " for " + this + "\n"); // NOI18N
            }
            descent = textDescent;
            change = true;
        }
        if (textLeading > leading) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("FontInfo: Leading change from " + leading + " to " + textLeading + " for " + this); // NOI18N
            }
            leading = textLeading;
            change = true;
        }
        if (change) {
            int rowHeightNew = ascentInt + (int) Math.ceil(descent + leading);
            if (rowHeightNew > rowHeightInt) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("FontInfo: rowHeightInt change from " + rowHeightInt + " to " + rowHeightNew + // NOI18N
                            " for " + this); // NOI18N
                }
                rowHeightInt = rowHeightNew;
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "renderFont=" + renderFont + // NOI18N
                "\n  ascent(Int)=" + ascent + "(" + ascentInt + "), descent=" + descent + ", leading=" + leading + // NOI18N
                ", rowHeightInt=" + rowHeightInt; // NOI18N
    }
    
}
