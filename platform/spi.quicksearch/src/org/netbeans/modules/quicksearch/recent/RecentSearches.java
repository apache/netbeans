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

package org.netbeans.modules.quicksearch.recent;

import java.awt.EventQueue;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.quicksearch.CategoryResult;
import org.netbeans.modules.quicksearch.CommandEvaluator;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.modules.quicksearch.ResultsModel;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Recent Searches items storage and its persistance
 *
 * @author Jan Becicka
 * @author Max Sauer
 */
public class RecentSearches {
    
    private static final int MAX_ITEMS = 5;
    private static final long FIVE_DAYS = 86400000 * 5;

    private LinkedList<ItemResult> recent;
    private static RecentSearches instance;
    private static final char dateSep = ':';
    private static final Pattern RECENT_PREFS_PATTERN =
            Pattern.compile("^(.*):(\\d{8,})(?::(.*))?$");              //NOI18N
    private static RequestProcessor RP =
            new RequestProcessor(RecentSearches.class.getName(), 2);

    private RecentSearches() {
        recent = new LinkedList<ItemResult>();
        readRecentFromPrefs(); //read recent searhces from preferences
    }
    
    public static RecentSearches getDefault() {
        if (instance==null) {
            instance = new RecentSearches();
        }
        return instance;
    } 
    
    public synchronized void add(ItemResult result) {
        Date now = new GregorianCalendar().getTime();

        // don't create duplicates, however poor-man's test only
        for (ItemResult ir : recent) {
            if (stripHTMLnames(ir.getDisplayName()).equals(
                    stripHTMLnames(result.getDisplayName()))) {
                ir.setDate(now);
                return;
            }
        }

        // ugly hack to not include special Maven setup search item
        if ("SearchSetup".equals(result.getAction().getClass().getSimpleName())) {
            return;
        }
        
        if (recent.size()>=MAX_ITEMS) {
            recent.removeLast();
        }
        result.setDate(now);
        recent.addFirst(result);
        storeRecentToPrefs();
    }
    
    public synchronized List<ItemResult> getSearches() {
        LinkedList<ItemResult> fiveDayList = new LinkedList<ItemResult>();
        for (ItemResult ir : recent) {
            if ((new GregorianCalendar().getTime().getTime() - ir.getDate().getTime()) < FIVE_DAYS)
                fiveDayList.add(ir);
        }
        //provide only recent searches newer than five days
        return fiveDayList;
    }

    /**
     * Clears the list of recent searches.
     */
    public synchronized void clear() {
        recent.clear();
        RP.post(new Runnable() {

            @Override
            public void run() {
                storeRecentToPrefs();
            }
        });
    }

    //preferences
    private static final String RECENT_SEARCHES = "recentSearches"; // NOI18N

    private Preferences prefs() {
        return NbPreferences.forModule(RecentSearches.class);
    }
    
    private void storeRecentToPrefs() {
        Iterator<ItemResult> it = recent.iterator();
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (it.hasNext()) {
                ItemResult td = it.next();
                CategoryResult cr = td.getCategory();
                Category category = cr == null ? null : cr.getCategory();
                String categoryName = category == null ? null
                        : stripHTMLnames(category.getDisplayName());
                prefs().put(RECENT_SEARCHES + i, stripHTMLnames(td.getDisplayName())
                        + dateSep + td.getDate().getTime()
                        + (categoryName == null ? "" : dateSep + categoryName));
            } else {
                prefs().put(RECENT_SEARCHES + i, "");
            }
        }
    }

    private void readRecentFromPrefs() {
        for (int i = 0; i < MAX_ITEMS; i++) {
            String item = prefs().get(RECENT_SEARCHES + i, "");
            Matcher m = RECENT_PREFS_PATTERN.matcher(item);
            if (m.find()) {
                try {
                    final String name = m.group(1);
                    final long time = Long.parseLong(m.group(2));
                    final String categ = m.group(3);
                    ItemResult incomplete = new ItemResult(null,
                            new FakeAction(name, categ), name, new Date(time));
                    recent.add(incomplete);
                } catch (NumberFormatException nfe) {
                    Logger l = Logger.getLogger(RecentSearches.class.getName());
                    l.log(Level.INFO, "Failed to read recent searches", item);
                }
            }
        }
    }

    /**
     * Lazy initied action used for recent searches
     * In order to not init all recent searched item
     */
    public final class FakeAction implements Runnable {

        private String name; //display name to search for
        private String category;
        private Runnable action; //remembered action

        private FakeAction(String name, String category) {
            this.name = name;
            this.category = category;
        }

        @Override
        public void run() {
            if (action == null || action instanceof FakeAction) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        findAndRunAction();
                    }
                });
            } else {
                action.run();
            }
        }

        /**
         * Find action by display name and run it.
         */
        @NbBundle.Messages({
            "LBL_SearchingRecentResult=Searching for a Quick Search Item",
            "MSG_RecentResultNotFound=Recent Quick Search Item was not found."})
        private void findAndRunAction() {
            final AtomicBoolean cancelled = new AtomicBoolean(false);
            ProgressHandle handle = ProgressHandle.createHandle(
                    Bundle.LBL_SearchingRecentResult(), new Cancellable() {
                @Override
                public boolean cancel() {
                    cancelled.set(true);
                    return true;
                }
            });
            handle.start();
            ResultsModel model = ResultsModel.getInstance();
            Task evaluate = CommandEvaluator.evaluate(
                    stripHTMLandPackageNames(name), model);
            RP.post(evaluate);
            int tries = 0;
            boolean found = false;
            while (tries++ < 30 && !cancelled.get()) {
                if (checkActionWasFound(model, true)) {
                    found = true;
                    break;
                } else if (evaluate.isFinished()) {
                    found = checkActionWasFound(model, false);
                    break;
                }
            }
            handle.finish();
            if (!found && !cancelled.get()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        Bundle.MSG_RecentResultNotFound(),
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }

        /**
         * Check if the correct action was found, and invoke it if so.
         *
         * @param model Model containing current search results.
         * @param wait Wait one second before testing the results. It is useful
         * if the search is still in progress.
         */
        private boolean checkActionWasFound(ResultsModel model, boolean wait) {
            if (wait) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            int rSize = model.getSize();
            for (int j = 0; j < rSize; j++) {
                ItemResult res = (ItemResult) model.getElementAt(j);
                if (nameMatches(res) && categoryMatches(res)) {
                    action = res.getAction();
                    if (!(action instanceof FakeAction)) {
                        EventQueue.invokeLater(action);
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean nameMatches(ItemResult res) {
            return stripHTMLnames(res.getDisplayName()).equals(
                    stripHTMLnames(name));
        }

        private boolean categoryMatches(ItemResult res) {
            if (category == null) {
                return true;
            } else if (res.getCategory() == null
                    || res.getCategory().getCategory() == null) {
                return false;
            } else {
                return stripHTMLnames(
                        res.getCategory().getCategory().getDisplayName()).equals(
                        stripHTMLnames(category));
            }
        }

        private String stripHTMLandPackageNames(String s) {
            s = stripHTMLnames(s);
            return s.replaceAll("\\(.*\\)", "").trim();
        }
    }

    private String stripHTMLnames(String s) {
        return translateHTMLEntities(s.replaceAll("<.*?>", "")).trim(); //NOI18N
    }

    /**
     * Convert HTML entities to plain characters. For efficiency and dependency
     * reasons support only entities that are known to appear in the search
     * results.
     */
    private String translateHTMLEntities(String s) {
        return s.replace("&amp;", "&");                            //NOI18N
    }
}
