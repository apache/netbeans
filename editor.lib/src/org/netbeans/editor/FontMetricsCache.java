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

package org.netbeans.editor;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.util.HashMap;

/** Static cache that holds the font metrics for the fonts.
* This can generally speed up drawing if the metrics are not cached
* directly by the system.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class FontMetricsCache {

    private static HashMap font2FM = new HashMap();

    private static HashMap font2Info = new HashMap();

    /** Get the font-metrics for the given font.
    * @param font font for which the metrics is being retrieved.
    * @param c component that is used to retrieve the metrics in case it's
    *   not yet in the cache.
    */
    public static synchronized FontMetrics getFontMetrics(Font f, Component c) {
        Object fm = font2FM.get(f);
        if (fm == null) {
            fm = c.getFontMetrics(f);
            font2FM.put(f, fm);
        }
        return (FontMetrics)fm;
    }

    /** Get the font-metrics for the given font.
    * @param font font for which the metrics is being retrieved.
    * @param g graphics that is used to retrieve the metrics in case it's
    *   not yet in the cache.
    */
    public static synchronized FontMetrics getFontMetrics(Font f, Graphics g) {
        Object fm = font2FM.get(f);
        if (fm == null) {
            fm = g.getFontMetrics(f);
            font2FM.put(f, fm);
        }
        return (FontMetrics)fm;
    }

    /** Get the info about the space-width and strike-through and underline
    * constants.
    * @param font font for which the info is being retrieved.
    */
    public static synchronized Info getInfo(Font f) {
        Info info = (Info)font2Info.get(f);
        if (info == null) {
            info = new InfoImpl(f);
            font2Info.put(f, info);
        }
        return info;
    }

    /** Clear all the metrics from the cache. It's usually done
    * when any of the editor ui is being garbage collected to
    * ensure there will be no more unused metrics.
    */
    public static synchronized void clear() {
        font2FM.clear();
        font2Info.clear();
    }

    public interface Info {

        /** Returns the width of the space character */
        public int getSpaceWidth(Graphics g);

        /** Returns the width of the space character */
        public int getSpaceWidth(Component c);

        /** Returns the position of the strike-through line
        * relative to the baseline.
        */
        public float getStrikethroughOffset(Graphics g);

        /** Returns the position of the strike-through line
        * relative to the baseline.
        */
        public float getStrikethroughOffset(Component c);

        /** Returns the thickness of the strike-through line. */
        public float getStrikethroughThickness(Graphics g);

        /** Returns the thickness of the strike-through line. */
        public float getStrikethroughThickness(Component c);

        /** Returns the position of the underline relative to the baseline. */
        public float getUnderlineOffset(Graphics g);

        /** Returns the position of the underline relative to the baseline. */
        public float getUnderlineOffset(Component c);

        /** Returns the thickness of the underline. */
        public float getUnderlineThickness(Graphics g);

        /** Returns the thickness of the underline. */
        public float getUnderlineThickness(Component c);

    }

    private static class InfoImpl implements Info {

        private static final int SW_INITED = 1;
        private static final int ST_INITED = 2;
        private static final int UL_INITED = 4;

        private Font font;

        private int inited;

        private int spaceWidth;

        private float strikethroughOffset;

        private float strikethroughThickness;

        private float underlineOffset;

        private float underlineThickness;

        InfoImpl(Font font) {
            this.font = font;
        }

        private synchronized void initSpaceWidth(Graphics g, Component c) {
            FontMetrics fm = (g != null) ? getFontMetrics(font, g) : getFontMetrics(font, c);
            spaceWidth = fm.stringWidth(" "); // NOI18N
            if (spaceWidth <= 0) {
                spaceWidth = fm.stringWidth("A") / 3; // NOI18N
            }
            inited |= SW_INITED;
        }

        private synchronized void initStrikethrough(Graphics g) {
            LineMetrics lm = font.getLineMetrics("aAyY", ((Graphics2D)g).getFontRenderContext()); // NOI18N
            strikethroughOffset = lm.getStrikethroughOffset();
            strikethroughThickness = lm.getStrikethroughThickness();
            inited |= ST_INITED;
        }

        private synchronized void initUnderline(Graphics g) {
            LineMetrics lm = font.getLineMetrics("aAyY", ((Graphics2D)g).getFontRenderContext()); // NOI18N
            underlineOffset = lm.getUnderlineOffset();
            underlineThickness = lm.getUnderlineThickness();
            inited |= UL_INITED;
        }

        /** Returns the width of the space character */
        public int getSpaceWidth(Graphics g) {
            if ((inited & SW_INITED) == 0) {
                initSpaceWidth(g, null);
            }

            return spaceWidth;
        }

        /** Returns the width of the space character */
        public int getSpaceWidth(Component c) {
            if ((inited & SW_INITED) == 0) {
                initSpaceWidth(null, c);
            }

            return spaceWidth;
        }

        /** Returns the position of the strike-through line
        * relative to the baseline.
        */
        public float getStrikethroughOffset(Graphics g) {
            if ((inited & ST_INITED) == 0) {
                initStrikethrough(g);
            }

            return strikethroughOffset;
        }

        /** Returns the position of the strike-through line
        * relative to the baseline.
        */
        public float getStrikethroughOffset(Component c) {
            if ((inited & ST_INITED) == 0) {
                initStrikethrough(c.getGraphics());
            }

            return strikethroughOffset;
        }

        /** Returns the thickness of the strike-through line. */
        public float getStrikethroughThickness(Graphics g) {
            if ((inited & ST_INITED) == 0) {
                initStrikethrough(g);
            }

            return strikethroughThickness;
        }

        /** Returns the thickness of the strike-through line. */
        public float getStrikethroughThickness(Component c) {
            if ((inited & ST_INITED) == 0) {
                initStrikethrough(c.getGraphics());
            }

            return strikethroughThickness;
        }

        /** Returns the position of the underline relative to the baseline. */
        public float getUnderlineOffset(Graphics g) {
            if ((inited & UL_INITED) == 0) {
                initUnderline(g);
            }

            return underlineOffset;
        }

        /** Returns the position of the underline relative to the baseline. */
        public float getUnderlineOffset(Component c) {
            if ((inited & UL_INITED) == 0) {
                initUnderline(c.getGraphics());
            }

            return underlineOffset;
        }

        /** Returns the thickness of the underline. */
        public float getUnderlineThickness(Graphics g) {
            if ((inited & UL_INITED) == 0) {
                initUnderline(g);
            }

            return underlineThickness;
        }

        /** Returns the thickness of the underline. */
        public float getUnderlineThickness(Component c) {
            if ((inited & UL_INITED) == 0) {
                initUnderline(c.getGraphics());
            }

            return underlineThickness;
        }

    }


}
