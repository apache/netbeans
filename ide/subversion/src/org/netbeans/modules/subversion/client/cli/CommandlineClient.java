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

package org.netbeans.modules.subversion.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.cli.commands.AddCommand;
import org.netbeans.modules.subversion.client.cli.commands.BlameCommand;
import org.netbeans.modules.subversion.client.cli.commands.CatCommand;
import org.netbeans.modules.subversion.client.cli.commands.CheckoutCommand;
import org.netbeans.modules.subversion.client.cli.commands.CleanupCommand;
import org.netbeans.modules.subversion.client.cli.commands.CommitCommand;
import org.netbeans.modules.subversion.client.cli.commands.CopyCommand;
import org.netbeans.modules.subversion.client.cli.commands.ExportCommand;
import org.netbeans.modules.subversion.client.cli.commands.ListPropertiesCommand;
import org.netbeans.modules.subversion.client.cli.commands.ImportCommand;
import org.netbeans.modules.subversion.client.cli.commands.InfoCommand;
import org.netbeans.modules.subversion.client.cli.commands.ListCommand;
import org.netbeans.modules.subversion.client.cli.commands.LogCommand;
import org.netbeans.modules.subversion.client.cli.commands.MergeCommand;
import org.netbeans.modules.subversion.client.cli.commands.MkdirCommand;
import org.netbeans.modules.subversion.client.cli.commands.MoveCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertyDelCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertyGetCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertySetCommand;
import org.netbeans.modules.subversion.client.cli.commands.RelocateCommand;
import org.netbeans.modules.subversion.client.cli.commands.RemoveCommand;
import org.netbeans.modules.subversion.client.cli.commands.ResolvedCommand;
import org.netbeans.modules.subversion.client.cli.commands.RevertCommand;
import org.netbeans.modules.subversion.client.cli.commands.StatusCommand;
import org.netbeans.modules.subversion.client.cli.commands.StatusCommand.Status;
import org.netbeans.modules.subversion.client.cli.commands.SwitchToCommand;
import org.netbeans.modules.subversion.client.cli.commands.UpdateCommand;
import org.netbeans.modules.subversion.client.cli.commands.UpgradeCommand;
import org.netbeans.modules.subversion.client.cli.commands.VersionCommand;
import org.netbeans.modules.subversion.client.parser.EntriesCache;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.ParserSvnStatus;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.AbstractClientAdapter;
import org.tigris.subversion.svnclientadapter.Annotations;
import org.tigris.subversion.svnclientadapter.Annotations.Annotation;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNConflictResolver;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback;
import org.tigris.subversion.svnclientadapter.ISVNMergeInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProgressListener;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.ISVNStatusCallback;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;
import org.tigris.subversion.svnclientadapter.SVNKeywords;
import org.tigris.subversion.svnclientadapter.SVNNotificationHandler;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNRevisionRange;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CommandlineClient extends AbstractClientAdapter implements ISVNClientAdapter {

    private String user;
    private String psswd;
    private File configDir;
    private NotificationHandler notificationHandler;
    private SvnWcParser wcParser;
    private Commandline cli;

    public static String ERR_CLI_NOT_AVALABLE = "commandline is not available";
    public static String ERR_JAVAHL_NOT_SUPPORTED = "unsupported javahl version";
    private static boolean supportedMetadataFormat;

    public CommandlineClient() {
        this.notificationHandler = new NotificationHandler();
        wcParser = new SvnWcParser();
        cli = new Commandline();
    }

    /**
     * @return true if old 1.5 format is supported
     */
    public boolean checkSupportedVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand();
        try {
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);
            if(!cmd.checkForErrors()) {
                if (cmd.isUnsupportedVersion()) {
                    Subversion.LOG.log(Level.WARNING, "Unsupported svn version. You need >= 1.5");
                }
                throw new SVNClientException(ERR_CLI_NOT_AVALABLE + "\n" + cmd.getOutput());
            } else {
                return supportedMetadataFormat = cmd.isMetadataFormatSupported();
            }
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw new SVNClientException(ERR_CLI_NOT_AVALABLE);
        }
    }

    public String getVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand();
        try {
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);
            return cmd.getOutput();
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw SVNClientException.wrapException(ex);
        }
    }

    @Override
    public void addNotifyListener(ISVNNotifyListener l) {
        notificationHandler.add(l);
    }

    @Override
    public void removeNotifyListener(ISVNNotifyListener l) {
        notificationHandler.remove(l);
    }

    @Override
    public void setUsername(String user) {
        this.user = user;
    }

    @Override
    public void setPassword(String psswd) {
        this.psswd = psswd;
    }

    @Override
    public void setConfigDirectory(File file) throws SVNClientException {
        this.configDir = file;
    }

    @Override
    public SVNNotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    @Override
    public void addFile(File file) throws SVNClientException {
        addFile(new File[] { file }, false);
    }

    public void addFile(File[] file, boolean recursive) throws SVNClientException {
        AddCommand cmd = new AddCommand(file, recursive, false);
        exec(cmd);
    }

    @Override
    public void addDirectory(File dir, boolean recursive) throws SVNClientException {
        addDirectory(dir, recursive, false);
    }

    @Override
    public void addDirectory(File dir, boolean recursive, boolean force) throws SVNClientException {
        AddCommand cmd = new AddCommand(new File[] { dir } , recursive, force);
        exec(cmd);
    }

    @Override
    public void checkout(SVNUrl url, File file, SVNRevision revision, boolean recurse) throws SVNClientException {
        CheckoutCommand cmd = new CheckoutCommand(url, file, revision, recurse);
        exec(cmd);
    }

    @Override
    public void doExport(SVNUrl url, File destination, SVNRevision revision, boolean force) throws SVNClientException {
        ExportCommand cmd = new ExportCommand(url, destination, revision, force);
        exec(cmd);
    }

    @Override
    public void doExport(File fileFrom, File fileTo, boolean force) throws SVNClientException {
        ExportCommand cmd = new ExportCommand(fileFrom, fileTo, force);
        exec(cmd);
    }

    @Override
    public long commit(File[] files, String message, boolean recurse) throws SVNClientException {
        return commit(files, message, false, recurse);
    }

    @Override
    public long commit(File[] files, String message, boolean keep, boolean recursive) throws SVNClientException {
        int retry = 0;
        CommitCommand cmd = null;
        while (true) {
            try {
                cmd = new CommitCommand(files, keep, recursive, message); // prevent cmd reuse
                exec(cmd);
                break;
            } catch (SVNClientException e) {
                if (e.getMessage().startsWith("svn: Attempted to lock an already-locked dir")) {
                    Subversion.LOG.fine("ComandlineClient.comit() : " + e.getMessage());
                    try {
                        retry++;
                        if (retry > 14) {
                            throw e;
                        }
                        Thread.sleep(retry * 50);
                    } catch (InterruptedException ex) {
                        break;
                    }
                } else {
                    throw e;
                }
            }
        }
        return cmd != null ? cmd.getRevision() : SVNRevision.SVN_INVALID_REVNUM;
    }

    @Override
    public ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision, boolean recursivelly) throws SVNClientException {
        ListCommand cmd = new ListCommand(url, revision, recursivelly);
        exec(cmd);
        return cmd.getEntries();
    }

    @Override
    public ISVNInfo getInfo(SVNUrl url) throws SVNClientException {
        return super.getInfo(url);
    }

    @Override
    public ISVNInfo getInfo(File file) throws SVNClientException {
        return getInfoFromWorkingCopy(file);
    }

    @Override
    public String getPostCommitError () {
        return null;
    }

    private ISVNInfo[] getInfo(File[] files, SVNRevision revision, SVNRevision pegging) throws SVNClientException, SVNClientException {
        if(files == null || files.length == 0) {
            return new ISVNInfo[0];
        }
        InfoCommand infoCmd = new InfoCommand(files, revision, pegging);
        exec(infoCmd);
        ISVNInfo[] infos = infoCmd.getInfo();

        return infos;
    }

    @Override
    public ISVNInfo getInfo(SVNUrl url, SVNRevision revision, SVNRevision pegging) throws SVNClientException {
        InfoCommand cmd = new InfoCommand(url, revision, pegging);
        exec(cmd);
        ISVNInfo[] infos = cmd.getInfo();
        ISVNInfo info = null;
        if (infos.length > 0) {
            info = infos[0];
        }
        return info;
    }

    @Override
    public void copy(File fileFrom, File fileTo) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fileFrom, fileTo);
        exec(cmd);
    }

    @Override
    public void copy(File file, SVNUrl url, String msg) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(file, url, msg);
        exec(cmd);
    }

    @Override
    public void copy(SVNUrl url, File file, SVNRevision rev) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(url, file, rev);
        exec(cmd);
    }

    @Override
    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException {
        copy(fromUrl, toUrl, msg, rev, false);
    }

    @Override
    public void remove(SVNUrl[] url, String msg) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(File[] files, boolean force) throws SVNClientException {
        RemoveCommand cmd = new RemoveCommand(files, force);
        exec(cmd);
    }

    @Override
    public void doImport(File File, SVNUrl url, String msg, boolean recursivelly) throws SVNClientException {
        ImportCommand cmd = new ImportCommand(File, url, recursivelly, msg);
        exec(cmd);
    }

    @Override
    public void mkdir(SVNUrl url, String msg) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(url, msg);
        exec(cmd);
    }

    @Override
    public void mkdir(SVNUrl url, boolean parents, String msg) throws SVNClientException {
        if(parents) {
            List<SVNUrl> parent = getAllNotExistingParents(url);
            for (SVNUrl p : parent) {
                mkdir(p, msg);
            }
        } else {
            mkdir(url, msg);
        }
    }

    @Override
    public void mkdir(File file) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(file);
        exec(cmd);
    }

    @Override
    public void move(File fromFile, File toFile, boolean force) throws SVNClientException {
        MoveCommand cmd = new MoveCommand(fromFile, toFile, force);
        exec(cmd);
    }

    @Override
    public void move(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException {
        MoveCommand cmd = new MoveCommand(fromUrl, toUrl, msg, rev);
        exec(cmd);
    }

    @Override
    public long update(File file, SVNRevision rev, boolean recursivelly) throws SVNClientException {
        UpdateCommand cmd = new UpdateCommand(new File[] { file }, rev, recursivelly, false);
        exec(cmd);
        return cmd.getRevision();
    }

    @Override
    public long[] update(File[] files, SVNRevision rev, boolean recursivelly, boolean ignoreExternals) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void revert(File file, boolean recursivelly) throws SVNClientException {
        revert(new File[]{file}, recursivelly);
    }

    public void revert(File[] files, boolean recursivelly) throws SVNClientException {
        if(files == null || files.length == 0) {
            return;
        }
        RevertCommand cmd = new RevertCommand(files, recursivelly);
        exec(cmd);
    }

    @Override
    public ISVNStatus[] getStatus(File[] files) throws SVNClientException {

        Map<File, ISVNStatus> unversionedMap = new HashMap<File, ISVNStatus>();
        List<File> filesForStatus = new ArrayList<File>();
        List<File> filesForInfo = new ArrayList<File>();
        for (File f : files) {
            if(!isManaged(f)) {
                unversionedMap.put(f, new SVNStatusUnversioned(f));
            } else {
                filesForStatus.add(f);
            }
        }

        Status[] statusValues = new Status[] {};
        if (!filesForStatus.isEmpty()) {
            StatusCommand statusCmd = new StatusCommand(filesForStatus.toArray(new File[0]), true, false, false, false);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        }
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(new File(status.getPath()));
            }
        }
        Map<File, ISVNInfo> infoMap = new HashMap<File, ISVNInfo>();
        if (!filesForInfo.isEmpty()) {
            ISVNInfo[] infos = getInfo(filesForInfo.toArray(new File[0]), null, null);
            for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);
        }

        Map<File, ISVNStatus> statusMap = new HashMap<File, ISVNStatus>();
        for (Status status : statusValues) {
            File file = new File(status.getPath());
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(file, new CLIStatus(status, status.getPath()));
                } else {
                    statusMap.put(file, new SVNStatusUnversioned(file, SVNStatusKind.IGNORED.equals(status.getWcStatus())));
                }
            } else {
                ISVNInfo info = infoMap.get(file);
                if (info != null) {
                    statusMap.put(file, new CLIStatus(status, info));
                }
            }
        }

        List<ISVNStatus> ret = new ArrayList<ISVNStatus>();
        for (File f : files) {
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = unversionedMap.get(f);
            }
            if(s != null) {
                ret.add(s);
            }
        }
        return ret.toArray(new ISVNStatus[0]);
    }

    @Override
    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException {
        return getStatus(file, descend, getAll, contactServer, false);
    }

    // XXX merge with get status
    @Override
    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals) throws SVNClientException {
        Status[] statusValues = null;
        try {
            if(!isManaged(file)) {
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            }
            StatusCommand statusCmd = new StatusCommand(new File[] { file }, getAll, descend, contactServer, ignoreExternals);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        } catch (SVNClientException e) {
            if(SvnClientExceptionHandler.isUnversionedResource(e.getMessage())) {
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            } else {
                throw e;
            }
        }

        List<File> filesForInfo = new ArrayList<File>();
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(new File(status.getPath()));
            }
        }
        ISVNInfo[] infos = getInfo(filesForInfo.toArray(new File[0]), null, null);

        Map<File, ISVNInfo> infoMap = new HashMap<File, ISVNInfo>();
        for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);

        Map<File, ISVNStatus> statusMap = new HashMap<File, ISVNStatus>();
        for (Status status : statusValues) {
            File f = new File(status.getPath());
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(f, new CLIStatus(status, status.getPath()));
                } else {
                    statusMap.put(f, new SVNStatusUnversioned(f, SVNStatusKind.IGNORED.equals(status.getWcStatus())));
                }
            } else {
                ISVNInfo info = infoMap.get(f);
                if (info != null) {
                    statusMap.put(f, new CLIStatus(status, info));
                }
            }
        }

        List<ISVNStatus> ret = new ArrayList<ISVNStatus>();
        for (Status status : statusValues) {
            File f = new File(status.getPath());
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = new SVNStatusUnversioned(f);
            }
            ret.add(s);
        }
        return ret.toArray(new ISVNStatus[0]);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return super.getLogMessages(url, revStart, revEnd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(url, null, revStart, revEnd, false, fetchChangePath);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        LogCommand cmd = new LogCommand(url, paths, revStart, revEnd, SVNRevision.HEAD, stopOnCopy, fetchChangePath, 0);
        return getLog(cmd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revPeg, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        LogCommand cmd = new LogCommand(url, null, revStart, revEnd, revPeg, stopOnCopy, fetchChangePath, limit);
        return getLog(cmd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return super.getLogMessages(file, revStart, revEnd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, false, fetchChangePath);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, stopOnCopy, fetchChangePath, 0);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        return getLogMessages(file, SVNRevision.HEAD, revStart, revEnd, stopOnCopy, fetchChangePath, limit, false);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision pegRevision, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit, boolean includeMergedRevisions) throws SVNClientException {
        LogCommand logCmd;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) &&
            info.getCopyUrl() != null)
        {
            logCmd = new LogCommand(info.getCopyUrl(), null, revStart, revEnd, pegRevision, stopOnCopy, fetchChangePath, limit);
        } else {
            logCmd = new LogCommand(file, revStart, revEnd, pegRevision, stopOnCopy, fetchChangePath, limit);
        }
        return getLog(logCmd);
    }

    private ISVNLogMessage[] getLog(LogCommand cmd) throws SVNClientException {
        exec(cmd);
        return cmd.getLogMessages();
    }

    @Override
    public InputStream getContent(SVNUrl url, SVNRevision rev) throws SVNClientException {
        return getContent(url, rev, null);
    }

    @Override
    public InputStream getContent(File file, SVNRevision rev) throws SVNClientException {
        CatCommand cmd = new CatCommand(file, rev);
        exec(cmd);
        return cmd.getOutput();
    }

    @Override
    public void propertySet(File file, String name, String value, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertySetCommand cmd = new PropertySetCommand(name, value, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }

    @Override
    public void propertySet(File file, String name, File propFile, boolean rec) throws SVNClientException, IOException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertySetCommand cmd = new PropertySetCommand(name, propFile, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }

    @Override
    public void propertyDel(File file, String name, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertyDelCommand cmd = new PropertyDelCommand(file, name, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }

    @Override
    public ISVNProperty propertyGet(final File file, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(file, name), name, null, file);
    }

    @Override
    public ISVNProperty propertyGet(SVNUrl url, String name) throws SVNClientException {
        return super.propertyGet(url, name);
    }

    @Override
    public ISVNProperty propertyGet(final SVNUrl url, SVNRevision rev, SVNRevision peg, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(url, rev, peg, name), name, url, null);
    }

    ISVNProperty propertyGet(PropertyGetCommand cmd, final String name, final SVNUrl url, final File file) throws SVNClientException {
        exec(cmd);
        final byte[] bytes = cmd.getOutput();
        if(bytes == null || bytes.length == 0) {
            return null;
        }
        return new ISVNProperty() {
            @Override
            public String getName() {
                return name;
            }
            @Override
            public String getValue() {
                return new String(bytes);
            }
            @Override
            public File getFile() {
                return file;
            }
            @Override
            public SVNUrl getUrl() {
                return url;
            }
            @Override
            public byte[] getData() {
                return bytes;
            }
        };
    }

    @Override
    public List getIgnoredPatterns(File file) throws SVNClientException {
        return super.getIgnoredPatterns(file);
    }

    @Override
    public void addToIgnoredPatterns(File file, String value) throws SVNClientException {
        super.addToIgnoredPatterns(file, value);
    }

    @Override
    public void setIgnoredPatterns(File file, List l) throws SVNClientException {
        super.setIgnoredPatterns(file, l);
    }

    @Override
    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return annotate(new BlameCommand(url, revStart, revEnd), new CatCommand(url, revEnd, null));
    }

    @Override
    public ISVNAnnotations annotate(File file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        BlameCommand blameCommand;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) &&
            info.getCopyUrl() != null)
        {
            blameCommand = new BlameCommand(info.getCopyUrl(), revStart, revEnd);
        } else {
            blameCommand = new BlameCommand(file, revStart, revEnd);
        }
        return annotate(blameCommand, new CatCommand(file, revEnd));
    }

    public ISVNAnnotations annotate(BlameCommand blameCmd, CatCommand catCmd) throws SVNClientException {
        exec(blameCmd);
        Annotation[] annotations = blameCmd.getAnnotation();
        exec(catCmd);
        InputStream is = catCmd.getOutput();

        Annotations ret = new Annotations();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
            for (Annotation annotation : annotations) {
                String line = null;
                try {
                    line = r.readLine();
                } catch (IOException ex) {
                    // try at least to return the annotations
                    Subversion.LOG.log(Level.INFO, ex.getMessage(), ex);
                }
                annotation.setLine(line);
                ret.addAnnotation(annotation);
            }
        } finally {
            if (r != null) {
                try { r.close(); } catch (IOException e) {}
            }
        }
        return ret;
    }

    @Override
    public ISVNProperty[] getProperties (final File file) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(file, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<ISVNProperty>(names.size());
        for (final String name : names) {
            ISVNProperty prop = propertyGet(file, name);
            if (prop == null) {
                props.add(new ISVNProperty() {
                    @Override
                    public String getName() {
                        return name;
                    }
                    @Override
                    public String getValue() {
                        return "";
                    }
                    @Override
                    public File getFile() {
                        return file;
                    }
                    @Override
                    public SVNUrl getUrl() {
                        return null;
                    }
                    @Override
                    public byte[] getData() {
                        return new byte[0];
                    }
                });
            } else {
                props.add(prop);
            }
        }
        return props.toArray(new ISVNProperty[0]);
    }

    @Override
    public ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(url, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<ISVNProperty>(names.size());
        for (String name : names) {
            ISVNProperty prop = propertyGet(url, name);
            if (prop != null) {
                props.add(prop);
            }
        }
        return props.toArray(new ISVNProperty[0]);
    }

    @Override
    public void resolved(File file) throws SVNClientException {
        ResolvedCommand cmd = new ResolvedCommand(file, false);
        exec(cmd);
    }

    @Override
    public void cancelOperation() throws SVNClientException {
        cli.interrupt();
    }

    @Override
    public void switchToUrl(File file, SVNUrl url, SVNRevision rev, boolean rec) throws SVNClientException {
        SwitchToCommand cmd = new SwitchToCommand(file, url, rev, rec);
        exec(cmd);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse) throws SVNClientException {
       super.merge(startUrl, startRev, endUrl, endRev, file, force, recurse);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse, boolean dryRun) throws SVNClientException {
        super.merge(startUrl, startRev, endUrl, endRev, file, force, recurse, dryRun);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException {
        MergeCommand cmd = new MergeCommand(startUrl, endUrl, startRev, endRev, file, recurse, force, ignoreAncestry, dryRun);
        exec(cmd);
    }

    @Override
    public void relocate(String from, String to, String path, boolean rec) throws SVNClientException {
        RelocateCommand cmd = new RelocateCommand(from, to, path, rec);
        exec(cmd);
    }

    // parser start
    @Override
    public ISVNStatus getSingleStatus(File file) throws SVNClientException {
        if (supportedMetadataFormat) {
            try {
                return wcParser.getSingleStatus(file);
            } catch (LocalSubversionException ex) {
                if (ex.getCause() != null && EntriesCache.WC17FORMAT.equals(ex.getCause().getMessage())) {
                    // probably 1.7 WC, need to run command
                } else {
                    throw new SVNClientException(ex);
                }
            }
        }
        try {
            ISVNStatus[] statuses = getStatus(new File[]{file});
            return statuses.length > 0 ? statuses[0] : new ParserSvnStatus(
                    file,                                
                    null,
                    0,
                    "unknown",                            // NOI18N   
                    SVNStatusKind.UNVERSIONED.toString(),
                    SVNStatusKind.UNVERSIONED.toString(),
                    null,
                    0,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null);
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) || SvnClientExceptionHandler.isNodeNotFound(ex.getMessage())) {
                return new SVNStatusUnversioned(file);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll) throws SVNClientException {
        if (supportedMetadataFormat) {
            try {
                return wcParser.getStatus(file, descend, getAll);
            } catch (LocalSubversionException ex) {
                if (ex.getCause() != null && EntriesCache.WC17FORMAT.equals(ex.getCause().getMessage())) {
                    // probably 1.7 WC, need to run command
                } else {
                    throw new SVNClientException(ex);
                }
            }
        }
        return getStatus(file, descend, getAll, false, true);
    }

    @Override
    public ISVNInfo getInfoFromWorkingCopy(File file) throws SVNClientException {
        if (supportedMetadataFormat) {
            try {
                return wcParser.getInfoFromWorkingCopy(file);
            } catch (LocalSubversionException ex) {
                if (ex.getCause() != null && EntriesCache.WC17FORMAT.equals(ex.getCause().getMessage())) {
                    // probably 1.7 WC, need to run command
                } else {
                    throw new SVNClientException(ex);
                }
            }
        }
        try {
            ISVNInfo[] infos = getInfo(new File[] { file }, null, null);
            return infos.length > 0 ? infos[0] : null;
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isNodeNotFound(ex.getMessage())) {
                return wcParser.getUnknownInfo(file);
            } else {
                throw ex;
            }
        }
    }

    // parser end

    private void exec(SvnCommand cmd) throws SVNClientException {
        try {
            config(cmd);
            cli.exec(cmd);
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw new SVNClientException(ex);
        }
        checkErrors(cmd);
    }

    private void config(SvnCommand cmd) {
        cmd.setNotificationHandler(notificationHandler);
        cmd.setConfigDir(configDir);
        cmd.setUsername(user);
        cmd.setPassword(psswd);
    }

    private void checkErrors(SvnCommand cmd) throws SVNClientException {
        List<String> errors = cmd.getCmdError();
        if(errors == null || errors.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            sb.append(errors.get(i));
            if (i < errors.size() - 1) {
                sb.append('\n');
            }
        }
        throw new SVNClientException(sb.toString());
    }

    private List<SVNUrl> getAllNotExistingParents(SVNUrl url) throws SVNClientException {
        List<SVNUrl> ret = new ArrayList<SVNUrl>();
        if(url == null) {
            return ret;
        }
        try {
            getInfo(url);
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("Not a valid URL") > -1 || e.getMessage().contains("non-existent in revision")) {
                ret.addAll(getAllNotExistingParents(url.getParent()));
                ret.add(url);
            } else {
                throw e;
            }
        }
        return ret;
    }

    private boolean isManaged(SVNStatusKind s) {
        return !(s.equals(SVNStatusKind.UNVERSIONED) ||
                 s.equals(SVNStatusKind.NONE) ||
                 s.equals(SVNStatusKind.IGNORED) ||
                 s.equals(SVNStatusKind.EXTERNAL));
    }

    private boolean hasMetadata(File file) {
        return SvnUtils.hasMetadata(file);
    }

    private boolean isManaged(File file) {
        boolean managed = true;
        if (supportedMetadataFormat) {
            managed = hasMetadata(file.getParentFile()) || hasMetadata(file);
        }
        return managed;
    }

    // unsupported start

    @Override
    public long[] commitAcrossWC(File[] arg0, String arg1, boolean arg2, boolean arg3, boolean arg4) throws SVNClientException {
        return super.commitAcrossWC(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public ISVNDirEntry getDirEntry(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNDirEntry getDirEntry(File arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRevProperty(SVNUrl arg0, Number arg1, String arg2, String arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(File arg0, SVNRevision arg1, File arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(File arg0, SVNRevision arg1, File arg2, SVNRevision arg3, File arg4, boolean arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(File arg0, File arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, File arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(File arg0, SVNUrl arg1, SVNRevision arg2, File arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SVNKeywords getKeywords(File arg0) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setKeywords(File arg0, SVNKeywords arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SVNKeywords addKeywords(File arg0, SVNKeywords arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SVNKeywords removeKeywords(File arg0, SVNKeywords arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createRepository(File arg0, String arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(SVNUrl[] arg0, String arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unlock(SVNUrl[] arg0, boolean arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(File[] arg0, String arg1, boolean arg2) throws SVNClientException {
        throw new SVNClientException(new UnsupportedOperationException("Not supported with commandline client."));
    }

    @Override
    public void unlock(File[] arg0, boolean arg1) throws SVNClientException {
        throw new SVNClientException(new UnsupportedOperationException("Not supported with commandline client."));
    }

    @Override
    public boolean statusReturnsRemoteInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canCommitAcrossWC() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAdminDirectoryName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAdminDirectory(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPasswordCallback(ISVNPromptUserPassword arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNDirEntry[] getList(File arg0, SVNRevision arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cleanup(File file) throws SVNClientException {
        CleanupCommand cmd = new CleanupCommand(file);
        exec(cmd);
    }

    private void notifyChangedStatus(File file, boolean rec, ISVNStatus[] oldStatuses) throws SVNClientException {
        Map<File, ISVNStatus> oldStatusMap = new HashMap<File, ISVNStatus>();
        for (ISVNStatus s : oldStatuses) {
            oldStatusMap.put(s.getFile(), s);
        }
        ISVNStatus[] newStatuses = getStatus(file, rec, true);
        for (ISVNStatus newStatus : newStatuses) {
            ISVNStatus oldStatus = oldStatusMap.get(newStatus.getFile());
            if( (oldStatus == null && newStatus != null) ||
                 oldStatus.getTextStatus() != newStatus.getTextStatus() ||
                 oldStatus.getPropStatus() != newStatus.getPropStatus())
            {
                notificationHandler.notifyListenersOfChange(newStatus.getPath()); /// onNotify(cmd.getAbsoluteFile(s.getFile().getAbsolutePath()), null);
            }
       }
    }

    @Override
    public ISVNDirEntryWithLock[] getListWithLocks(SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, boolean bln) throws SVNClientException {
        // will not implement in commandline client
        return new ISVNDirEntryWithLock[0];
    }

    @Override
    public void copy(SVNUrl svnurl, File file, SVNRevision svnr, SVNRevision svnr1, boolean bln, boolean bln1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long[] update(File[] files, SVNRevision svnr, int i, boolean bln, boolean bln1, boolean bln2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // unsupported start
    @Override
    public void getLogMessages(File file, SVNRevision svnr, SVNRevision svnr1, SVNRevision svnr2, boolean bln, boolean bln1, long l, boolean bln2, String[] strings, ISVNLogMessageCallback i) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getLogMessages(SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, SVNRevision svnr2, boolean bln, boolean bln1, long l, boolean bln2, String[] strings, ISVNLogMessageCallback i) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRevProperty(SVNUrl svnurl, Number number, String string) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNAnnotations annotate(SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, SVNRevision svnr2, boolean bln, boolean bln1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNAnnotations annotate(File file, SVNRevision svnr, SVNRevision svnr1, SVNRevision svnr2, boolean bln, boolean bln1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision) throws SVNClientException {
        return getProperties(url, revision, pegRevision, false);
    }
    
    @Override
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision, boolean recursive) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(url, revision.toString(), recursive);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<ISVNProperty>(names.size());
        for (String name : names) {
            ISVNProperty prop = propertyGet(url, name);
            if (prop != null) {
                props.add(prop);
            }
        }
        return props.toArray(new ISVNProperty[0]);
    }

    @Override
    public ISVNProperty[] getRevProperties(SVNUrl svnurl, Number number) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNInfo[] getInfo(File file, boolean bln) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SVNDiffSummary[] diffSummarize(File file, SVNUrl svnurl, SVNRevision svnr, boolean bln) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNStatus[] getStatus (File file, boolean bln, boolean bln1, boolean bln2, boolean bln3, ISVNStatusCallback isvnsc) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void propertySet (SVNUrl svnurl, Number number, String string, String string1, String string2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNProperty[] getProperties (File file, boolean bln) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void upgrade (File wcRoot) throws SVNClientException {
        UpgradeCommand cmd = new UpgradeCommand(wcRoot);
        exec(cmd);
    }

    @Override
    public void switchToUrl (File file, SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, int i, boolean bln, boolean bln1, boolean bln2, boolean bln3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNStatus[] getStatus (File file, boolean bln, boolean bln1, boolean bln2, boolean bln3, boolean bln4, ISVNStatusCallback isvnsc) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ISVNProperty[] getPropertiesIncludingInherited (File file) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ISVNProperty[] getPropertiesIncludingInherited (File file, boolean bln, boolean bln1, List<String> list) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ISVNProperty[] getPropertiesIncludingInherited (SVNUrl svnurl) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ISVNProperty[] getPropertiesIncludingInherited (SVNUrl svnurl, boolean bln, boolean bln1, List<String> list) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class NotificationHandler extends SVNNotificationHandler {   }

    @Override
    public boolean isThreadsafe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addConflictResolutionCallback(ISVNConflictResolver arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProgressListener(ISVNProgressListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkout(SVNUrl arg0, File arg1, SVNRevision arg2, int arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNDirEntry[] getList(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNDirEntry[] getList(File arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Copies all files from <code>files</code> to repository URL at <code>targetUrl</code>.
     * @param files array of files which will be copied
     * @param targetUrl destination repository Url
     * @param message commit message
     * @param addAsChild not supported
     * @param makeParents creates parent folders
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    @Override
    public void copy(File[] files, SVNUrl targetUrl, String message, boolean addAsChild, boolean makeParents) throws SVNClientException {
        for (File file : files) {
            CopyCommand cmd = new CopyCommand(file, targetUrl, message, makeParents);
            exec(cmd);
        }
    }

    @Override
    public void copy(SVNUrl arg0, File arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev, boolean makeParents) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fromUrl, toUrl, msg, rev, makeParents);
        exec(cmd);
    }

    @Override
    public void copy(SVNUrl[] arg0, SVNUrl arg1, String arg2, SVNRevision arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long update(File arg0, SVNRevision arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, boolean arg4, boolean arg5, long arg6, boolean arg7) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getContent(SVNUrl url, SVNRevision rev, SVNRevision pegRevision) throws SVNClientException {
        CatCommand cmd = new CatCommand(url, rev, pegRevision);
        exec(cmd);
        return cmd.getOutput();
    }

    @Override
    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, File arg4, int arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNAnnotations annotate(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNAnnotations annotate(File arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resolve(File arg0, int arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void switchToUrl(File arg0, SVNUrl arg1, SVNRevision arg2, int arg3, boolean arg4, boolean arg5, boolean arg6) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void switchToUrl(File arg0, SVNUrl arg1, SVNRevision arg2, SVNRevision arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void merge(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5, int arg6, boolean arg7, boolean arg8, boolean arg9) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @NbBundle.Messages({
        "MSG_Error.reintegrateBranchWithCLI.CLIforced=Reintegrating branch is not supported with commandline client.",
        "MSG_Error.reintegrateBranchWithCLI=Reintegrating branch is not supported with commandline client.\nPlease switch to SVNKit or JavaHL."
    })
    public void mergeReintegrate(SVNUrl arg0, SVNRevision arg1, File arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new SVNClientException(SvnModuleConfig.getDefault().isForcedCommandlineClient()
                ? Bundle.MSG_Error_reintegrateBranchWithCLI_CLIforced()
                : Bundle.MSG_Error_reintegrateBranchWithCLI());
    }

    @Override
    public void merge(SVNUrl arg0, SVNRevision arg1, SVNRevisionRange[] arg2, File arg3, boolean arg4, int arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNMergeInfo getMergeInfo(File arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNMergeInfo getMergeInfo(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNLogMessage[] getMergeinfoLog(int arg0, File arg1, SVNRevision arg2, SVNUrl arg3, SVNRevision arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISVNLogMessage[] getMergeinfoLog(int arg0, SVNUrl arg1, SVNRevision arg2, SVNUrl arg3, SVNRevision arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @NbBundle.Messages({
        "MSG_Error.diffSummaryWithCLI.CLIforced=Diffing between revision trees is not supported with commandline client.",
        "MSG_Error.diffSummaryWithCLI=Diffing between revision trees is not supported with commandline client.\nPlease switch to SVNKit or JavaHL."
    })
    public SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, int arg4, boolean arg5) throws SVNClientException {
        throw new SVNClientException(SvnModuleConfig.getDefault().isForcedCommandlineClient()
                ? Bundle.MSG_Error_diffSummaryWithCLI_CLIforced()
                : Bundle.MSG_Error_diffSummaryWithCLI());
    }

    @Override
    public SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, int arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] suggestMergeSources(File arg0) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] suggestMergeSources(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose() {
        
    }
}
