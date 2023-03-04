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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "MSG_NO_REPOSITORY_ERR=Mercurial repository could not be found"
})
public final class StatusCommand extends HgCommand<Map<File, FileInformation>> {
    
    private final File repository;
    private List<String> output;
    private String statusFlags;
    private String revisionFrom;
    private String revisionTo;
    private final List<File> files;
    private static final Logger LOG = Logger.getLogger(StatusCommand.class.getName());
    
    private static final String HG_STATUS_FLAG_INTERESTING_CMD = "-mardu"; // NOI18N
    private static final String HG_STATUS_FLAG_COPIES = "C"; // NOI18N
    
    private static final char HG_STATUS_CODE_MODIFIED = 'M' + ' ';    // NOI18N // STATUS_VERSIONED_MODIFIEDLOCALLY
    private static final char HG_STATUS_CODE_ADDED = 'A' + ' ';      // NOI18N // STATUS_VERSIONED_ADDEDLOCALLY
    private static final char HG_STATUS_CODE_REMOVED = 'R' + ' ';   // NOI18N  // STATUS_VERSIONED_REMOVEDLOCALLY - still tracked, hg update will recover, hg commit
    private static final char HG_STATUS_CODE_CLEAN = 'C' + ' ';     // NOI18N  // STATUS_VERSIONED_UPTODATE
    private static final char HG_STATUS_CODE_DELETED = '!' + ' ';    // NOI18N // STATUS_VERSIONED_DELETEDLOCALLY - still tracked, hg update will recover, hg commit no effect
    private static final char HG_STATUS_CODE_NOTTRACKED = '?' + ' '; // NOI18N // STATUS_NOTVERSIONED_NEWLOCALLY - not tracked
    private static final char HG_STATUS_CODE_IGNORED = 'I' + ' ';     // NOI18N // STATUS_NOTVERSIONED_EXCLUDE - not shown by default
    private static final char HG_STATUS_CODE_CONFLICT = 'U' + ' ';    // NOI18N // STATUS_VERSIONED_CONFLICT - TODO when Hg status supports conflict markers
    private static final char HG_STATUS_CODE_ABORT = 'a' + 'b';    // NOI18N
    private boolean detectCopies;
    private boolean detectConflicts;
    
    public StatusCommand (File repository, List<File> files) {
        Parameters.notNull("repository", repository);
        Parameters.notNull("files", files);
        this.repository = repository;
        this.files = files;
        this.statusFlags = HG_STATUS_FLAG_INTERESTING_CMD;
        this.detectConflicts = true;
    }
    
    /**
     * Creates a status command for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param files files or directories of interest
     * @param detectCopies if set to true then the command takes longer and returns also original files for renames and copies
     */
    public static StatusCommand create (File repository, List<File> files, boolean detectCopies) {
        return new StatusCommand(repository, files)
                .setStatusFlags(HG_STATUS_FLAG_INTERESTING_CMD)
                .setDetectCopies(detectCopies);
    }
    
    public StatusCommand setRevisionFrom (String revisionFrom) {
        this.revisionFrom = revisionFrom;
        return this;
    }
    
    public StatusCommand setRevisionTo (String revisionTo) {
        this.revisionTo = revisionTo;
        return this;
    }
    
    public StatusCommand setDetectCopies (boolean detectCopies) {
        this.detectCopies = detectCopies;
        return this;
    }
    
    /**
     * Instruct the status command to also detect conflicts. The default
     * value is <code>false</code> so you need to call this method only if
     * you actually want not to detect conflicts and make the command a bit
     * faster.
     * 
     * @param detectConflicts detect conflicts or not
     */
    public StatusCommand setDetectConflicts (boolean detectConflicts) {
        this.detectConflicts = detectConflicts;
        return this;
    }
    
    private StatusCommand setStatusFlags (String statusFlags) {
        this.statusFlags = statusFlags;
        return this;
    }
    
