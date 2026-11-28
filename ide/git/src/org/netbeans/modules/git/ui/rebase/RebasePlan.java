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

package org.netbeans.modules.git.ui.rebase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;

/**
 * The plan of an interactive rebase, the way the user settled it in the
 * rebase dialog: the commits in the order of the plan (oldest first), the
 * action of each commit, and the messages the user settled for the reworded
 * commits and for the runs of squashed commits.
 *
 * <p>The plan is persisted into the rebase folder when the rebase starts,
 * so that the settled messages survive the stops of the rebase: when an
 * interrupted rebase is continued, a handler built from the persisted plan
 * is attached to the driver, and the settled messages replace the commit
 * messages the driver would compose.
 *
 * @author mbien
 */
final class RebasePlan {

    /** The name of the plan file in the rebase folder. */
    static final String FILE = "rebase-plan.txt"; // NOI18N

    private final List<Row> rows;

    /**
     * The row of the plan: a commit in the order of the plan and the
     * messages the user settled for it (the message of a reworded commit)
     * and for the run of squashed or fixup-ed rows the commit absorbs (the
     * message of the squashed run, if the row settles one).
     *
     * @param id the id of the commit
     * @param action the action of the commit in the plan
     * @param originalMessage the original message of the commit
     * @param settledMessage the message the user settled for a reworded
     *        commit, or <code>null</code> if the commit keeps its original
     *        message
     * @param settledRunMessage the message the user settled for the run of
     *        squashed rows the row absorbs, or <code>null</code> if the run
     *        keeps the combined message of the driver
     */
    static record Row(String id, RebaseAction action, String originalMessage, String settledMessage, String settledRunMessage) {}

    /**
     * A run of squashed commits the driver combines into a single commit:
     * the size of the run (the head and the absorbed rows), the message of
     * the head commit, the original messages of the squashed rows, and the
     * message the user settled for the run.
     */
    static record SquashRun(int size, String headMessage, List<String> squashMessages, String settledMessage) {

        /**
         * Whether the message is the combined message of the run, as the
         * driver composes it in the message file of the rebase state: the
         * head message and the messages of the squashed rows are part of it
         * unchanged, the count of the combined commits is the size of the
         * run. The comment character of the driver's lines is not known
         * here (the driver picks one), it is not matched.
         */
        boolean matches(String combined) {
            if (!combined.contains("This is a combination of " + size + " commits")) { // NOI18N
                return false;
            }
            if (!combined.contains(headMessage)) {
                return false;
            }
            for (String message : squashMessages) {
                if (!combined.contains(message)) {
                    return false;
                }
            }
            return true;
        }
    }

    private RebasePlan(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * The plan of the rows, in the order of the plan (oldest first).
     */
    static RebasePlan of(List<Row> rows) {
        return new RebasePlan(new ArrayList<>(rows));
    }

    List<Row> getRows() {
        return rows;
    }

    /**
     * The settled messages of the reworded commits, keyed by the original
     * full message of the commit: the driver invokes the
     * <code>modifyCommitMessage</code> callback with the original message
     * of the commit it rewords, the settled message replaces it.
     */
    Map<String, String> rewordMessages() {
        Map<String, String> map = new HashMap<>();
        for (Row row : rows) {
            if (row.settledMessage() != null) {
                map.put(row.originalMessage(), row.settledMessage());
            }
        }
        return map;
    }

    /**
     * The runs of squashed commits the driver combines, each with the
     * message the user settled for the run, in the order of the plan.
     */
    List<SquashRun> squashRuns() {
        List<SquashRun> runs = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Row head = rows.get(i);
            if (head.settledRunMessage() == null) {
                continue;
            }
            // the rows the head absorbs: the squashed or fixup-ed rows after
            // it, up to the next row that survives on the branch
            int end = i;
            boolean hasSquash = false;
            for (int j = i + 1; j < rows.size(); j++) {
                RebaseAction action = rows.get(j).action();
                if (action != RebaseAction.SQUASH && action != RebaseAction.FIXUP) {
                    break;
                }
                end = j;
                if (action == RebaseAction.SQUASH) {
                    hasSquash = true;
                }
            }
            if (!hasSquash) {
                continue;
            }
            List<String> squashMessages = new ArrayList<>();
            for (int j = i + 1; j <= end; j++) {
                if (rows.get(j).action() == RebaseAction.SQUASH) {
                    squashMessages.add(rows.get(j).originalMessage());
                }
            }
            // the message of the head commit the driver combines with the
            // messages of the run: the message the user settled for a
            // reworded head, the original message otherwise
            String headMessage = head.settledMessage() != null
                    ? head.settledMessage() : head.originalMessage();
            runs.add(new SquashRun(end - i + 1, headMessage, squashMessages, head.settledRunMessage()));
        }
        return runs;
    }

    /**
     * Persists the plan into the rebase folder, if the plan file is not
     * there yet: the driver creates the folder when the rebase starts,
     * before it invokes the <code>prepareSteps</code> callback.
     */
    void persistIfAbsent(File folder) {
        File file = new File(folder, FILE);
        if (file.exists()) {
            return;
        }
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            for (Row row : rows) {
                writer.write(row.action().name());
                writer.write('\t');
                writer.write(row.id());
                writer.write('\t');
                writer.write(encode(row.originalMessage()));
                writer.write('\t');
                writer.write(row.settledMessage() == null ? "-" : encode(row.settledMessage()));
                writer.write('\t');
                writer.write(row.settledRunMessage() == null ? "-" : encode(row.settledRunMessage()));
                writer.write('\n');
            }
        } catch (IOException ex) {
            // the settled messages are lost for the recovery path, the
            // rebase continues with the default messages of the driver
        }
    }

    /**
     * The plan persisted in the rebase folder, <code>null</code> if the
     * folder does not carry a plan (the rebase was not started from the
     * interactive rebase dialog) or the plan cannot be read.
     */
    static RebasePlan read(File folder) {
        File file = new File(folder, FILE);
        if (!file.canRead()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<Row> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", -1); // NOI18N
                if (parts.length != 5) {
                    return null;
                }
                RebaseAction action;
                try {
                    action = RebaseAction.valueOf(parts[0]);
                } catch (IllegalArgumentException ex) {
                    return null;
                }
                rows.add(new Row(
                    parts[1],
                    action,
                    decode(parts[2]),
                    dash(parts[3]),
                    dash(parts[4]))
                );
            }
            if (rows.isEmpty()) {
                return null;
            }
            return of(rows);
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static String encode(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    private static String dash(String encoded) {
        return "-".equals(encoded) ? null : decode(encoded);
    }

}
