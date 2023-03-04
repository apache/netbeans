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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Command Evaluator. It evaluates commands from toolbar and creates results.
 * 
 * @author Jan Becicka, Dafe Simonek
 */
public class CommandEvaluator {
    
    static final String RECENT = "Recent";                              //NOI18N
    private static final String PROP_ENABLED_CATEGORIES
            = "enabledCategories";                                      //NOI18N
    
    /**
     * command pattern is:
     * "command arguments"
     */
    private static final Pattern COMMAND_PATTERN = Pattern.compile("(\\w+)(\\s+)(.+)"); //NOI18N

    private static final RequestProcessor RP = new RequestProcessor("QuickSearch Command Evaluator", 10); // NOI18N
    /**
     * Narrow evaluation to specified set of categories.
     */
    private static Set<ProviderModel.Category> evalCats = loadEvalCats();

    /**
     * Temporarily narrow evaluation to specified category.
     */
    private static Category temporaryCat = null;
    
    /**
     * Runs evaluation.
     *
     * @param command text to evauate, to search for
     *
     * @return task of this evaluation, which waits for all providers to complete
     * execution. Use returned instance to recognize if this evaluation still
     * runs and when it actually will finish.
     */
    public static org.openide.util.Task evaluate (String command, ResultsModel model) {
        List<CategoryResult> l = new ArrayList<CategoryResult>();
        String[] commands = parseCommand(command);
        SearchRequest sRequest = Accessor.DEFAULT.createRequest(commands[1], null);
        List<Task> tasks = new ArrayList<Task>();

        List<Category> provCats = new ArrayList<Category>();
        boolean allResults = getProviderCategories(commands, provCats);

        for (ProviderModel.Category curCat : provCats) {
            CategoryResult catResult = new CategoryResult(curCat, allResults);
            SearchResponse sResponse = Accessor.DEFAULT.createResponse(catResult, sRequest);
            for (SearchProvider provider : curCat.getProviders()) {
                Task t = runEvaluation(provider, sRequest, sResponse, curCat);
                if (t != null) {
                    tasks.add(t);
                }
            }
            l.add(catResult);
        }

        model.setContent(l);

        return new Wait4AllTask(tasks);
    }

    private static Set<Category> loadEvalCats() {
        final Set<Category> cats = new LinkedHashSet<Category>(
                ProviderModel.getInstance().getCategories());
        RP.post(new Runnable() {
            @Override
            public void run() {
                String ec = NbPreferences.forModule(CommandEvaluator.class).get(
                        PROP_ENABLED_CATEGORIES, null);
                if (ec != null) {
                    Set<String> categoryNames = new LinkedHashSet<String>();
                    categoryNames.addAll(Arrays.asList(ec.split(":"))); //NOI18N
                    Iterator<Category> iterator = cats.iterator();
                    while (iterator.hasNext()) {
                        Category category = iterator.next();
                        if (!categoryNames.contains(category.getName())
                                && !RECENT.equals(category.getName())) {
                            iterator.remove();
                        }
                    }
                }
            }
        });
        return cats;
    }

    private static void storeEvalCats() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (Category category : evalCats) {
                    if (!RECENT.equals(category.getName())) {
                        sb.append(category.getName());
                        sb.append(':');
                    }
                }
                NbPreferences.forModule(CommandEvaluator.class).put(
                        PROP_ENABLED_CATEGORIES, sb.toString());
            }
        });
    }

    public static Set<Category> getEvalCats () {
        return evalCats;
    }

    public static void setEvalCats(Set<Category> cat) {
        CommandEvaluator.evalCats = (cat == null)
                ? new LinkedHashSet<Category>(ProviderModel.getInstance()
                .getCategories())
                : cat;
        storeEvalCats();
    }

    public static void dropTemporaryCat() {
        temporaryCat = null;
    }

    public static void setTemporaryCat(Category temporaryCat) {
        CommandEvaluator.temporaryCat = temporaryCat;
    }

    public static boolean isTemporaryCatSpecified() {
        return temporaryCat != null;
    }

    public static Category getTemporaryCat() {
        return temporaryCat;
    }

    private static String[] parseCommand (String command) {
        String[] results = new String[2];

        Matcher m = COMMAND_PATTERN.matcher(command);

        if (m.matches()) {
            results[0] = m.group(1);
            if (ProviderModel.getInstance().isKnownCommand(results[0])) {
                results[1] = m.group(3);
            } else {
                results[0] = null;
                results[1] = command;
            }
        } else {
            results[1] = command;
        }
                
        return results;
    }

    /** Returns array of providers to ask for evaluation according to
     * current evaluation rules.
     *
     * @return true if providers are expected to return all results, false otherwise
     */
    static boolean getProviderCategories (String[] commands, List<Category> result) {
        List<Category> cats = ProviderModel.getInstance().getCategories();

        // always include recent searches
        for (Category cat : cats) {
            if (RECENT.equals(cat.getName())) {
                result.add(cat);
            }
        }

        // skip all but recent if empty string came
        if (commands[1] == null || commands[1].trim().equals("")) {
            return false;
        }

        // command string has biggest priority for narrow evaluation to category
        if (commands[0] != null) {
            for (Category curCat : cats) {
                String commandPrefix = curCat.getCommandPrefix();
                if (commandPrefix != null && commandPrefix.equalsIgnoreCase(commands[0])) {
                    result.add(curCat);
                    return true;
                }
            }
        }

        //evaluation narrowed to category perhaps?
        if (temporaryCat != null) {
            result.add(temporaryCat);
            return true;
        }

        //no narrowing
        for (Category c : evalCats) {
            if (!RECENT.equals(c.getName())) { //already present
                result.add(c);
            }
        }
        return result.size() < 3; // recent searches + one selected category
    }

    private static Task runEvaluation (final SearchProvider provider, final SearchRequest request,
                                final SearchResponse response, final ProviderModel.Category cat) {
        return RP.post(new Runnable() {
            @Override
            public void run() {
                provider.evaluate(request, response);
            }
        });
    }

    /** Task implementation that computes nothing itself, it just waits
     * for all given RequestProcessor tasks to finish and then it finishes as well.
     */
    private static class Wait4AllTask extends org.openide.util.Task implements Runnable {

        private static final long TIMEOUT = 60000;

        private final List<Task> tasks;

        private Wait4AllTask (List<Task> tasks) {
            super();
            this.tasks = tasks;
        }

        @Override
        public void run () {
            try {
                notifyRunning();
                for (Task task : tasks) {
                    try {
                        // wait no longer then one minute
                        task.waitFinished(TIMEOUT);
                    } catch (InterruptedException ex) {
                        // ignore, we are not interested
                    }
                }
            } finally {
                notifyFinished();
            }
        }
    }

}
