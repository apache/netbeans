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
package org.netbeans.modules.subversion.remote.client.cli;

import org.netbeans.modules.subversion.remote.client.cli.commands.VersionCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.Annotations;
import org.netbeans.modules.subversion.remote.api.Annotations.Annotation;
import org.netbeans.modules.subversion.remote.api.Depth;
import org.netbeans.modules.subversion.remote.api.ISVNAnnotations;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntry;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntryWithLock;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNDiffSummary;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNScheduleKind;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.api.SVNNotificationHandler;
import org.netbeans.modules.subversion.remote.api.SVNStatusUnversioned;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.client.cli.commands.AddCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.BlameCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CatCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CheckoutCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CleanupCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CommitCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CopyCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.ExportCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.ImportCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.InfoCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.ListCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.ListPropertiesCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.LockCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.LogCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.MergeCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.MkdirCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.MoveCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.PropertyDelCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.PropertyGetCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.PropertySetCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.RelocateCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.RemoveCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.ResolvedCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.RevertCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.StatusCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.StatusCommand.Status;
import org.netbeans.modules.subversion.remote.client.cli.commands.SwitchToCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.UnlockCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.UpdateCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.UpgradeCommand;
import org.netbeans.modules.subversion.remote.client.parser.EntriesCache;
import org.netbeans.modules.subversion.remote.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.remote.client.parser.ParserSvnStatus;
import org.netbeans.modules.subversion.remote.client.parser.SvnWcParser;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
public class CommandlineClient implements SvnClient {

    private String user;
    private String psswd;
    private VCSFileProxy configDir;
    private final NotificationHandler notificationHandler;
    private final SvnWcParser wcParser;
    private final Commandline cli;
    private final FileSystem fileSystem;

    public static final String ERR_CLI_NOT_AVALABLE = "commandline is not available"; //NOI18N
    //private static boolean supportedMetadataFormat;

