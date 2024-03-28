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

package org.netbeans.modules.mercurial.util;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.commands.StatusCommand;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.queues.QPatch;
import org.netbeans.modules.mercurial.ui.queues.Queue;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.repository.UserCredentialsSupport;
import org.netbeans.modules.mercurial.ui.tag.HgTag;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NetworkSettings;
import org.openide.util.Utilities;

/**
 *
 * @author jrice
 */
public abstract class HgCommand<T> implements Callable<T> {
    public static final String HG_COMMAND = "hg";  // NOI18N
    public static final String HG_WINDOWS_EXE = ".exe";  // NOI18N
    public static final String HG_WINDOWS_BAT = ".bat";  // NOI18N
    public static final String HG_WINDOWS_CMD = ".cmd";  // NOI18N
    public static final String[] HG_WINDOWS_EXECUTABLES = new String[] {
            HG_COMMAND + HG_WINDOWS_EXE,
            HG_COMMAND + HG_WINDOWS_BAT,
            HG_COMMAND + HG_WINDOWS_CMD,
    };
    public static final String HG_COMMAND_PLACEHOLDER = HG_COMMAND;
    public static final String HGK_COMMAND = "hgk";  // NOI18N

    private static final String HG_DIFF_CMD = "diff"; //NOI18N
    private static final String HG_OPT_STAT = "--stat"; //NOI18N
    
    protected static final String HG_STATUS_CMD = "status";  // NOI18N // need -A to see ignored files, specified in .hgignore, see man hgignore for details
    private static final String HG_OPT_REPOSITORY = "--repository"; // NOI18N
    private static final String HG_OPT_BUNDLE = "--bundle"; // NOI18N
    protected static final String HG_OPT_CWD_CMD = "--cwd"; // NOI18N
    private static final String HG_OPT_USERNAME = "--user"; // NOI18N
    private static final String HG_OPT_CLOSE_BRANCH = "--close-branch"; // NOI18N

    private static final String HG_OPT_FOLLOW = "--follow"; // NOI18N
    protected static final String HG_FLAG_REV_CMD = "--rev"; // NOI18N
    private static final String HG_STATUS_FLAG_TIP_CMD = "tip"; // NOI18N
    private static final String HG_HEAD_STR = "HEAD"; // NOI18N
    private static final String HG_FLAG_DATE_CMD = "--date"; // NOI18N

    private static final String HG_COMMIT_CMD = "commit"; // NOI18N
    private static final String HG_COMMIT_OPT_LOGFILE_CMD = "--logfile"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME = "hgcommit"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME_SUFFIX = ".hgm"; // NOI18N
    private static final String HG_COMMIT_DEFAULT_MESSAGE = "[no commit message]"; // NOI18N

    private static final String HG_REVERT_CMD = "revert"; // NOI18N
    private static final String HG_REVERT_NOBACKUP_CMD = "--no-backup"; // NOI18N
    private static final String HG_PURGE_CMD = "purge"; // NOI18N
    private static final String HG_EXT_PURGE = "extensions.purge="; //NOI18N
    private static final String HG_ADD_CMD = "add"; // NOI18N

    private static final String HG_TIP_CONST = "tip"; // NOI18N

    private static final String HG_CREATE_CMD = "init"; // NOI18N
    private static final String HG_CLONE_CMD = "clone"; // NOI18N

    private static final String HG_UPDATE_ALL_CMD = "update"; // NOI18N
    private static final String HG_UPDATE_FORCE_ALL_CMD = "-C"; // NOI18N

    private static final String HG_REMOVE_CMD = "remove"; // NOI18N
    private static final String HG_REMOVE_FLAG_FORCE_CMD = "--force"; // NOI18N

    private static final String HG_LOG_CMD = "log"; // NOI18N
    private static final String HG_TIP_CMD = "tip"; // NOI18N
    private static final String HG_OUT_CMD = "out"; // NOI18N
    private static final String HG_LOG_LIMIT_ONE_CMD = "-l 1"; // NOI18N
    private static final String HG_LOG_LIMIT_CMD = "-l"; // NOI18N
    private static final String HG_PARENT_CMD = "parents";              //NOI18N
    private static final String HG_PARAM_BRANCH = "-b"; //NOI18N
    private static final String HG_PARAM_PUSH_NEW_BRANCH = "--new-branch"; //NOI18N

    private static final String HG_LOG_NO_MERGES_CMD = "-M";
    private static final String HG_LOG_DEBUG_CMD = "--debug";
    private static final String HG_LOG_REVISION_OUT = "rev:"; // NOI18N
    private static final String HG_LOG_AUTHOR_OUT = "auth:"; // NOI18N
    private static final String HG_LOG_USER_OUT = "user:"; // NOI18N
    private static final String HG_LOG_DESCRIPTION_OUT = "desc:"; // NOI18N
    private static final String HG_LOG_DATE_OUT = "date:"; // NOI18N
    private static final String HG_LOG_ID_OUT = "id:"; // NOI18N
    private static final String HG_LOG_PARENTS_OUT = "parents:"; // NOI18N
    private static final String HG_LOG_FILEMODS_OUT = "file_mods:"; // NOI18N
    private static final String HG_LOG_FILEADDS_OUT = "file_adds:"; // NOI18N
    private static final String HG_LOG_FILEDELS_OUT = "file_dels:"; // NOI18N
    private static final String HG_LOG_FILECOPIESS_OUT = "file_copies:"; // NOI18N
    private static final String HG_LOG_BRANCHES_OUT = "branches:"; // NOI18N
    private static final String HG_LOG_TAGS_OUT = "tags:"; // NOI18N
    private static final String HG_LOG_ENDCS_OUT = "endCS:"; // NOI18N

    private static final String HG_LOG_PATCH_CMD = "-p";
    private static final String HG_LOG_TEMPLATE_EXPORT_FILE_CMD =
        "--template=# Mercurial Export File Diff\\n# changeset: \\t{rev}:{node|short}\\n# user:\\t\\t{author}\\n# date:\\t\\t{date|isodate}\\n# summary:\\t{desc}\\n\\n";

    private static final String HG_REV_TEMPLATE_CMD = "--template={rev}\\n"; // NOI18N

    private static final String HG_CAT_CMD = "cat"; // NOI18N
    private static final String HG_FLAG_OUTPUT_CMD = "--output"; // NOI18N

    private static final String HG_COMMONANCESTOR_CMD = "debugancestor"; // NOI18N

    private static final String HG_ANNOTATE_CMD = "annotate"; // NOI18N
    private static final String HG_ANNOTATE_FLAGN_CMD = "--number"; // NOI18N
    private static final String HG_ANNOTATE_FLAGU_CMD = "--user"; // NOI18N
    private static final String HG_ANNOTATE_FLAGL_CMD = "--line-number"; // NOI18N

    private static final String HG_EXPORT_CMD = "export"; // NOI18N
    private static final String HG_IMPORT_CMD = "import"; // NOI18N

    private static final String HG_RENAME_CMD = "rename"; // NOI18N
    private static final String HG_RENAME_AFTER_CMD = "-A"; // NOI18N
    private static final String HG_COPY_CMD = "copy"; // NOI18N
    private static final String HG_COPY_AFTER_CMD = "-A"; // NOI18N
    private static final String HG_NEWEST_FIRST = "--newest-first"; // NOI18N

    private static final String HG_RESOLVE_CMD = "resolve";             //NOI18N
    private static final String HG_RESOLVE_MARK_RESOLVED = "--mark";   //NOI18N
    
    private static final String HG_MQ_EXT_CMD = "extensions.mq="; //NOI18N
    private static final String HG_QPATCHES_NAME = "patches"; //NOI18N
    private static final String HG_QQUEUE_CMD = "qqueue"; //NOI18N
    private static final String HG_QSERIES_CMD = "qseries"; //NOI18N
    private static final String HG_OPT_SUMMARY = "--summary"; //NOI18N
    private static final String HG_OPT_LIST = "--list"; //NOI18N
    private static final String HG_QGOTO_CMD = "qgoto"; //NOI18N
    private static final String HG_QPOP_CMD = "qpop"; //NOI18N
    private static final String HG_QPUSH_CMD = "qpush"; //NOI18N
    private static final String HG_OPT_ALL = "--all"; //NOI18N
    private static final String HG_QCREATE_CMD = "qnew"; //NOI18N
    private static final String HG_QREFRESH_PATCH = "qrefresh"; //NOI18N
    private static final String HG_OPT_EXCLUDE = "--exclude"; //NOI18N
    private static final String HG_OPT_SHORT = "--short"; //NOI18N
    private static final String HG_QFINISH_CMD = "qfinish"; //NOI18N
    private static final String QUEUE_ACTIVE = "(active)"; //NOI18N

    protected static final String HG_REBASE_CMD = "rebase"; //NOI18N
    
    // TODO: replace this hack
    // Causes /usr/bin/hgmerge script to return when a merge
    // has conflicts with exit 0, instead of throwing up EDITOR.
    // Problem is after this Hg thinks the merge succeded and no longer
    // marks repository with a merge needed flag. So Plugin needs to
    // track this merge required status by changing merge conflict file
    // status. If the cache is removed this information would be lost.
    //
    // Really need Hg to give us back merge status information,
    // which it currently does not
    private static final String HG_MERGE_CMD = "merge"; // NOI18N
    private static final String HG_MERGE_FORCE_CMD = "-f"; // NOI18N
    private static final String HG_MERGE_ENV = "EDITOR=success || $TEST -s"; // NOI18N
    protected static final String HG_MERGE_SIMPLE_TOOL = "ui.merge=internal:merge"; //NOI18N

    public static final String HG_HGK_PATH_SOLARIS10 = "/usr/demo/mercurial"; // NOI18N
    private static final String HG_HGK_PATH_SOLARIS10_ENV = "PATH=/usr/bin/:/usr/sbin:/bin:"+ HG_HGK_PATH_SOLARIS10; // NOI18N

    private static final String HG_PULL_CMD = "pull"; // NOI18N
    private static final String HG_UPDATE_CMD = "-u"; // NOI18N
    private static final String HG_PUSH_CMD = "push"; // NOI18N
    private static final String HG_BUNDLE_CMD = "bundle"; // NOI18N
    private static final String HG_UNBUNDLE_CMD = "unbundle"; // NOI18N
    private static final String HG_ROLLBACK_CMD = "rollback"; // NOI18N
    private static final String HG_BACKOUT_CMD = "backout"; // NOI18N
    private static final String HG_BACKOUT_MERGE_CMD = "--merge"; // NOI18N
    private static final String HG_BACKOUT_COMMIT_MSG_CMD = "-m"; // NOI18N
    private static final String HG_REV_CMD = "-r"; // NOI18N
    private static final String HG_BASE_CMD = "--base"; // NOI18N
    private static final String HG_OPTION_GIT = "--git"; //NOI18N

    private static final String HG_STRIP_CMD = "strip"; // NOI18N
    private static final String HG_STRIP_EXT_CMD = "extensions.mq="; // NOI18N
    private static final String HG_STRIP_NOBACKUP_CMD = "-n"; // NOI18N
    private static final String HG_STRIP_FORCE_MULTIHEAD_CMD = "-f"; // NOI18N

    private static final String HG_VERIFY_CMD = "verify"; // NOI18N

    private static final String HG_VERSION_CMD = "version"; // NOI18N
    private static final String HG_INCOMING_CMD = "incoming"; // NOI18N
    private static final String HG_OUTGOING_CMD = "outgoing"; // NOI18N
    private static final String HG_VIEW_CMD = "view"; // NOI18N
    private static final String HG_VERBOSE_CMD = "-v"; // NOI18N
    private static final String HG_CONFIG_OPTION_CMD = "--config"; // NOI18N
    private static final String HG_FETCH_EXT_CMD = "extensions.fetch="; // NOI18N
    private static final String HG_FETCH_CMD = "fetch"; // NOI18N
    public static final String HG_PROXY_ENV = "http_proxy="; // NOI18N
    private static final String HG_NO_COMMIT = "--no-commit"; // NOI18N

    private static final String HG_UPDATE_NEEDED_ERR = "(run 'hg update' to get a working copy)"; //NOI18N
    public static final String HG_MERGE_CONFLICT_ERR = "conflicts detected in "; // NOI18N
    public static final String HG_MERGE_FAILED1_ERR = "merging"; // NOI18N
    public static final String HG_MERGE_FAILED2_ERR = "failed!"; // NOI18N
    public static final String HG_MERGE_FAILED3_ERR = "incomplete!"; // NOI18N
    private static final String HG_MERGE_MULTIPLE_HEADS_ERR = "abort: repo has "; // NOI18N
    private static final String HG_MERGE_UNCOMMITTED_ERR = "abort: outstanding uncommitted merges"; // NOI18N

    private static final String HG_MERGE_UNAVAILABLE_ERR = "is not recognized as an internal or external command";

    private static final String HG_NO_CHANGES_ERR = "no changes found"; // NOI18N
    private static final String HG_CREATE_NEW_BRANCH_ERR = "abort: push creates new remote "; // NOI18N
    private static final String HG_HEADS_CREATED_ERR = "(+1 heads)"; // NOI18N
    private static final String HG_NO_HG_CMD_FOUND_ERR = "hg: not found";
    private static final String HG_ARG_LIST_TOO_LONG_ERR = "Arg list too long";
    private static final String HG_ARGUMENT_LIST_TOO_LONG_ERR = "Argument list too long"; //NOI18N

    private static final String HG_HEADS_CMD = "heads"; // NOI18N
    private static final String HG_BRANCHES_CMD = "branches"; // NOI18N
    private static final String HG_BRANCH_CMD = "branch"; // NOI18N
    private static final String HG_TAG_CMD = "tag"; // NOI18N
    private static final String HG_TAG_OPT_MESSAGE = "--message"; // NOI18N
    private static final String HG_TAG_OPT_REMOVE = "--remove"; // NOI18N
    private static final String HG_TAG_OPT_REVISION = "--rev"; // NOI18N
    private static final String HG_TAG_OPT_LOCAL = "--local"; // NOI18N
    private static final String HG_TAGS_CMD = "tags"; // NOI18N

    private static final String HG_NO_REPOSITORY_ERR = "There is no Mercurial repository here"; // NOI18N
    private static final String HG_NO_RESPONSE_ERR = "no suitable response from remote hg!"; // NOI18N
    private static final String HG_NOT_REPOSITORY_ERR = "does not appear to be an hg repository"; // NOI18N
    private static final String HG_REPOSITORY = "repository"; // NOI18N
    private static final String HG_NOT_FOUND_ERR = "not found!"; // NOI18N
    private static final String HG_UPDATE_SPAN_BRANCHES_ERR = "abort: update spans branches"; // NOI18N
    private static final String HG_UPDATE_CROSS_BRANCHES_ERR = "abort: crosses branches"; // NOI18N
    private static final String HG_ALREADY_TRACKED_ERR = " already tracked!"; // NOI18N
    private static final String HG_NOT_TRACKED_ERR = " no tracked!"; // NOI18N
    private static final String HG_CANNOT_READ_COMMIT_MESSAGE_ERR = "abort: can't read commit message"; // NOI18N
    private static final String HG_CANNOT_RUN_ERR = "Cannot run program"; // NOI18N
    private static final String HG_ABORT_ERR = "abort: "; // NOI18N
    //#132984: range of issues with upgrade to Hg 1.0, error string changed from branches to heads, just removed ending
    private static final String HG_ABORT_PUSH_ERR = "abort: push creates new remote "; // NOI18N
    private static final String HG_ABORT_NO_FILES_TO_COPY_ERR = "abort: no files to copy"; // NOI18N
    private static final String HG_ABORT_NO_DEFAULT_PUSH_ERR = "abort: repository default-push not found!"; // NOI18N
    private static final String HG_ABORT_NO_DEFAULT_ERR = "abort: repository default not found!"; // NOI18N
    private static final String HG_ABORT_POSSIBLE_PROXY_ERR = "abort: error: node name or service name not known"; // NOI18N
    private static final String HG_ABORT_UNCOMMITTED_CHANGES_ERR = "abort: outstanding uncommitted changes"; // NOI18N
    private static final String HG_BACKOUT_MERGE_NEEDED_ERR = "(use \"backout --merge\" if you want to auto-merge)";
    private static final String HG_ABORT_BACKOUT_MERGE_CSET_ERR = "abort: cannot back out a merge changeset without --parent"; // NOI18N"
    private static final String HG_COMMIT_AFTER_MERGE_ERR = "abort: cannot partially commit a merge (do not specify files or patterns)"; // NOI18N"
    private static final String HG_ADDING = "adding";                   //NOI18N
    private static final String HG_WARNING_PERFORMANCE_FILES_OVER = ": files over"; //NOI18N
    private static final String HG_WARNING_PERFORMANCE_CAUSE_PROBLEMS = "cause memory and performance problems"; //NOI18N
    private static final String HG_ABORT_CANNOT_FOLLOW_NONEXISTENT_FILE = "cannot follow nonexistent file"; //NOI18N

    private static final String HG_NO_CHANGE_NEEDED_ERR = "no change needed"; // NOI18N
    private static final String HG_NO_ROLLBACK_ERR = "no rollback information available"; // NOI18N
    private static final String HG_NO_UPDATES_ERR = "0 files updated, 0 files merged, 0 files removed, 0 files unresolved"; // NOI18N
    private static final String HG_NO_VIEW_ERR = "hg: unknown command 'view'"; // NOI18N
    private static final String HG_HGK_NOT_FOUND_ERR = "sh: hgk: not found"; // NOI18N
    private static final String HG_NO_SUCH_FILE_ERR = "no such file"; // NOI18N

    private static final String HG_NO_REV_STRIP_ERR = "abort: unknown revision"; // NOI18N
    private static final String HG_LOCAL_CHANGES_STRIP_ERR = "abort: local changes found"; // NOI18N
    private static final String HG_MULTIPLE_HEADS_STRIP_ERR = "no rollback information available"; // NOI18N

