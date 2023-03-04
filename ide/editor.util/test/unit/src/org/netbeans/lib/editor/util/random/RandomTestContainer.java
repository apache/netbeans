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

package org.netbeans.lib.editor.util.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Random testing container allows to manage a random test which can be composed
 * from multiple independent resources (maintained as properties) and operations over them.
 * <p>
 * Typical usage:
 * <br/><code>
 *    RandomTestContainer container = new RandomTestContainer();
 * </code><br/>
 * or
 * <br/><code>
 *    RandomTestContainer container = DocumentTesting.initContainer(null);
 * </code><br/>
 * then name the test container by
 * <br/><code>
 *    container.setName(this.getName());
 * </code><br/>
 * <br/><code>
 *    container.addOp(new MyOp());
 *    container.addCheck(new MyCheck());
 * </code><br/>
 * possibly set properties
 * <br/><code>
 *    BaseDocument doc = new BaseDocument(BaseKit.class, false);
 *    UndoManager undoManager = new UndoManager();
 *    doc.addUndoableEditListener(undoManager);
 *    doc.putProperty(UndoManager.class, undoManager);
 *    container.putProperty(Document.class, doc); // Replace original doc
 * </code><br/>
 * possibly set properties
 * <br/><code>
 *    BaseDocument doc = new BaseDocument(BaseKit.class, false);
 *    UndoManager undoManager = new UndoManager();
 *    doc.addUndoableEditListener(undoManager);
 *    doc.putProperty(UndoManager.class, undoManager);
 *    container.putProperty(Document.class, doc); // Replace original doc
 * </code><br/>
 * <br/><code>
 *    RandomText randomText = RandomText.join(
 *            RandomText.lowerCaseAZ(1),
 *            RandomText.spaceTabNewline(1)
 *    );
 *    container.putProperty(RandomText.class, randomText);
 * </code><br/>
 * add one or more rounds
 * <br/><code>
 *    RandomTestContainer.Round round = container.addRound();
 *    round.setOpCount(1000);
 *    round.setRatio(DocumentTesting.INSERT_CHAR, 6);
 *    round.setRatio(DocumentTesting.INSERT_TEXT, 3);
 *    round.setRatio(DocumentTesting.REMOVE_CHAR, 3);
 *    round.setRatio(DocumentTesting.REMOVE_TEXT, 1);
 *    round.setRatio(DocumentTesting.UNDO, 1);
 *    round.setRatio(DocumentTesting.REDO, 1);
 *    round.setRatio(MyOp.NAME, 0.5d);
 * </code><br/>
 * finally run either fixed or random test
 * <br/><code>
 *    container.run(1213202006348L); // Fixed test
 *    container.run(0L); // Random operation
 * </code><br/>
 *
 * @author mmetelka
 */
public final class RandomTestContainer extends PropertyProvider {

    /** java.lang.Boolean whether operation description should be logged. */
    public static final String LOG_OP = "log-op";

    /** Whether progress in number of operations should be logged. */
    public static final String LOG_PROGRESS = "log-progress";

    private static final int PROGRESS_COUNT = 5;

    // -J-Dorg.netbeans.lib.editor.util.random.RandomTestContainer.level=FINE
    private static final Logger LOG = Logger.getLogger(RandomTestContainer.class.getName());

    private String name;

    private final Map<String,Op> name2Op;

    final List<Check> checks;

    private final List<Round> rounds;

    private Random random;

    int totalOpCount;

    private Context context; // Global context

    private Context runContext; // Context for running of random test

    private long seed;
    