    public CommandlineClient(FileSystem fileSystem) {
        this.notificationHandler = new NotificationHandler();
        wcParser = new SvnWcParser();
        this.fileSystem = fileSystem;
        cli = new Commandline(fileSystem);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
    
    /**
     * @return true if old 1.5 format is supported
     */
    @Override
    public boolean checkSupportedVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand(fileSystem);
        try {
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);
            if(!cmd.checkForErrors()) {
                if (cmd.isUnsupportedVersion()) {
                    Subversion.LOG.log(Level.WARNING, "Unsupported svn version. You need >= 1.5"); //NOI18N
                }
                throw new SVNClientException(ERR_CLI_NOT_AVALABLE + "\n" + cmd.getOutput()); //NOI18N
            } else {
                return /*supportedMetadataFormat =*/ cmd.isMetadataFormatSupported();
            }
        } catch (IOException ex) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
            throw new SVNClientException(ERR_CLI_NOT_AVALABLE);
        }
    }

    @Override
    public String getVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand(fileSystem);
        try {
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);
            return cmd.getOutput();
        } catch (IOException ex) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
            throw new SVNClientException(ex);
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
    public void setConfigDirectory(VCSFileProxy file) throws SVNClientException {
        this.configDir = file;
    }

    @Override
    public SVNNotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    @Override
    public void addFile(VCSFileProxy file) throws SVNClientException {
        AddCommand cmd = new AddCommand(fileSystem, new VCSFileProxy[] { file }, false, false);
        exec(cmd);
        // TODO: do we need refresh?
    }


    @Override
    public void addDirectory(VCSFileProxy dir, boolean recursive) throws SVNClientException {
        AddCommand cmd = new AddCommand(fileSystem, new VCSFileProxy[] { dir } , recursive, false);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void checkout(SVNUrl url, VCSFileProxy file, SVNRevision revision, boolean recurse) throws SVNClientException {
        CheckoutCommand cmd = new CheckoutCommand(fileSystem, url, file, revision, recurse);
        exec(cmd);
        refresh(file);
    }

    @Override
    public void doExport(SVNUrl url, VCSFileProxy destination, SVNRevision revision, boolean force) throws SVNClientException {
        ExportCommand cmd = new ExportCommand(fileSystem, url, destination, revision, force);
        exec(cmd);
        refresh(destination);
    }

    @Override
    public void doExport(VCSFileProxy fileFrom, VCSFileProxy fileTo, boolean force) throws SVNClientException {
        ExportCommand cmd = new ExportCommand(fileSystem, fileFrom, fileTo, force);
        exec(cmd);
        refresh(fileTo);
    }

    @Override
    public long commit(VCSFileProxy[] files, String message, boolean recurse) throws SVNClientException {
        return commit(files, message, false, recurse);
    }

    @Override
    public long commit(VCSFileProxy[] files, String message, boolean keep, boolean recursive) throws SVNClientException {
        int retry = 0;
        CommitCommand cmd = null;
        while (true) {
            try {
                cmd = new CommitCommand(fileSystem, files, keep, recursive, message); // prevent cmd reuse
                exec(cmd);
                break;
            } catch (SVNClientException e) {
                if (e.getMessage().startsWith("svn: Attempted to lock an already-locked dir")) { //NOI18N
                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                        Subversion.LOG.fine("ComandlineClient.comit() : " + e.getMessage());
                    }
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
        refresh(files);
        return cmd.getRevision();
    }
    
    @Override
    public ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision, boolean recursivelly) throws SVNClientException {
        ListCommand cmd = new ListCommand(fileSystem, url, revision, recursivelly);
        exec(cmd);
        return cmd.getEntries();
    }

    @Override
    public ISVNInfo getInfo(Context context, SVNUrl url) throws SVNClientException {
        return getInfo(context, url, SVNRevision.HEAD, SVNRevision.HEAD);
    }

    @Override
    public ISVNInfo getInfo(VCSFileProxy file) throws SVNClientException {
        return getInfoFromWorkingCopy(file);
    }

    @Override
    public String getPostCommitError () {
        return null;
    }

    private ISVNInfo[] getInfo(VCSFileProxy[] files, SVNRevision revision, SVNRevision pegging) throws SVNClientException, SVNClientException {
        if(files == null || files.length == 0) {
            return new ISVNInfo[0];
        }
        InfoCommand infoCmd = new InfoCommand(fileSystem, files, revision, pegging);
        exec(infoCmd);
        ISVNInfo[] infos = infoCmd.getInfo();

        return infos;
    }

    @Override
    public ISVNInfo getInfo(Context context, SVNUrl url, SVNRevision revision, SVNRevision pegging) throws SVNClientException {
        InfoCommand cmd = new InfoCommand(fileSystem, context, url, revision, pegging);
        exec(cmd);
        ISVNInfo[] infos = cmd.getInfo();
        ISVNInfo info = null;
        if (infos.length > 0) {
            info = infos[0];
        }
        return info;
    }

    @Override
    public void copy(VCSFileProxy fileFrom, VCSFileProxy fileTo) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fileSystem, fileFrom, fileTo);
        exec(cmd);
        refresh(fileTo);
    }

    @Override
    public void copy(VCSFileProxy file, SVNUrl url, String msg) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fileSystem, file, url, msg);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void copy(SVNUrl url, VCSFileProxy file, SVNRevision rev) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fileSystem, url, file, rev);
        exec(cmd);
        refresh(file);
    }

    @Override
    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException {
        copy(fromUrl, toUrl, msg, rev, false);
    }

    @Override
    public void remove(VCSFileProxy[] files, boolean force) throws SVNClientException {
        RemoveCommand cmd = new RemoveCommand(fileSystem, files, force);
        exec(cmd);
        refreshParents(files);
    }

    @Override
    public void doImport(VCSFileProxy File, SVNUrl url, String msg, boolean recursivelly) throws SVNClientException {
        ImportCommand cmd = new ImportCommand(fileSystem, File, url, recursivelly, msg);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void mkdir(SVNUrl url, String msg) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(fileSystem, url, msg);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void mkdir(VCSFileProxy file) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(fileSystem, file);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void move(VCSFileProxy fromFile, VCSFileProxy toFile, boolean force) throws SVNClientException {
        MoveCommand cmd = new MoveCommand(fileSystem, fromFile, toFile, force);
        exec(cmd);
        refreshParents(fromFile, toFile);
    }

    @Override
    public long update(VCSFileProxy file, SVNRevision rev, boolean recursivelly) throws SVNClientException {
        UpdateCommand cmd = new UpdateCommand(fileSystem, new VCSFileProxy[] { file }, rev, recursivelly, false);
        exec(cmd);
        refresh(file);
        return cmd.getRevision();
    }

    @Override
    public void revert(VCSFileProxy file, boolean recursivelly) throws SVNClientException {
        revert(new VCSFileProxy[]{file}, recursivelly);
    }

    @Override
    public void revert(VCSFileProxy[] files, boolean recursivelly) throws SVNClientException {
        if(files == null || files.length == 0) {
            return;
        }
        RevertCommand cmd = new RevertCommand(fileSystem, files, recursivelly);
        exec(cmd);
        refresh(files);
    }

    @Override
    public ISVNStatus[] getStatus(VCSFileProxy[] files) throws SVNClientException {

        Map<VCSFileProxy, ISVNStatus> unversionedMap = new HashMap<>();
        List<VCSFileProxy> filesForStatus = new ArrayList<>();
        List<VCSFileProxy> filesForInfo = new ArrayList<>();
        for (VCSFileProxy f : files) {
            if(!isManaged(f)) {
                unversionedMap.put(f, new SVNStatusUnversioned(f));
            } else {
                filesForStatus.add(f);
            }
        }

        Status[] statusValues = new Status[] {};
        if (!filesForStatus.isEmpty()) {
            StatusCommand statusCmd = new StatusCommand(fileSystem, filesForStatus.toArray(new VCSFileProxy[filesForStatus.size()]), true, false, false, false);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        }
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(status.getPath());
            }
        }
        Map<VCSFileProxy, ISVNInfo> infoMap = new HashMap<>();
        if (!filesForInfo.isEmpty()) {
            ISVNInfo[] infos = getInfo(filesForInfo.toArray(new VCSFileProxy[filesForInfo.size()]), null, null);
            for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);
        }

        Map<VCSFileProxy, ISVNStatus> statusMap = new HashMap<>();
        for (Status status : statusValues) {
            VCSFileProxy file = status.getPath();
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(file, new CLIStatus(status));
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

        List<ISVNStatus> ret = new ArrayList<>();
        for (VCSFileProxy f : files) {
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = unversionedMap.get(f);
            }
            if(s != null) {
                ret.add(s);
            }
        }
        return ret.toArray(new ISVNStatus[ret.size()]);
    }

    @Override
    public ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException {
        return getStatus(file, descend, getAll, contactServer, false);
    }

    // XXX merge with get status
    @Override
    public ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals) throws SVNClientException {
        Status[] statusValues = null;
        try {
            if(!isManaged(file)) {
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            }
            StatusCommand statusCmd = new StatusCommand(fileSystem, new VCSFileProxy[] { file }, getAll, descend, contactServer, ignoreExternals);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        } catch (SVNClientException e) {
            if(SvnClientExceptionHandler.isUnversionedResource(e.getMessage())) {
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            } else {
                throw e;
            }
        }

        List<VCSFileProxy> filesForInfo = new ArrayList<>();
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(status.getPath());
            }
        }
        ISVNInfo[] infos = getInfo(filesForInfo.toArray(new VCSFileProxy[filesForInfo.size()]), null, null);

        Map<VCSFileProxy, ISVNInfo> infoMap = new HashMap<>();
        for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);

        Map<VCSFileProxy, ISVNStatus> statusMap = new HashMap<>();
        for (Status status : statusValues) {
            VCSFileProxy f = status.getPath();
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(f, new CLIStatus(status));
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

        List<ISVNStatus> ret = new ArrayList<>();
        for (Status status : statusValues) {
            VCSFileProxy f = status.getPath();
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = new SVNStatusUnversioned(f);
            }
            ret.add(s);
        }
        return ret.toArray(new ISVNStatus[ret.size()]);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return getLogMessages(url, revStart, revEnd, false);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(url, null, revStart, revEnd, false, fetchChangePath);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        LogCommand cmd = new LogCommand(fileSystem, url, paths, revStart, revEnd, SVNRevision.HEAD, stopOnCopy, fetchChangePath, 0);
        return getLog(cmd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revPeg, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        LogCommand cmd = new LogCommand(fileSystem, url, null, revStart, revEnd, revPeg, stopOnCopy, fetchChangePath, limit);
        return getLog(cmd);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, false);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, false, fetchChangePath);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, stopOnCopy, fetchChangePath, 0);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        return getLogMessages(file, SVNRevision.HEAD, revStart, revEnd, stopOnCopy, fetchChangePath, limit, false);
    }

    @Override
    public ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision pegRevision, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit, boolean includeMergedRevisions) throws SVNClientException {
        LogCommand logCmd;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) &&
            info.getCopyUrl() != null)
        {
            logCmd = new LogCommand(fileSystem, info.getCopyUrl(), null, revStart, revEnd, pegRevision, stopOnCopy, fetchChangePath, limit);
        } else {
            logCmd = new LogCommand(fileSystem, file, revStart, revEnd, pegRevision, stopOnCopy, fetchChangePath, limit);
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
    public InputStream getContent(VCSFileProxy file, SVNRevision rev) throws SVNClientException {
        CatCommand cmd = new CatCommand(fileSystem, file, rev);
        exec(cmd);
        return cmd.getOutput();
    }

    @Override
    public void propertySet(VCSFileProxy file, String name, String value, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertySetCommand cmd = new PropertySetCommand(fileSystem, name, value, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
        // TODO: do we need refresh?
    }

    @Override
    public void propertySet(VCSFileProxy file, String name, VCSFileProxy propFile, boolean rec) throws SVNClientException, IOException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertySetCommand cmd = new PropertySetCommand(fileSystem, name, propFile, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
        // TODO: do we need refresh?
    }

    @Override
    public void propertyDel(VCSFileProxy file, String name, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, true);
        PropertyDelCommand cmd = new PropertyDelCommand(fileSystem, file, name, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
        // TODO: do we need refresh?
    }

    @Override
    public ISVNProperty propertyGet(final VCSFileProxy file, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(fileSystem, file, name), name, null, file);
    }

    @Override
    public ISVNProperty propertyGet(SVNUrl url, String name) throws SVNClientException {
        return propertyGet(url, SVNRevision.HEAD, SVNRevision.HEAD, name);
    }

    @Override
    public ISVNProperty propertyGet(final SVNUrl url, SVNRevision rev, SVNRevision peg, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(fileSystem, url, rev, peg, name), name, url, null);
    }

    ISVNProperty propertyGet(PropertyGetCommand cmd, final String name, final SVNUrl url, final VCSFileProxy file) throws SVNClientException {
        exec(cmd);
        final byte[] bytes = cmd.getOutput();
        if(bytes.length == 0) {
            return null;
        }
        return new ISVNProperty() {
            @Override
            public String getName() {
                return name;
            }
            @Override
            @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
            public String getValue() {
                try {
                    return new String(bytes, "UTF-8"); //NOI18N
                } catch (UnsupportedEncodingException ex) {
                    // ignore error
                    return new String(bytes);
                }
            }
            @Override
            public VCSFileProxy getFile() {
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
    public List<String> getIgnoredPatterns(VCSFileProxy file) throws SVNClientException {
        if (!file.isDirectory()) {
            return null;
        }
        List<String> res = new ArrayList<>();
        for(ISVNProperty property : getProperties(file)) {
            if (ISVNProperty.IGNORE.equals(property.getName())) {
                String value = property.getValue();
                for(String s : value.split("\n")) { //NOI18N
                    if (!s.isEmpty()) {
                        res.add(s);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void addToIgnoredPatterns(VCSFileProxy file, String value) throws SVNClientException {
        List<String> ignoredPatterns = getIgnoredPatterns(file);
        if (ignoredPatterns == null) {
            return;
        }
        if (!ignoredPatterns.contains(value)) {
            ignoredPatterns.add(value);
            setIgnoredPatterns(file, ignoredPatterns);
        }
    }

    @Override
    public void setIgnoredPatterns(VCSFileProxy file, List<String>  l) throws SVNClientException {
        if (!file.isDirectory()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        for(String s : l) {
            buf.append(s);
            buf.append('\n');
        }
        propertySet(file, ISVNProperty.IGNORE, buf.toString(), false);
    }

    //@Override
    //public ISVNAnnotations annotate(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
    //    return annotate(new BlameCommand(fileSystem, url, revStart, revEnd), new CatCommand(fileSystem, url, revEnd, null));
    //}

    @Override
    public ISVNAnnotations annotate(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        BlameCommand blameCommand;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) &&
            info.getCopyUrl() != null)
        {
            blameCommand = new BlameCommand(fileSystem, info.getCopyUrl(), revStart, revEnd);
        } else {
            blameCommand = new BlameCommand(fileSystem, file, revStart, revEnd);
        }
        return annotate(file, blameCommand, new CatCommand(fileSystem, file, revEnd));
    }

    @Override
    public ISVNAnnotations annotate(VCSFileProxy file, BlameCommand blameCmd, CatCommand catCmd) throws SVNClientException {
        exec(blameCmd);
        Annotation[] annotations = blameCmd.getAnnotation();
        exec(catCmd);
        InputStream is = catCmd.getOutput();
        Charset encoding = RemoteVcsSupport.getEncoding(file);

        Annotations ret = new Annotations();
        BufferedReader r = new BufferedReader(new InputStreamReader(is, encoding));
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
            try { r.close(); } catch (IOException e) {}
        }
        return ret;
    }

    @Override
    public ISVNProperty[] getProperties (final VCSFileProxy file) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(fileSystem, file, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<>(names.size());
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
                    public VCSFileProxy getFile() {
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
        return props.toArray(new ISVNProperty[props.size()]);
    }

    @Override
    public ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(fileSystem, url, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<>(names.size());
        for (String name : names) {
            ISVNProperty prop = propertyGet(url, name);
            if (prop != null) {
                props.add(prop);
            }
        }
        return props.toArray(new ISVNProperty[props.size()]);
    }

    @Override
    public void resolved(VCSFileProxy file) throws SVNClientException {
        ResolvedCommand cmd = new ResolvedCommand(fileSystem, file, false);
        exec(cmd);
    }

    @Override
    public void cancelOperation() throws SVNClientException {
        cli.interrupt();
    }

    @Override
    public void switchToUrl(VCSFileProxy file, SVNUrl url, SVNRevision rev, boolean rec) throws SVNClientException {
        SwitchToCommand cmd = new SwitchToCommand(fileSystem, file, url, rev, rec);
        exec(cmd);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse) throws SVNClientException {
       merge(startUrl, startRev, endUrl, endRev, file, force, recurse,false);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse, boolean dryRun) throws SVNClientException {
        merge(startUrl, startRev, endUrl, endRev, file, force, recurse, dryRun, false);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException {
        MergeCommand cmd = new MergeCommand(fileSystem, startUrl, endUrl, startRev, endRev, file, recurse, force, ignoreAncestry, dryRun);
        exec(cmd);
        refresh(file);
    }

    @Override
    public void relocate(Context context, String from, String to, String path, boolean rec) throws SVNClientException {
        RelocateCommand cmd = new RelocateCommand(fileSystem, null, from, to, path, rec);
        exec(cmd);
        // TODO: do we need refresh?
    }

    // parser start
    @Override
    public ISVNStatus getSingleStatus(VCSFileProxy file) throws SVNClientException {
        if (SvnClientFactory.getInstance(new Context(file)).isCLIOldFormat()) {
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
            ISVNStatus[] statuses = getStatus(new VCSFileProxy[]{file});
            return statuses.length > 0 ? statuses[0] : new ParserSvnStatus(
                    file,                                
                    null,
                    0,
                    "unknown",                            // NOI18N   
                    SVNStatusKind.UNVERSIONED,
                    SVNStatusKind.UNVERSIONED,
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
    public ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll) throws SVNClientException {
        if (SvnClientFactory.getInstance(new Context(file)).isCLIOldFormat()) {
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
    public ISVNInfo getInfoFromWorkingCopy(VCSFileProxy file) throws SVNClientException {
        if (SvnClientFactory.getInstance(new Context(file)).isCLIOldFormat()) {
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
            ISVNInfo[] infos = getInfo(new VCSFileProxy[] { file }, null, null);
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
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
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

    private boolean isManaged(SVNStatusKind s) {
        return !(s.equals(SVNStatusKind.UNVERSIONED) ||
                 s.equals(SVNStatusKind.NONE) ||
                 s.equals(SVNStatusKind.IGNORED) ||
                 s.equals(SVNStatusKind.EXTERNAL));
    }

    private boolean hasMetadata(VCSFileProxy file) {
        return SvnUtils.hasMetadata(file);
    }

    private boolean isManaged(VCSFileProxy file) {
        boolean managed = true;
        if (SvnClientFactory.getInstance(new Context(file)).isCLIOldFormat()) {
            managed = hasMetadata(file.getParentFile()) || hasMetadata(file);
        }
        return managed;
    }

    // unsupported start

    @Override
    public void cleanup(VCSFileProxy file) throws SVNClientException {
        CleanupCommand cmd = new CleanupCommand(fileSystem, file);
        exec(cmd);
        refresh(file);
    }

    private void notifyChangedStatus(VCSFileProxy file, boolean rec, ISVNStatus[] oldStatuses) throws SVNClientException {
        Map<VCSFileProxy, ISVNStatus> oldStatusMap = new HashMap<>();
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
                notificationHandler.notifyListenersOfChange(newStatus.getFile()); /// onNotify(cmd.getAbsoluteFile(s.getFile().getAbsolutePath()), null);
            }
       }
    }

    @Override
    public ISVNDirEntryWithLock[] getListWithLocks(SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, boolean bln) throws SVNClientException {
        // will not implement in commandline client
        return new ISVNDirEntryWithLock[0];
    }

    @Override
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision) throws SVNClientException {
        return getProperties(url, revision, pegRevision, false);
    }
    
    @Override
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision, boolean recursive) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(fileSystem, url, revision.toString(), recursive);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<>(names.size());
        for (String name : names) {
            ISVNProperty prop = propertyGet(url, name);
            if (prop != null) {
                props.add(prop);
            }
        }
        return props.toArray(new ISVNProperty[props.size()]);
    }

    @Override
    public void upgrade (VCSFileProxy wcRoot) throws SVNClientException {
        UpgradeCommand cmd = new UpgradeCommand(fileSystem, wcRoot);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void unlock(VCSFileProxy[] vcsFileProxy, boolean b) throws SVNClientException {
        UnlockCommand cmd = new UnlockCommand(fileSystem, vcsFileProxy);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public void lock(VCSFileProxy[] vcsFileProxy, String string, boolean b) throws SVNClientException {
        LockCommand cmd = new LockCommand(fileSystem, vcsFileProxy);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public boolean cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SVNUrl getSvnUrl() {
        throw new UnsupportedOperationException();
    }

    static class NotificationHandler extends SVNNotificationHandler {

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
    public void copy(VCSFileProxy[] files, SVNUrl targetUrl, String message, boolean addAsChild, boolean makeParents) throws SVNClientException {
        for (VCSFileProxy file : files) {
            CopyCommand cmd = new CopyCommand(fileSystem, file, targetUrl, message, makeParents);
            exec(cmd);
        }
        // TODO: do we need refresh?
    }

    @Override
    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev, boolean makeParents) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fileSystem, fromUrl, toUrl, msg, rev, makeParents);
        exec(cmd);
        // TODO: do we need refresh?
    }

    @Override
    public InputStream getContent(SVNUrl url, SVNRevision rev, SVNRevision pegRevision) throws SVNClientException {
        CatCommand cmd = new CatCommand(fileSystem, url, rev, pegRevision);
        exec(cmd);
        return cmd.getOutput();
    }

    @Override
    @Messages({
        "MSG_Error.reintegrateBranchWithCLI.CLIforced=Reintegrating branch is not supported with commandline client.",
        "MSG_Error.reintegrateBranchWithCLI=Reintegrating branch is not supported with commandline client.\nPlease switch to SVNKit or JavaHL."
    })
    public void mergeReintegrate(SVNUrl arg0, SVNRevision arg1, VCSFileProxy arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new SVNClientException(SvnModuleConfig.getDefault(fileSystem).isForcedCommandlineClient()
                ? Bundle.MSG_Error_reintegrateBranchWithCLI_CLIforced()
                : Bundle.MSG_Error_reintegrateBranchWithCLI());
    }

    @Override
    @Messages({
        "MSG_Error.diffSummaryWithCLI.CLIforced=Diffing between revision trees is not supported with commandline client.",
        "MSG_Error.diffSummaryWithCLI=Diffing between revision trees is not supported with commandline client.\nPlease switch to SVNKit or JavaHL."
    })
    public SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, Depth arg4, boolean arg5) throws SVNClientException {
        throw new SVNClientException(SvnModuleConfig.getDefault(fileSystem).isForcedCommandlineClient()
                ? Bundle.MSG_Error_diffSummaryWithCLI_CLIforced()
                : Bundle.MSG_Error_diffSummaryWithCLI());
    }

    @Override
    public void dispose() {
    }
    
    private void refresh(VCSFileProxy... files) {
        try {
            RemoteVcsSupport.refreshFor(files);
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, "error when refreshing: {0}", ex.getLocalizedMessage());
        }
    }

    private void refreshParents(VCSFileProxy... files) {
        VCSFileProxy[] parents = new VCSFileProxy[files.length];
        for (int i = 0; i < files.length; i++) {
            parents[i] = files[i].getParentFile();  
            if (parents[i] == null) {
                parents[i] = files[i];
            }
        }
        refresh(parents);
    }
}