    private static final char HG_STATUS_CODE_CONFLICT = 'U' + ' ';    // NOI18N // STATUS_VERSIONED_CONFLICT - TODO when Hg status supports conflict markers
    public static final String HG_STR_CONFLICT_EXT = ".conflict~"; // NOI18N

    private static final String HG_EPOCH_PLUS_ONE_YEAR = "1971-01-01"; // NOI18N

    private static final String HG_AUTHORIZATION_REQUIRED_ERR = "authorization required"; // NOI18N
    private static final String HG_AUTHORIZATION_FAILED_ERR = "authorization failed"; // NOI18N
    public static final String COMMIT_AFTER_MERGE = "commitAfterMerge"; //NOI18N

    private static final String ENV_HGPLAIN = "HGPLAIN"; //NOI18N
    private static final String ENV_HGENCODING = "HGENCODING"; //NOI18N
    public static final String ENCODING = getEncoding();

    private static final String HG_LOG_FULL_CHANGESET_NAME = "log-full-changeset.tmpl"; //NOI18N
    private static final String HG_LOG_ONLY_FILE_COPIES_CHANGESET_NAME = "log-only-file-copies-changeset.tmpl"; //NOI18N
    private static final String HG_LOG_BASIC_CHANGESET_NAME = "log-no-files-changeset.tmpl"; //NOI18N
    private static final String HG_LOG_CHANGESET_GENERAL_NAME = "changeset.tmpl"; //NOI18N
    private static final String HG_LOG_STYLE_NAME = "log.style";        //NOI18N
    private static final String HG_ARGUMENT_STYLE = "--style=";         //NOI18N
    private static final int WINDOWS_MAX_COMMANDLINE_SIZE = 8000;
    private static final int MAC_MAX_COMMANDLINE_SIZE = 64000;
    private static final int UNIX_MAX_COMMANDLINE_SIZE = 128000;
    private static final int MAX_COMMANDLINE_SIZE;
    static {
        String maxCmdSizeProp = System.getProperty("mercurial.maxCommandlineSize");
        if (maxCmdSizeProp == null) {
            maxCmdSizeProp = "";                                            //NOI18N
        }
        int maxCmdSize = 0;
        try {
            maxCmdSize = Integer.parseInt(maxCmdSizeProp);
        } catch (NumberFormatException e) {
            maxCmdSize = 0;
        }
        if (maxCmdSize < 1024) {
            if (Utilities.isWindows()) {
                maxCmdSize = WINDOWS_MAX_COMMANDLINE_SIZE;
            } else if (Utilities.isMac()) {
                maxCmdSize = MAC_MAX_COMMANDLINE_SIZE;
            } else {
                maxCmdSize = UNIX_MAX_COMMANDLINE_SIZE;
            }
        }
        MAX_COMMANDLINE_SIZE = maxCmdSize;
    }

    private static final HashSet<String> WORKING_COPY_PARENT_MODIFYING_COMMANDS = new HashSet<String>(Arrays.asList(
        HG_BACKOUT_CMD,
        HG_CLONE_CMD,
        HG_COMMIT_CMD,
        HG_CREATE_CMD,
        HG_FETCH_CMD,
        HG_IMPORT_CMD,
        HG_MERGE_CMD,
        HG_PULL_CMD,
        HG_ROLLBACK_CMD,
        HG_QCREATE_CMD,
        HG_QGOTO_CMD,
        HG_QFINISH_CMD,
        HG_QPOP_CMD,
        HG_QPUSH_CMD,
        HG_QREFRESH_PATCH,
        HG_REBASE_CMD,
        HG_STRIP_CMD,
        HG_TAG_CMD,
        HG_UNBUNDLE_CMD,
        HG_UPDATE_ALL_CMD
    ));

    private static final HashSet<String> REPOSITORY_NOMODIFICATION_COMMANDS = new HashSet<String>(Arrays.asList(
        HG_ANNOTATE_CMD,
        HG_BRANCH_CMD,
        HG_BRANCHES_CMD,
        HG_BUNDLE_CMD,
        HG_CAT_CMD,
        HG_DIFF_CMD,
        HG_EXPORT_CMD,
        HG_HEADS_CMD,
        HG_INCOMING_CMD,
        HG_LOG_CMD,
        HG_OUTGOING_CMD,
        HG_OUT_CMD,
        HG_PARENT_CMD,
        HG_PUSH_CMD,
        HG_RESOLVE_CMD,
        HG_QSERIES_CMD,
        HG_QQUEUE_CMD,
        HG_STATUS_CMD,
        HG_TAG_CMD,
        HG_TAGS_CMD,
        HG_TIP_CMD,
        HG_VERIFY_CMD,
        HG_VERSION_CMD,
        HG_VIEW_CMD
    ));
    private static final String HG_FLAG_TOPO = "--topo"; //NOI18N
    
    private static final String CMD_EXE = "cmd.exe"; //NOI18N
    private static ThreadLocal<Boolean> doNotAddHgPlain = new ThreadLocal<Boolean>();
    
    protected static final class CommandParameters {
        private final ArrayList<String> arguments;
        private final String commandName;

        public CommandParameters (String commandName) {
            this.commandName = commandName;
            this.arguments = new ArrayList<String>();
        }

        public CommandParameters add (String parameter) {
            arguments.add(parameter);
            return this;
        }

        public CommandParameters addVerboseOption () {
            arguments.add(HG_VERBOSE_CMD);
            return this;
        }

        public CommandParameters addConfigOption (String configOption) {
            arguments.add(HG_CONFIG_OPTION_CMD);
            arguments.add(configOption);
            return this;
        }

        public CommandParameters addRepositoryLocation (String repositoryRootLocation) {
            arguments.add(HG_OPT_REPOSITORY);
            arguments.add(repositoryRootLocation);
            return this;
        }

        public List<String> toCommand () {
            List<String> command = new ArrayList<String>(arguments.size() + 2);
            command.add(getHgCommand());
            command.add(commandName);
            command.addAll(arguments);
            return command;
        }
    }
    