    @Override
    public Map<File, FileInformation> call () throws HgException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "getStatusWithFlags: starting for {0}", files); //NOI18N
            startTime = System.currentTimeMillis();
        }
        try {
            return runInternal();
        } finally {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "getStatusWithFlags for {0} lasted {1}", new Object[]{files, System.currentTimeMillis() - startTime}); //NOI18N
            }
        }
    }
        
    private Map<File, FileInformation> runInternal () throws HgException {
        CommandParameters args = new CommandParameters(HgCommand.HG_STATUS_CMD);
        
        String flags = this.statusFlags;
        if (detectCopies) {
            flags = flags + HG_STATUS_FLAG_COPIES;
        }
        
        args.add(flags)
                .addRepositoryLocation(repository.getAbsolutePath())
                .add(HgCommand.HG_OPT_CWD_CMD)
                .add(repository.getAbsolutePath());
        
        List<String> command = args.toCommand();
        List<List<String>> attributeGroups = HgCommand.splitAttributes(repository, command, files, true);
        boolean workDirStatus = true;
        boolean skipMidChanges = false;
        if (revisionFrom != null) {
            command.add(HgCommand.HG_FLAG_REV_CMD);
            if (revisionTo == null || HgRevision.CURRENT.getRevisionNumber().equals(revisionTo)) {
                skipMidChanges = !HgRevision.BASE.getRevisionNumber().equals(revisionFrom);
                command.add(revisionFrom);
            } else {
                skipMidChanges = true;
                command.add(revisionFrom + ":" + revisionTo); //NOI18N
                workDirStatus = false;
            }
        }
        List<String> commandOutput = new ArrayList<String>();
        List<String> changedPaths = skipMidChanges ? new ArrayList<String>() : null;
        for (List<String> attributes : attributeGroups) {
            if (changedPaths != null) {
                changedPaths.addAll(HgCommand.getListOfChangedFiles(repository, attributes, revisionFrom, revisionTo));
            }
            List<String> finalCommand = new ArrayList<String>(command);
            finalCommand.addAll(attributes);
            List<String> list = exec(finalCommand);
            if (!list.isEmpty() && HgCommand.isErrorNoRepository(list.get(0))) {
                OutputLogger logger = OutputLogger.getLogger(repository);
                try {
                    handleError(finalCommand, list, Bundle.MSG_NO_REPOSITORY_ERR(), logger);
                } finally {
                    logger.closeLog();
                }
            } else if (detectConflicts && workDirStatus && HgUtils.hasResolveCommand(Mercurial.getInstance().getVersion())) {
                try {
                    List<String> unresolved = HgCommand.getUnresolvedFiles(repository, attributes);
                    list.addAll(unresolved);
                } catch (HgException ex) {
                    //
                }
            }
            commandOutput.addAll(list);
        }
        Map<File, FileInformation> infos = processStatusResult(commandOutput, repository, flags, changedPaths);
        if (LOG.isLoggable(Level.FINE)) {
            if (commandOutput.size() < 10) {
                LOG.log(Level.FINE, "getStatusWithFlags(): repository path: {0} status flags: {1} status list {2}", // NOI18N
                    new Object[] {repository.getAbsolutePath(), flags, commandOutput} );
            } else {
                LOG.log(Level.FINE, "getStatusWithFlags(): repository path: {0} status flags: {1} status list has {2} elements", // NOI18N
                    new Object[] {repository.getAbsolutePath(), flags, commandOutput.size()} );
            }
        }
        return infos;
    }

    private static Map<File, FileInformation> processStatusResult (List<String> commandOutput, File repository,
            String statusFlags, List<String> changedPaths) {
        Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>(commandOutput.size());
        File file = null;
        FileInformation prev_info = null;
        String repositoryPath = repository.getAbsolutePath();
        for (String statusLine : commandOutput) {
            if (statusLine.isEmpty()) {
                continue;
            }
            FileInformation info = getFileInformationFromStatusLine(statusLine);
            LOG.log(Level.FINE, "getStatusWithFlags(): status line {0}  info {1}", new Object[]{statusLine, info}); // NOI18N
            if (statusLine.length() > 0) {
                if (statusLine.charAt(0) == ' ') {
                    // Locally Added but Copied
                    if (file != null) {
                        File original = getFileFromStatusLine(statusLine, repository);
                        prev_info =  new FileInformation(prev_info.getStatus(),
                                new FileStatus(file, original), false);
                        LOG.log(Level.FINE, "getStatusWithFlags(): prev_info {0}  filePath {1}", new Object[]{prev_info, file}); // NOI18N
                    } else {
                        LOG.log(Level.FINE, "getStatusWithFlags(): repository path: {0} status flags: {1} status line {2} filepath == nullfor prev_info ", new Object[]{repository.getAbsolutePath(), statusFlags, statusLine}); // NOI18N
                    }
                    continue;
                } else {
                    if (file != null) {
                        repositoryFiles.put(file, prev_info);
                    }
                }
            }
            if(info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED 
                    || info.getStatus() == FileInformation.STATUS_UNKNOWN) continue;
            if (changedPaths == null || (info.getStatus() & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY) == 0
                    || changedPaths.contains(getRelativePathFromStatusLine(statusLine, repositoryPath))) {
                file = getFileFromStatusLine(statusLine, repository);
            } else {
                // uninteresting file, changed in the middle of revision range and back again
                // e.g. line added and then removed again
                file = null;
                continue;
            }

            // Handle Conflict Status
            // TODO: remove this if Hg status supports Conflict marker
            if (existsConflictFile(file.getAbsolutePath())) {
                info = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, null, false);
                LOG.log(Level.FINE, "getStatusWithFlags(): CONFLICT repository path: {0} status flags: {1} status line {2} CONFLICT {3}", new Object[]{repository.getAbsolutePath(), statusFlags, statusLine, file + HgCommand.HG_STR_CONFLICT_EXT}); // NOI18N
            }
            prev_info = info;
        }
        if (prev_info != null) {
            repositoryFiles.put(file, prev_info);
        }
        return repositoryFiles;
    }
    
    private static File getFileFromStatusLine (String statusLine, File repository) {
        File file;
        String repositoryPath = repository.getAbsolutePath();
        String path = getRelativePathFromStatusLine(statusLine, repositoryPath);
        if(Utilities.isWindows() && path.startsWith(repositoryPath)) {
            file = new File(path);  // prevent bogus paths (C:\tmp\hg\C:\tmp\hg\whatever) - see issue #139500
        } else {
            file = new File(repository, path);
        }
        return file;
    }

    /**
     * Gets file information for a given hg status output status line
     */
    private static FileInformation getFileInformationFromStatusLine(String status){
        FileInformation info = null;
        if (status == null || (status.length() == 0)) return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, null, false);

        char c0 = status.charAt(0);
        char c1 = status.charAt(1);
        switch(c0 + c1) {
        case HG_STATUS_CODE_MODIFIED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_ADDED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_REMOVED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_CLEAN:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE,null, false);
            break;
        case HG_STATUS_CODE_DELETED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_IGNORED:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED,null, false);
            break;
        case HG_STATUS_CODE_NOTTRACKED:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY,null, false);
            break;
        // Leave this here for whenever Hg status suports conflict markers
        case HG_STATUS_CODE_CONFLICT:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT,null, false);
            break;
        case HG_STATUS_CODE_ABORT:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED,null, false);
            break;
        default:
            info = new FileInformation(FileInformation.STATUS_UNKNOWN,null, false);
            break;
        }

        return info;
    }

    private static String getRelativePathFromStatusLine (String statusLine, String repositoryPath) {
        String path = statusLine.substring(2);
        if (Utilities.isWindows() && path.startsWith(repositoryPath)) {
            path = path.substring(repositoryPath.length() + 1);
        }
        return path;
    }
    
    public List<String> getOutput () {
        return output;
    }
}
