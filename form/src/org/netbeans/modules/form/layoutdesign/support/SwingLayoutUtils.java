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

package org.netbeans.modules.form.layoutdesign.support;

import java.awt.Component;
import java.util.*;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;

/**
 * Utilities for swing layout support.
 *
 * @author Jan Stola
 */
public class SwingLayoutUtils {
    /** The default resizability of the component is not known. */
    public static final int STATUS_UNKNOWN = -1;
    /** The component is not resizable by default. */
    public static final int STATUS_NON_RESIZABLE = 0;
    /** The component is resizable by default. */
    public static final int STATUS_RESIZABLE = 1;

    /**
     * Contains class names of non-resizable components e.g.
     * components that are non-resizable unless one (or more) of
     * minimumSize, preferredSize or maximumSize properties is changed.
     */
    private static Set<String> nonResizableComponents = new HashSet<String>();
    static {
        nonResizableComponents.addAll(
            Arrays.asList(new String[] {
                "javax.swing.JLabel", // NOI18N
                "javax.swing.JButton", // NOI18N
                "javax.swing.JToggleButton", // NOI18N
                "javax.swing.JCheckBox", // NOI18N
                "javax.swing.JRadioButton", // NOI18N
                "javax.swing.JList", // NOI18N
            })
        );
    }

    /**
     * Contains class names of resizable components e.g.
     * components that are resizable unless one (or more) of
     * minimumSize, preferredSize or maximumSize properties is changed.
     */
    private static Set<String> resizableComponents = new HashSet<String>();
    static {
        resizableComponents.addAll(
            Arrays.asList(new String[] {
                "javax.swing.JComboBox", // NOI18N
                "javax.swing.JTextField", // NOI18N
                "javax.swing.JTextArea", // NOI18N
                "javax.swing.JTabbedPane", // NOI18N
                "javax.swing.JScrollPane", // NOI18N
                "javax.swing.JSplitPane", // NOI18N
                "javax.swing.JFormattedTextField", // NOI18N
                "javax.swing.JPasswordField", // NOI18N
                "javax.swing.JSpinner", // NOI18N
                "javax.swing.JSeparator", // NOI18N
                "javax.swing.JTextPane", // NOI18N
                "javax.swing.JEditorPane", // NOI18N
                "javax.swing.JInternalFrame", // NOI18N
                "javax.swing.JLayeredPane", // NOI18N
                "javax.swing.JDesktopPane" // NOI18N
            })
        );
    }

    /**
     * Determines whether the given class represents component
     * that is resizable (by default) or not.
     *
     * @param componentClass <code>Class</code> object corresponding
     * to component we are interested in.
     * @return <code>STATUS_RESIZABLE</code>, <code>STATUS_NON_RESIZABLE</code>
     * or <code>STATUS_UNKNOWN</code>.
     */
    public static int getResizableStatus(Component component) {
        int status = getSpecialStatus(component);
        if (status == STATUS_UNKNOWN) {
            String className = component.getClass().getName();
            if (resizableComponents.contains(className)) {
                status = STATUS_RESIZABLE;
            } else if (nonResizableComponents.contains(className)) {
                status = STATUS_NON_RESIZABLE;
            }
        }
        return status;
    }

    private static int getSpecialStatus(Component component) {
        // hack: labels and buttons with html text are resizable (#73255)
        String htmlText = null;
        if (component instanceof JLabel) {
            htmlText = ((JLabel)component).getText();
        } else if (component instanceof AbstractButton) {
            htmlText = ((AbstractButton)component).getText();
        }
        if (htmlText != null && htmlText.length() >= 6 && htmlText.trim().toLowerCase().startsWith("<html>")) { // NOI18N
            return STATUS_RESIZABLE;
        }
        return STATUS_UNKNOWN;
    }

    public static Map<Integer,List<String>> createLinkSizeGroups(LayoutComponent layoutComponent, int dimension) {
        
        Map<Integer,List<String>> linkSizeGroup = new TreeMap<Integer,List<String>>();
        
        if (layoutComponent.isLayoutContainer()) {
            for (LayoutComponent lc : layoutComponent.getSubcomponents()) {
                if (lc != null) {
                    if (lc.isLinkSized(dimension)) {
                        String cid = lc.getId();
                        Integer id = new Integer(lc.getLinkSizeId(dimension));
                        List<String> l = linkSizeGroup.get(id);
                        if (l == null) {
                            l = new ArrayList<String>();
                            l.add(cid);
                            linkSizeGroup.put(id, l);
                        } else {
                            l.add(cid);
                        }
                    }
                }
            }
        }
        return linkSizeGroup;
    }

    /**
     * GroupLayout does not like the default ending container gap resizing if
     * there is some other interval in parallel aligned at the trailing edge
     * with it (or resizing), having smaller min size. It fails to compute the
     * correct group size then. Simplest case of two buttons:
     *   par(jButton1_resizing_or_trailing,
     *       seq(jButton2_fixed, default_cont_gap_resizing))
     * It leads to incorrect (bigger) size of the default container gap which
     * may show up either as jButton1_resizing_or_trailing goes several pixels
     * out of the container boundaries, or the gap is simply bigger then it
     * should be (if the growth can be compensated somewhere left of the
     * parallel group).
     * @param seq the sequence to check
     * @return the undesirable resizing container gap at the end of the sequence
     *         that should not be used, null if everything's ok
     */
    public static LayoutInterval getUnsupportedResizingContainerGap(LayoutInterval seq) {
        int count = seq.getSubIntervalCount();
        if (!seq.isSequential() || count <= 1 || seq.getParent() == null) {
            return null; // not even a valid sequence (basic check)
        }
        LayoutInterval gap = seq.getSubInterval(count-1);
        if (gap.getPreferredSize() != LayoutConstants.NOT_EXPLICITLY_DEFINED || gap.getMaximumSize() != Short.MAX_VALUE
                || LayoutInterval.getNeighbor(seq, LayoutConstants.TRAILING, false, true, false) != null) {
            return null; // not a resizing container gap
        }
        LayoutInterval otherGap = seq.getSubInterval(0);
        if (otherGap.isEmptySpace() && LayoutInterval.wantResize(otherGap)) {
            return null; // there's a leading gap resizing - not a problem
        }
        // search for parallel sibling aligned at trailing edge with the gap
        boolean parallelSibling = false;
        for (Iterator<LayoutInterval> it=seq.getParent().getSubIntervals(); it.hasNext(); ) {
            LayoutInterval sub = it.next();
            if (sub != seq && LayoutInterval.isAlignedAtBorder(sub, LayoutConstants.TRAILING)
                    && (!sub.isSequential() || (sub.getSubIntervalCount() > 0 && !sub.getSubInterval(sub.getSubIntervalCount()-1).isEmptySpace()))) {
                parallelSibling = true;
                break;
            }
        }
        if (!parallelSibling) {
            return null;
        }
        // check if there is anything left of the whole parallel group
        LayoutInterval neighbor = LayoutInterval.getNeighbor(seq.getParent(), LayoutConstants.LEADING, false, true, false);
        if (neighbor == null
                || (neighbor.isEmptySpace() && !LayoutInterval.canResize(neighbor)
                    && neighbor.getPreferredSize() == LayoutConstants.NOT_EXPLICITLY_DEFINED
                    && LayoutInterval.getNeighbor(neighbor, LayoutConstants.LEADING, false, true, false) == null)) {
            return null; // nothing or just a fixed default container gap next to the group, not a problem
        }

        return gap;
    }
}