    /**
     * Merge working directory with the head revision
     * Merge the contents of the current working directory and the
     * requested revision. Files that changed between either parent are
     * marked as changed for the next commit and a commit must be
     * performed before any further updates are allowed.
     *
     * @param File repository of the mercurial repository's root directory
     * @param Revision to merge with, if null will merge with default tip rev
     * @return hg merge output
     * @throws HgException
     */
    public static List<String> doMerge(File repository, String revStr) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();
        List<String> env = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_MERGE_CMD);
        command.add(HG_MERGE_FORCE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (HgModuleConfig.getDefault().isInternalMergeToolEnabled()) {
            command.add(HG_CONFIG_OPTION_CMD);
            command.add(HG_MERGE_SIMPLE_TOOL);
        }
        if(revStr != null)
             command.add(revStr);
        env.add(HG_MERGE_ENV);

        List<String> list = execEnv(command, env);
        return list;
    }

    /**
     * Update the working directory to the tip revision.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param Boolean force an Update and overwrite any modified files in the  working directory
     * @param String revision to be updated to
     * @param Boolean throw exception on error
     * @return hg update output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    @NbBundle.Messages({
        "MSG_WARN_UPDATE_MERGE_TEXT=Cannot update because it would end on a different head - \"Merge\" or \"Rebase\" is needed.",
        "MSG_WARN_UPDATE_COMMIT_TEXT=Merge has been done, invoke \"Commit\" menu item\n "
            + "to commit these changes before doing an \"Update\""
    })
    public static List<String> doUpdateAll(File repository, boolean bForce, String revision, boolean bThrowException) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_UPDATE_ALL_CMD);
        command.add(HG_VERBOSE_CMD);
        if (bForce) command.add(HG_UPDATE_FORCE_ALL_CMD);
        if (HgModuleConfig.getDefault().isInternalMergeToolEnabled()) {
            command.add(HG_CONFIG_OPTION_CMD);
            command.add(HG_MERGE_SIMPLE_TOOL);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(revision);
        }

        List<String> list = exec(command);
        if (bThrowException) {
            if (!list.isEmpty()) {
                if  (isErrorUpdateSpansBranches(list.get(0))) {
                    handleError(command, list, Bundle.MSG_WARN_UPDATE_MERGE_TEXT(),
                            OutputLogger.getLogger(repository));
                } else if (isMergeAbortUncommittedMsg(list.get(0))) {
                    handleError(command, list, Bundle.MSG_WARN_UPDATE_COMMIT_TEXT(),
                            OutputLogger.getLogger(repository));
                } else if (isErrorAbort(list.get(list.size() -1))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"),
                            OutputLogger.getLogger(repository));
                }
            }
        }
        return list;
    }

    public static List<String> doUpdateAll(File repository, boolean bForce, String revision) throws HgException {
        return doUpdateAll(repository, bForce, revision, true);
    }

    /**
     * Roll back the last transaction in this repository
     * Transactions are used to encapsulate the effects of all commands
     * that create new changesets or propagate existing changesets into a
     * repository. For example, the following commands are transactional,
     * and their effects can be rolled back:
     * commit, import, pull, push (with this repository as destination)
     * unbundle
     * There is only one level of rollback, and there is no way to undo a rollback.
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg update output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doRollback(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ROLLBACK_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ROLLBACK_FAILED"), logger);

        return list;
    }
    public static List<String> doBackout(File repository, String revision,
            boolean doMerge, String commitMsg, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> env = new ArrayList<String>();
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BACKOUT_CMD);
        if(doMerge){
            command.add(HG_BACKOUT_MERGE_CMD);
            env.add(HG_MERGE_ENV);
        }

        if (commitMsg != null && !commitMsg.equals("")) { // NOI18N
            command.add(HG_BACKOUT_COMMIT_MSG_CMD);
            command.add(commitMsg);
        } else {
            command.add(HG_BACKOUT_COMMIT_MSG_CMD);
            command.add(NbBundle.getMessage(HgCommand.class, "MSG_BACKOUT_MERGE_COMMIT_MSG", revision));  // NOI18N
        }

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(HG_REV_CMD);
            command.add(revision);
        }

        List<String> list;
        if(doMerge){
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_BACKOUT_FAILED"), logger);

        return list;
    }

    public static List<String> doStrip(File repository, String revision,
            boolean doForceMultiHead, boolean doBackup, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_STRIP_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_STRIP_EXT_CMD);
        if(doForceMultiHead){
            command.add(HG_STRIP_FORCE_MULTIHEAD_CMD);
        }
        if(!doBackup){
            command.add(HG_STRIP_NOBACKUP_CMD);
        }
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(revision);
        }

        List<String> list = exec(command);
        if (list.isEmpty() && doBackup)
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_STRIP_FAILED"), logger);

        return list;
    }

        public static List<String> doVerify(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VERIFY_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_VERIFY_FAILED"), logger);

        return list;
    }

    /**
     * Return the version of hg, e.g. "0.9.3". // NOI18N
     *
     * @return String
     */
    public static String getHgVersion() {
        List<String> list;
        try {
            list = execForVersionCheck();
        } catch (HgException ex) {
            // Ignore Exception
            return null;
        }
        if (!list.isEmpty()) {
            int start = list.get(0).indexOf('(');
            int end = list.get(0).indexOf(')');
            if (start != -1 && end != -1) {
                return list.get(0).substring(start + 9, end);
            }
        }
        return null;
    }

    /**
     * Pull changes from the default pull locarion and update working directory.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg pull output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPull (File repository, String revision, String branch, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_PULL_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (HgModuleConfig.getDefault().isInternalMergeToolEnabled()) {
            command.add(HG_CONFIG_OPTION_CMD);
            command.add(HG_MERGE_SIMPLE_TOOL);
        }
        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);            
        }
        if (branch != null) {
            command.add(HG_PARAM_BRANCH);
            command.add(branch);            
        }

        List<String> list;
        String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
        String proxy = getGlobalProxyIfNeeded(defaultPull, true, logger);
        if(proxy != null){
            List<String> env = new ArrayList<String>();
            env.add(HG_PROXY_ENV + proxy);
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }

        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return list;
    }

    /**
     * Unbundle changes from the specified local source repository and
     * update working directory.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File bundle identfies the compressed changegroup file to be applied
     * @return hg unbundle output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doUnbundle(File repository, File bundle, boolean update, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_UNBUNDLE_CMD);
        command.add(HG_VERBOSE_CMD);
        if (update) {
            command.add(HG_UPDATE_CMD);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (HgModuleConfig.getDefault().isInternalMergeToolEnabled()) {
            command.add(HG_CONFIG_OPTION_CMD);
            command.add(HG_MERGE_SIMPLE_TOOL);
        }
        if (bundle != null) {
            command.add(bundle.getAbsolutePath());
        }

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return list;
    }

    /**
     * Show the changesets that would be pulled if a pull
     * was requested from the default pull location
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg incoming output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncoming(File repository, String revision, String branch, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<Object> command = new ArrayList<Object>();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);            
        }
        if (branch != null) {
            command.add(HG_PARAM_BRANCH);
            command.add(branch);            
        }

        List<String> cmdOutput;
        String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
        String proxy = getGlobalProxyIfNeeded(defaultPull, false, null);
        if(proxy != null){
            List<String> env = new ArrayList<String>();
            env.add(HG_PROXY_ENV + proxy);
            cmdOutput = execEnv(command, env);
        }else{
            cmdOutput = exec(command);
        }

        if (!cmdOutput.isEmpty() &&
             isErrorAbort(cmdOutput.get(cmdOutput.size() -1))) {
            handleError(command, cmdOutput, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return cmdOutput;
    }

    /**
     * Show the changesets that would be pulled if a pull
     * was requested from the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to query
     * @param File bundle to store downloaded changesets.
     * @return hg incoming output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncoming(File repository, HgURL from, String revision, String branch, File bundle, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || from == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_INCOMING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = from;
        command.repository = repository;
        if (revision != null) {
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revision);
        }
        if (branch != null) {
            command.additionalOptions.add(HG_PARAM_BRANCH);
            command.additionalOptions.add(branch);            
        }
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.showSaveOption = showSaveCredentialsOption;
        if (bundle != null) {
            command.additionalOptions.add(HG_OPT_BUNDLE);
            command.additionalOptions.add(bundle.getAbsolutePath());
        }
        command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PULL_VALUE, HgConfigFiles.HG_DEFAULT_PULL};

        List<String> retval = command.invoke();

        return retval;
    }

    /**
     * Show the changesets that would be pushed if a push
     * was requested to the specified local source repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to query
     * @return hg outgoing output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doOutgoing(File repository, HgURL toUrl, String revision, String branch, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || toUrl == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPush(false);
        command.hgCommandType = HG_OUTGOING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = toUrl;
        command.repository = repository;
        if (revision != null) {
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revision);
        }
        if (branch != null) {
            command.additionalOptions.add(HG_PARAM_BRANCH);
            command.additionalOptions.add(branch);            
        }
        command.additionalOptions.add(HG_VERBOSE_CMD);
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.additionalOptions.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            command.showSaveOption = showSaveCredentialsOption;
            command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PUSH};

            return command.invoke();
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    /**
     * Push changes to the specified repository
     * By default, push will refuse to run if doing so would create multiple heads
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to push to
     * @return hg push output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPush (File repository, final HgURL toUrl,
            String revision, String branch, boolean allowNewBranch,
            OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || toUrl == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPush(false);
        command.hgCommandType = HG_PUSH_CMD;
        command.logger = logger;
        command.remoteUrl = toUrl;
        command.repository = repository;
        if (revision != null) {
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revision);
        }
        if (branch != null) {
            command.additionalOptions.add(HG_PARAM_BRANCH);
            command.additionalOptions.add(branch);
            command.additionalOptions.add(HG_PARAM_PUSH_NEW_BRANCH);
        } else if (allowNewBranch) {
            command.additionalOptions.add(HG_PARAM_PUSH_NEW_BRANCH);
        }
        command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PUSH};

        List<String> retval = command.invoke();

        return retval;
    }

    /**
     * Run the command hg view for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doView(File repository, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        List<String> command = new ArrayList<String>();
        List<String> env = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VIEW_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list;

        if(HgUtils.isSolaris()){
            env.add(HG_HGK_PATH_SOLARIS10_ENV);
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }

        if (!list.isEmpty()) {
            if (isErrorNoView(list.get(list.size() -1))) {
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_NO_VIEW_TEXT"));
             }
            else if (isErrorHgkNotFound(list.get(0)) || isErrorNoSuchFile(list.get(0))) {
                OutputLogger.getLogger(repository).outputInRed(list.toString());
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_HGK_NOT_FOUND_TEXT"));
            } else if (isErrorAbort(list.get(list.size() -1))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
            }
        }
        return list;
    }

    @Override
    public abstract T call () throws HgException;
    
    private static final ThreadLocal<Boolean> disabledUI = new ThreadLocal<Boolean>();
    
    public static <T> T runWithoutUI (Callable<T> callable) throws HgException {
        try {
            disabledUI.set(true);
            return callable.call();
        } catch (HgException ex) {
            throw ex;
        } catch (Exception ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            disabledUI.remove();
        }
    }

    private static String getGlobalProxyIfNeeded(String defaultPath, boolean bOutputDetails, OutputLogger logger){
        String proxy = null;
        if( defaultPath != null &&
           (defaultPath.startsWith("http:") ||                                  // NOI18N
            defaultPath.startsWith("https:")))                                  // NOI18N
        { 
        
            URI uri = null;
            try {
                uri = new URI(defaultPath);
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
        
            String proxyHost = NetworkSettings.getProxyHost(uri);

            // check DIRECT connection
            if(proxyHost != null && proxyHost.length() > 0) {
                proxy = proxyHost;
                String proxyPort = NetworkSettings.getProxyPort(uri);
                assert proxyPort != null;

                proxy += !proxyPort.equals("") ? ":" + proxyPort : ""; // NOI18N
            }
        }
        if(proxy != null && bOutputDetails){
            logger.output(NbBundle.getMessage(HgCommand.class, "MSG_USING_PROXY_INFO", proxy)); // NOI18N
        }
        return proxy;
    }
    
    public static List<String> doFetch(File repository, HgURL from, String revision, boolean enableFetchExtension, OutputLogger logger) throws HgException {
        if (repository == null || from == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_FETCH_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = from;
        command.repository = repository;
        if (revision != null) {
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revision);
        }
        command.additionalOptions.add(HG_VERBOSE_CMD);
        if (enableFetchExtension && !"false".equals(System.getProperty("versioning.mercurial.enableFetchExtension"))) { //NOI18N
            command.additionalOptions.add(HG_CONFIG_OPTION_CMD);
            command.additionalOptions.add(HG_FETCH_EXT_CMD);
        }
        if (HgModuleConfig.getDefault().isInternalMergeToolEnabled()) {
            command.additionalOptions.add(HG_CONFIG_OPTION_CMD);
            command.additionalOptions.add(HG_MERGE_SIMPLE_TOOL);
        }
        command.showSaveOption = true;
        command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PULL_VALUE, HgConfigFiles.HG_DEFAULT_PULL};

        try {
            // remove when rebase is implemented
            if ("false".equals(System.getProperty("versioning.mercurial.enableFetchExtension"))) { //NOI18N
                doNotAddHgPlain.set(true);
            }
            return command.invoke();
        } finally {
            doNotAddHgPlain.remove();
        }
    }

    public static List<HgLogMessage> processLogMessages (File root, List<File> files, List<String> list) {
        return processLogMessages(root, files, list, false);
    }

    public static List<HgLogMessage> processLogMessages (File root, List<File> files, List<String> list, boolean revertOrder) {
        List<HgLogMessage> messages = new ArrayList<HgLogMessage>();
        String rev, author, username, desc, date, id, parents, fm, fa, fd, fc, branches, tags;
        List<String> filesShortPaths = new ArrayList<String>();

        String rootPath = root.getAbsolutePath();
        if (!rootPath.endsWith(File.separator)) {
            rootPath = rootPath + File.separator;
        }
        if (list != null && !list.isEmpty()) {
            if (files != null) {
                for (File f : files) {
                    if (!f.isFile()) {
                        continue;
                    }
                    String shortPath = f.getAbsolutePath();
                    if (shortPath.startsWith(rootPath) && shortPath.length() > rootPath.length()) {
                        if (Utilities.isWindows()) {
                            filesShortPaths.add(shortPath.substring(rootPath.length()).replace(File.separatorChar, '/')); // NOI18N
                        } else {
                            filesShortPaths.add(shortPath.substring(rootPath.length())); // NOI18N
                        }
                    }
                }
            }

            rev = author = username = desc = date = id = parents = fm = fa = fd = fc = null;
            branches = tags = "";
            boolean bEnd = false;
            boolean stillInMessage = false; // commit message can have multiple lines !!!
            for (String s : list) {
                if (s.indexOf(HG_LOG_REVISION_OUT) == 0) {
                    rev = s.substring(HG_LOG_REVISION_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_AUTHOR_OUT) == 0) {
                    author = s.substring(HG_LOG_AUTHOR_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_USER_OUT) == 0) {
                    username = s.substring(HG_LOG_USER_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_DESCRIPTION_OUT) == 0) {
                    desc = s.substring(HG_LOG_DESCRIPTION_OUT.length()).trim();
                    stillInMessage = true;
                } else if (s.indexOf(HG_LOG_DATE_OUT) == 0) {
                    date = s.substring(HG_LOG_DATE_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_ID_OUT) == 0) {
                    id = s.substring(HG_LOG_ID_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_PARENTS_OUT) == 0) {
                    parents = s.substring(HG_LOG_PARENTS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEMODS_OUT) == 0) {
                    fm = s.substring(HG_LOG_FILEMODS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEADDS_OUT) == 0) {
                    fa = s.substring(HG_LOG_FILEADDS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEDELS_OUT) == 0) {
                    fd = s.substring(HG_LOG_FILEDELS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILECOPIESS_OUT) == 0) {
                    fc = s.substring(HG_LOG_FILECOPIESS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_BRANCHES_OUT) == 0) {
                    branches = s.substring(HG_LOG_BRANCHES_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_TAGS_OUT) == 0) {
                    tags = s.substring(HG_LOG_TAGS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_ENDCS_OUT) == 0) {
                    stillInMessage = false;
                    bEnd = true;
                } else {
                    if (stillInMessage) {
                        // add next lines of commit message
                        desc += "\n" + s;
                    }
                }

                if (rev != null & bEnd) {
                    HgLogMessage hgMsg = new HgLogMessage(rootPath, filesShortPaths, rev, author, username, desc, date, id, parents, fm, fa, fd, fc, branches, tags);
                    messages.add(hgMsg);
                    rev = author = desc = date = id = parents = fm = fa = fd = fc = null;
                    bEnd = false;
                }
            }
        }
        if (revertOrder) {
            Collections.reverse(messages);
        }
        return messages;
    }

    private static HgBranch[] processBranches (List<String> lines, List<HgLogMessage> heads) {
        List<HgBranch> branches = new ArrayList<HgBranch>();
        Pattern p = Pattern.compile("^(.+)(\\b\\d+):(\\S+)(.*)$"); //NOI18N
        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (!m.matches()){
                Mercurial.LOG.log(Level.WARNING, "HgCommand.processBranches(): Failed when matching: {0}", new Object[] { line }); //NOI18N
            } else {
                String branchName = m.group(1).trim();
                String revNumber = m.group(2).trim();
                String changeSetId = m.group(3).trim();
                String status = m.group(4).trim().toLowerCase();
                HgLogMessage info = null;
                for (HgLogMessage head : heads) {
                    if (head.getRevisionNumber().equals(revNumber) || head.getCSetShortID().equals(changeSetId)) {
                        info = head;
                    }
                }
                if (info == null) {
                    Mercurial.LOG.log(Level.WARNING, "HgCommand.processBranches(): Failed when pairing branch with head info : {0}:{1}:{2}\n{3}", //NOI18N
                            new Object[] { branchName, revNumber, changeSetId, heads });
                } else {
                    boolean closed = false;
                    boolean active = true;
                    if (status.contains("inactive")) { //NOI18N
                        active = false;
                    } else if (status.contains("closed")) { //NOI18N
                        closed = true;
                    }
                    branches.add(new HgBranch(branchName, info, closed, active));
                }
            }
        }
        return branches.toArray(new HgBranch[0]);
    }

    private static HgTag[] processTags (List<String> lines, File repository, OutputLogger logger) throws HgException {
        class TagInfo {
            String name;
            String revNumber;
            String changeSetId;
            boolean local;

            private TagInfo (String tagName, String revNumber, String changeSetId, boolean local) {
                this.name = tagName;
                this.revNumber = revNumber;
                this.changeSetId = changeSetId;
                this.local = local;
            }
        }
        List<HgTag> tags = new ArrayList<HgTag>();
        Pattern p = Pattern.compile("^(.+)(\\b\\d+):(\\S+)(.*)$"); //NOI18N
        List<TagInfo> tagInfos = new ArrayList<TagInfo>(lines.size());
        List<String> revisions = new ArrayList<String>(lines.size());
        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (!m.matches()) {
                Mercurial.LOG.log(Level.WARNING, "HgCommand.processTags(): Failed when matching: {0}", new Object[] { line }); //NOI18N
            } else {
                String tagName = m.group(1).trim();
                String revNumber = m.group(2).trim();
                String changeSetId = m.group(3).trim();
                String status = m.group(4).trim().toLowerCase();
                boolean local = false;
                if (status.contains("local")) { //NOI18N
                    local = true;
                }
                tagInfos.add(new TagInfo(tagName, revNumber, changeSetId, local));
                revisions.add(revNumber);
            }
        }
        List<String> list = doLog(repository, revisions, -1, logger);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
             } else if (isErrorAbort(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
             }
        }
        List<HgLogMessage> messages = processLogMessages(repository, null, list, false);
        for (TagInfo t : tagInfos) {
            HgLogMessage info = null;
            for (HgLogMessage head : messages) {
                if (head.getRevisionNumber().equals(t.revNumber) || head.getCSetShortID().equals(t.changeSetId)) {
                    info = head;
                }
            }
            if (info == null) {
                Mercurial.LOG.log(Level.WARNING, "HgCommand.processTags(): Failed when pairing tag with commit info : {0}:{1}:{2}\n{3}", //NOI18N
                        new Object[] { t.name, t.revNumber, t.changeSetId, messages });
            } else {
                tags.add(new HgTag(t.name, info, t.local, !HG_TIP_CONST.equals(t.name)));
            }
        }
        return tags.toArray(new HgTag[0]);
    }

    public static HgLogMessage[] getIncomingMessages(final File root, String toRevision, String branchName,
            boolean bShowMerges, boolean bGetFileInfo, boolean getParents,
            int limitRevisions, OutputLogger logger) throws HgException {
        List<HgLogMessage> messages = Collections.<HgLogMessage>emptyList();

        try {
            List<String> list = HgCommand.doIncomingForSearch(root, toRevision, branchName, bShowMerges, bGetFileInfo, getParents, limitRevisions, logger);
            messages = processLogMessages(root, null, list, true);
        } finally {
            logger.closeLog();
        }

        return messages.toArray(new HgLogMessage[0]);
    }

    public static HgLogMessage[] getOutMessages(final File root, String toRevision, String branchName,
            boolean bShowMerges, boolean getParents, int limitRevisions, OutputLogger logger) throws HgException {
        List<HgLogMessage> messages = Collections.<HgLogMessage>emptyList();

        try {
            List<String> list = HgCommand.doOutForSearch(root, toRevision, branchName, bShowMerges, getParents, limitRevisions, logger);
            messages = processLogMessages(root, null, list, true);
        } finally {
            logger.closeLog();
        }

        return messages.toArray(new HgLogMessage[0]);
    }

    public static HgLogMessage[] getLogMessagesNoFileInfo(final File root, final Set<File> files, String fromRevision, String toRevision, boolean bShowMerges, int limitRevisions, List<String> branchNames, OutputLogger logger) {
         return getLogMessages(root, files, fromRevision, toRevision, bShowMerges, false, true, limitRevisions, branchNames, logger, true);
    }

    public static HgLogMessage[] getLogMessagesNoFileInfo(final File root, final Set<File> files, int limit, OutputLogger logger) {
         return getLogMessages(root, files, "0", HG_STATUS_FLAG_TIP_CMD, true, false, true, limit, Collections.<String>emptyList(), logger, false);
    }

    public static HgLogMessage[] getLogMessages(final File root,
            final Set<File> files, String fromRevision, String toRevision,
            boolean bShowMerges,  boolean bGetFileInfo, boolean getParents, int limit, List<String> branchNames, OutputLogger logger, boolean ascOrder) {
        List<HgLogMessage> messages = Collections.<HgLogMessage>emptyList();

        try {
            String headRev = HgCommand.getLastRevision(root, null);
            if (headRev == null) {
                return messages.toArray(new HgLogMessage[0]);
            }

            List<File> filesList = files != null ? new ArrayList<File>(files) : null;
            List<String> list = HgCommand.doLog(root,
                    filesList,
                    fromRevision, toRevision, headRev, bShowMerges, bGetFileInfo, getParents, limit, branchNames, logger);
            messages = processLogMessages(root, filesList, list, ascOrder);
        } catch (HgException.HgCommandCanceledException ex) {
            // do not take any action
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            if(logger != null) {
                logger.closeLog();
            }
        }

        return messages.toArray(new HgLogMessage[0]);
   }
    
    public static HgLogMessage[] getRevisionInfo (File root, List<String> revisions, OutputLogger logger) {
        List<HgLogMessage> messages = Collections.<HgLogMessage>emptyList();

        try {
            List<String> list = HgCommand.doLog(root, revisions, -1, logger);
            messages = processLogMessages(root, null, list, false);
        } catch (HgException.HgCommandCanceledException ex) {
            // do not take any action
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            if(logger != null) {
                logger.closeLog();
            }
        }

        return messages.toArray(new HgLogMessage[0]);
    }

    /**
     * Determines whether anything has been committed to the repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return Boolean which is true if the repository has revision history.
     */
    public static Boolean hasHistory(File repository) {
        if (repository == null ) return false;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_ONE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        try {
            List<String> list = exec(command);
            if (!list.isEmpty() && isErrorAbort(list.get(0)))
                return false;
            else
                return !list.isEmpty();
        } catch (HgException e) {
            return false;
        }
    }

    /**
     * Determines the previous name of the specified file
     * We make the assumption that the previous file name is in the
     * cmdOutput of files returned by hg log command immediately befor
     * the file we started with.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose previous name is required
     * @param String revision which the revision to start from.
     * @return File for the previous name of the file
     */
    private static File getPreviousName(File repository, File file, String revision, boolean tryHard) throws HgException {
        if (repository == null ) return null;
        if (revision == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_FOLLOW);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_REV_CMD);
        command.add(revision);

        List<String> list = null;
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_ONLY_FILE_COPIES_CHANGESET_NAME));
            command.add(file.getAbsolutePath());
            list = exec(command);
            if (list.isEmpty() || isErrorAbort(list.get(0))) {
                return null;
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } catch (HgException e) {
            Mercurial.LOG.log(Level.WARNING, "command: {0}", HgUtils.replaceHttpPassword(command)); // NOI18N
            Mercurial.LOG.log(e instanceof HgException.HgCommandCanceledException ? Level.FINE : Level.INFO, null, e); // NOI18N
            throw e;
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
        if(!tryHard) {
            String[] fileNames = list.get(0).split("\t");
            for (int j = 0; j < fileNames.length / 2; ++j) {
                File name = new File(repository, fileNames[2 * j]);
                if (name.equals(file)) {
                    return new File(repository, fileNames[2 * j + 1]);
                }
            }
        }
        return null;
    }

    /**
     * Retrives the log information for the specified files.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<File> of files which revision history is to be retrieved.
     * @param String Template specifying how output should be returned
     * @param boolean flag indicating if debug param should be used - required to get all file mod, add, del info
     * @param branchNames list of branches you want to browse - equiv to --branch
     * @return List<String> cmdOutput of the log entries for the specified file.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    private static List<String> doLog(File repository, List<File> files,
            String from, String to, String headRev, boolean bShowMerges, boolean bGetFileInfo, boolean getAllParents, int limit, List<String> branchNames, OutputLogger logger) throws HgException {
        List<String> dateConstraints = new ArrayList<String>();
        String dateStr = handleRevDates(from, to);
        if (dateStr != null) {
            dateConstraints.add(HG_FLAG_DATE_CMD);
            dateConstraints.add(dateStr);
        }
        List<String> list = null;
        // try first without and then with limiting the rev numbers
        for (String lastRev : new String[] { null, headRev }) {
            String revStr = handleRevNumbers(from, to, lastRev);
            if (revStr == null) {
                // from is probably higher than head revision, it's useless to run the command
                return Collections.emptyList();
            }
            List<String> constraints = new ArrayList<String>(dateConstraints);
            if (dateStr == null) {
                constraints.add(HG_FLAG_REV_CMD);
                constraints.add(revStr);
            }
            list = doLog(repository, files, constraints, bShowMerges, bGetFileInfo, getAllParents, limit, branchNames, logger);
            if (list.size() > 0 && lastRev == null && isNoRevStrip(list.get(0))) {
                // try again
            } else {
                break;
            }
        }
        return list;
    }

    private static List<String> doLog(File repository, List<String> revisions, int limit, OutputLogger logger) throws HgException {
        List<String> constraints = new ArrayList<String>(revisions.size() * 2);
        for (String rev : revisions) {
            constraints.add(HG_FLAG_REV_CMD);
            constraints.add(rev);
        }
        return doLog(repository, null, constraints, true, false, false, limit, Collections.<String>emptyList(), logger);
    }

    private static List<String> doLog(File repository, List<File> files,
            List<String> revisionConstraints, boolean bShowMerges, boolean bGetFileInfo, boolean getParents, int limit, List<String> branchNames, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        if (files != null && files.isEmpty()) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_VERBOSE_CMD);
        if (limit > 0) {
                command.add(HG_LOG_LIMIT_CMD);
                command.add(Integer.toString(limit));
        }
        boolean doFollow = false;
        if( files != null){
            doFollow = true;
            for (File f : files) {
                if (f.isDirectory()) {
                    doFollow = false;
                    break;
                }
            }
        }
        if (doFollow) {
            command.add(HG_OPT_FOLLOW);
        }
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (getParents) {
            command.add(HG_LOG_DEBUG_CMD);
        }

        for (String rc : revisionConstraints) {
            command.add(rc);
        }
        
        for (String branch : branchNames) {
            command.add(HG_PARAM_BRANCH);
            command.add(branch);
        }

        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, bGetFileInfo ? HG_LOG_FULL_CHANGESET_NAME : HG_LOG_BASIC_CHANGESET_NAME));

            if (files != null) {
                for (File f : files) {
                    command.add(f.getAbsolutePath());
                }
            }
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isFollowNotAllowed(list.get(0))) {
                    // nothing
                } else if (isErrorAbort(list.get(0))) {
                    if (isNoRevStrip(list.get(0))) {
                        // nothing, return error in the list
                    } else {
                        handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                    }
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    /**
     * Retrives the tip information for the specified repository, as defined by the LOG_TEMPLATE.
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the log entries for the tip
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static HgLogMessage doTip(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_TIP_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                 } else if (isErrorAbort(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                 }
            }
            List<HgLogMessage> messages = processLogMessages(repository, null, list, false);
            return messages.get(0);
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }


    /**
     * Retrives the Out information for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the out entries for the specified repo.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doOutForSearch(File repository, String to, String branchName, boolean bShowMerges, boolean getParents, int limit, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        String defaultPush = new HgConfigFiles(repository).getDefaultPush(false);
        if (HgUtils.isNullOrEmpty(defaultPush)) {
            Mercurial.LOG.log(Level.FINE, "No push url, falling back to command without target");
        } else {
            try {
                HgURL pushUrl = new HgURL(defaultPush);
                return doOutForSearch(repository, pushUrl, to, branchName, bShowMerges, getParents, limit, logger);
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.INFO, "Invalid push url: {0}, falling back to command without target", defaultPush);
            }
        }

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_OUT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        if (getParents) {
            command.add(HG_LOG_DEBUG_CMD);
        }
        String revStr = handleIncomingRev(to);
        if(revStr != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revStr);
        }
        if (branchName != null) {
            command.add(HG_PARAM_BRANCH);
            command.add(branchName);
        }
        if (limit > 0) {
            command.add(HG_LOG_LIMIT_CMD);
            command.add(Integer.toString(limit));
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            List<String> list;
            String proxy = getGlobalProxyIfNeeded(defaultPush, false, null);
            if(proxy != null){
                List<String> env = new ArrayList<String>();
                env.add(HG_PROXY_ENV + proxy);
                list = execEnv(command, env);
            }else{
                list = exec(command);
            }
            if (!list.isEmpty()) {
                if(isErrorNoDefaultPush(list.get(0))){
                    // Ignore
                }else if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }
    
    private static List<String> doOutForSearch (File repository, HgURL repositoryUrl, String to, String branchName,
            boolean bShowMerges, boolean getParents, int limit, OutputLogger logger) throws HgException {
        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPush(false);
        command.hgCommandType = HG_OUTGOING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = repositoryUrl;
        command.repository = repository;
        command.additionalOptions.add(HG_OPT_REPOSITORY);
        command.additionalOptions.add(repository.getAbsolutePath());
        command.additionalOptions.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.additionalOptions.add(HG_LOG_NO_MERGES_CMD);
        }
        if (getParents) {
            command.additionalOptions.add(HG_LOG_DEBUG_CMD);
        }
        String revStr = handleIncomingRev(to);
        if(revStr != null){
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revStr);
        }
        if (branchName != null) {
            command.additionalOptions.add(HG_PARAM_BRANCH);
            command.additionalOptions.add(branchName);
        }
        if (limit > 0) {
            command.additionalOptions.add(HG_LOG_LIMIT_CMD);
            command.additionalOptions.add(Integer.toString(limit));
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.additionalOptions.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            command.showSaveOption = true;
            command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PUSH};
            return command.invoke();
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

        /**
     * Retrives the Incoming changeset information for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the out entries for the specified repo.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncomingForSearch(File repository, String to, String branchName,
            boolean bShowMerges, boolean bGetFileInfo, boolean getParents,
            int limit, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
        if (HgUtils.isNullOrEmpty(defaultPull)) {
            Mercurial.LOG.log(Level.FINE, "No pull url, falling back to command without target");
        } else {
            try {
                HgURL pullUrl = new HgURL(defaultPull);
                return doIncomingForSearch(repository, pullUrl, to, branchName, bShowMerges, bGetFileInfo, getParents, limit, logger);
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.INFO, "Invalid pull url: {0}, falling back to command without target", defaultPull);
            }
        }

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        if (getParents) {
            command.add(HG_LOG_DEBUG_CMD);
        }
        String revStr = handleIncomingRev(to);
        if(revStr != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revStr);
        }
        if (branchName != null) {
            command.add(HG_PARAM_BRANCH);
            command.add(branchName);
        }
        if (limit > 0) {
            command.add(HG_LOG_LIMIT_CMD);
            command.add(Integer.toString(limit));
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, bGetFileInfo ? HG_LOG_FULL_CHANGESET_NAME : HG_LOG_BASIC_CHANGESET_NAME));
            List<String> list;
            String proxy = getGlobalProxyIfNeeded(defaultPull, false, null);
            if (proxy != null) {
                List<String> env = new ArrayList<String>();
                env.add(HG_PROXY_ENV + proxy);
                list = execEnv(command, env);
            } else {
                list = exec(command);
            }

            if (!list.isEmpty()) {
                if (isErrorNoDefaultPath(list.get(0))) {
                    // Ignore
                } else if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0)) || isErrorAbort(list.get(list.size() - 1))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    public static List<HgLogMessage> getBundleChangesets (File repository, File bundleFile, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(bundleFile.getAbsolutePath());
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            List<String> list;
            list = exec(command);

            if (!list.isEmpty()) {
                if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0)) || isErrorAbort(list.get(list.size() - 1))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return processLogMessages(repository, null, list);
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }
    
    private static List<String> doIncomingForSearch (File repository, HgURL repositoryUrl, String to, String branchName,
            boolean bShowMerges, boolean bGetFileInfo, boolean getParents, int limit, OutputLogger logger) throws HgException {
        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_INCOMING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = repositoryUrl;
        command.repository = repository;
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.additionalOptions.add(HG_OPT_REPOSITORY);
        command.additionalOptions.add(repository.getAbsolutePath());
        command.additionalOptions.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.additionalOptions.add(HG_LOG_NO_MERGES_CMD);
        }
        if (getParents) {
            command.additionalOptions.add(HG_LOG_DEBUG_CMD);
        }
        String revStr = handleIncomingRev(to);
        if(revStr != null){
            command.additionalOptions.add(HG_FLAG_REV_CMD);
            command.additionalOptions.add(revStr);
        }
        if (branchName != null) {
            command.additionalOptions.add(HG_PARAM_BRANCH);
            command.additionalOptions.add(branchName);
        }
        if (limit > 0) {
            command.additionalOptions.add(HG_LOG_LIMIT_CMD);
            command.additionalOptions.add(Integer.toString(limit));
        }
        command.showSaveOption = true;
        command.urlPathProperties = new String[] {HgConfigFiles.HG_DEFAULT_PULL_VALUE, HgConfigFiles.HG_DEFAULT_PULL};
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.additionalOptions.add(prepareLogTemplate(tempFolder, bGetFileInfo ? HG_LOG_FULL_CHANGESET_NAME : HG_LOG_BASIC_CHANGESET_NAME));
            return command.invoke();
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    private static String handleRevDates(String from, String to){
        // Check for Date range:
        Date fromDate = null;
        Date toDate = null;
        Date currentDate = new Date(); // Current Date
        Date epochPlusOneDate = null;

        try {
            epochPlusOneDate = new SimpleDateFormat("yyyy-MM-dd").parse(HG_EPOCH_PLUS_ONE_YEAR); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // Set From date
        try {
            if(from != null)
                fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(from); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // Set To date
        try {
            if(to != null)
                toDate = new SimpleDateFormat("yyyy-MM-dd").parse(to); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // If From date is set, but To date is not - default To date to current date
        if( fromDate != null && toDate == null && to == null){
            toDate = currentDate;
            to = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
        }
        // If To date is set, but From date is not - default From date to 1971-01-01
        if (fromDate == null && from == null  && toDate != null) {
            fromDate = epochPlusOneDate;
            from = HG_EPOCH_PLUS_ONE_YEAR; // NOI18N
        }

        // If using dates make sure both From and To are set to dates
        if( (fromDate != null && toDate == null && to != null) ||
                (fromDate == null && from != null && toDate != null)){
            HgUtils.warningDialog(HgCommand.class,"MSG_SEARCH_HISTORY_TITLE",// NOI18N
                    "MSG_SEARCH_HISTORY_WARN_BOTHDATES_NEEDED_TEXT");   // NOI18N
            return null;
        }

        if(fromDate != null && toDate != null){
            // Check From date - default to 1971-01-01 if From date is earlier than this
            if(epochPlusOneDate != null && fromDate.before(epochPlusOneDate)){
                fromDate = epochPlusOneDate;
                from = HG_EPOCH_PLUS_ONE_YEAR; // NOI18N
            }
            // Set To date - default to current date if To date is later than this
            if(currentDate != null && toDate.after(currentDate)){
                toDate = currentDate;
                to = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
            }

            // Make sure the From date is before the To date
            if( fromDate.after(toDate)){
                HgUtils.warningDialog(HgCommand.class,"MSG_SEARCH_HISTORY_TITLE",// NOI18N
                        "MSG_SEARCH_HISTORY_WARN_FROM_BEFORE_TODATE_NEEDED_TEXT");   // NOI18N
                return null;
            }
            return from + " to " + to; // NOI18N
        }
        return null;
    }

    private static String handleIncomingRev(String to) {
        // Handle users entering head or tip for revision, instead of a number
        if (to != null && (to.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || to.equalsIgnoreCase(HG_HEAD_STR))) {
            to = HG_STATUS_FLAG_TIP_CMD;
        }
        return to;
    }

    /**
     *
     * @param from
     * @param to
     * @param headRev if not <code>null</code> then from and to are compared to headRev and a relevant value is returned
     * @return revision string or null if headRev is not null and from is outside of valid limits - e.g. from is higher than headRev
     */
    private static String handleRevNumbers(String from, String to, String headRev){
        int fromInt = -1;
        int toInt = -1;
        int headRevInt = -1;

        // Handle users entering head or tip for revision, instead of a number
        if(headRev != null && from != null && (from.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || from.equalsIgnoreCase(HG_HEAD_STR)))
            from = headRev;
        if(headRev != null && to != null && (to.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || to.equalsIgnoreCase(HG_HEAD_STR)))
            to = headRev;

        try{
            fromInt = Integer.parseInt(from);
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }
        try{
            toInt = Integer.parseInt(to);
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }
        try{
            if (headRev != null) {
                headRevInt = Integer.parseInt(headRev);
            }
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }

        // Handle out of range revisions
        if (headRevInt > -1 && toInt > headRevInt) {
            to = headRev;
            toInt = headRevInt;
        }
        if (headRevInt > -1 && fromInt > headRevInt) {
            return null;
        }

        // Handle revision ranges
        String revStr = null;
        if (fromInt > -1 && toInt > -1){
            revStr = to + ":" + from;
        }else if (fromInt > -1){
            revStr = (headRevInt != -1 ? headRevInt + ":" : "tip:") + from;
        }else if (toInt > -1){
            revStr = to + ":0";
        }

        if(revStr == null) {
            if(to == null) {
                to = HG_STATUS_FLAG_TIP_CMD;
            }
            if(from == null) {
                from = "0";
            }
            revStr = to + ":" + from;
        }

        return revStr;
    }
    /**
     * Retrieves the base revision of the specified file to the
     * specified output file.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file in the mercurial repository
     * @param File outFile to contain the contents of the file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCat(File repository, File file, File outFile, OutputLogger logger) throws HgException {
        doCat(repository, file, outFile, null, true, logger); //NOI18N
    }

    public static void doCat(File repository, File file, File outFile, String revision, OutputLogger logger) throws HgException {
        doCat(repository, file, outFile, revision, logger, true);
    }
    
    /**
     * Retrieves the specified revision of the specified file to the
     * specified output file.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file in the mercurial repository
     * @param File outFile to contain the contents of the file
     * @param String of revision for the revision of the file to be
     * printed to the output file.
     * @return List<String> cmdOutput of all the log entries
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCat(File repository, File file, File outFile, String revision, OutputLogger logger, boolean tryHard) throws HgException {
        doCat(repository, file, outFile, revision, true, logger); //NOI18N
    }

    public static void doCat(File repository, File file, File outFile, String revision, boolean retry, OutputLogger logger) throws HgException {
        doCat(repository, file, outFile, revision, retry, logger, true);
    }
    
    public static void doCat(File repository, File file, File outFile, String revision, boolean retry, OutputLogger logger, boolean tryHard) throws HgException {
        if (repository == null) return;
        if (file == null) return;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CAT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_OUTPUT_CMD);
        command.add(outFile.getAbsolutePath());

        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }
        try {
            // cmd returns error if there are simlinks in absolute path and file is deleted
            // abort: /path/file not under root
            command.add(file.getCanonicalPath());
        } catch (IOException e) {
            Mercurial.LOG.log(Level.WARNING, "command: {0}", HgUtils.replaceHttpPassword(command)); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, e); // NOI18N
            throw new HgException(e.getMessage());
        }
        List<String> list = exec(command);

        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
             } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
             }
        }
        if (outFile.length() == 0 && retry) {
            if (revision == null) {
                // maybe the file is copied?
                FileInformation fi = getStatus(repository, Collections.singletonList(file), null, null, true).get(file);
                if (fi != null && fi.getStatus(null) != null && fi.getStatus(null).getOriginalFile() != null) {
                    doCat(repository, fi.getStatus(null).getOriginalFile(), outFile, revision, false, logger);
                }
            } else {
                // Perhaps the file has changed its name
                try {
                    String newRevision = Integer.toString(Integer.parseInt(revision)+1);
                    File prevFile = getPreviousName(repository, file, newRevision, tryHard);
                    if (prevFile != null) {
                        doCat(repository, prevFile, outFile, revision, false, logger); //NOI18N
                    }
                } catch (NumberFormatException ex) {
                    // revision is not a number
                }
            }
        }
    }

    /**
     * Get common ancestor for two provided revisions.
     *
     * @param root for the mercurial repository
     * @param first rev to get ancestor for
     * @param second rev to get ancestor for
     * @param output logger
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static HgRevision getCommonAncestor(String rootURL, String rev1, String rev2, OutputLogger logger) throws HgException {
        HgRevision res = getCommonAncestor(rootURL, rev1, rev2, false, logger);
        if( res == null){
            res = getCommonAncestor(rootURL, rev1, rev2, true, logger);
        }
        return res;
    }

    private static HgRevision getCommonAncestor(String rootURL, String rev1, String rev2, boolean bUseIndex, OutputLogger logger) throws HgException {
        if (rootURL == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_COMMONANCESTOR_CMD);
        if(bUseIndex){
            command.add(".hg/store/00changelog.i"); //NOI18N
        }
        command.add(rev1);
        command.add(rev2);
        command.add(HG_OPT_REPOSITORY);
        command.add(rootURL);
        command.add(HG_OPT_CWD_CMD);
        command.add(rootURL);

        List<String> list = exec(command);
        if (!list.isEmpty()){
            String splits[] = list.get(0).split(":"); // NOI18N
            String tmp = splits != null && splits.length >= 1 ? splits[0]: null;
            String tmpId = splits != null && splits.length >= 2 ? splits[1]: null;
            int tmpRev = -1;
            try{
                tmpRev = Integer.parseInt(tmp);
            }catch(NumberFormatException ex){
                // Ignore
            }
            return tmpRev > -1 ? new HgRevision(tmpId, tmp): null;
        } else {
            return null;
        }
    }

    /**
     * Initialize a new repository in the given directory.  If the given
     * directory does not exist, it is created. Will throw a HgException
     * if the repository already exists.
     *
     * @param root for the mercurial repository
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCreate(File root, OutputLogger logger) throws HgException {
        if (root == null ) return;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CREATE_CMD);
        command.add(root.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_CREATE_FAILED"), logger);
    }

    /**
     * Clone an exisiting repository to the specified target directory
     *
     * @param File repository of the mercurial repository's root directory
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(File repository, File target, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        return doClone(new HgURL(repository), target, logger);
    }

    /**
     * Clone a repository to the specified target directory
     *
     * @param String repository of the mercurial repository
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(HgURL repository, File target, OutputLogger logger) throws HgException {
        if (repository == null || target == null) return null;

        // Ensure that parent directory of target exists, creating if necessary
        File parentTarget = target.getParentFile();
        try {
            if (!parentTarget.mkdirs()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : {0}", parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : {0} threw SecurityException {1}", new Object[]{parentTarget.getAbsolutePath(), e.getMessage()}); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }

        List<String> list = null;
        boolean retry = true;
        PasswordAuthentication credentials = null;
        String rawUrl = repository.toUrlStringWithoutUserInfo();
        HgURL url = repository;
        while (retry) {
            retry = false;
            try {
                if (credentials != null) {
                    url = new HgURL(repository.toHgCommandUrlString(), credentials.getUserName(), credentials.getPassword());
                }
            } catch (URISyntaxException ex) {
                // this should NEVER happen
                Mercurial.LOG.log(Level.SEVERE, null, ex);
                break;
            }
            List<Object> command = new ArrayList<Object>();

            command.add(getHgCommand());
            command.add(HG_CLONE_CMD);
            command.add(HG_VERBOSE_CMD);
            command.add(url);
            command.add(target); // target must be the last argument

            String proxy = getGlobalProxyIfNeeded(url.toUrlStringWithoutUserInfo(), true, logger);
            if (proxy != null) {
                List<String> env = new ArrayList<String>();
                env.add(HG_PROXY_ENV + proxy);
                list = execEnv(command, env);
            } else {
                list = exec(command);
            }
            try {
                if (!list.isEmpty()) {
                    if (isErrorNoRepository(list.get(0))) {
                        handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                    } else if (isErrorNoResponse(list.get(list.size() - 1))) {
                        handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_RESPONSE_ERR"), logger);
                    } else if (isErrorAbort(list.get(0)) || isErrorAbort(list.get(list.size() - 1))) {
                        if ((credentials = handleAuthenticationError(list, target, rawUrl, credentials == null ? "" : credentials.getUserName(), new UserCredentialsSupport(), HG_CLONE_CMD)) != null) { //NOI18N
                            // try again with new credentials
                            retry = true;
                        } else {
                            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                        }
                    } else {
                        // save credentials
                        if (url.getPassword() != null) {
                            try {
                                HgModuleConfig.getDefault().setProperty(target, HgConfigFiles.HG_DEFAULT_PULL,
                                        new HgURL(url.toUrlStringWithoutUserInfo(), url.getUsername(), null).toCompleteUrlString());
                            } catch (URISyntaxException ex) {
                                Mercurial.LOG.log(Level.INFO, null, ex);
                            } catch (IOException ex) {
                                Mercurial.LOG.log(Level.INFO, null, ex);
                            }
                            KeyringSupport.save(HgUtils.PREFIX_VERSIONING_MERCURIAL_URL, url.toHgCommandStringWithNoPassword(), url.getPassword().clone(), null);
                        }
                    }
                }
            } finally {
                if (url != repository) {
                    url.clearPassword();
                }
            }
        }
        return list;
    }

    /**
     * Commits the cmdOutput of Locally Changed files to the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<files> of files to be committed to hg
     * @param String for commitMessage
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCommit(File repository, List<File> commitFiles, String commitMessage, OutputLogger logger)  throws HgException {
        doCommit(repository, commitFiles, commitMessage, null, false, logger);
    }

    /**
     * Commits the cmdOutput of Locally Changed files to the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<files> of files to be committed to hg
     * @param commitMessage for commitMessage
     * @param user author of the commit or <code>null</code> if to use the default committer
     * @param closeBranch runs commit with --close-branch option
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCommit(File repository, List<File> commitFiles, String commitMessage, String user,
            boolean closeBranch, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_COMMIT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        if (user == null) {
            String projectUserName = new HgConfigFiles(repository).getUserName(false);
            String globalUsername = HgModuleConfig.getDefault().getSysUserName();
            if (projectUserName != null && projectUserName.length() > 0) {
                user = projectUserName;
            } else if (globalUsername != null && globalUsername.length() > 0) {
                user = globalUsername;
            }
        }

        if(user != null ){
            command.add(HG_OPT_USERNAME);
            command.add(user);
        }
        
        if (closeBranch) {
            command.add(HG_OPT_CLOSE_BRANCH);
        }

        File tempfile = null;

        try {
            if (commitMessage == null || commitMessage.length() == 0) {
                commitMessage = HG_COMMIT_DEFAULT_MESSAGE;
            }
            // Create temporary file.
            tempfile = Files.createTempFile(HG_COMMIT_TEMPNAME, HG_COMMIT_TEMPNAME_SUFFIX).toFile();

            // Write to temp file
            BufferedWriter out = new BufferedWriter(ENCODING == null 
                    ? new OutputStreamWriter(new FileOutputStream(tempfile)) 
                    : new OutputStreamWriter(new FileOutputStream(tempfile), ENCODING));
            out.write(commitMessage);
            out.close();

            command.add(HG_COMMIT_OPT_LOGFILE_CMD);
            command.add(tempfile.getAbsolutePath());
            String repoPath = repository.getAbsolutePath();
            if (!repoPath.endsWith(File.separator)) {
                repoPath = repoPath + File.separator;
            }
            for(File f: commitFiles){
                if (f.getAbsolutePath().length() <= repoPath.length()) {
                    // list contains the root itself
                    command.add(f.getAbsolutePath());
                } else {
                    command.add(f.getAbsolutePath().substring(repoPath.length()));
                }
            }
            if(Utilities.isWindows()) {
                int size = 0;
                // Count size of command
                for (String line : command) {
                    size += line.length();
                }
                if (isTooLongCommand(size)) {
                    throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_ARG_LIST_TOO_LONG_ERR", command.get(1), command.size() -2 )); //NOI18N
                }
            }
            List<String> list = exec(command);
            //#132984: range of issues with upgrade to Hg 1.0, new restriction whereby you cannot commit using explicit file names after a merge.
            if (!list.isEmpty() && isCommitAfterMerge(list.get(list.size() -1))) {
                throw new HgException(COMMIT_AFTER_MERGE);
            }

            if (!list.isEmpty()
                    && (isErrorNotTracked(list.get(0)) ||
                    isErrorCannotReadCommitMsg(list.get(0)) ||
                    isErrorAbort(list.get(list.size() -1)) ||
                    isErrorAbort(list.get(0))))
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMIT_FAILED"), logger);

        }catch (IOException ex){
            throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_FAILED_TO_READ_COMMIT_MESSAGE"));
        }finally{
            if (commitMessage != null && tempfile != null){
                tempfile.delete();
            }
        }
    }


    /**
     * Rename a source file to a destination file.
     * mercurial hg rename
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of sourceFile which was renamed
     * @param File of destFile to which sourceFile has been renaned
     * @param boolean whether to do a rename --after
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRename(File repository, File sourceFile, File destFile, OutputLogger logger)  throws HgException {
        doRename(repository, sourceFile, destFile, false, logger);
    }

    private static void doRename(File repository, File sourceFile, File destFile, boolean bAfter, OutputLogger logger)  throws HgException {
        if (repository == null) return;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_RENAME_CMD);
        if (bAfter) command.add(HG_RENAME_AFTER_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        String repoPath = repository.getAbsolutePath();
        if (!repoPath.endsWith(File.separator)) {
            repoPath = repoPath + File.separator;
        }
        command.add(sourceFile.getAbsolutePath().substring(repoPath.length()));
        command.add(destFile.getAbsolutePath().substring(repoPath.length()));

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            if (!bAfter || !isErrorAbortNoFilesToCopy(list.get(list.size() -1))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_RENAME_FAILED"), logger);
            }
        }
    }

    /**
     * Mark a source file as having been renamed to a destination file.
     * mercurial hg rename -A.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of sourceFile which was renamed
     * @param File of destFile to which sourceFile has been renaned
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRenameAfter(File repository, File sourceFile, File destFile, OutputLogger logger)  throws HgException {
       doRename(repository, sourceFile, destFile, true, logger);
    }

    /**
     * Copy a source file to a destination file.
     * mercurial hg copy
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of sourceFile which was copied
     * @param File of destFile to which sourceFile has been copied
     * @param boolean whether to do a copy --after
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCopy (File repository, File sourceFile, File destFile, boolean bAfter, OutputLogger logger)  throws HgException {
        if (repository == null) return;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_COPY_CMD);
        if (bAfter) command.add(HG_COPY_AFTER_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        command.add(sourceFile.getAbsolutePath().substring(repository.getAbsolutePath().length() + 1));
        command.add(destFile.getAbsolutePath().substring(repository.getAbsolutePath().length() + 1));

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            if (!bAfter || !isErrorAbortNoFilesToCopy(list.get(list.size() -1))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COPY_FAILED"), logger);
            }
        }
    }


    /**
     * Adds the cmdOutput of Locally New files to the mercurial Repository
     * Their status will change to added and they will be added on the next
     * mercurial hg add.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be added to hg
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doAdd(File repository, List<File> addFiles, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (addFiles.isEmpty()) return;
        List<String> basicCommand = new ArrayList<String>();
        basicCommand.add(getHgCommand());
        basicCommand.add(HG_ADD_CMD);
        basicCommand.add(HG_OPT_REPOSITORY);
        basicCommand.add(repository.getAbsolutePath());

        List<List<String>> attributeGroups = splitAttributes(repository, basicCommand, addFiles, false);
        for (List<String> attributes : attributeGroups) {
            List<String> command = new ArrayList<String>(basicCommand);
            command.addAll(attributes);
            List<String> list = exec(command);
            if (!list.isEmpty() && !isErrorAlreadyTracked(list.get(0)) && !isAddingLine(list.get(0))) {
                if (getFilesWithPerformanceWarning(list).isEmpty()) {
                    // XXX we could notify the user about the performance warning and abort the command
                    handleError(command, list, list.get(0), logger);
                }
            }
        }
    }

    /**
     * Reverts the cmdOutput of files in the mercurial Repository to the specified revision
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be reverted
     * @param String revision to be reverted to
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRevert(File repository, List<File> revertFiles,
            String revision, boolean doBackup, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (revertFiles.isEmpty()) return;

        final List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_REVERT_CMD);
        if(!doBackup){
            command.add(HG_REVERT_NOBACKUP_CMD);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }

        for(File f: revertFiles){
            command.add(f.getAbsolutePath());
        }
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoChangeNeeded(list.get(0)))
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_REVERT_FAILED"), logger);
    }

    /**
     * Removes newly added files and folders under revertFiles
     * @param repository
     * @param revertFiles
     * @param excludedPaths
     * @param logger
     * @throws HgException 
     */
    public static void doPurge (File repository, List<File> revertFiles, List<String> excludedPaths, OutputLogger logger) throws HgException {
        if (repository == null) return;

        final List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_PURGE_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_EXT_PURGE);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        for (String excluded : excludedPaths) {
            command.add(HG_OPT_EXCLUDE);
            command.add(excluded);
        }

        for (File f : revertFiles){
            command.add(f.getAbsolutePath());
        }
        List<String> list = exec(command);
        if (!list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_REVERT_FAILED"), logger);
    }

    /**
     * Adds a Locally New file to the mercurial Repository
     * The status will change to added and they will be added on the next
     * mercurial hg commit.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of file to be added to hg
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doAdd(File repository, File file, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (file == null) return;
        if (file.isDirectory()) return;
        // We do not look for file to ignore as we should not here
        // with a file to be ignored.

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ADD_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ALREADY_TRACKED"), logger);
    }

    /**
     * Get the annotations for the specified file
     *
     * @param File repository of the mercurial repository
     * @param File file to be annotated
     * @param String revision of the file to be annotated
     * @return List<String> cmdOutput of the annotated lines of the file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doAnnotate(File repository, File file, String revision, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ANNOTATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }
        command.add(HG_ANNOTATE_FLAGN_CMD);
        command.add(HG_ANNOTATE_FLAGU_CMD);
        command.add(HG_ANNOTATE_FLAGL_CMD);
        command.add(HG_OPT_FOLLOW);
        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
            } else if (isErrorNoSuchFile(list.get(0))) {
                // This can happen if we have multiple heads and the wrong
                // one was picked by default hg annotation
                if (revision == null) {
                    String rev = getLastRevision(repository, file);
                    if (rev != null) {
                        list = doAnnotate(repository, file, rev, logger);
                    } else {
                        list = null;
                    }
                } else {
                    list = null;
                }
            }
        }
        return list;
    }

    public static List<String> doAnnotate(File repository, File file, OutputLogger logger) throws HgException {
        return doAnnotate(repository, file, null, logger);
    }

    /**
     * Returns the current branch for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return current branch
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getBranch (File repository) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BRANCH_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return list.get(0);
        }else{
            return null;
        }
    }

    /**
     * Returns the revision number for the heads in a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> of revision numbers.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadRevisions(File repository) throws HgException {
        return getHeadInfo(repository, true, HG_REV_TEMPLATE_CMD, false);
    }

    /**
     * Returns the revision number for the heads in a repository
     *
     * @param String repository of the mercurial repository
     * @return List<String> of revision numbers.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadRevisions(String repository) throws HgException {
        return getHeadInfo(repository, true, HG_REV_TEMPLATE_CMD, false);
    }

    /**
     * Returns the info of heads in repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param onlyTopologicalHeads only topological heads, without any children
     * @return head info.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static HgLogMessage[] getHeadRevisionsInfo (File repository, boolean onlyTopologicalHeads, OutputLogger logger) throws HgException {
        List<String> list = getHeadInfo(repository, onlyTopologicalHeads, HG_LOG_BASIC_CHANGESET_NAME, true);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
             } else if (isErrorAbort(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
             }
        }
        List<HgLogMessage> messages = processLogMessages(repository, null, list, false);
        return messages.toArray(new HgLogMessage[0]);
    }

    public static HgBranch[] getBranches (File repository, OutputLogger logger) throws HgException {
        List<String> list = getHeadInfo(repository, false, HG_LOG_BASIC_CHANGESET_NAME, true);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
             } else if (isErrorAbort(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
             }
        }
        List<HgLogMessage> heads = processLogMessages(repository, null, list, false);
        list = getBranches(repository);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
             } else if (isErrorAbort(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
             }
        }
        return processBranches(list, heads);
    }

    public static void markBranch (File repository, String branchName, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_BRANCH_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(branchName);
        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
            } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
            }
        }
        WorkingCopyInfo.refreshAsync(repository);
    }

    public static HgTag[] getTags (File repository, OutputLogger logger) throws HgException {
        List<String> list = getTags(repository);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
             } else if (isErrorAbort(list.get(0))) {
                handleError(null, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
             }
        }
        return processTags(list, repository, logger);
    }

    public static void createTag (File repository, String tagName, String message, String revision, boolean isLocal, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_TAG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (isLocal) {
            command.add(HG_TAG_OPT_LOCAL);
        } else if (message != null && !message.isEmpty()) {
            command.add(HG_TAG_OPT_MESSAGE);
            command.add(message);
        }
        if (revision != null && !revision.isEmpty()) {
            command.add(HG_TAG_OPT_REVISION);
            command.add(revision);
        }

        command.add(tagName);
        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
            } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
            }
        }
    }

    public static void removeTag (File repository, String tagName, boolean isLocal, String message, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_TAG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (isLocal) {
            command.add(HG_TAG_OPT_LOCAL);
        } else if (message != null && !message.isEmpty()) {
            command.add(HG_TAG_OPT_MESSAGE);
            command.add(message);
        }
        command.add(HG_TAG_OPT_REMOVE);

        command.add(tagName);
        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger); //NOI18N
            } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
            }
        }
    }

    private static Boolean topoAvailable;
    private static List<String> getHeadInfo (String repository, boolean topo, String template, boolean useStyle) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_HEADS_CMD);
        if (topo) {
            topoAvailable = Boolean.TRUE.equals(topoAvailable) || topoAvailable == null && HgUtils.hasTopoOption(Mercurial.getInstance().getVersion());
            if (topoAvailable) {
                command.add(HG_FLAG_TOPO);
            }
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository);
        File tempFolder = Utils.getTempFolder(false);
        try {
            if (useStyle) {
                command.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            } else {
                command.add(template);
            }
            List<String> output = exec(command);
            if (topo && topoAvailable && output.contains("hg heads: option --topo not recognized")) { //NOI18N
                topoAvailable = false;
                return getHeadInfo(repository, topo, template, useStyle);
            }
            return output;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    private static List<String> getHeadInfo(File repository, boolean topo, String template, boolean useStyle) throws HgException {
        if (repository == null) return null;
        return getHeadInfo(repository.getAbsolutePath(), topo, template, useStyle);
    }

    private static List<String> getBranches (File repository) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BRANCHES_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        return exec(command);
    }

    private static List<String> getTags (File repository) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_TAGS_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        return exec(command);
    }

    /**
     * Returns the revision number for the last change to a file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last revision number is to be returned, if null test for repo
     * @return String in the form of a revision number.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getLastRevision(File repository, File file) throws HgException {
        return  getLastChange(repository, file, HG_REV_TEMPLATE_CMD);
    }

    private static String getLastChange(File repository, File file, String template) throws HgException {

        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_ONE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(template);
        if( file != null)
            command.add(file.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return new StringBuffer(list.get(0)).toString();
        }else{
            return null;
        }
    }

    /**
     * Returns parent revision of the given revision
     * @param repository cannot be null
     * @param file if not null, parent revision limited on this file will be returned
     * @param revision if null, parent of the WC is returned
     * @return parent revision, HgLogMessage.Empty if has no parent and null if error occurs
     * @throws HgException
     */
    public static HgRevision getParent (File repository, File file, String revision) throws HgException {
        if (repository == null ) return null;

        HgRevision parentRevision = HgRevision.EMPTY;
        List<HgLogMessage> revisions = getParents(repository, file, revision);
        if (revisions.size() > 1) {
            String rev1 = revisions.get(0).getRevisionNumber();
            String rev2 = revisions.get(1).getRevisionNumber();
            parentRevision = HgCommand.getCommonAncestor(repository.getAbsolutePath(), rev1, rev2, OutputLogger.getLogger(null));
        } else if (revisions.size() == 1) {
            parentRevision = revisions.get(0).getHgRevision();
        }
        return parentRevision;
    }

    /**
     * Returns parent revisions of the given file
     * @param repository cannot be null
     * @param file revisions of this file will be returned
     * @param revision if not null, returns parents of this revision limited on the file
     * @return parent revisions
     * @throws HgException
     */
    public static List<HgLogMessage> getParents (File repository, File file, String revision) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_PARENT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_BASIC_CHANGESET_NAME));
            if (file != null) command.add(file.getAbsolutePath());
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                if (isErrorNotFoundInManifest(list.get(0))) {
                    return Collections.<HgLogMessage>emptyList();
                } else if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), OutputLogger.getLogger(null));
                } else if (isErrorAbort(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), OutputLogger.getLogger(null));
                }
            }
            return processLogMessages(repository, file == null ? Collections.<File>emptyList() : Collections.singletonList(file), list);
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    /**
     * Returns the mercurial status for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param files files or directories of interest
     * @return Map of files and status for all files of interest, map contains normalized files as keys
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getStatus (File repository, List<File> files, String revisionFrom, String revisionTo) throws HgException{
        return getStatus(repository, files, revisionFrom, revisionTo, true);
    }

    /**
     * Returns the mercurial status for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param files files or directories of interest
     * @param detectCopies if set to true then the command takes longer and returns also original files for renames and copies
     * @return Map of files and status for all files of interest, map contains normalized files as keys
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getStatus (File repository, List<File> files,
            String revisionFrom, String revisionTo, boolean detectCopies) throws HgException{
        return StatusCommand.create(repository, files, detectCopies)
                .setRevisionFrom(revisionFrom).setRevisionTo(revisionTo).call();
    }

    /**
     * Remove the specified file from the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be added to hg
     * @param f path to be removed from the repository
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRemove(File repository, List<File> removeFiles, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (removeFiles.isEmpty()) return;
        List<String> basicCommand = new ArrayList<String>();
        basicCommand.add(getHgCommand());
        basicCommand.add(HG_REMOVE_CMD);
        basicCommand.add(HG_OPT_REPOSITORY);
        basicCommand.add(repository.getAbsolutePath());
        basicCommand.add(HG_REMOVE_FLAG_FORCE_CMD);

        List<List<String>> attributeGroups = splitAttributes(repository, basicCommand, removeFiles, false);
        for (List<String> attributes : attributeGroups) {
            List<String> command = new ArrayList<String>(basicCommand);
            command.addAll(attributes);
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                handleError(command, list, list.get(0), logger);
            }
        }
    }

    /**
     * Remove the specified files from the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param f path to be removed from the repository
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRemove(File repository, File f, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_REMOVE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_REMOVE_FLAG_FORCE_CMD);
        try {
            command.add(f.getCanonicalPath());
        } catch (IOException ioe) {
            Mercurial.LOG.log(Level.WARNING, null, ioe); // NOI18N
            command.add(f.getAbsolutePath()); // don't give up
        }

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ALREADY_TRACKED"), logger);
    }

    /**
     * Export the diffs for the specified revision to the specified output file
    /**
     * Export the diffs for the specified revision to the specified output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revStr the revision whose diffs are to be exported
     * @param outputFileName path of the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doExport(File repository, String revStr, String outputFileName, OutputLogger logger)  throws HgException {
        // Ensure that parent directory of target exists, creating if necessary
        File fileTarget = new File (outputFileName);
        File parentTarget = fileTarget.getParentFile();
        try {
            if (!parentTarget.mkdir()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : {0}", parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : {0} threw SecurityException {1}", new Object[]{parentTarget.getAbsolutePath(), e.getMessage()}); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_EXPORT_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPTION_GIT);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_OUTPUT_CMD);
        command.add(outputFileName);
        if(revStr != null) command.add(revStr);

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_EXPORT_FAILED"), logger);
        }
        return list;
    }

    /**
     * Exports a changeset bundle for the given revision range to the given output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revBase the base revision
     * @param revTo the revision up to which to export, can be null
     * @param outputFile the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doBundle (File repository, String revBase, String revTo, File outputFile, OutputLogger logger) throws HgException {
        // Ensure that parent directory of target exists, creating if necessary
        File parentTarget = outputFile.getParentFile();
        try {
            if (!parentTarget.mkdirs()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdirs() failed for : {0}", parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : {0} threw SecurityException {1}", new Object[]{parentTarget.getAbsolutePath(), e.getMessage()}); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BUNDLE_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_BASE_CMD);
        command.add(revBase);
        if (revTo != null) {
            command.add(HG_REV_CMD);
            command.add(revTo);
        }
        command.add(outputFile.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_BUNDLE_FAILED"), logger);
        }
        return list;
    }

        /**
     * Export the diffs for the specified revision to the specified output file
    /**
     * Export the diffs for the specified revision to the specified output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revStr the revision whose diffs are to be exported
     * @param outputFileName path of the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doExportFileDiff(File repository, File file, String revStr, String outputFileName, OutputLogger logger)  throws HgException {
        // Ensure that parent directory of target exists, creating if necessary
        File fileTarget = new File (outputFileName);
        File parentTarget = fileTarget.getParentFile();
        try {
            if (!parentTarget.mkdir()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : {0}", parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : {0} threw SecurityException {1}", new Object[]{parentTarget.getAbsolutePath(), e.getMessage()}); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_REV_CMD);
        command.add(revStr);
        command.add(HG_LOG_TEMPLATE_EXPORT_FILE_CMD);
        command.add(HG_LOG_PATCH_CMD);
        command.add(HG_OPTION_GIT);
        command.add(file.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_EXPORT_FAILED"), logger);
        }else{
            writeOutputFileDiff(list, outputFileName);
        }
        return list;
    }
    private static void writeOutputFileDiff(List<String> list, String outputFileName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(outputFileName));
            for(String s: list){
                pw.println(s);
                pw.flush();
            }
        } catch (IOException ex) {
            // Ignore
        } finally {
            if(pw != null) pw.close();
        }
    }

    /**
     * Imports the diffs from the specified file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File patchFile of the patch file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doImport (File repository, File patchFile, boolean commit, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_IMPORT_CMD);
        command.add(HG_VERBOSE_CMD);
        if (!commit) {
            command.add(HG_NO_COMMIT);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(patchFile.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            logger.output(list); // need the failure info from import
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_IMPORT_FAILED"), logger);
        }
        return list;
    }

    /**
     * Gets unresolved files from a previous merge
     */
    protected static List<String> getUnresolvedFiles (File repository, List<String> attributes) throws HgException {
        assert attributes != null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_RESOLVE_CMD);

        command.add("-l");                                              //NOI18N
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.addAll(attributes);
        
        List<String> list =  exec(command);
        // filter out resolved files - they could be wrongly considered as removed - common R status
        // also removes error lines
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String line = it.next();
            if (line.length() < 2 || line.charAt(0) + line.charAt(1) != HG_STATUS_CODE_CONFLICT) {
                it.remove();
            }
        }

        return list;
    }

    public static QPatch[] qListSeries (File repository) throws HgException {
        Queue activeQueue = null;
        for (Queue q : qListQueues(repository)) {
            if (q.isActive()) {
                activeQueue = q;
            }
        }
        if (activeQueue == null) {
            return new QPatch[0];
        }
        
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QSERIES_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_SUMMARY);
        
        List<String> list = exec(command);
        QPatch[] patches;
        if (list.isEmpty()) {
            patches = new QPatch[0];
        } else {
            patches = parsePatches(list, activeQueue);
        }
        return patches;
    }

    public static Map<Queue, QPatch[]> qListAvailablePatches (File repository) throws HgException {
        Map<Queue, QPatch[]> patches = new LinkedHashMap<Queue, QPatch[]>();
        Map<Queue, QPatch[]> otherPatches = new LinkedHashMap<Queue, QPatch[]>();
        for (Queue q : qListQueues(repository)) {
            if (q.isActive()) {
                patches.put(q, qListSeries(repository));
            } else {
                otherPatches.put(q, qListSeries(repository, q.getName()));
            }
        }
        patches.putAll(otherPatches);
        return patches;
    }

    private static QPatch[] qListSeries (File repository, String queueName) throws HgException {
        Queue q = new Queue(queueName, false);
        List<QPatch> patches = new ArrayList<QPatch>();
        File seriesFile = getQSeriesFile(repository, queueName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(seriesFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                if (!line.startsWith("#")) {
                    patches.add(new QPatch(line, null, q, false));
                }
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, ex.getMessage());
            Mercurial.LOG.log(Level.FINE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return patches.toArray(new QPatch[0]);
    }

    public static Queue[] qListQueues (File repository) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QQUEUE_CMD);
        
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_LIST);
        
        List<String> list = exec(command);
        Queue[] queues;
        if (list.isEmpty()) {
            queues = new Queue[0];
        } else {
            queues = parseQueues(list);
        }
        return queues;
    }

    public static void qSwitchQueue (File repository, String queueName, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QQUEUE_CMD);
        
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(queueName);
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAbort(list.get(0))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_QQUEUE_SWITCH_FAILED"), logger); //NOI18N
        }
    }

    public static List<String> qPushPatches (File repository, String onTopPatch, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QPUSH_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        if (onTopPatch == null) {
            command.add(HG_OPT_ALL);
        } else {
            command.add(onTopPatch);
        }
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAbort(list.get(0))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_QPUSH_FAILED"), logger); //NOI18N
        }
        return list;
    }

    public static void qPopPatches (File repository, String onTopPatch, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QPOP_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        if (onTopPatch == null) {
            command.add(HG_OPT_ALL);
        } else {
            command.add(onTopPatch);
        }
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAbort(list.get(0))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_QPOP_FAILED"), logger); //NOI18N
        }
    }

    public static List<String> qGoToPatch (File repository, String patch, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QGOTO_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(patch);
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAbort(list.get(0))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_QGOTO_FAILED"), logger); //NOI18N
        }
        return list;
    }

    private static QPatch[] parsePatches (List<String> list, Queue q) {
        List<QPatch> patches = new ArrayList<QPatch>(list.size());
        Pattern p = Pattern.compile("^\\s*(\\b\\d+)\\s([AU])\\s([^:]+?):\\s?(.*)$"); //NOI18N
        for (String line : list) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String status = m.group(2);
                String id = m.group(3);
                String message = m.group(4);
                patches.add(new QPatch(id, message, q, "A".equals(status))); //NOI18N
            }
        }
        if (patches.isEmpty() && !list.isEmpty()) {
            Mercurial.LOG.log(Level.INFO, "parsePatches(): No qpatches found: {0}", list);
        }
        return patches.toArray(new QPatch[0]);
    }

    private static Queue[] parseQueues (List<String> list) {
        List<Queue> queues = new ArrayList<Queue>(list.size());
        for (String line : list) {
            line = line.trim();
            boolean active = false;
            if (line.endsWith(QUEUE_ACTIVE)) {
                active = true;
                line = line.substring(0, line.length() - QUEUE_ACTIVE.length()).trim();
            }
            queues.add(new Queue(line, active));
        }
        if (queues.isEmpty() && !list.isEmpty()) {
            Mercurial.LOG.log(Level.INFO, "parseQueues(): No qqueue found: {0}", list);
        }
        return queues.toArray(new Queue[0]);
    }

    public static void qCreatePatch (File repository, Collection<File> includedFiles, Collection<File> excludedFiles,
            String patchId, String commitMessage, String user, OutputLogger logger) throws HgException {
        qCreateRefreshPatch(repository, includedFiles, excludedFiles, patchId, commitMessage, user, logger);
    }

    public static void qRefreshPatch (File repository, Collection<File> includedFiles, Collection<File> excludedFiles,
            String commitMessage, String user, OutputLogger logger) throws HgException {
        qCreateRefreshPatch(repository, includedFiles, excludedFiles, null, commitMessage, user, logger);
    }

    private static void qCreateRefreshPatch (File repository, Collection<File> includedFiles, Collection<File> excludedFiles,
            String patchId, String commitMessage, String user, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(patchId == null ? HG_QREFRESH_PATCH : HG_QCREATE_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        String projectUserName = new HgConfigFiles(repository).getUserName(false);
        String globalUsername = HgModuleConfig.getDefault().getSysUserName();
        if (user == null) {
            if(projectUserName != null && projectUserName.length() > 0) {
                user = projectUserName;
            } else if (globalUsername != null && globalUsername.length() > 0) {
                user = globalUsername;
            }
        }

        if (user != null){
            command.add(HG_OPT_USERNAME);
            command.add(user);
        }

        File tempfile = null;

        try {
            if (commitMessage == null || commitMessage.length() == 0) {
                commitMessage = HG_COMMIT_DEFAULT_MESSAGE;
            }
            // Create temporary file.
            tempfile = Files.createTempFile(HG_COMMIT_TEMPNAME, HG_COMMIT_TEMPNAME_SUFFIX).toFile();

            // Write to temp file
            BufferedWriter out = new BufferedWriter(ENCODING == null 
                    ? new OutputStreamWriter(new FileOutputStream(tempfile)) 
                    : new OutputStreamWriter(new FileOutputStream(tempfile), ENCODING));
            out.write(commitMessage);
            out.close();

            command.add(HG_COMMIT_OPT_LOGFILE_CMD);
            command.add(tempfile.getAbsolutePath());
            if (patchId == null) {
                command.add(HG_OPT_SHORT);
                for (File f : excludedFiles) {
                    command.add(HG_OPT_EXCLUDE);
                    command.add(f.getAbsolutePath());
                }
            } else {
                if (includedFiles.isEmpty()) {
                    command.add(HG_OPT_EXCLUDE);
                    command.add("*"); //NOI18N
                }
                command.add(patchId);
            }
            for (File f: includedFiles) {
                if (f.getAbsolutePath().length() <= repository.getAbsolutePath().length()) {
                    // list contains the root itself
                    command.add(f.getAbsolutePath());
                } else {
                    command.add(f.getAbsolutePath().substring(repository.getAbsolutePath().length() + 1));
                }
            }
            if(Utilities.isWindows()) {
                int size = 0;
                // Count size of command
                for (String line : command) {
                    size += line.length();
                }
                if (isTooLongCommand(size)) {
                    throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_ARG_LIST_TOO_LONG_ERR", command.get(1), command.size() -2 )); //NOI18N
                }
            }
            List<String> list = exec(command);
            //#132984: range of issues with upgrade to Hg 1.0, new restriction whereby you cannot commit using explicit file names after a merge.
            if (!list.isEmpty() && isCommitAfterMerge(list.get(list.size() -1))) {
                throw new HgException(COMMIT_AFTER_MERGE);
            }

            if (!list.isEmpty()
                    && (isErrorNotTracked(list.get(0)) ||
                    isErrorCannotReadCommitMsg(list.get(0)) ||
                    isErrorAbort(list.get(list.size() -1)) ||
                    isErrorAbort(list.get(0))))
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);

        } catch (IOException ex) {
            throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_FAILED_TO_READ_COMMIT_MESSAGE"));
        } finally {
            if (commitMessage != null && tempfile != null){
                tempfile.delete();
            }
        }
    }
    
    public static void qFinishPatches (File repository, String patch, OutputLogger logger) throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_QFINISH_CMD);
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_MQ_EXT_CMD);

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(patch);
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAbort(list.get(0))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger); //NOI18N
        }
    }

    private static List<String> execEnv(List<? extends Object> command, List<String> env) throws HgException{
        return execEnv(command, env, true);
    }

    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */
    private static List<String> execEnv(final List<? extends Object> command, List<String> env, boolean logUsage) throws HgException {
        if( EventQueue.isDispatchThread()){
            Mercurial.LOG.log(Level.FINE, "WARNING execEnv():  calling Hg command in AWT Thread - could stall UI"); // NOI18N
        }
        assert ( command != null && command.size() > 0);
        if(logUsage) {
            Utils.logVCSClientEvent("HG", "CLI");
        }
        logCommand(command);
        File outputStyleFile = null;
        final String hgCommand = getHgCommandName(command); // command name
        final File repository = getRepositoryFromCommand(command, hgCommand);
        try {
            try {
                outputStyleFile = createOutputStyleFile(command);
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.WARNING, "Failed to create temporary file defining Hg output style."); //NOI18N
            }
            final List<String> commandLine = toCommandList(command, outputStyleFile);
            final ProcessBuilder pb = new ProcessBuilder(commandLine);
            if (repository != null && repository.isDirectory()) {
                pb.directory(repository);
            }
            Map<String, String> envOrig = pb.environment();
            setGlobalEnvVariables(envOrig);
            if (env != null && env.size() > 0) {
                for (String s : env) {
                    envOrig.put(s.substring(0, s.indexOf('=')), s.substring(s.indexOf('=') + 1));
                }
            }
            try {
                Callable<List<String>> callable = new Callable<List<String>>() {
                    @Override
                    public List<String> call () throws HgException {
                        return exec(commandLine, pb);
                    }
                };
                if (repository != null) {
                    logExternalRepositories(repository, hgCommand);
                }
                if (modifiesRepository(hgCommand) && repository != null) {
                    return Mercurial.getInstance().runWithoutExternalEvents(repository, hgCommand, callable);
                } else {
                    return callable.call();
                }
            } catch (HgException ex) {
                throw ex;
            } catch (Exception ex) {
                Mercurial.LOG.log(Level.WARNING, null, ex);
                return null;
            } finally {
                if (repository != null && changesParents(hgCommand)) {
                    WorkingCopyInfo.refreshAsync(repository);
                }
            }
        } finally{
            if (outputStyleFile != null) {
                outputStyleFile.delete();
            }
        }
    }

    private static void setGlobalEnvVariables (Map<String, String> environment) {
        if (!Boolean.TRUE.equals(doNotAddHgPlain.get())) {
            environment.put(ENV_HGPLAIN, "true"); //NOI18N
        }
        if (ENCODING != null) {
            environment.put(ENV_HGENCODING, ENCODING);
        }
    }

    /**
     * Logs the hg command if allowed
     * @param command
     */
    private static void logCommand (List<? extends Object> command) {
        if (Mercurial.LOG.isLoggable(Level.FINE)) {
            if (command.size() > 10) {
                List<String> smallCommand = new ArrayList<String>();
                int count = 0;
                for (Iterator i = command.iterator(); i.hasNext();) {
                    smallCommand.add((String)i.next());
                    if (count++ > 10) break;
                }
                Mercurial.LOG.log(Level.FINE, "execEnv(): {0}", smallCommand); // NOI18N
            } else {
                Mercurial.LOG.log(Level.FINE, "execEnv(): {0}", command); // NOI18N
            }
        }
    }

    private static List<String> exec (List<? extends Object> command, ProcessBuilder pb) throws HgException {
        final List<String> list = new ArrayList<String>();
        BufferedReader input = null;
        BufferedReader error = null;
        Process proc = null;
        try{
            proc = pb.start();

            input = new BufferedReader(ENCODING == null 
                    ? new InputStreamReader(proc.getInputStream())
                    : new InputStreamReader(proc.getInputStream(), ENCODING));
            error = new BufferedReader(ENCODING == null 
                    ? new InputStreamReader(proc.getErrorStream())
                    : new InputStreamReader(proc.getErrorStream(), ENCODING));
            final BufferedReader errorReader = error;
            final List<String> errorOutput = new ArrayList<String>();
            final BufferedReader inputReader = input;
            final List<String> inputOutput = new ArrayList<String>();
            Thread errorThread = new Thread(new Runnable () {
                @Override
                public void run() {
                    try {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            if (!skipErrorLine(line)) {
                                errorOutput.add(line);
                            }
                        }
                    } catch (IOException ex) {
                        // not interested
                    }
                }
            });
            errorThread.start();
            Thread inputThread = new Thread(new Runnable () {
                @Override
                public void run() {
                    try {
                        String line;
                        while ((line = inputReader.readLine()) != null) {
                            inputOutput.add(line);
                        }
                    } catch (IOException ex) {
                        // not interested
                    }
                }
            });
            inputThread.start();
            try {
                inputThread.join();
                errorThread.join();
            } catch (InterruptedException ex) {
                Mercurial.LOG.log(Level.FINE, "execEnv():  process interrupted {0}", ex); // NOI18N
                // We get here is we try to cancel so kill the process
                if (proc != null) {
                    proc.destroy();
                }
                throw new HgException.HgCommandCanceledException(NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_CANCELLED")); //NOI18N
            }
            list.addAll(inputOutput); // appending output
            input.close();
            input = null;
            list.addAll(errorOutput); // appending error output
            error.close();
            error = null;
            try {
                proc.waitFor();
                // By convention we assume that 255 (or -1) is a serious error.
                // For instance, the command line could be too long.
                if (proc.exitValue() == 255) {
                    Mercurial.LOG.log(Level.FINE, "execEnv():  process returned 255"); // NOI18N
                    if (list.isEmpty()) {
                        Mercurial.LOG.log(Level.SEVERE, "command: {0}", command); // NOI18N
                        throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_EXECUTE_COMMAND"));
                    } else {
                        Mercurial.LOG.log(Level.FINE, "command: {0}", command); // NOI18N
                        Mercurial.LOG.log(Level.FINE, "result: {0}", list); //NOI18N
                    }
                }
            } catch (InterruptedException e) {
                Mercurial.LOG.log(Level.FINE, "execEnv():  process interrupted {0}", e); // NOI18N
            }
        }catch(InterruptedIOException e){
            // We get here is we try to cancel so kill the process
            Mercurial.LOG.log(Level.FINE, "execEnv():  execEnv(): InterruptedIOException {0}", e); // NOI18N
            if (proc != null)  {
                proc.destroy();
            }
            throw new HgException.HgCommandCanceledException(NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_CANCELLED"));
        }catch(IOException e){
            // Hg does not seem to be returning error status != 0
            // even when it fails when for instance adding an already tracked file to
            // the repository - we will have to examine the output in the context of the
            // calling func and raise exceptions there if needed
            Mercurial.LOG.log(HG_VERSION_CMD.equals(command.get(1)) ? Level.FINE : Level.INFO, "execEnv():  execEnv(): IOException", e); // NOI18N

            // Handle low level Mercurial failures
            if (isErrorArgsTooLong(e.getMessage())){
                assert(command.size()> 2);
                throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_ARG_LIST_TOO_LONG_ERR",
                            getHgCommandName(command), command.size() -2 ));
            }else if (isErrorNoHg(e.getMessage()) || isErrorCannotRun(e.getMessage())){
                throw new HgException(NbBundle.getMessage(Mercurial.class, "MSG_VERSION_NONE_MSG"));
            }else{
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_EXECUTE_COMMAND"));
            }
        }finally{
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
                input = null;
            }
            if (error != null) {
                try {
                    error.close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
            }
        }
        return list;
    }

    private static File createOutputStyleFile(List<? extends Object> cmdLine) throws IOException {
        File result = null;

        for (Object obj : cmdLine) {
            if (obj == null) {
                assert false;
                continue;
            }

            if (obj.getClass() == String.class) {
                String str = (String) obj;
                if (str.startsWith("--template=")) {                    //NOI18N
                    if (result != null) {
                        assert false : "implementation not ready for multiple templates on one command line"; //NOI18N
                        continue;
                    }

                    String template = str.substring("--template=".length()); //NOI18N

                    File tempFile = Files.createTempFile("hg-output-style", //NOI18N
                            null).toFile();    //extension (default)
                    Writer writer = ENCODING == null 
                            ? new OutputStreamWriter(new FileOutputStream(tempFile)) 
                            : new OutputStreamWriter(new FileOutputStream(tempFile), ENCODING);
                    try {
                        writer.append("changeset = ")                   //NOI18N
                              .append('"').append(template).append('"');
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException ex) {
                                //ignore
                            }
                        }
                    }

                    /*
                     * only store the reference to the file to variable 'result'
                     * if the file's content was successfully written:
                     */
                    result = tempFile;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<String> toCommandList(List<? extends Object> cmdLine, File styleFile) {
        if (cmdLine.isEmpty()) {
            return (List<String>) cmdLine;
        }

        List<String> result = new ArrayList<String>(cmdLine.size() + 2);
        boolean first = true;
        for (Object obj : cmdLine) {
            if (obj == null) {
                assert false;
                continue;
            }
            if (obj == HG_COMMAND_PLACEHOLDER) {
                result.addAll(makeHgLauncherCommandLine());
            } else if (obj.getClass() == String.class) {
                String str = (String) obj;
                if (str.startsWith("--template=") && (styleFile != null)) { //NOI18N
                    result.add("--style");                              //NOI18N
                    result.add(styleFile.getAbsolutePath());
                } else {
                    result.add(str);
                }
            } else if (obj instanceof HgURL) {
                if (first) {
                    assert false;
                    result.add(obj.toString());
                } else {
                    result.add(((HgURL) obj).toHgCommandUrlString());
                }
            } else if (obj instanceof File) {
                result.add(((File) obj).getPath());
            } else {
                assert false;
                result.add(obj.toString());
            }
            first = false;
        }
        assert !result.isEmpty();
        modifyArguments(result);
        return result;
    }
    
    private static void modifyArguments (List<String> result) {
        if (CMD_EXE.equals(result.get(0))) {
            // it seems that when running a command in a win cmd.exe, the command needs to be passed as a single parameter
            // and all spaces in it's arguments need to be enclosed in double-quotes
            StringBuilder commandArg = new StringBuilder();
            int pos = 0;
            for (ListIterator<String> it = result.listIterator(); it.hasNext(); ++pos) {
                String arg = it.next();
                if (pos >= 2) {
                    it.remove();
                    commandArg.append(arg.replace(" ", "\" \"")).append(' '); //NOI18N
                }
            }
            assert result.size() == 2;
            int len = commandArg.length();
            result.add((len == 0 ? commandArg : commandArg.delete(len - 1, len)).toString());
        }
    }
    
    private static boolean skipErrorLine (String errorLine) {
        boolean skip = false;
        if (errorLine.startsWith("warning:")) { //NOI18N
            skip = true;
        } else {
            for (String s : new String[] {
                "is deprecated:" //NOI18N
            }) {
                if (errorLine.contains(s)) {
                    skip = true;
                    break;
                }
            }
        }
        return skip;
    }

    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */
    protected static List<String> exec(List<? extends Object> command) throws HgException{
        if(!Mercurial.getInstance().isAvailable()){
            return new ArrayList<String>();
        }
        return execEnv(command, null);
    }
    private static List<String> execForVersionCheck() throws HgException{
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_VERSION_CMD);

        return execEnv(command, null, false);
    }

    protected static String getHgCommand() {
        return HG_COMMAND_PLACEHOLDER;
    }

    private static List<String> makeHgLauncherCommandLine() {
        String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath();

        if (defaultPath == null || defaultPath.length() == 0) {
            return Collections.singletonList(HG_COMMAND);
        }

        File f = new File(defaultPath);
        File launcherFile;
        if(f.isFile()) {
            launcherFile = f;
        } else {
            if(Utilities.isWindows()){
                launcherFile = null;
                for (String hgExecutable : HG_WINDOWS_EXECUTABLES) {
                    File executableFile = new File(f, hgExecutable);
                    if (executableFile.isFile()) {
                        launcherFile = executableFile;
                        break;
                    }
                }
                if (launcherFile == null) {
                    launcherFile = new File(f, HG_COMMAND + HG_WINDOWS_EXE);
                }
            } else {
                launcherFile = new File(f, HG_COMMAND);
            }
        }
        String launcherPath = launcherFile.getAbsolutePath();

        List<String> result;
        if (Utilities.isWindows() && !launcherPath.endsWith(HG_WINDOWS_EXE)) {
            /* handle .bat and .cmd files: */
            result = new ArrayList<String>(3);
            result.add(CMD_EXE);
            result.add("/C");                                           //NOI18N
            result.add(launcherPath);
            return result;
        } else {
            result = Collections.singletonList(launcherPath);
        }
        return result;
    }

    protected static void handleError(List<? extends Object> command, List<String> cmdOutput, String message, OutputLogger logger) throws HgException{
        if (command != null && cmdOutput != null && logger != null){
            Mercurial.LOG.log(Level.WARNING, "command: {0}", command); // NOI18N
            Mercurial.LOG.log(Level.WARNING, "output: {0}", HgUtils.replaceHttpPassword(cmdOutput)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ERR")); // NOI18N
            logger.output(NbBundle.getMessage(
                                HgCommand.class,
                                "MSG_COMMAND_INFO_ERR",                 //NOI18N
                                command,
                                HgUtils.replaceHttpPassword(cmdOutput)));
        }

        if (cmdOutput != null && !cmdOutput.isEmpty() && (isErrorPossibleProxyIssue(cmdOutput.get(0)) || isErrorPossibleProxyIssue(cmdOutput.get(cmdOutput.size() - 1)))) {
            boolean bConfirmSetProxy;
            bConfirmSetProxy = HgUtils.confirmDialog(HgCommand.class, "MSG_POSSIBLE_PROXY_ISSUE_TITLE", "MSG_POSSIBLE_PROXY_ISSUE_QUERY"); // NOI18N
            if(bConfirmSetProxy){
                OptionsDisplayer.getDefault().open("General");              // NOI18N
            }
        } else {
            throw new HgException(message);
        }
    }

    private static PasswordAuthentication handleAuthenticationError(List<String> cmdOutput, File repository, String url, String userName, UserCredentialsSupport credentialsSupport, String hgCommand) throws HgException {
        return handleAuthenticationError(cmdOutput, repository, url, userName, credentialsSupport, hgCommand, true);
    }

    private static PasswordAuthentication handleAuthenticationError(List<String> cmdOutput, File repository, String url, String userName, UserCredentialsSupport credentialsSupport, String hgCommand, boolean showLoginDialog) throws HgException {
        PasswordAuthentication credentials = null;
        String msg = cmdOutput.get(cmdOutput.size() - 1).toLowerCase();
        if (isAuthMsg(msg) && showLoginDialog) {
            credentials = credentialsSupport.getUsernamePasswordCredentials(repository, url, userName);
        }
        return credentials;
    }

    public static boolean isAuthMsg(String msg) {
        return msg.contains(HG_AUTHORIZATION_REQUIRED_ERR)
                || msg.contains(HG_AUTHORIZATION_FAILED_ERR);
    }

    public static boolean isMergeNeededMsg (String msg) {
        return msg.contains("run") //NOI18N
                && msg.contains("hg heads") //NOI18N
                && msg.contains("to see heads") //NOI18N
                && msg.contains("hg merge") //NOI18N
                && msg.contains("to merge"); //NOI18N
    }

    public static boolean isUpdateNeededMsg (String msg) {
        return msg.contains(HG_UPDATE_NEEDED_ERR);
    }

    public static boolean isHeadsNeededMsg (String msg) {
        return msg.contains("run") //NOI18N
                && msg.contains("hg heads") //NOI18N
                && msg.contains("to see heads"); //NOI18N
    }

    public static boolean isBackoutMergeNeededMsg(String msg) {
        return msg.indexOf(HG_BACKOUT_MERGE_NEEDED_ERR) > -1;                       // NOI18N
    }

    public static boolean isMergeFailedMsg (String msg) {
        return (msg.indexOf(HG_MERGE_FAILED1_ERR) > -1) 
                && (msg.indexOf(HG_MERGE_FAILED2_ERR) > -1 || msg.indexOf(HG_MERGE_FAILED3_ERR) > -1);
    }

    public static boolean isConflictDetectedInMsg (String msg) {
        return msg.indexOf(HG_MERGE_CONFLICT_ERR) > -1;
    }

    public static boolean isMergeUnavailableMsg(String msg) {
        return msg.indexOf(HG_MERGE_UNAVAILABLE_ERR) > -1;                 // NOI18N
    }

    public static boolean isMergeAbortMultipleHeadsMsg(String msg) {
        return msg.indexOf(HG_MERGE_MULTIPLE_HEADS_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMergeAbortUncommittedMsg(String msg) {
        return msg.indexOf(HG_MERGE_UNCOMMITTED_ERR) > -1;                                   // NOI18N
    }

    public static boolean isNoChanges(String msg) {
        return msg.indexOf(HG_NO_CHANGES_ERR) > -1;                                   // NOI18N
    }

    private static boolean isErrorNoDefaultPush(String msg) {
        return msg.indexOf(HG_ABORT_NO_DEFAULT_PUSH_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNoDefaultPath(String msg) {
        return msg.indexOf(HG_ABORT_NO_DEFAULT_ERR) > -1; // NOI18N
    }

    private static boolean isErrorPossibleProxyIssue(String msg) {
        return msg.indexOf(HG_ABORT_POSSIBLE_PROXY_ERR) > -1; // NOI18N
    }

    protected static boolean isErrorNoRepository(String msg) {
        return msg.indexOf(HG_NO_REPOSITORY_ERR) > -1 ||
                 msg.indexOf(HG_NOT_REPOSITORY_ERR) > -1 ||
                 (msg.indexOf(HG_REPOSITORY) > -1 && msg.indexOf(HG_NOT_FOUND_ERR) > -1); // NOI18N
    }

    private static boolean isErrorNoHg(String msg) {
        return msg.indexOf(HG_NO_HG_CMD_FOUND_ERR) > -1; // NOI18N
    }
    private static boolean isErrorArgsTooLong(String msg) {
        return msg.indexOf(HG_ARG_LIST_TOO_LONG_ERR) > -1
                || msg.contains(HG_ARGUMENT_LIST_TOO_LONG_ERR);
    }

    private static boolean isErrorCannotRun(String msg) {
        return msg.indexOf(HG_CANNOT_RUN_ERR) > -1; // NOI18N
    }

    private static boolean isErrorUpdateSpansBranches(String msg) {
        return msg.indexOf(HG_UPDATE_SPAN_BRANCHES_ERR) > -1
                || msg.contains(HG_UPDATE_CROSS_BRANCHES_ERR);
    }

    private static boolean isErrorAlreadyTracked(String msg) {
        return msg.indexOf(HG_ALREADY_TRACKED_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNotTracked(String msg) {
        return msg.indexOf(HG_NOT_TRACKED_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNotFound(String msg) {
        return msg.indexOf(HG_NOT_FOUND_ERR) > -1; // NOI18N
    }

    private static boolean isErrorCannotReadCommitMsg(String msg) {
        return msg.indexOf(HG_CANNOT_READ_COMMIT_MESSAGE_ERR) > -1; // NOI18N
    }

    protected static boolean isErrorAbort(String msg) {
        return msg.indexOf(HG_ABORT_ERR) > -1; // NOI18N
    }

    protected static boolean isFollowNotAllowed (String msg) {
        msg = msg.toLowerCase();
        return msg.contains(HG_ABORT_CANNOT_FOLLOW_NONEXISTENT_FILE)
                || msg.contains("cannot follow file not in parent revision"); //NOI18N
    }

    public static boolean isErrorAbortPush(String msg) {
        return msg.indexOf(HG_ABORT_PUSH_ERR) > -1; // NOI18N
    }

    public static boolean isErrorAbortNoFilesToCopy(String msg) {
        return msg.indexOf(HG_ABORT_NO_FILES_TO_COPY_ERR) > -1; // NOI18N
    }

    public static boolean isCommitAfterMerge(String msg) {
        return msg.indexOf(HG_COMMIT_AFTER_MERGE_ERR) > -1;                                   // NOI18N
    }

    private static boolean isErrorNoChangeNeeded(String msg) {
        return msg.indexOf(HG_NO_CHANGE_NEEDED_ERR) > -1;    // NOI18N
    }

    public static boolean isCreateNewBranch(String msg) {
        return msg.indexOf(HG_CREATE_NEW_BRANCH_ERR) > -1;                                   // NOI18N
    }

    public static boolean isHeadsCreated(String msg) {
        return msg.indexOf(HG_HEADS_CREATED_ERR) > -1;                                   // NOI18N
    }

    public static boolean isNoRollbackPossible(String msg) {
        return msg.indexOf(HG_NO_ROLLBACK_ERR) > -1;                                   // NOI18N
    }
    public static boolean isNoRevStrip(String msg) {
        return msg.indexOf(HG_NO_REV_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isLocalChangesStrip(String msg) {
        return msg.indexOf(HG_LOCAL_CHANGES_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMultipleHeadsStrip(String msg) {
        return msg.indexOf(HG_MULTIPLE_HEADS_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isUncommittedChangesBackout(String msg) {
        return msg.indexOf(HG_ABORT_UNCOMMITTED_CHANGES_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMergeChangesetBackout(String msg) {
        return msg.indexOf(HG_ABORT_BACKOUT_MERGE_CSET_ERR) > -1;                                   // NOI18N
    }

    public static boolean isNoUpdates(String msg) {
        return msg.indexOf(HG_NO_UPDATES_ERR) > -1;                                   // NOI18N
    }

    private static boolean isErrorNoView(String msg) {
        return msg.indexOf(HG_NO_VIEW_ERR) > -1;                                     // NOI18N
    }

    private static boolean isErrorHgkNotFound(String msg) {
        return msg.indexOf(HG_HGK_NOT_FOUND_ERR) > -1;                               // NOI18N
    }

    private static boolean isErrorNoSuchFile(String msg) {
        return msg.toLowerCase().indexOf(HG_NO_SUCH_FILE_ERR) > -1;                               // NOI18N
    }

    private static boolean isErrorNoResponse(String msg) {
        return msg.indexOf(HG_NO_RESPONSE_ERR) > -1;                               // NOI18N
    }
    
    private static boolean isAddingLine (String msg) {
        return msg.toLowerCase().indexOf(HG_ADDING) > -1;
    }

    private static boolean isErrorNotFoundInManifest (String msg) {
        return msg.toLowerCase().contains("not found in manifest"); //NOI18N
    }

    private static List<String> getFilesWithPerformanceWarning (List<String> list) {
        List<String> fileList = new ArrayList<String>();
        for (String line : list) {
            int pos;
            if ((pos = line.indexOf(HG_WARNING_PERFORMANCE_FILES_OVER)) > 0 && line.contains(HG_WARNING_PERFORMANCE_CAUSE_PROBLEMS)) {
                fileList.add(line.substring(0, pos));
            }
        }
        return fileList;
    }

    public static void createConflictFile(String path) {
        try {
            File file = new File(path + HG_STR_CONFLICT_EXT);

            boolean success = file.createNewFile();
            Mercurial.LOG.log(Level.FINE, "createConflictFile(): File: {0} {1}", // NOI18N
                new Object[] {path + HG_STR_CONFLICT_EXT, success? "Created": "Not Created"} ); // NOI18N
        } catch (IOException e) {
        }
    }

    /**
     * Marks the given file as resolved if the resolve command is available
     * @param repository
     * @param file
     * @param logger
     * @throws HgException
     */
    public static void markAsResolved (File repository, File file, OutputLogger logger) throws HgException {
        if (file == null) return;
        if (!HgUtils.hasResolveCommand(Mercurial.getInstance().getVersion())) {
            return;
        }

        final List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_RESOLVE_CMD);
        command.add(HG_RESOLVE_MARK_RESOLVED);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(FileUtil.normalizeFile(file).getAbsolutePath());
        List<String> list;
        try {
            list = Mercurial.getInstance().runWithoutExternalEvents(repository, HG_RESOLVE_CMD, new Callable<List<String>>() {
                @Override
                public List<String> call () throws Exception {
                    return exec(command);
                }
            });
        } catch (HgException ex) {
            throw ex;
        } catch (Exception ex) {
            Mercurial.LOG.log(Level.WARNING, null, ex);
            return;
        }

        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
             } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
             }
        }
    }

    public static void deleteConflictFile(String path) {
        boolean success = (new File(path + HG_STR_CONFLICT_EXT)).delete();

        Mercurial.LOG.log(Level.FINE, "deleteConflictFile(): File: {0} {1}", // NOI18N
                new Object[] {path + HG_STR_CONFLICT_EXT, success? "Deleted": "Not Deleted"} ); // NOI18N
    }

    public static boolean existsConflictFile(String path) {
        File file = new File(path + HG_STR_CONFLICT_EXT);
        boolean bExists = file.canWrite();

        if (bExists) {
            Mercurial.LOG.log(Level.FINE, "existsConflictFile(): File: {0} {1}", // NOI18N
                    new Object[] {path + HG_STR_CONFLICT_EXT, "Exists"} ); // NOI18N
        }
        return bExists;
    }

    /**
     * Tries to determine if the given url represents real mercurial repository
     * @param hgUrl
     * @return true if the given url represents real mercurial repository
     */
    public static boolean checkRemoteRepository(String repository) {
        boolean retval = false;
        if (repository == null || "".equals(repository)) {
            return retval;
        }
        // temporary folder will be deleted manually
        File tmpFolder = Utils.getTempFolder(false);
        File tmpTarget = new File(tmpFolder, "rep");                    //NOI18N
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CLONE_CMD);
        command.add(repository);
        command.add(tmpTarget.getAbsolutePath());

        retval = execCheckClone(command);
        Utils.deleteRecursively(tmpFolder);
        return retval;
    }

    /**
     * This is a repulsive piece of code.
     * Have no other idea how to determine if the target is a real mercurial repository.
     * So this tries to clone into a temporary folder. If the command does not finish any time soon (e.g. huge repos as the netbeans repository)
     * or returns some recognizable messages in its output, then the target is assumed to be a real repository.
     * @param command
     * @return
     */
    private static boolean execCheckClone(List<String> command) {
        final boolean[] isRepository = new boolean[] {false};
        if(!Mercurial.getInstance().isAvailable(true, false)){
            Mercurial.LOG.info("Unsupported hg version");
            return isRepository[0];
        }

        Process proc = null;
        try{
            Mercurial.LOG.log(Level.FINE, "execCheckClone(): {0}", command); // NOI18N
            ProcessBuilder b = new ProcessBuilder(command);
            b.redirectErrorStream(true);
            // start the clone
            proc = b.start();
            final Process procf = proc;
            final Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader in = new BufferedReader(new InputStreamReader(procf.getInputStream()));
                    String line;
                    try {
                        // if the clone command returns any recognized messages, consider the target a real repository
                        while ((line = in.readLine()) != null) {
                            line = line.toLowerCase();
                            if (line.contains("requesting all changes") //NOI18N
                                    || line.contains("adding changesets") //NOI18N
                                    || line.contains("no changes found") //NOI18N
                                    || line.contains("updating working directory")) { //NOI18N
                                isRepository[0] = true;
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        Mercurial.LOG.log(Level.FINE, null, ex); // NOI18N
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Mercurial.LOG.log(Level.FINE, null, ex); // NOI18N
                        }
                    }
                }
            });
            t1.start();
            // wait for the clone to finish
            int rounds = HgUtils.getNumberOfRoundsForRepositoryValidityCheck();

            while (t1.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // ignore
                }
                if (--rounds == 0) {
                    // assume hg is cloning, we've been waiting for a long time, it has not thrown any error
                    proc.destroy();
                    isRepository[0] = true;
                }
            }
        } catch(InterruptedIOException e){
            // We get here is we try to cancel so kill the process
            Mercurial.LOG.log(Level.FINE, "execCheckClone():  InterruptedIOException {0}", e); // NOI18N
            if (proc != null)  {
                try {
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                    proc.getErrorStream().close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
                proc.destroy();
            }
        } catch(IOException e){
            Mercurial.LOG.log(Level.INFO, null, e);
        } finally {
            if (proc != null) {
                try {
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();
                } catch (IOException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return isRepository[0];
    }

    /**
     * Commands are limited by size
     * @param commandSize
     * @return
     */
    private static boolean isTooLongCommand(int commandSize) {
        return (Utilities.isWindows() || Utilities.isMac()) && commandSize > MAX_COMMANDLINE_SIZE;
    }

    /**
     * Returns name of the hg command or null if no argument follows {@link #HG_COMMAND_PLACEHOLDER} in commandList
     * @param commandList commandline arguments
     * @return name of the hg command or null
     */
    private static String getHgCommandName (List<? extends Object> commandList) {
        String commandName = null;
        if (commandList.size() > 1 && HG_COMMAND_PLACEHOLDER.equals(commandList.get(0))) {
            commandName = commandList.get(1).toString();
        }
        return commandName;
    }

    private static boolean changesParents (String hgCommand) {
        return WORKING_COPY_PARENT_MODIFYING_COMMANDS.contains(hgCommand);
    }

    private static boolean modifiesRepository (String hgCommand) {
        return !REPOSITORY_NOMODIFICATION_COMMANDS.contains(hgCommand);
    }

    /**
     * Tries to find the path to the repository for which the command is invoked
     * @param commandList
     * @return
     */
    private static File getRepositoryFromCommand (List<? extends Object> commandList, String hgCommand) {
        File repositoryFile = null;
        boolean isRepositoryArgument = false;
        for (ListIterator<? extends Object> it = commandList.listIterator(); it.hasNext(); ) {
            Object argument = it.next();
            if (isRepositoryArgument 
                    || HG_CLONE_CMD.equals(hgCommand) && !it.hasNext()) { // clone command has no --repository argument
                repositoryFile = new File(argument.toString());
                break;
            } else if (HG_OPT_REPOSITORY.equals(argument)) { // repository path follows --repository option
                isRepositoryArgument = true;
            }
        }
        return repositoryFile;
    }

    private static final Set<File> loggedRepositories = new HashSet<File>();
    private static final Set<String> noLogCommands = new HashSet<String>(Arrays.asList(
        HG_BRANCH_CMD,
        HG_BRANCHES_CMD,
        HG_CAT_CMD,
        HG_HEADS_CMD,
        HG_PARENT_CMD,
        HG_RESOLVE_CMD,
        HG_STATUS_CMD,
        HG_DIFF_CMD,
        HG_TAGS_CMD,
        HG_VERSION_CMD
    ));
    private static void logExternalRepositories (File repository, String hgCommand) {
        if (!noLogCommands.contains(hgCommand) && loggedRepositories.add(repository)) {
            HgConfigFiles hgConfigFiles = new HgConfigFiles(repository);
            if (hgConfigFiles.getException() == null) {
                boolean empty = true;
                for (Entry<Object, Object> prop : hgConfigFiles.getProperties(HgConfigFiles.HG_PATHS_SECTION).entrySet()) {
                    if (!prop.getValue().toString().isEmpty()) {
                        empty = false;
                        Utils.logVCSExternalRepository("HG", prop.getValue().toString()); //NOI18N
                    }
                }
                if (empty) {
                    Utils.logVCSExternalRepository("HG", null); //NOI18N
                }
            }
        }
    }

    /**
     * This utility class should not be instantiated anywhere.
     * But since we want to rewrite to usual command pattern, it is protected
     */
    protected HgCommand() {
    }

    private static String getEncoding() {
        String enc = null;
        String prop = System.getProperty("mercurial.encoding", ""); //NOI18N
        if (prop.isEmpty()) {
            prop = System.getenv(ENV_HGENCODING);
        }
        if (prop != null && !prop.isEmpty()) {
            try {
                if (Charset.isSupported(prop)) {
                    enc = prop;
                }
            } catch (java.nio.charset.IllegalCharsetNameException ex) { }
            if (enc == null) {
                Mercurial.LOG.log(Level.WARNING, "Unsupported encoding {0}, using default", prop); //NOI18N
            }
        }
        return enc;
    }

    private static File getQSeriesFile (File repository, String queueName) {
        String folderName = HG_QPATCHES_NAME;
        if (!HG_QPATCHES_NAME.equals(queueName)) {
            folderName += "-" + queueName; //NOI18N
        }
        return new File(HgUtils.getHgFolderForRoot(repository), folderName + File.separatorChar + "series");
    }

    protected static List<String> getListOfChangedFiles (File repository, List<String> attributes,
            String rev1, String rev2) throws HgException {
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_DIFF_CMD);
        command.add(HG_OPT_STAT);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        if (rev1 != null) {
            command.add(HG_FLAG_REV_CMD);
            if (rev2 == null || HgRevision.CURRENT.getRevisionNumber().equals(rev2)) {
                command.add(rev1);
            } else {
                command.add(rev1 + ":" + rev2); //NOI18N
            }
        }
        command.addAll(attributes);
        List<String> list = exec(command);
        List<String> changedFiles = new ArrayList<String>(list.size());
        if (!list.isEmpty() && isErrorNoRepository(list.get(0))) {
            OutputLogger logger = OutputLogger.getLogger(repository);
            try {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
            } finally {
                logger.closeLog();
            }
        }
        Pattern p = Pattern.compile("^ (.+)\\s*\\|.*?$"); //NOI18N
        for (String line : list) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String path = m.group(1);
                while (path.endsWith(" ")) path = path.substring(0, path.length() - 1);
                if (Utilities.isWindows()) {
                    path = path.replace("/", File.separator); //NOI18N
                }
                changedFiles.add(path);
            }
        }
        return changedFiles;
    }

    /**
     * Command working with a remote repository.
     * If a hg command fails because of authentication failure, login dialog is raised and the command is ovoked again with
     * entered credentials.
     */
    private static class InterRepositoryCommand {
        protected File repository;
        protected HgURL remoteUrl;
        protected OutputLogger logger;
        protected String hgCommand;
        protected String hgCommandType;
        protected String defaultUrl;
        protected boolean outputDetails;
        protected List<String> additionalOptions;
        protected UserCredentialsSupport credentialsSupport;
        protected boolean showSaveOption;
        protected String[] urlPathProperties;
        private PasswordAuthentication credentials;

        public InterRepositoryCommand () {
            hgCommand = getHgCommand();
            outputDetails = true;
            additionalOptions = new ArrayList<String>();
            urlPathProperties = new String[0];
        }

        /**
         * This will save the credentials along with URLs into the hgrc config file if user checked 'Save values' in a login dialog
         * @param propertyName property to be saved (default, default-push/pull)
         */
        private void saveCredentials (String propertyName) {
            try {
                // user logged-in successfully during the process and checked 'Save values'
                HgModuleConfig.getDefault().setProperty(repository, propertyName, new HgURL(remoteUrl.toHgCommandUrlString(), credentials.getUserName(), null).toCompleteUrlString());
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
        }

        public List<String> invoke() throws HgException {
            List<String> list = null;
            boolean retry = true;
            boolean showLoginWindow = !Boolean.TRUE.equals(disabledUI.get());
            credentials = null;
            String rawUrl = remoteUrl.toUrlStringWithoutUserInfo();
            if (remoteUrl.getUsername() != null && remoteUrl.getPassword() == null) {
                char[] password = KeyringSupport.read(HgUtils.PREFIX_VERSIONING_MERCURIAL_URL, remoteUrl.toHgCommandStringWithNoPassword()); //NOI18N
                if (password != null) {
                    credentials = new PasswordAuthentication(remoteUrl.getUsername(), password);
                }
            }

            HgURL url = remoteUrl;
            credentialsSupport = new UserCredentialsSupport();
            credentialsSupport.setShowSaveOption(showSaveOption);
            try {
                while (retry) {
                    retry = false;
                    try {
                        if (credentials != null) {
                            url = new HgURL(remoteUrl.toHgCommandUrlString(), credentials.getUserName(), credentials.getPassword());
                        }
                    } catch (URISyntaxException ex) {
                        // this should NEVER happen
                        Mercurial.LOG.log(Level.SEVERE, null, ex);
                        break;
                    }
                    List<Object> command = new ArrayList<Object>();

                    command.add(hgCommand);
                    command.add(hgCommandType);
                    for (String s : additionalOptions) {
                        command.add(s);
                    }
                    command.add(HG_OPT_REPOSITORY);
                    command.add(repository.getAbsolutePath());
                    command.add(url);

                    String proxy = getGlobalProxyIfNeeded(defaultUrl, outputDetails, logger);
                    if (proxy != null) {
                        List<String> env = new ArrayList<String>();
                        env.add(HG_PROXY_ENV + proxy);
                        list = execEnv(command, env);
                    } else {
                        list = exec(command);
                    }
                    // clear the cached password, remove it from memory
                    if (url != remoteUrl) {
                        url.clearPassword();
                    }

                    if (!list.isEmpty() &&
                            isErrorAbort(list.get(list.size() - 1))) {
                        if (HG_PUSH_CMD.equals(hgCommandType) && isErrorAbortPush(list.get(list.size() - 1))) {
                            //
                        } else {
                            if ((credentials = handleAuthenticationError(list, repository, rawUrl, credentials == null ? "" : credentials.getUserName(), credentialsSupport, hgCommandType, showLoginWindow)) != null) { //NOI18N
                                // auth redone, try again
                                retry = true;
                                if (credentials != null) {
                                    try {
                                        KeyringSupport.save(HgUtils.PREFIX_VERSIONING_MERCURIAL_URL, new HgURL(remoteUrl.toHgCommandUrlString(), credentials.getUserName(), null).toHgCommandStringWithNoPassword(), credentials.getPassword().clone(), null);
                                    } catch (URISyntaxException ex) {
                                        Mercurial.LOG.log(Level.SEVERE, null, ex);
                                    }
                                }
                            } else {
                                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                            }
                        }
                    }
                }
            } finally {
                if (credentials != null) {
                    savePathProperties();
                    Arrays.fill(credentials.getPassword(), '\0');
                }
            }
            return list;
        }

        private void savePathProperties () {
            if (credentialsSupport != null && credentialsSupport.shallSaveValues()) {
                for (String pathProp : urlPathProperties) {
                    saveCredentials(pathProp);
                }
            }
        }
    }

    private static String prepareLogTemplate (File temporaryFolder, String changesetFileName) throws IOException {
        InputStream isChangeset = HgCommand.class.getResourceAsStream(changesetFileName);
        InputStream isStyle = HgCommand.class.getResourceAsStream(HG_LOG_STYLE_NAME);
        File styleFile = new File(temporaryFolder, HG_LOG_STYLE_NAME);
        File changesetFile = new File(temporaryFolder, HG_LOG_CHANGESET_GENERAL_NAME);
        Utils.copyStreamsCloseAll(new FileOutputStream(changesetFile), isChangeset);
        Utils.copyStreamsCloseAll(new FileOutputStream(styleFile), isStyle);

        return HG_ARGUMENT_STYLE + styleFile.getAbsolutePath();
    }

    /**
     * Splits attributes into groups so the final length of the command does not outgrow the max length of commandline
     * @param basicCommand basic command
     * @param files files to add
     * @param includeFolders if set to false, folders will not be added to attributes
     * @return
     */
    protected static List<List<String>> splitAttributes (File repository, List<String> basicCommand, List<File> files, boolean includeFolders) {
        List<List<String>> attributes = new ArrayList<List<String>>();
        int basicCommandSize = 0, commandSize;
        boolean cwdParamIncluded = false;
        for (String s : basicCommand) {
            if (HG_OPT_CWD_CMD.equals(s)) {
                cwdParamIncluded = true;
            }
            basicCommandSize += s.length() + 1;
        }
        if (!cwdParamIncluded) {
            // we're adding files as relative paths, we have to set CWD manually!
            basicCommand.add(HG_OPT_CWD_CMD);
            basicCommand.add(repository.getAbsolutePath());
            basicCommandSize += HG_OPT_CWD_CMD.length() + repository.getAbsolutePath().length() + 2;
        }
        // iterating through all files, cannot add all files immediately, too many files can cause troubles
        // adding files to the command one by one and testing if the command's size doesn't exceed OS limits
        ListIterator<File> iterator = files.listIterator();
        while (iterator.hasNext()) {
            // each loop will call one add command
            List<String> commandAttributes = new ArrayList<String>();
            commandSize = basicCommandSize;
            boolean fileAdded = false;
            while (iterator.hasNext()) {
                File f = iterator.next();
                if (!includeFolders && f.isDirectory()) {
                    continue;
                }
                // test if limits aren't exceeded
                String filePath = getPathParameter(repository, f);
                commandSize += filePath.length() + 1;
                if (fileAdded // at least one file must be added
                        && isTooLongCommand(commandSize)) {
                    Mercurial.LOG.fine("splitAttributes: files in loop");  //NOI18N
                    iterator.previous();
                    break;
                }
                // We do not look for files to ignore as we should not here
                // with a file to be ignored.
                commandAttributes.add(filePath);
                fileAdded = true;
            }
            attributes.add(commandAttributes);
        }
        return attributes;
    }

    /**
     * Returns file's path as a mercurial command parameter. It is either:
     * <ul>
     * <li>relative path to given root if the file lies under the root, or:</li>
     * <li>canonical path if the file does not exist - fix for #198353 -, or:</li>
     * <li>absolute path</li>
     * </ul>
     */
    private static String getPathParameter (File root, File file) {
        String rootPath = root.getAbsolutePath();
        if (!rootPath.endsWith(File.separator)) {
            rootPath = rootPath + File.separator;
        }
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath)) {
            filePath = filePath.substring(rootPath.length());
        } else if (!file.exists()) {
            try {
                filePath = file.getCanonicalPath();
                if (filePath.startsWith(rootPath)) {
                    filePath = filePath.substring(rootPath.length());
                }
            } catch (IOException ex) { }
        }
        return filePath;
    }
}
