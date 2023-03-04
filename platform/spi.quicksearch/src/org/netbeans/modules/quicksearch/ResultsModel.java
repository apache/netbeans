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
                sb.append(safeEscape(text.substring(endIndex)));
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
