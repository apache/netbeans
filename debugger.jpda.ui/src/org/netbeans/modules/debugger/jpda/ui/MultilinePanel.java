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

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.Font;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel for displaying multiline labels.
 * Also a title (emphasized first line of the text) may be specified.
 * The text is parsed using <CODE>\n</CODE> as a delimiter.
 * <P>
 * The panel is built from several <TT>JLabel</TT>s, layed out vertically
 * using {@link BoxLayout}. A vertical glue ({@link Box#createVerticalGlue()})
 * is added on the bottom of the panel.
 *
 * @author  Marian Petras
 */
public final class MultilinePanel extends JPanel {
    
    /**
     * Creates a panel containing the specified text.
     *
     * @param  text  text to be displayed
     */
    public MultilinePanel(String text) {
        this(null, text);
    }
    
    /**
     * Creates a panel containing the specified title and text.
     * The title will be displayed as an emphasized (bold) first
     * line of text.
     *
     * @param  title  title of the dialog
     *                (if <CODE>null</CODE>, no title is displayed)
     * @param  text  text to be displayed
     */
    public MultilinePanel(String title, String text) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //
        if (title != null) {
            JLabel label = new JLabel(title);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            add(label);
        }
        //
        StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);  //NOI18N
        boolean lastWasNewline = true;
        for (int i = 0; tokenizer.hasMoreTokens(); ++i) {
            String line = tokenizer.nextToken();
            if ("\n".equals(line)) {   //NOI18N
                if (!lastWasNewline) {
                    lastWasNewline = true;
                    continue;   //newline after text - end of line
                }
                //two adjacent newlines - empty line
                line = " ";  //empty JLabels have zero height    //NOI18N
            }
            else {
                lastWasNewline = false;
            }
            add(new JLabel(line));
        }
        add(Box.createVerticalGlue());
    }
    
}
