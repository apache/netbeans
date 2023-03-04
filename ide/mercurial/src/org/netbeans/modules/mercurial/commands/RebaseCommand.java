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
package org.netbeans.modules.mercurial.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "MSG_HgCommand.Rebase.failed=Rebase failed!"
})
public final class RebaseCommand extends HgCommand<RebaseCommand.Result> {
    
    private static final String HG_REBASE_EXT_CMD = "extensions.rebase="; // NOI18N
    private static final String HG_REBASE_OPT_ABORT = "--abort"; //NOI18N
    private static final String HG_REBASE_OPT_CONTINUE = "--continue"; //NOI18N
    private static final String HG_REBASE_OPT_BASE = "--base"; //NOI18N
    private static final String HG_REBASE_OPT_SOURCE = "--source"; //NOI18N
    private static final String HG_REBASE_OPT_DEST = "--dest"; //NOI18N
    
    private final File repository;
    private final Operation operation;
    private final OutputLogger logger;
    private String revisionBase;
    private String revisionSource;
    private String revisionDest;
    private List<String> output;
    
    public enum Operation {
        CONTINUE,
        ABORT,
        START
    }
    
    public RebaseCommand (File repository, Operation operation, OutputLogger logger) {
        Parameters.notNull("repository", repository);
        Parameters.notNull("operation", operation);
        this.repository = repository;
        this.operation = operation;
        this.logger = logger;
    }
    
    public RebaseCommand setRevisionBase (String revisionBase) {
        this.revisionBase = revisionBase;
        return this;
    }
    
    public RebaseCommand setRevisionSource (String revisionSource) {
        this.revisionSource = revisionSource;
        return this;
    }
    
    public RebaseCommand setRevisionDest (String revisionDest) {
        this.revisionDest = revisionDest;
        return this;
    }
    
    @Override
    public Result call () throws HgException {
        CommandParameters args = new CommandParameters(HgCommand.HG_REBASE_CMD);
        
        switch (operation) {
            case ABORT:
                args.add(HG_REBASE_OPT_ABORT);
                break;
            case CONTINUE:
                args.add(HG_REBASE_OPT_CONTINUE);
                break;
            case START:
                if (revisionBase != null) {
                    args.add(HG_REBASE_OPT_BASE);
                    args.add(revisionBase);
                }
                if (revisionSource != null) {
                    args.add(HG_REBASE_OPT_SOURCE);
                    args.add(revisionSource);
                }
                if (revisionDest != null) {
                    args.add(HG_REBASE_OPT_DEST);
                    args.add(revisionDest);
                }
                break;
        }
        
        args.addVerboseOption()
                .addConfigOption(HG_REBASE_EXT_CMD)
                .addConfigOption(HgCommand.HG_MERGE_SIMPLE_TOOL)
                .addRepositoryLocation(repository.getAbsolutePath());
        
        List<String> command = args.toCommand();
        output = exec(command);
        if (!output.isEmpty() && isErrorAbort(output.get(0))) {
            handleError(command, output, Bundle.MSG_HgCommand_Rebase_failed(), logger);
        }
        return Result.build(repository, output);
    }
    
    public List<String> getOutput () {
        return output;
    }
    
    public static class Result {
        private static final String SAVED_BACKUP_BUNDLE_TO = "saved backup bundle to "; //NOI18N
        private File bundleFile;

        public static enum State {
            OK,
            ABORTED,
            MERGING
        }

        private final List<String> output;
        private final Set<File> touchedFiles;
        private final List<File> conflicts;
        private State state;

        private Result (List<String> output) {
            this.output = output;
            touchedFiles = new LinkedHashSet<File>();
            conflicts = new ArrayList<File>();
        }

        public static Result build (File repo, List<String> output) {
            Result res = new Result(output);
            res.state = State.OK;
            for (String line : output) {
                boolean removed = false;
                boolean merging = false;
                if (line.startsWith("getting ") //NOI18N
                        || (merging = line.startsWith("merging ")) //NOI18N
                        || (removed = line.startsWith("removing "))) { //NOI18N
                    String name = line.substring(removed ? 9 : 8);
                    if (merging && name.endsWith(" failed!")) { //NOI18N
                        name = name.substring(0, name.length() - 8);
                        res.conflicts.add(new File (repo, name));
                    } else {
                        res.touchedFiles.add(new File (repo, name));
                    }
                } else if (line.startsWith("abort: unresolved conflicts")) { //NOI18N
                    res.state = State.MERGING;
                } else if (line.equals("rebase aborted")) { //NOI18N
                    res.state = State.ABORTED;
                } else if (line.startsWith(SAVED_BACKUP_BUNDLE_TO)) {
                    res.bundleFile = new File(line.substring(SAVED_BACKUP_BUNDLE_TO.length()));
                }
            }
            return res;
        }

        public File getBundleFile () {
            return bundleFile;
        }

        public Collection<File> getConflicts () {
            return conflicts;
        }

        public State getState () {
            return state;
        }

        public Collection<File> getTouchedFiles () {
            return touchedFiles;
        }

        public List<String> getOutput () {
            return output;
        }
    }
}