    public RandomTestContainer() {
        name2Op = new HashMap<String, Op>();
        checks = new ArrayList<Check>(3);
        rounds = new ArrayList<Round>(3);
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Run the whole testing.
     * <br/>
     * Take all rounds and process them one by one.
     *
     * @param seed seed for a <code>java.util.Random</code> with which the test should run
     *  or <code>0L</code> to run a truly random test.
     */
    public void run(long seed) throws Exception {
        runInit(seed);
        runOps(0); // Run all
    }

    /**
     * Initialize for random test running. Next one or more calls to runOps() should follow.
     *
     * @param seed specific seed or 0L for random seed generation.
     */
    public void runInit(long seed) {
        this.random = new Random();
        if (seed == 0L) { // Use currentTimeMillis() (btw nanoTime() in 1.5 instead)
            seed = System.currentTimeMillis();
        }
        this.seed = seed;
        random.setSeed(this.seed);
        dumpString(name() + " with SEED=" + this.seed + "L - use container.run(" + // NOI18N
                this.seed + "L) for same test\n"); // NOI18N

        if (name2Op.isEmpty()) {
            throw new IllegalStateException("No operations defined for the test."); // NOI18N
        }
        if (rounds.isEmpty()) {
            throw new IllegalStateException("No rounds defined for the test.");
        }

        // Use a fresh run context
        runContext = new Context(this, context().propertiesCopy());
        runContext.runInit(rounds);
    }

    /**
     * Run random operations.
     *
     * @param opCount Number of operations to run or 0 to run all remaining operations till end of test.
     * @throws Exception 
     */
    public void runOps(int opCount) throws Exception {
        runContext.runOps(opCount);
    }

    /**
     * Get context for last test run (or null if no test was run yet).
     * <br/>
     * Initial properties of run context are copy of properties from {@link #context()}.
     *
     * @return context or null.
     */
    public Context runContext() {
        return runContext;
    }

    public Op op(String name) {
        return name2Op.get(name);
    }

    public void addOp(Op op) {
        if (name2Op.containsKey(op.name())) {
            throw new IllegalArgumentException("Operation " + op.name + " already defined.");
        }
        name2Op.put(op.name(), op);
    }

    public void addCheck(Check check) {
        checks.add(check);
    }
    
    public void runChecks(Context context) throws Exception {
        for (Check check : context.container().checks) {
            check.check(context);
        }
    }

    public Random random() {
        return random;
    }

    /**
     * Create a new round.
     * <br/>
     * The caller should set opCount and additional ratios for ops into the round.
     *
     *
     * @return newly created round that is already added into list of rounds of this testing.
     */
    public Round addRound() {
        return addClonedRound(null);
    }

    /**
     * Create a new round to be a copy of existing round.
     * <br/>
     * The caller should set opCount and additional ratios for ops into the round.
     *
     * @return newly created round that is already added into list of rounds of this testing.
     */
    public Round addClonedRound(Round roundToClone) {
        Round round = new Round(this, roundToClone);
        rounds.add(round);
        totalOpCount += round.opCount();
        return round;
    }

    @Override
    public Object getPropertyOrNull(Object key) {
        return context().getPropertyOrNull(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        context().putProperty(key, value);
    }

    /**
     * Get a special non-test context (global context) that has a null {@link Context#round()}.
     * It allows to run certain operations that operate over context for a custom
     * non-randomized operations that the supports may provide.
     *
     * @return non-null context.
     */
    public Context context() {
        if (context == null) {
            context = new Context(this, new HashMap<Object,Object>());
        }
        return context;
    }

    public Logger logger() {
        return LOG;
    }

    /**
     * Set whether operations descriptions should be logged.
     * <br/>
     * This is "off" by default and it can be turned on when debugging
     * a failing test.
     *
     * @param logOp true for logging of the operations.
     */

    public void setLogOp(boolean logOp) {
        putProperty(LOG_OP, logOp);
    }
    
    void dumpString(String s) {
        if (true) { // Change to false if desired
            System.out.print(s); // Non-limited output with no extra undesired decoration
        } else {
            LOG.info(s);
        }
    }
    
    /**
     * Random operation that can be registered for random testing container
     * and that can be triggered with certain probability.
     */
    public abstract static class Op {

        private final String name;

        public Op(String name) {
            this.name = name;
        }

        public final String name() {
            return name;
        }

        /**
         * Execute the operation.
         */
        protected abstract void run(Context context) throws Exception;

    }

    /**
     * Check correctness of things after each operation.
     */
    public abstract static class Check {

        protected abstract void check(Context context) throws Exception;

    }

    /**
     * One round of testing having a specific count of random operations with specific ratios.
     * It can also have extra properties overriding properties of container.
     */
    public static final class Round extends PropertyProvider {

        private final RandomTestContainer container;

        private int totalOpCount;

        final Map<String,Double> op2Ratio;

        private final Map<Object,Object> properties;

        Round(RandomTestContainer container, Round roundToClone) {
            this.container = container;
            if (roundToClone != null)
                this.totalOpCount = roundToClone.totalOpCount;
            this.op2Ratio = new HashMap<String, Double>((roundToClone != null)
                    ? roundToClone.op2Ratio
                    : Collections.<String,Double>emptyMap()
            );
            this.properties = new HashMap<Object, Object>((roundToClone != null)
                    ? roundToClone.properties
                    : Collections.<Object,Object>emptyMap()
            );
        }

        void run(Context context) throws Exception {
            boolean ok = false;
            try {
                double opRatioSum = computeOpRatioSum();
                while (context.continueRun(totalOpCount)) {
                    Op op = findOp(context, opRatioSum);
                    boolean opSuccess = false;
                    try {
                        op.run(context); // Run the fetched random operation
                        container.runChecks(context);
                        opSuccess = true;
                    } finally {
                        context.finishLogOp(opSuccess);
                    }
                    context.incrementOpCount();
                }
                container.dumpString(container.name() + " finished successfully.\n"); // NOI18N
                ok = true;
            } finally {
                if (!ok) {
                    container.dumpString("ERROR: " + container.name() + // NOI18N
                            " during TESTOP[" + context.opCount() + "] (SEED=" + container.seed + "L)\n"); // NOI18N
                }
            }
        }

        public int opCount() {
            return totalOpCount;
        }

        public void setOpCount(int opCount) {
            container.totalOpCount += (opCount - this.totalOpCount); // Diff before assignment
            this.totalOpCount = opCount;
        }

        public void setRatio(String opName, double ratio) {
            op2Ratio.put(opName, ratio);
        }

        @Override
        public Object getPropertyOrNull(Object key) {
            Object value = properties.get(key);
            if (value == null)
                value = container.getPropertyOrNull(key);
            return value;
        }

        @Override
        public void putProperty(Object key, Object value) {
            properties.put(key, value);
        }

        private double computeOpRatioSum() {
            double ratioSum = 0d;
            for (Double ratio : op2Ratio.values()) {
                ratioSum += ratio;
            }
            return ratioSum;
        }

        private Op findOp(Context context, double opRatioSum) {
            while (true) { // Prevent rounding errors problems
                double r = context.container().random().nextDouble() * opRatioSum;
                for (Map.Entry<String,Double> entry : op2Ratio.entrySet()) {
                    r -= entry.getValue();
                    if (r <= 0) {
                        Op op = context.container().op(entry.getKey());
                        if (op == null) {
                            throw new IllegalStateException("No op for name=" + entry.getKey()); // NOI18N
                        }
                        if (context.isLogOp()) { // Possibly log operation number
                            context.logOpBuilder().append("\nTESTOP[").append(context.opCount()).append("]: ");
                        }
                        return op;
                    }
                }
            }
        }
    }

    /**
     * Context of the test being run.
     * It maintains total a current test round being executed and also total operation count performed.
     * It provides property-related operations fully delegating to current round when a test is performed
     * or to container if test is not active (i.e. fixed operations are being performed).
     */
    public static final class Context extends PropertyProvider {

        private final RandomTestContainer container;

        private Round currentRound;

        private Iterator<Round> roundIterator;

        private int roundOpCount; // How many ops performed inside round so far

        private int opCount;

        private int stopOpCount;
        
        private StringBuilder logOpBuilder = new StringBuilder(256);

        private int maxOpsLogged = 4; // By default log only last 4 operations

        private final Map<Object,Object> properties;

        /**
         * List of logs for each operation performed.
         */
        final LinkedList<String> opLogs = new LinkedList<String>();

        Context(RandomTestContainer container, Map<Object,Object> properties) {
            this.container = container;
            this.properties = properties;
        }

        public RandomTestContainer container() {
            return container;
        }

        public Round round() {
            return currentRound;
        }

        private void setCurrentRound(Round round) {
            this.currentRound = round;
            this.roundOpCount = 0;
        }

        /**
         * Total operation count performed so far.
         *
         * @return operation count.
         */
        public int opCount() {
            return opCount;
        }

        /**
         * Get string builder for logging operation description.
         *
         * @return non-null string builder.
         */
        public StringBuilder logOpBuilder() {
            return logOpBuilder;
        }

        /**
         * Dump an operation description either with logOpBuilder() or another StringBuilder.
         *
         * @param sb non-null string builder.
         */
        public void logOp(StringBuilder sb) {
            if (sb != logOpBuilder) { // Only when not already appended to logOpBuilder
                logOpBuilder.append(sb);
            }
            if (currentRound == null) { // If test is not running dump immediately
                container.dumpString(logOpBuilder.toString());
                logOpBuilder.setLength(0);
            }
        }

        @Override
        public Object getPropertyOrNull(Object key) {
            return properties.get(key);
        }

        @Override
        public void putProperty(Object key, Object value) {
            properties.put(key, value);
        }

        void runInit(List<Round> rounds) {
            roundIterator = rounds.iterator();
            setCurrentRound(roundIterator.next()); // should not be empty
        }

        void runOps(int opCount) throws Exception {
            stopOpCount = (opCount == 0) ? container.totalOpCount : (stopOpCount + opCount);
            while (continueRun()) {
                currentRound.run(this); // Run the test round
                if (continueRun() && roundIterator.hasNext()) {
                    setCurrentRound(roundIterator.next());
                } else {
                    break; // stopOpCount too high so break now
                }
            }
        }
        
        Map<Object,Object> propertiesCopy() {
            return new HashMap<Object,Object>(properties);
        }

        boolean continueRun(int roundTotalOpCount) {
            return (continueRun() && roundOpCount < roundTotalOpCount);
        }

        boolean continueRun() {
            return (opCount < stopOpCount);
        }

        void incrementOpCount() {
            roundOpCount++;
            opCount++;
            if (opCount % (container.totalOpCount / PROGRESS_COUNT) == 0) {
                container.dumpString(container.name() + ": " + opCount + " operations finished.\n"); // NOI18N
            }
        }
        
        void finishLogOp(boolean opSuccess) {
            if (logOpBuilder.length() > 0) {
                logOpBuilder.append("\n-------------------\n\n\n");
                String opLog = logOpBuilder.toString();
                logOpBuilder.setLength(0);
                addOpLog(opLog);
            }
            if (true) { // Dump the op logs always (not only after failure) - might want to examine something
                StringBuilder sb = new StringBuilder(1024);
                for (String opLog : opLogs) {
                    sb.append(opLog);
                }
                opLogs.clear();
                container.dumpString(sb.toString());
            }
        }
        
        /**
         * Set maximum number of last operations logged (older logs will be discarded).
         */
        public void setMaxOpsLogged(int maxOpsLogged) {
            this.maxOpsLogged = maxOpsLogged;
        }

        void addOpLog(String logString) {
            opLogs.add(logString);
            if (opLogs.size() > maxOpsLogged) {
                opLogs.removeFirst();
            }
        }
    
    }

}
