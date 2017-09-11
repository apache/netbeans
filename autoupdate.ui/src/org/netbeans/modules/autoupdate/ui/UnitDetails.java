/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Image;
import java.io.CharConversionException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitDetails extends DetailsPanel {

    private static final Logger err = Logger.getLogger("org.netbeans.modules.autoupdate.ui.UnitDetails");
    private RequestProcessor.Task unitDetailsTask = null;
    static final RequestProcessor UNIT_DETAILS_PROCESSOR = new RequestProcessor("unit-details-processor", 1, true);

    /** Creates a new instance of UnitDetails */
    public UnitDetails() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTable.class, "ACN_UnitDetails")); // NOI18N
    }

    public void setUnit(Unit u) {
        setUnit(u, null);
    }

    public void setUnit(final Unit u, Action action) {
        if (unitDetailsTask != null && !unitDetailsTask.isFinished()) {
            unitDetailsTask.cancel();
        }

        if (u == null) {
            getDetails().setText("<i>" + getBundle("UnitDetails_Category_NoDescription") + "</i>"); // NOI18N
            setTitle(null);
        } else {
            try {
                setTitle(XMLUtil.toElementContent(u.getDisplayName()));
            } catch (CharConversionException e) {
                err.log(Level.INFO, null, e);
                return;
            }
            setActionListener(action);
            setUnitText(u, getUnitText(u, false));

            if (u instanceof Unit.Update) {
                unitDetailsTask = UNIT_DETAILS_PROCESSOR.post(new Runnable() {

                    @Override
                    public void run() {
                        final StringBuilder text = getUnitText(u, true);
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                setUnitText(u, text);
                            }
                        });
                    }
                });
            }
        }
    }

    private void buildUnitText(Unit u, StringBuilder text, boolean collectDependencies) {
        if (u instanceof Unit.Available) {
            Unit.Available u1 = (Unit.Available) u;
            Image c = u1.getSourceIcon();
            Object url = c.getProperty("url", null);
            String categoryName = u1.getSourceDescription();
            text.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
            if (url instanceof URL) {
                text.append("<td><img src=\"").append(url).append("\"></img></td>");
            }
            text.append("<td></td>");
            text.append("<td>&nbsp;&nbsp;</td>");
            text.append("<td><b>").append(categoryName).append("</b></td>");
            text.append("</tr></table><br>");
        }

        if (Utilities.modulesOnly() || Utilities.showExtendedDescription()) {
            text.append("<b>").append(getBundle("UnitDetails_Plugin_CodeName")).append("</b>").append(u.updateUnit.getCodeName()); // NOI18N
            text.append("<br>");

        }
        String desc = null;
        if (u instanceof Unit.Update) {
            Unit.Update uu = ((Unit.Update) u);
            text.append("<b>").append(getBundle("UnitDetails_Plugin_InstalledVersion")).append("</b>").append(uu.getInstalledVersion()).append("<br>"); // NOI18N
            text.append("<b>").append(getBundle("UnitDetails_Plugin_AvailableVersion")).append("</b>").append(uu.getAvailableVersion()).append("<br>"); // NOI18N
            desc = getDependencies(uu, collectDependencies);
        } else {
            text.append("<b>").append(getBundle("UnitDetails_Plugin_Version")).append("</b>").append(u.getDisplayVersion()).append("<br>"); // NOI18N
        }
        if (u.getAuthor() != null && u.getAuthor().length() > 0) {
            text.append("<b>").append(getBundle("UnitDetails_Plugin_Author")).append("</b>").append(u.getAuthor()).append("<br>"); // NOI18N
        }
        if (u.getDisplayDate() != null && u.getDisplayDate().length() > 0) {
            text.append("<b>").append(getBundle("UnitDetails_Plugin_Date")).append("</b>").append(u.getDisplayDate()).append("<br>"); // NOI18N
        }
        text.append("<b>").append(getBundle("UnitDetails_Plugin_Source")).append("</b>").append(u.getSource()).append("<br>"); // NOI18N

        if (u.getHomepage() != null && u.getHomepage().length() > 0) {
            text.append("<b>").append(getBundle("UnitDetails_Plugin_Homepage")).append("</b><a href=\"").append(u.getHomepage()).append("\">").append(u.getHomepage()).append("</a><br>"); // NOI18N
        }

        if (u.getNotification() != null && u.getNotification().length() > 0) {
            text.append("<br><h3>").append(getBundle("UnitDetails_Plugin_Notification")).append("</h3>"); // NOI18N
            text.append("<font color=\"red\">"); // NOI18N
            text.append(u.getNotification());
            text.append("</font><br>");  // NOI18N
        }

        if (u.getDescription() != null && u.getDescription().length() > 0) {
            text.append("<br><h3>").append(getBundle("UnitDetails_Plugin_Description")).append("</h3>"); // NOI18N
            String description = u.getDescription();
            if(description.toLowerCase().startsWith("<html>")) {
                text.append(description.substring(6));
            } else {
                text.append(description);
            }
        }
        if (desc != null && desc.length() > 0) {
            text.append("<br><br><h4>").append(getBundle("Unit_InternalUpdates_Title")).append("</h4>"); // NOI18N
            text.append(desc);
        }
    }

    private void setUnitText(Unit u, StringBuilder text) {
        getDetails().setText(text.toString());
        setUnitHighlighing(u);
    }

    private StringBuilder getUnitText(Unit u, boolean collectDependencies) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; ; i++) {
            try {
                buildUnitText(u, text, collectDependencies);
            } catch (IllegalStateException ex) {
                if (i > 100) {
                    throw ex;
                }
                Unit.log.log(Level.INFO, "Can't compute getUnitText for " + u, ex); // NOI18N
                continue;
            }
            break;
        }
        return text;
    }

    private void setUnitHighlighing(Unit u) {
        Color highlightColor = UIManager.getColor("nb.autoupdate.search.highlight");
        highlightColor = highlightColor == null ? Color.YELLOW : highlightColor;
        final ColorHighlighter highlighter = new ColorHighlighter(getDetails(), highlightColor);

        int idx = highlighter.highlight(u.getFilter());
        getDetails().setCaretPosition(idx > 0 ? idx : 0);
    }

    private String getDependencies(Unit.Update uu, boolean collectDependencies) {
        if (!collectDependencies) {
            return "<i>" + getBundle("UnitDetails_Plugin_Collecting_Dependencies") + "</i><br>";
        }

        Unit u = uu;
        if (u instanceof Unit.CompoundUpdate) {
            Unit.CompoundUpdate cu = (Unit.CompoundUpdate) u;
            StringBuilder desc = new StringBuilder();
            for (UpdateUnit internalUnits : cu.getUpdateUnits()) {
                appendInternalUpdates(desc, internalUnits.getAvailableUpdates().get(0));
            }
            return desc.toString();
        } else {
            return "";
        }
    }

    private void appendInternalUpdates(StringBuilder desc, UpdateElement ue) {
        desc.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        desc.append(ue.getDisplayName());
        if (ue.getUpdateUnit().getInstalled() != null) {
            desc.append(" [").append(ue.getUpdateUnit().getInstalled().getSpecificationVersion()).append("->");
        } else {
            desc.append(" <span color=\"red\">").append(getBundle("UnitDetails_New_Internal_Update_Mark")).append("</span> [");
        }

        desc.append(ue.getUpdateUnit().getAvailableUpdates().get(0).getSpecificationVersion());
        desc.append("]<br>");
    }



    private static String getBundle(String key) {
        return NbBundle.getMessage(UnitDetails.class, key);
    }
}
