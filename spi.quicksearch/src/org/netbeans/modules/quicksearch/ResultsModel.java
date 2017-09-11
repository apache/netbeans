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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.quicksearch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractListModel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.openide.xml.XMLUtil;

/**
 * Model of search results. Works as ListModel for JList which is displaying
 * results. Actual results data are stored in List of CategoryResult objects.
 *
 * As model changes can come very frequently, firing of changes is coalesced.
 * Coalescing of changes helps UI to reduce flicker and unnecessary updates.
 *
 * @author Jan Becicka
 */
public final class ResultsModel extends AbstractListModel implements ActionListener {

    private static ResultsModel instance;

    private List<? extends CategoryResult> results;

    /* Timer for coalescing fast coming changes of model */
    private Timer fireTimer;

    /** Amount of time during which model has to be unchanged in order to fire
     * changes to listeners. */
    static final int COALESCE_TIME = 200;

    /** Singleton */
    private ResultsModel () {
    }

    public static ResultsModel getInstance () {
        if (instance == null) {
            instance = new ResultsModel();
        }
        return instance;
    }

    public void setContent (List<? extends CategoryResult> categories) {
        List<? extends CategoryResult> oldRes = this.results;
        this.results = categories;

        if (oldRes != null) {
            for (CategoryResult cr : oldRes) {
                cr.setObsolete(true);
            }
        }

        maybeFireChanges();
    }

    public List<? extends CategoryResult> getContent () {
        return results;
    }

    /******* AbstractListModel impl ********/

    public int getSize() {
        if (results == null) {
            return 0;
        }
        int size = 0;
        for (CategoryResult cr : results) {
            size += cr.getItems().size();
        }
        return size;
    }

    public Object getElementAt (int index) {
        if (results == null) {
            return null;
        }
        // TBD - should probably throw AIOOBE if invalid index is on input
        int catIndex = index;
        int catSize = 0;
        List<ItemResult> catItems = null;
        for (CategoryResult cr : results) {
            catItems = cr.getItems();
            catSize = catItems.size();
            if (catIndex < catSize) {
                return catIndex >= 0 ? catItems.get(catIndex) : null;
            }
            catIndex -= catSize;
        }
        return null;
    }

    public static final class ItemResult {

        private static final String HTML = "<html>";

        private CategoryResult category;
        private Runnable action;
        private String displayName;
        private List<? extends KeyStroke> shortcut;
        private String displayHint;

        private Date date; //time of last access, used for recent searches

        public ItemResult (CategoryResult category, SearchRequest sRequest,
                Runnable action, String displayName) {
            this(category, sRequest, action, displayName, null, null);
        }

        public ItemResult (CategoryResult category, Runnable action, String displayName, Date date) {
            this(category, null, action, displayName, null, null);
            this.date = date;
        }

        public ItemResult (CategoryResult category, SearchRequest sRequest, 
                Runnable action, String displayName, List<? extends KeyStroke> shortcut,
                String displayHint) {
            this.category = category;
            this.action = action;
            this.displayName = sRequest != null ?
                highlightSubstring(displayName, sRequest) : displayName;
            this.shortcut = shortcut;
            this.displayHint = displayHint;
        }

        public Runnable getAction() {
            return action;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getDisplayName () {
            return displayName;
        }

        public String getDisplayHint() {
            return displayHint;
        }

        public List<? extends KeyStroke> getShortcut() {
            return shortcut;
        }

        public CategoryResult getCategory() {
            return category;
        }

        private String highlightSubstring (String text, SearchRequest sRequest) {
            if (text.startsWith(HTML)) {
                // provider handles highliting itself, okay
                return text;
            }
            // try to find substring
            String searchedText = sRequest.getText();
            int index = text.toLowerCase(Locale.ENGLISH).indexOf(searchedText.toLowerCase(Locale.ENGLISH));
            if (index == -1) {
                return HTML + safeEscape(text);
            }
            // found, bold it
            int endIndex = index + searchedText.length();
            StringBuilder sb = new StringBuilder(HTML);
            if (index > 0) {
                sb.append(safeEscape(text.substring(0, index)));
            }
            sb.append("<b>");
            sb.append(safeEscape(text.substring(index, endIndex)));
            sb.append("</b>");
            if (endIndex < text.length()) {
                sb.append(safeEscape(text.substring(endIndex, text.length())));
            }
            return sb.toString();
        }

        // XXX need API in XMLUtil
        private static String safeEscape(String raw) {
            try {
                return XMLUtil.toElementContent(raw);
            } catch (CharConversionException x) {
                return raw;
            }
        }

    }

    void categoryChanged (CategoryResult cr) {
        // fire change only if category is contained in model
        if (results != null && results.contains(cr)) {
            maybeFireChanges();
        }
    }

    private void maybeFireChanges () {
        if (fireTimer == null) {
            fireTimer = new Timer(COALESCE_TIME, this);
        }
        if (!fireTimer.isRunning()) {
            // first change in possible flurry, start timer
            fireTimer.start();
        } else {
            // model change came too fast, let's wait until providers calm down :)
            fireTimer.restart();
        }
    }

    public void actionPerformed(ActionEvent e) {
        fireTimer.stop();
        fireContentsChanged(this, 0, getSize());
    }

}
