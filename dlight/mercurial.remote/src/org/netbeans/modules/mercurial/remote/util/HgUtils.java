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

package org.netbeans.modules.mercurial.remote.util;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgException.HgCommandCanceledException;
import org.netbeans.modules.mercurial.remote.HgFileNode;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.WorkingCopyInfo;
import org.netbeans.modules.mercurial.remote.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.remote.ui.commit.CommitOptions;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.ui.status.SyncFileNode;
import org.netbeans.modules.remotefs.versioning.api.FileObjectIndexingBridgeProvider;
import org.netbeans.modules.remotefs.versioning.api.FileSelector;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * 
 */
public class HgUtils {    
    private static final Pattern httpPasswordPattern = Pattern.compile("(https*://)(\\w+\\b):(\\b\\S*)@"); //NOI18N
    private static final String httpPasswordReplacementStr = "$1$2:\\*\\*\\*\\*@"; //NOI18N
    private static final Pattern httpCredentialsPattern = Pattern.compile("(.*://)(\\w+\\b)(?::(\\b\\S*))?@"); //NOI18N
    private static final String httpCredentialsReplacementStr = "$1"; //NOI18N
    
    private static final Pattern metadataPattern = Pattern.compile(".*\\" + "/" + "(\\.)hg(\\" + "/" + ".*|$)"); // NOI18N
    
    // IGNORE SUPPORT HG: following file patterns are added to {Hg repos}/.hgignore and Hg will ignore any files
    // that match these patterns, reporting "I"status for them // NOI18N
    private static final String [] HG_IGNORE_FILES = { "\\.orig$", "\\.orig\\..*$", "\\.chg\\..*$", "\\.rej$", "\\.conflict\\~$"}; // NOI18N
    private static final String HG_IGNORE_ORIG_FILES = "\\.orig$"; // NOI18N
    private static final String HG_IGNORE_ORIG_ANY_FILES = "\\.orig\\..*$"; // NOI18N
    private static final String HG_IGNORE_CHG_ANY_FILES = "\\.chg\\..*$"; // NOI18N
    private static final String HG_IGNORE_REJ_ANY_FILES = "\\.rej$"; // NOI18N
    private static final String HG_IGNORE_CONFLICT_ANY_FILES = "\\.conflict\\~$"; // NOI18N
    
    private static final String FILENAME_HGIGNORE = ".hgignore"; // NOI18N
    private static final String IGNORE_SYNTAX_PREFIX = "syntax:";       //NOI18N
    private static final String IGNORE_SYNTAX_GLOB = "glob";            //NOI18N
    private static final String IGNORE_SYNTAX_REGEXP = "regexp";        //NOI18N

    private static HashMap<String, Set<Pattern>> ignorePatterns;
    private static HashMap<String, Long> ignoreFilesTimestamps;

    private static final Logger LOG = Logger.getLogger(HgUtils.class.getName());

    /**
     * Timeout for remote repository check in seconds, after expires the repository will be considered valid.
     */
    public static final String HG_CHECK_REPOSITORY_TIMEOUT_SWITCH = "mercurial.checkRepositoryTimeout"; //NOI18N
    public static final String HG_CHECK_REPOSITORY_DEFAULT_TIMEOUT = "5"; //NOI18N
    public static final int HG_CHECK_REPOSITORY_DEFAULT_ROUNDS = 50;
    public static final String HG_FOLDER_NAME = ".hg";                 //NOI18N
    public static final String WLOCK_FILE = "wlock"; //NOI18N
    private static int repositoryValidityCheckRounds = 0;
    public static final String PREFIX_VERSIONING_MERCURIAL_URL = "versioning.mercurial.url."; //NOI18N

    /**
     * addDaysToDate - add days (+days) or subtract (-days) from the given date
     *
     * @param int days to add or substract
     * @return Date new date that has been calculated
     */
    public static Date addDaysToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    /**
     * Creates annotation format string.
     * @param format format specified by the user, e.g. [{status}]
     * @return modified format, e.g. [{0}]
     */
    public static String createAnnotationFormat(final String format) {
        String string = format;
        string = Utils.skipUnsupportedVariables(string, new String[] {"{status}", "{folder}"});     // NOI18N
        string = string.replaceAll("\\{status\\}", "\\{0\\}");                                      // NOI18N
        string = string.replaceAll("\\{folder\\}", "\\{1\\}");                                      // NOI18N
        return string;
    }

    /**
     * replaceHttpPassword - replace any http or https passwords in the string
     *
     * @return String modified string with **** instead of passwords
     */
    public static String removeHttpCredentials(String s){
        Matcher m = httpCredentialsPattern.matcher(s);
        return m.replaceAll(httpCredentialsReplacementStr);
    }

    /**
     * replaceHttpPassword - replace any http or https passwords in the string
     *
     * @return String modified string with **** instead of passwords
     */
    public static String replaceHttpPassword(String s){
        Matcher m = httpPasswordPattern.matcher(s);
        return m.replaceAll(httpPasswordReplacementStr); 
    }
    
    /**
     * replaceHttpPassword - replace any http or https passwords in the List<String>
     *
     * @return List<String> containing modified strings with **** instead of passwords
     */
    public static List<String> replaceHttpPassword(List<String> list){
        if(list == null) {
            return null;
        }

        List<String> out = new ArrayList<>(list.size());
        for(String s: list){
            out.add(replaceHttpPassword(s));
        } 
        return out;
    }

    /**
     * confirmDialog - display a confirmation dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param query ask user
     * @return boolean true - answered Yes, false - answered No
     */
    public static boolean confirmDialog(Class bundleLocation, String title, String query) {
        int response = JOptionPane.showOptionDialog(null, NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (response == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * warningDialog - display a warning dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param warning to display to the user
     */
     public static void warningDialog(Class bundleLocation, String title, String warning) {
        JOptionPane.showMessageDialog(null,
                NbBundle.getMessage(bundleLocation,warning),
                NbBundle.getMessage(bundleLocation,title),
                JOptionPane.WARNING_MESSAGE);
    }

    public static JComponent addContainerBorder(JComponent comp) {
        final LayoutStyle layoutStyle = LayoutStyle.getInstance();

        JPanel panel = new JPanel();
        panel.add(comp);
        panel.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(comp, SwingConstants.NORTH, null),
                layoutStyle.getContainerGap(comp, SwingConstants.WEST,  null),
                layoutStyle.getContainerGap(comp, SwingConstants.SOUTH, null),
                layoutStyle.getContainerGap(comp, SwingConstants.EAST,  null)));
        return panel;
    }

    /**
     * stripDoubleSlash - converts '\\' to '\' in path on Windows
     *
     * @param String path to convert
     * @return String converted path
     */
    public static String stripDoubleSlash(String path){
        return path;
    }

    /**
     * Tells whether the given string is {@code null} or empty.
     * A string is considered empty if it consists only of spaces (and possibly
     * other spacing characters). The current implementation checks just for
     * spaces, future implementations may also check for other spacing
     * characters.
     *
     * @param  string to be verified or {@code null}
     * @return  {@code true} if the string is {@null} or empty,
     *          {@code false} otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static boolean isRebasing (VCSFileProxy repositoryRoot) {
        WorkingCopyInfo info = WorkingCopyInfo.getInstance(repositoryRoot);
        info.refresh();
        HgLogMessage[] parents = info.getWorkingCopyParents();
        if (parents.length > 1) {
            // two parents, possible abort, rebase or simply inside a merge
            return VCSFileProxy.createFileProxy(getHgFolderForRoot(repositoryRoot), "rebasestate").exists(); //NOI18N
        }
        return false;
    }

    public static boolean onlyProjects (Node[] nodes) {
        if (nodes == null) {
            return false;
        }
        for (Node node : nodes) {
            if (node.getLookup().lookup(Project.class) == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean onlyFolders (Set<VCSFileProxy> files) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (VCSFileProxy file : files) {
            if (file.isFile()) {
                return false;
            }
            FileInformation status = cache.getCachedStatus(file);
            if (status == null || (!file.exists() && !status.isDirectory())) {
                return false;
            }
        }
        return true;
    }
    
    private static void resetIgnorePatterns(VCSFileProxy file) {
        if (ignorePatterns == null) {
            return;
        }
        String key = file.getPath();
        ignorePatterns.remove(key);
        ignoreFilesTimestamps.remove(key);
    }

    private static Set<Pattern> getIgnorePatterns(VCSFileProxy file) {
        if (ignorePatterns == null) {
            ignoreFilesTimestamps = new HashMap<>();
            ignorePatterns = new HashMap<>();
        }
        String key = file.getPath();
        Set<Pattern> patterns = ignorePatterns.get(key);
        Long oldTs = ignoreFilesTimestamps.get(key);
        long ts;
        if (EventQueue.isDispatchThread()) {
            ts = oldTs == null ? 0 : oldTs; //keep cached
        } else {
            ts = VCSFileProxy.createFileProxy(file, FILENAME_HGIGNORE).lastModified(); //check for external modification
        }
        if (patterns == null || oldTs == null && ts > 0 || oldTs != null && (ts == 0 || oldTs < ts)) {
            patterns = new HashSet<>(5);
            ignoreFilesTimestamps.put(key, ts);
            addIgnorePatterns(patterns, file);
            ignorePatterns.put(key, patterns);
        }
        return patterns;
    }

    // cached not sharable files and folders
    private static final Map<VCSFileProxy, Set<String>> notSharable = new HashMap<>(5);
    private static void addNotSharable (VCSFileProxy topFile, String ignoredPath) {
        synchronized (notSharable) {
            // get cached patterns
            Set<String> ignores = notSharable.get(topFile);
            if (ignores == null) {
                ignores = new HashSet<>();
            }
            String patternCandidate = ignoredPath;
            // test for duplicate patterns
            for (Iterator<String> it = ignores.iterator(); it.hasNext();) {
                String storedPattern = it.next();
                if (storedPattern.equals(ignoredPath) // already present
                        || ignoredPath.startsWith(storedPattern + '/')) { // path already ignored by its ancestor
                    patternCandidate = null;
                    break;
                } else if (storedPattern.startsWith(ignoredPath + '/')) { // stored pattern matches a subset of ignored path
                    // remove the stored pattern and add the ignored path
                    it.remove();
                }
            }
            if (patternCandidate != null) {
                ignores.add(patternCandidate);
            }
            notSharable.put(topFile, ignores);
        }
    }

    private static boolean isNotSharable (String path, VCSFileProxy topFile) {
        synchronized (notSharable) {
            Set<String> notSharablePaths = notSharable.get(topFile);
            if (notSharablePaths == null) {
                notSharablePaths = Collections.emptySet();
            }
            return notSharablePaths.contains(path);
        }
    }

    /**
     * isIgnored - checks to see if this is a file Hg should ignore
     *
     * @param File file to check
     * @return boolean true - ignore, false - not ignored
     */
    public static boolean isIgnored(VCSFileProxy file){
        return isIgnored(file, true);
    }

    public static boolean isIgnored(VCSFileProxy file, boolean checkSharability){
        if (file == null) {
            return false;
        }
        VCSFileProxy topFile = Mercurial.getInstance().getRepositoryRoot(file);
        
        // We assume that the toplevel directory should not be ignored.
        if (topFile == null || topFile.equals(file)) {
            return false;
        }
        
        Set<Pattern> patterns = getIgnorePatterns(topFile);
        String path = getRelativePath(file, topFile);

        for (Iterator i = patterns.iterator(); i.hasNext();) {
            Pattern pattern = (Pattern) i.next();
            if (pattern.matcher(path).find()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "File {0}:::{1} ignored because of pattern {2}", new Object[] { file, path, pattern.toString() }); //NOI18N
                }
                return true;
            }
        }

        // check cached not sharable folders and files
        if (isNotSharable(path, topFile)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "File {0}:::{1} ignored because of cached NOT_SHARABLE", new Object[] { file, path }); //NOI18N
            }
            return true;
        }

        // If a parent of the file matches a pattern ignore the file
        VCSFileProxy parentFile = file.getParentFile();
        if (!parentFile.equals(topFile)) {
            if (isIgnored(parentFile, false)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "File {0}:::{1} ignored because of ignored parent {2}", new Object[] { file, path, parentFile }); //NOI18N
                }
                return true;
            }
        }

        if (FILENAME_HGIGNORE.equals(file.getName())) {
            return false;
        }
        if (checkSharability) {
            Logger.getLogger(HgUtils.class.getName()).log(Level.FINER, "Calling sharability for {0}:::{1}", new Object[] { file, topFile }); //NOI18N
            SharabilityQuery.Sharability sharability = SharabilityQuery.getSharability(VCSFileProxySupport.toURI(file.normalizeFile()));
            if (sharability == SharabilityQuery.Sharability.NOT_SHARABLE) {
                addNotSharable(topFile, path);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "File {0}:::{1} ignored by sharability {2}", new Object[] { file, path, sharability }); //NOI18N
                }
                return true;
            }
        }
        return false;
    }

    private static String getRelativePath (VCSFileProxy file, VCSFileProxy ancestor) {
        String path = file.getPath();
        String ancestorPath = ancestor.getPath();
        path = path.substring(ancestorPath.length());
        if (path.startsWith("/")) { //NOI18N
            path = path.substring(1);
        }
        return path;
    }

    /**
     * createIgnored - creates .hgignore file in the repository in which 
     * the given file belongs. This .hgignore file ensures Hg will ignore 
     * the files specified in HG_IGNORE_FILES list
     *
     * @param path to repository to place .hgignore file
     */
    public static void createIgnored(VCSFileProxy path){
        if( path == null) {
            return;
        }
        BufferedWriter fileWriter = null;
        Mercurial hg = Mercurial.getInstance();
        VCSFileProxy root = hg.getRepositoryRoot(path);
        if( root == null) {
            return;
        }
        VCSFileProxy ignore = VCSFileProxy.createFileProxy(root, FILENAME_HGIGNORE);
        
        try     {
            if (!ignore.exists()) {
                fileWriter = new BufferedWriter(
                        new OutputStreamWriter(VCSFileProxySupport.getOutputStream(ignore), "UTF-8")); // NOI18N
                for (String name : HG_IGNORE_FILES) {
                    fileWriter.write(name + "\n"); // NOI18N
                }
            }else{
                addToExistingIgnoredFile(ignore);
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                    new Object[] {ignore.getPath(), ex.toString()});
        }finally {
            try {
                if(fileWriter != null) {
                    fileWriter.close();
                }
                hg.getFileStatusCache().refresh(ignore);
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                        new Object[] {ignore.getPath(), ex.toString()});
            }
        }
    }
    
    private static final int HG_NUM_PATTERNS_TO_CHECK = 5;
    private static void addToExistingIgnoredFile(VCSFileProxy hgignoreFile) {
        if(hgignoreFile == null || !hgignoreFile.exists() || !hgignoreFile.canWrite()) {
            return;
        }
        VCSFileProxy tempFile = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        boolean bOrigAnyPresent = false;
        boolean bOrigPresent = false;
        boolean bChgAnyPresent = false;
        boolean bRejAnyPresent = false;
        boolean bConflictAnyPresent = false;
        
        // If new patterns are added to HG_IGNORE_FILES, following code needs to
        // check for these new patterns
        assert( HG_IGNORE_FILES.length == HG_NUM_PATTERNS_TO_CHECK);
        
        try {
            tempFile = VCSFileProxySupport.getResource(hgignoreFile, hgignoreFile.getPath() + ".tmp"); // NOI18N
            if (tempFile == null) {
                return;
            }
            
            br = new BufferedReader(new InputStreamReader(hgignoreFile.getInputStream(false), "UTF-8")); //NOI18N
            pw = new PrintWriter(new OutputStreamWriter(VCSFileProxySupport.getOutputStream(tempFile), "UTF-8")); //NOI18N

            String line = null;            
            while ((line = br.readLine()) != null) {
                if(!bOrigAnyPresent && line.equals(HG_IGNORE_ORIG_ANY_FILES)){
                    bOrigAnyPresent = true;
                }else if (!bOrigPresent && line.equals(HG_IGNORE_ORIG_FILES)){
                    bOrigPresent = true;
                }else if (!bChgAnyPresent && line.equals(HG_IGNORE_CHG_ANY_FILES)){
                    bChgAnyPresent = true;
                }else if (!bRejAnyPresent && line.equals(HG_IGNORE_REJ_ANY_FILES)){
                    bRejAnyPresent = true;
                }else if (!bConflictAnyPresent && line.equals(HG_IGNORE_CONFLICT_ANY_FILES)){
                    bConflictAnyPresent = true;
                }
                pw.println(line);
                pw.flush();
            }
            // If not found add as required
            if (!bOrigAnyPresent) {
                pw.println(HG_IGNORE_ORIG_ANY_FILES );
                pw.flush();
            }
            if (!bOrigPresent) {
                pw.println(HG_IGNORE_ORIG_FILES );
                pw.flush();
            }
            if (!bChgAnyPresent) {
                pw.println(HG_IGNORE_CHG_ANY_FILES );
                pw.flush();
            }
            if (!bRejAnyPresent) {
                pw.println(HG_IGNORE_REJ_ANY_FILES );
                pw.flush();
            }     
            if (!bConflictAnyPresent) {
                pw.println(HG_IGNORE_CONFLICT_ANY_FILES );
                pw.flush();
            }     
            
        } catch (IOException ex) {
            // Ignore
        } finally {
            try {
                if(pw != null) {
                    pw.close();
                }
                if(br != null) {
                    br.close();
                }

                boolean bAnyAdditions = !bOrigAnyPresent || !bOrigPresent  || 
                        !bChgAnyPresent || !bRejAnyPresent || !bConflictAnyPresent;               
                if(bAnyAdditions){
                    if (!confirmDialog(HgUtils.class, "MSG_IGNORE_FILES_TITLE", "MSG_IGNORE_FILES")) { // NOI18N 
                        VCSFileProxySupport.delete(tempFile);
                        return;
                    }
                    if(tempFile != null && tempFile.isFile() && tempFile.canWrite() && hgignoreFile != null){ 
                        VCSFileProxySupport.delete(hgignoreFile);
                        VCSFileProxySupport.renameTo(tempFile, hgignoreFile);
                    }
                }else{
                    VCSFileProxySupport.delete(tempFile);
                }
            } catch (IOException ex) {
            // Ignore
            }
        }
    }

    private static void addIgnorePatterns(Set<Pattern> patterns, VCSFileProxy file) {
        Set<String> shPatterns;
        try {
            shPatterns = readIgnoreEntries(file, true);
        } catch (IOException e) {
            // ignore invalid entries
            return;
        }
        for (Iterator i = shPatterns.iterator(); i.hasNext();) {
            String shPattern = (String) i.next();
            if ("!".equals(shPattern)) { // NOI18N
                patterns.clear();
            } else {
                try {
                    patterns.add(Pattern.compile(shPattern));
                } catch (Exception e) {
                    // unsupported pattern
                }
            }
        }
    }

    /**
     * Removes parts of the pattern denoting commentaries
     * @param line initial pattern
     * @return pattern with comments removed.
     */
    private static String removeCommentsInIgnore(String line) {
        int indexOfHash = -1;
        boolean cont;
        do {
            cont = false;
            indexOfHash = line.indexOf("#", indexOfHash);   // NOI18N
            // do not consider \# as a comment, skip that character and try to find the next comment
            if (indexOfHash > 0 && line.charAt(indexOfHash - 1) == '\\') {   // NOI18N
                ++indexOfHash;
                cont = true;
            }
        } while (cont);
        if (indexOfHash != -1) {
            if (indexOfHash == 0) {
                line = "";
            } else {
                line = line.substring(0, indexOfHash).trim();
            }
        }

        return line;
    }

    private static Boolean ignoreContainsSyntax(VCSFileProxy directory) throws IOException {
        VCSFileProxy hgIgnore = VCSFileProxy.createFileProxy(directory, FILENAME_HGIGNORE);
        Boolean val = false;

        if (!VCSFileProxySupport.canRead(hgIgnore)) {
            return val;
        }

        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(hgIgnore.getInputStream(false), "UTF-8")); //NOI18N
            while ((s = r.readLine()) != null) {
                String line = s.trim();
                line = removeCommentsInIgnore(line);
                if (line.length() == 0) {
                    continue;
                }
                String [] array = line.split(" "); //NOI18N
                if (array[0].equals("syntax:")) { //NOI18N
                    val = true;
                    break;
                }
            }
        } finally {
            if (r != null) {
                try { r.close(); } catch (IOException e) {}
            }
        }
        return val;
    }

    /**
     * @param transformEntries if set to false, this method returns the exact .hgignore file's content as a set of lines. 
     * If set to true, the method will parse the output, remove all comments, process globa and regexp syntax, etc. So if you just want to 
     * add or remove a certain line in the file, set the parameter to false.
     */
    private static Set<String> readIgnoreEntries(VCSFileProxy directory, boolean transformEntries) throws IOException {
        VCSFileProxy hgIgnore = VCSFileProxy.createFileProxy(directory, FILENAME_HGIGNORE);

        Set<String> entries = new LinkedHashSet<>(5);
        if (!VCSFileProxySupport.canRead(hgIgnore)) {
            return entries;
        }

        String s;
        BufferedReader r = null;
        boolean glob = false;
        try {
            r = new BufferedReader(new InputStreamReader(hgIgnore.getInputStream(false), "UTF-8")); //NOI18N
            while ((s = r.readLine()) != null) {
                String line = s;
                if (transformEntries) {
                    line = line.trim();
                    line = removeCommentsInIgnore(line);
                    if (line.length() == 0) {
                        continue;
                    }
                    String [] array = line.split(" ");                  //NOI18N
                    if (array[0].equals(IGNORE_SYNTAX_PREFIX)) {
                        String syntax = line.substring(IGNORE_SYNTAX_PREFIX.length()).trim();
                        if (IGNORE_SYNTAX_GLOB.equals(syntax)) {
                            glob = true;
                        } else if (IGNORE_SYNTAX_REGEXP.equals(syntax)) {
                            glob = false;
                        }
                        continue;
                    }
                }
                entries.add(glob ? transformFromGlobPattern(line) : line);
            }
        } finally {
            if (r != null) {
                try { r.close(); } catch (IOException e) {}
            }
        }
        return entries;
    }

    private static String transformFromGlobPattern (String pattern) {
        // returned pattern consists of two patterns - one for a file/folder directly under
        pattern = pattern.replace("$", "\\$").replace("^", "\\^").replace(".", "\\.").replace("*", ".*") + '$'; //NOI18N
        return  "^" + pattern // a folder/file directly under the repository, does not start with '/' //NOI18N
                + "|"                                                   //NOI18N
                + ".*/" + pattern; // a folder in 2nd+ depth            //NOI18N
    }

    private static String computePatternToIgnore(VCSFileProxy directory, VCSFileProxy file) {
        String name = "^" + file.getPath().substring(directory.getPath().length()+1) + "$"; //NOI18N
        // # should be escaped, otherwise works as a comment
        // . should be escaped, otherwise works as a special char in regexp
        return name.replace("#", "\\#").replace(".", "\\.").replace("+", "\\+"); //NOI18N
    }

    private static void writeIgnoreEntries(VCSFileProxy directory, Set entries) throws IOException {
        VCSFileProxy hgIgnore = VCSFileProxy.createFileProxy(directory, FILENAME_HGIGNORE);
        FileObject fo = hgIgnore.toFileObject();

        if (entries.isEmpty()) {
            if (fo != null) {
                fo.delete();
            }
            resetIgnorePatterns(directory);
            return;
        }

        if (fo == null || !fo.isValid()) {
            fo = directory.toFileObject();
            fo = fo.createData(FILENAME_HGIGNORE);
        }
        FileLock lock = fo.lock();
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8")); //NOI18N
            for (Iterator i = entries.iterator(); i.hasNext();) {
                w.write(i.next().toString());
                w.write('\n');
            }
        } finally {
            lock.releaseLock();
            if (w != null) {
                w.close();
            }
            resetIgnorePatterns(directory);
        }
    }

    /**
     * addIgnored - Add the specified files to the .hgignore file in the 
     * specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be added
     */
    public static void addIgnored(VCSFileProxy directory, VCSFileProxy[] files) throws IOException {
        if (ignoreContainsSyntax(directory)) {
            warningDialog(HgUtils.class, "MSG_UNABLE_TO_IGNORE_TITLE", "MSG_UNABLE_TO_IGNORE"); //NOI18N
            return;
        }
        Set<String> entries = readIgnoreEntries(directory, false);
        for (VCSFileProxy file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.add(patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }

    /**
     * removeIgnored - Remove the specified files from the .hgignore file in 
     * the specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be removed
     */
    public static void removeIgnored(VCSFileProxy directory, VCSFileProxy[] files) throws IOException {
        if (ignoreContainsSyntax(directory)) {
            warningDialog(HgUtils.class, "MSG_UNABLE_TO_UNIGNORE_TITLE", "MSG_UNABLE_TO_UNIGNORE"); //NOI18N
            return;
        }
        Set entries = readIgnoreEntries(directory, false);
        for (VCSFileProxy file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.remove(patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static VCSContext getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        VCSContext context = getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (VCSFileProxy file : context.getRootFiles()) {
            FileInformation fi = cache.getStatus(file);
            if (file.isDirectory()) {
                if ((fi.getStatus() & includingFolderStatus) == 0) {
                    return VCSContext.EMPTY;
                }
            } else {
                if ((fi.getStatus() & includingFileStatus) == 0) {
                    return VCSContext.EMPTY;
                }
            }
        }
        return context;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActiva
tedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for
Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are repr
esented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on ed
itor tabs #66700).
     */

    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static String getRootPath(VCSContext context){
        VCSFileProxy root = getRootFile(context);
        return (root == null) ? null: root.getPath();
    }
    
   /**
     * Determines if the given context contains at least one root file from
     * a mercurial repository
     *
     * @param VCSContext
     * @return true if the given conetxt contains a root file from a hg repository
     */
    public static boolean isFromHgRepository(VCSContext context){
        return getRootFile(context) != null;
    }

   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static VCSFileProxy getRootFile(VCSContext context){
        if (context == null) {
            return null;
        }
        Mercurial hg = Mercurial.getInstance();
        VCSFileProxy [] files = context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]);
        if (files == null || files.length == 0) {
            return null;
        }
        
        VCSFileProxy root = hg.getRepositoryRoot(files[0]);
        return root;
    }

   /**
     * Returns repository roots for all root files from context
     *
     * @param VCSContext
     * @return repository roots
     */
    public static Set<VCSFileProxy> getRepositoryRoots(VCSContext context) {
        Set<VCSFileProxy> rootsSet = context.getRootFiles();
        return getRepositoryRoots(rootsSet);
    }

    /**
     * Returns repository roots for all root files from context
     *
     * @param roots root files
     * @return repository roots
     */
    public static Set<VCSFileProxy> getRepositoryRoots (Set<VCSFileProxy> roots) {
        Set<VCSFileProxy> ret = new HashSet<>();

        // filter managed roots
        for (VCSFileProxy file : roots) {
            if(Mercurial.getInstance().isManaged(file)) {
                VCSFileProxy repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    ret.add(repoRoot);
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param ctx
     * @return
     */
    public static VCSFileProxy[] getActionRoots(VCSContext ctx) {
        Set<VCSFileProxy> rootsSet = ctx.getRootFiles();
        Map<VCSFileProxy, List<VCSFileProxy>> map = new HashMap<>();

        // filter managed roots
        for (VCSFileProxy file : rootsSet) {
            if(Mercurial.getInstance().isManaged(file)) {
                VCSFileProxy repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    List<VCSFileProxy> l = map.get(repoRoot);
                    if(l == null) {
                        l = new LinkedList<>();
                        map.put(repoRoot, l);
                    }
                    l.add(file);
                }
            }
        }

        Set<VCSFileProxy> repoRoots = map.keySet();
        if(map.size() > 1) {
            // more than one managed root => need a dlg
            FileSelector fs = new FileSelector(
                    NbBundle.getMessage(HgUtils.class, "LBL_FileSelector_Title"),
                    NbBundle.getMessage(HgUtils.class, "FileSelector.jLabel1.text"),
                    new HelpCtx("org.netbeans.modules.mercurial.FileSelector"),
                    HgModuleConfig.getDefault(repoRoots.iterator().next()).getPreferences());
            if(fs.show(repoRoots.toArray(new VCSFileProxy[repoRoots.size()]))) {
                VCSFileProxy selection = fs.getSelectedFile();
                List<VCSFileProxy> l = map.get(selection);
                return l.toArray(new VCSFileProxy[l.size()]);
            } else {
                return null;
            }
        } else if (map.isEmpty()) {
            return new VCSFileProxy[0];
        } else {
            List<VCSFileProxy> l = map.get(map.keySet().iterator().next());
            return l.toArray(new VCSFileProxy[l.size()]);
        }
    }

    /**
     * Returns only those root files from the given context which belong to repository
     * @param ctx
     * @param repository
     * @param rootFiles
     * @return
     */
    public static VCSFileProxy[] filterForRepository(final VCSContext ctx, final VCSFileProxy repository, boolean rootFiles) {
        VCSFileProxy[] files = null;
        if(ctx != null) {
            Set<VCSFileProxy> s = rootFiles ? ctx.getRootFiles() : ctx.getFiles();
            files = s.toArray(new VCSFileProxy[s.size()]);
        }
        if (files != null) {
            List<VCSFileProxy> l = new LinkedList<>();
            for (VCSFileProxy file : files) {
                VCSFileProxy r = Mercurial.getInstance().getRepositoryRoot(file);
                if (r != null && r.equals(repository)) {
                    l.add(file);
                }
            }
            files = l.toArray(new VCSFileProxy[l.size()]);
        }
        return files;
    }

    /**
     * Returns root files sorted per their repository roots
     * @param ctx
     * @param rootFiles
     * @return
     */
    public static Map<VCSFileProxy, Set<VCSFileProxy>> sortUnderRepository (final VCSContext ctx, boolean rootFiles) {
        Set<VCSFileProxy> files = null;
        if(ctx != null) {
            files = rootFiles ? ctx.getRootFiles() : ctx.getFiles();
        }
        return sortUnderRepository(files);
    }

    /**
     * Returns root files sorted per their repository roots
     * @param files
     * @return
     */
    public static Map<VCSFileProxy, Set<VCSFileProxy>> sortUnderRepository (Set<VCSFileProxy> files) {
        Map<VCSFileProxy, Set<VCSFileProxy>> sortedRoots = null;
        if (files != null) {
            sortedRoots = new HashMap<>();
            for (VCSFileProxy file : files) {
                VCSFileProxy r = Mercurial.getInstance().getRepositoryRoot(file);
                Set<VCSFileProxy> repositoryRoots = sortedRoots.get(r);
                if (repositoryRoots == null) {
                    repositoryRoots = new HashSet<>();
                    sortedRoots.put(r, repositoryRoots);
                }
                repositoryRoots.add(file);
            }
        }
        return sortedRoots == null ? Collections.<VCSFileProxy, Set<VCSFileProxy>>emptyMap() : sortedRoots;
    }   

    /**
     * Checks file location to see if it is part of mercurial metdata
     *
     * @param file file to check
     * @return true if the file or folder is a part of mercurial metadata, false otherwise
     */
    public static boolean isPartOfMercurialMetadata(VCSFileProxy file) {
        return metadataPattern.matcher(file.getPath()).matches();
    }
    

    /**
     * Forces refresh of Status for the given directory 
     * If a repository root is passed as the parameter, the cache will be refreshed only for already seen or open folders.
     * @param start file or dir to begin refresh from
     * @return void
     */
    public static void forceStatusRefresh(VCSFileProxy file) {
        if (isAdministrative(file)) {
            return;
        }
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        VCSFileProxy repositoryRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if (file.equals(repositoryRoot)) {
            // do not scan the whole repository, only open folders, projects etc. should be enough
            cache.refreshAllRoots(Collections.singletonMap(repositoryRoot, Mercurial.getInstance().getSeenRoots(repositoryRoot)));
        } else {
            cache.refresh(file);
        }
    }

    /**
     * Forces refresh of Status for the project of the specified context
     *
     * @param VCSContext ctx whose project is be updated.
     * @return void
     */
    public static void forceStatusRefreshProject(VCSContext context) {
        // XXX and what if there is more then one project in the ctx?!
        Project project = VCSFileProxySupport.getProject(context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]));
        if (project == null) {
            return;
        }
        VCSFileProxy[] files = org.netbeans.modules.mercurial.remote.versioning.util.Utils.getProjectRootFiles(project);
        for (int j = 0; j < files.length; j++) {
            forceStatusRefresh(files[j]);
        }
    }

    /**
     * Tests parent/child relationship of files.
     *
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */
    public static boolean isParentOrEqual(VCSFileProxy parent, VCSFileProxy file) {
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns path of file relative to root repository or a warning message
     * if the file is not under the repository root.
     *
     * @param File to get relative path from the repository root
     * @return String of relative path of the file from teh repository root
     */
    public static String getRelativePath(VCSFileProxy file) {
            if (file == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            String shortPath = file.getPath();
            if (shortPath == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            Mercurial mercurial = Mercurial.getInstance();
            VCSFileProxy rootManagedFolder = mercurial.getRepositoryRoot(file);
            if ( rootManagedFolder == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            String root = rootManagedFolder.getPath();
            if(shortPath.startsWith(root)) {
                return shortPath.substring(root.length()+1);
            }else{
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
     }

    /**
     * Normalize flat files, Mercurial treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     * <strong>Does not return up-to-date files</strong>
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */

    public static VCSFileProxy[] flatten(VCSFileProxy[] files, int status) {
        LinkedList<VCSFileProxy> ret = new LinkedList<>();

        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i<files.length; i++) {
            VCSFileProxy dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if ((status & info.getStatus()) != 0) {
                ret.add(dir);
            }
            VCSFileProxy[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                VCSFileProxy entry = entries[e];
                info = cache.getStatus(entry);
                if ((status & info.getStatus()) != 0) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new VCSFileProxy[ret.size()]);
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @param testCommitExclusions if set to true then returned files will not contain those excluded from commit
     * @return File [] array of Files having specified status
     */
    public static VCSFileProxy [] getModifiedFiles(VCSContext context, int includeStatus, boolean testCommitExclusions) {
        VCSFileProxy[] all = Mercurial.getInstance().getFileStatusCache().listFiles(context, includeStatus);
        List<VCSFileProxy> files = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            VCSFileProxy file = all[i];
            if (!testCommitExclusions || !HgModuleConfig.getDefault(HgUtils.getRootFile(context)).isExcludedFromCommit(file.getPath())) {
                files.add(file);
            }
        }

        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (VCSFileProxy file : context.getRootFiles()) {
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new VCSFileProxy[files.size()]);
    }

    /**
     * Checks if the file is binary.
     *
     * @param file file to check
     * @return true if the file cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo == null) {
            return false;
        }
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a number of 100ms-lasting waiting loops in a repository validity check.
     * If a sysprop defined in HgUtils.HG_CHECK_REPOSITORY_TIMEOUT_SWITCH is set, this returns a number count from HG_CHECK_REPOSITORY_TIMEOUT_SWITCH, otherwise it uses
     * a defaut value HgUtils.HG_CHECK_REPOSITORY_DEFAULT_ROUNDS.
     * @return number of rounds
     */
    public static int getNumberOfRoundsForRepositoryValidityCheck() {
        if (repositoryValidityCheckRounds <= 0) {
            try {
                repositoryValidityCheckRounds = Integer.parseInt(System.getProperty(HG_CHECK_REPOSITORY_TIMEOUT_SWITCH, HG_CHECK_REPOSITORY_DEFAULT_TIMEOUT)) * 10; // number of 100ms lasting rounds
            } catch (NumberFormatException ex) {
                Mercurial.LOG.log(Level.INFO, "Parsing integer failed, default value will be used", ex);
            }
            if (repositoryValidityCheckRounds <= 0) {
                Mercurial.LOG.fine("Using default value for number of rounds in repository validity check: " + HG_CHECK_REPOSITORY_DEFAULT_ROUNDS);
                repositoryValidityCheckRounds = HG_CHECK_REPOSITORY_DEFAULT_ROUNDS;
            }
        }
        return repositoryValidityCheckRounds;
    }

    /**
     * Returns true if hg in a given version supports 'hg resolve' command
     * Resolve command was introduced in 1.1
     * @param version
     * @return
     */
    public static boolean hasResolveCommand(String version) {
        if (version != null && (!version.startsWith("0.")               //NOI18N
                || !version.startsWith("1.0"))) {                       //NOI18N
            return true;
        }

        return false;
    }

    /**
     * Returns true if hg in a given version supports '--topo' option
     * --topo available probably since 1.5
     * @param version
     * @return
     */
    public static boolean hasTopoOption (String version) {
        if (version != null && !version.startsWith("0.") //NOI18N
                && !version.startsWith("1.0") //NOI18N
                && !version.startsWith("1.1") //NOI18N
                && !version.startsWith("1.2") //NOI18N
                && !version.startsWith("1.3") //NOI18N
                && !version.startsWith("1.4")) { //NOI18N
            return true;
        }

        return false;
    }

    /**
     * Returns the remote repository url for the given file.</br>
     * It will be the pull url in the first case, otherwise push url or null
     * in case there is nothig set in .hg
     *
     * @param file
     * @return
     */
    public static String getRemoteRepository(VCSFileProxy file) {
        if(file == null) {
            return null;
        }
        String remotePath = HgRepositoryContextCache.getInstance().getPullDefault(file);
        if (remotePath == null || remotePath.trim().isEmpty()) {
            Mercurial.LOG.log(Level.FINE, "No default pull available for managed file : [{0}]", file);
            remotePath = HgRepositoryContextCache.getInstance().getPushDefault(file);
            if (remotePath == null || remotePath.trim().isEmpty()) {
                Mercurial.LOG.log(Level.FINE, "No default pull or push available for managed file : [{0}]", file);
            }
        }
        if(remotePath != null) {
            remotePath = remotePath.trim();
            remotePath = HgUtils.removeHttpCredentials(remotePath);
            if (remotePath.isEmpty()) {
                // return null if empty
                remotePath = null;
            }
        }
        return remotePath;
    }

    public static void openInRevision (VCSFileProxy fileRevision1, HgRevision revision1, int lineNumber, 
            VCSFileProxy fileToOpen, HgRevision revisionToOpen, boolean showAnnotations) throws IOException {
        VCSFileProxy file = org.netbeans.modules.mercurial.remote.VersionsCache.getInstance().getFileRevision(fileRevision1, revision1);
        if (file == null) { // can be null if the file does not exist or is empty in the given revision
            file = VCSFileProxySupport.createTempFile(fileToOpen, "tmp", "-" + fileRevision1.getName(), true); //NOI18N
        }
        fileRevision1 = file;
        
        file = org.netbeans.modules.mercurial.remote.VersionsCache.getInstance().getFileRevision(fileToOpen, revisionToOpen);
        if (file == null) { // can be null if the file does not exist or is empty in the given revision
            file = VCSFileProxySupport.createTempFile(fileToOpen, "tmp", "-" + fileToOpen.getName(), true); //NOI18N
        }
        BufferedReader r1 = null;
        BufferedReader r2 = null;
        Charset encoding = RemoteVcsSupport.getEncoding(fileToOpen);
        try {
            r1 = new BufferedReader(new InputStreamReader(fileRevision1.getInputStream(false), encoding));
            r2 = new BufferedReader(new InputStreamReader(file.getInputStream(false), encoding));
            int matchingLine = DiffUtils.getMatchingLine(r1, r2, lineNumber);
            openFile(file, fileToOpen, matchingLine, revisionToOpen, showAnnotations);
        } finally {
            if (r1 != null) {
                r1.close();
            }
            if (r2 != null) {
                r2.close();
            }
        }
        
    }
    
    public static void openInRevision (VCSFileProxy originalFile, int lineNumber, HgRevision revision, boolean showAnnotations) throws IOException {
        VCSFileProxy file = org.netbeans.modules.mercurial.remote.VersionsCache.getInstance().getFileRevision(originalFile, revision);

        if (file == null) { // can be null if the file does not exist or is empty in the given revision
            file = VCSFileProxySupport.createTempFile(originalFile, "tmp", "-" + originalFile.getName(), true); //NOI18N
        }
        openFile(file, originalFile, lineNumber, revision, showAnnotations);
    }

    private static void openFile (VCSFileProxy fileToOpen, final VCSFileProxy originalFile, final int lineNumber,
            final HgRevision revision, boolean showAnnotations) throws IOException {
        final FileObject fo = fileToOpen.normalizeFile().toFileObject();
        EditorCookie ec = null;
        org.openide.cookies.OpenCookie oc = null;
        try {
            DataObject dobj = DataObject.find(fo);
            ec = dobj.getCookie(EditorCookie.class);
            oc = dobj.getCookie(org.openide.cookies.OpenCookie.class);
        } catch (DataObjectNotFoundException ex) {
            Mercurial.LOG.log(Level.FINE, null, ex);
        }
        org.openide.text.CloneableEditorSupport ces = null;
        if (ec == null && oc != null) {
            oc.open();
        } else {
            ces = org.netbeans.modules.versioning.util.Utils.openFile(fo, revision.getRevisionNumber());
        }
        if (showAnnotations) {
            if (ces != null) {
                final org.openide.text.CloneableEditorSupport support = ces;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        javax.swing.JEditorPane[] panes = support.getOpenedPanes();
                        if (panes != null) {
                            if (lineNumber >= 0 && lineNumber < support.getLineSet().getLines().size()) {
                                support.getLineSet().getCurrent(lineNumber).show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.FRONT);
                            }
                            org.netbeans.modules.mercurial.remote.ui.annotate.AnnotateAction.showAnnotations(panes[0], originalFile, revision.getRevisionNumber());
                        }
                    }
                });
            }
        }
    }

    public static boolean isCanceled (Exception e) {
        Throwable cause = e;
        while (cause != null && !(cause instanceof HgCommandCanceledException)) {
            cause = cause.getCause();
        }
        return cause instanceof HgCommandCanceledException;
    }

    /**
     * Returns an array with root as its only item or all seen roots under the root when it's a repository root
     * @param root
     * @return 
     */
    public static VCSFileProxy[] splitIntoSeenRoots (VCSFileProxy root) {
        VCSFileProxy[] roots;
        VCSFileProxy repositoryRoot = Mercurial.getInstance().getRepositoryRoot(root);
        if (root.equals(repositoryRoot)) {
            Set<VCSFileProxy> seenRoots = Mercurial.getInstance().getSeenRoots(repositoryRoot);
            roots = seenRoots.toArray(new VCSFileProxy[seenRoots.size()]);
        } else {
            roots = new VCSFileProxy[] { root };
        }
        return roots;
    }

    public static boolean isRepositoryLocked (VCSFileProxy repository) {
        List<String> locks = new ArrayList<>();
        final VCSFileProxy[] files = getHgFolderForRoot(repository).listFiles();
        if (files != null) {
            for(VCSFileProxy file : files) {
                locks.add(file.getPath());
            }
        }
        return locks.contains(WLOCK_FILE);
    }

    public static boolean contains (Collection<VCSFileProxy> roots, VCSFileProxy file) {
        return contains(roots.toArray(new VCSFileProxy[roots.size()]), file);
    }

    public static boolean contains (VCSFileProxy[] roots, VCSFileProxy file) {
        for (VCSFileProxy root : roots) {
            if (VCSFileProxySupport.isAncestorOrEqual(root, file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns set of opened files belonging to a given repository
     * @param repository 
     */
    public static Set<VCSFileProxy> getOpenedFiles (VCSFileProxy repository) {
        Set<VCSFileProxy> openFiles = VCSFileProxySupport.getOpenFiles();
        for (Iterator<VCSFileProxy> it = openFiles.iterator(); it.hasNext(); ) {
            VCSFileProxy file = it.next();
            if (!repository.equals(Mercurial.getInstance().getRepositoryRoot(file))) {
                it.remove();
            }
        }
        return openFiles;
    }

    /**
     * Opens the output window for a given logger.
     */
    public static void openOutput (final OutputLogger logger) {
        final Action a = logger.getOpenOutputAction();
        if (a != null) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run () {
                    a.actionPerformed(new ActionEvent(logger, ActionEvent.ACTION_PERFORMED, null));
                }
            });
        }
    }

    public static void notifyException (Exception ex) {
        Mercurial.LOG.log(Level.FINE, null, ex);
        NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
        DialogDisplayer.getDefault().notifyLater(e);
    }

    private static String getHex (int i) {
        String hex = Integer.toHexString(i & 255);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
    }

    public static String getColorString (Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }

    public static List<String> getNotSharablePaths (VCSFileProxy repository, List<VCSFileProxy> roots) {
        List<String> ignored;
        synchronized (notSharable) {
            Set<String> set = notSharable.get(repository);
            if (set == null) {
                ignored = Collections.<String>emptyList();
            } else {
                ignored = new ArrayList<>(set);
            }
        }
        if (ignored.size() > 10 && !roots.contains(repository)) {
            // we could optimize and return only a subset of ignored files/folders
            // there are applicable to the selected context
            Set<String> acceptedPaths = new HashSet<>(ignored.size());
            for (VCSFileProxy root : roots) {
                String relPath = getRelativePath(root, repository);
                for (String ignoredPath : ignored) {
                    if (ignoredPath.startsWith(relPath) || relPath.startsWith(ignoredPath)) {
                        acceptedPaths.add(ignoredPath);
                    }
                }
            }
            ignored = new ArrayList<>(acceptedPaths);
        }
        return ignored;
    }

    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator<T> implements Comparator<FileInformation> {
        @Override
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }

    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
       } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)){
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }

    /**
     * Rips an eventual username off - e.g. user@svn.host.org
     *
     * @param host - hostname with a userneame
     * @return host - hostname without the username
     */
    public static String ripUserFromHost(String host) {
        int idx = host.indexOf('@');
        if(idx < 0) {
            return host;
        } else {
            return host.substring(idx + 1);
        }
    }

    /**
     * Uses content analysis for unversioned files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public static String getMimeType(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        String foMime;
        if (fo == null) {
            foMime = "content/unknown"; //NOI18N
        } else {
            foMime = fo.getMIMEType();
        }
        if(foMime.startsWith("text")) { //NOI18N
            return foMime;
        }
        return VCSFileProxySupport.isFileContentText(file) ? "text/plain" : "application/octet-stream"; //NOI18N
    }

    public static void logHgLog(HgLogMessage log, OutputLogger logger) {
        String lbChangeset = NbBundle.getMessage(HgUtils.class, "LB_CHANGESET");   // NOI18N
        String lbUser =      NbBundle.getMessage(HgUtils.class, "LB_AUTHOR");      // NOI18N
        String lbBranch =    NbBundle.getMessage(HgUtils.class, "LB_BRANCH");      // NOI18N
        String lbTags =      NbBundle.getMessage(HgUtils.class, "LB_TAGS");      // NOI18N
        String lbDate =      NbBundle.getMessage(HgUtils.class, "LB_DATE");        // NOI18N
        String lbSummary =   NbBundle.getMessage(HgUtils.class, "LB_SUMMARY");     // NOI18N
        int l = 0;
        List<String> list = new LinkedList<>(Arrays.asList(new String[] {lbChangeset, lbUser, lbDate, lbSummary}));
        if (log.getBranches().length > 0) {
            list.add(lbBranch);
        }
        if (log.getTags().length > 0) {
            list.add(lbTags);
        }
        for (String s : list) {
            if(l < s.length()) {
                l = s.length();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(formatlabel(lbChangeset, l));
        sb.append(log.getRevisionNumber());
        sb.append(":"); // NOI18N
        sb.append(log.getCSetShortID());
        sb.append('\n'); // NOI18N
        if (log.getBranches().length > 0) {
            sb.append(formatlabel(lbBranch, l));
            for (String branch : log.getBranches()) {
                sb.append(branch);
            }
            sb.append('\n'); // NOI18N
        }
        if (log.getTags().length > 0) {
            sb.append(formatlabel(lbTags, l));
            for (String tag : log.getTags()) {
                sb.append(tag).append(' ');
            }
            sb.append('\n'); // NOI18N
        }
        sb.append(formatlabel(lbUser, l));
        sb.append(log.getAuthor());
        sb.append('\n'); // NOI18N
        sb.append(formatlabel(lbDate, l));
        sb.append(log.getDate());
        sb.append('\n'); // NOI18N
        sb.append(formatlabel(lbSummary, l));
        sb.append(log.getMessage());
        sb.append('\n'); // NOI18N

        logger.output(sb.toString());
    }


    private static String formatlabel(String label, int l) {
        label = label + spaces(l - label.length()) + ": "; //NOI18N
        return label;
    }

    private static String spaces(int l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l + 3; i++) {
            sb.append(" "); //NOI18N
        }
        return sb.toString();
    }

    /**
     * This utility class should not be instantiated anywhere.
     */
    private HgUtils() {
    }

    private static Logger TY9_LOG = null;
    public static void logT9Y(String msg) {
        if(TY9_LOG == null) {
            TY9_LOG = Logger.getLogger("org.netbeans.modules.mercurial.t9y"); //NOI18N
        }
        TY9_LOG.log(Level.FINEST, msg);
    }

    /**
     * Validates annotation format text
     * @param format format to be validatet
     * @return <code>true</code> if the format is correct, <code>false</code> otherwise.
     */
    public static boolean isAnnotationFormatValid(String format) {
        boolean retval = true;
        if (format != null) {
            try {
                new MessageFormat(format);
            } catch (IllegalArgumentException ex) {
                Mercurial.LOG.log(Level.FINER, "Bad user input - annotation format", ex);
                retval = false;
            }
        }
        return retval;
    }

    /**
     * Tests <tt>.hg</tt> directory itself.
     */
    public static boolean isAdministrative(VCSFileProxy file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public static boolean isAdministrative(String fileName) {
        return fileName.equals(".hg"); // NOI18N
    }

    public static boolean hgExistsFor(VCSFileProxy file) {
        return VCSFileProxy.createFileProxy(file, ".hg").exists(); //NOI18N
    }

    public static CommitOptions[] createDefaultCommitOptions (HgFileNode[] nodes, boolean excludeNew) {
        CommitOptions[] commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            HgFileNode node = nodes[i];
            VCSFileProxy file = node.getFile();
            if (HgModuleConfig.getDefault(file).isExcludedFromCommit(file.getPath())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                switch (node.getInformation().getStatus()) {
                case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
                case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                    commitOptions[i] = CommitOptions.COMMIT_REMOVE;
                    break;
                case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                    commitOptions[i] = excludeNew ? CommitOptions.EXCLUDE : CommitOptions.COMMIT;
                    break;
                default:
                    commitOptions[i] = CommitOptions.COMMIT;
                }
            }
        }
        return commitOptions;
    }

    /**
     * Returns the administrative hg folder for the given repository and normalizes the file
     * @param repositoryRoot root of the repository
     * @return administrative hg folder
     */
    public static VCSFileProxy getHgFolderForRoot (VCSFileProxy repositoryRoot) {
        return VCSFileProxy.createFileProxy(repositoryRoot, HG_FOLDER_NAME).normalizeFile();
    }

    /**
     * Asynchronously tests if hg is available and if positive runs the given runnable in AWT.
     * @param runnable 
     */
    public static void runIfHgAvailable (final VCSFileProxy root, final Runnable runnable) {
        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (Mercurial.getInstance().isAvailable(root, true, true)) {
                    EventQueue.invokeLater(runnable);
                }
            }
        });
    }

    /**
     * Adds the given file into filesUnderRoot:
     * <ul>
     * <li>if the file was already in the set, does nothing and returns true</li>
     * <li>if the file lies under a folder already present in the set, does nothing and returns true</li>
     * <li>if the file and none of it's ancestors is not in the set yet, this adds the file into the set,
     * removes all it's children from the set and returns false</li>
     * @param repository repository root
     * @param filesUnderRoot set of repository roots
     * @param file file to add
     * @return newly added root or null if the to be added file is already contained in seen roots
     */
    public static VCSFileProxy prepareRootFiles(VCSFileProxy repository, Set<VCSFileProxy> filesUnderRoot, VCSFileProxy file) {
        boolean added = false;
        VCSFileProxy addedRoot = null;
        for (VCSFileProxy fileUnderRoot : filesUnderRoot) {
            // try to find a common parent for planned files
            VCSFileProxy childCandidate = file;
            VCSFileProxy ancestorCandidate = fileUnderRoot;
            added = true;
            if (childCandidate.equals(ancestorCandidate) || ancestorCandidate.equals(repository)) {
                // file has already been inserted or scan is planned for the whole repository root
                break;
            }
            if (childCandidate.equals(repository)) {
                // plan the scan for the whole repository root
                ancestorCandidate = childCandidate;
            } else {
                if (file.getPath().length() < fileUnderRoot.getPath().length()) {
                    // ancestor's path is too short to be the child's parent
                    ancestorCandidate = file;
                    childCandidate = fileUnderRoot;
                }
                if (!VCSFileProxySupport.isAncestorOrEqual(ancestorCandidate, childCandidate)) {
                    ancestorCandidate = VCSFileProxySupport.getCommonParent(childCandidate, ancestorCandidate);
                }
            }
            if (ancestorCandidate == fileUnderRoot) {
                // already added
                break;
            } else if (!FileStatusCache.FULL_REPO_SCAN_ENABLED && ancestorCandidate != childCandidate && ancestorCandidate.equals(repository)) {
                // common ancestor is the repo root and neither one of the candidates was originally the repo root
                // do not scan the whole clone, it might be a performance killer
                added = false;
            } else if (ancestorCandidate != null) {
                // file is under the repository root
                if (ancestorCandidate.equals(repository)) {
                    // adding the repository, there's no need to leave all other files
                    filesUnderRoot.clear();
                } else {
                    filesUnderRoot.remove(fileUnderRoot);
                }
                filesUnderRoot.add(addedRoot = ancestorCandidate);
                break;
            } else {
                added = false;
            }
        }
        if (!added) {
            // not added yet
            filesUnderRoot.add(addedRoot = file);
        }
        return addedRoot;
    }

    /**
     * Fires events for updated files. Thus diff sidebars are refreshed.
     * @param repo
     * @param list
     * @return true if any file triggered the notification
     */
    public static boolean notifyUpdatedFiles(VCSFileProxy repo, List<String> list){
        boolean anyFileNotified = false;
        // When hg -v output, or hg -v unbundle or hg -v pull is called
        // the output contains line
        // getting <file>
        // for each file updated.
        //
        for (String line : list) {
            if (line.startsWith("getting ") || line.startsWith("merging ")) { //NOI18N
                String name = line.substring(8);
                VCSFileProxy file = VCSFileProxy.createFileProxy(repo, name);
                anyFileNotified = true;
                Mercurial.getInstance().notifyFileChanged(file);
            }
        }
        return anyFileNotified;
    }
    
    /**
     * Sorts heads by branch they belong to.
     * @param heads
     * @return 
     */
    public static Map<String, Collection<HgLogMessage>> sortByBranch (HgLogMessage[] heads) {
        Map<String, Collection<HgLogMessage>> branchHeadsMap = new HashMap<>(heads.length);
        for (HgLogMessage head : heads) {
            String[] branches = head.getBranches().length > 0 ? head.getBranches() : new String[] { HgBranch.DEFAULT_NAME };
            for (String branch : branches) {
                Collection<HgLogMessage> branchHeads = branchHeadsMap.get(branch);
                if (branchHeads == null) {
                    branchHeads = new LinkedList<>();
                    branchHeadsMap.put(branch, branchHeads);
                }
                branchHeads.add(head);
            }
        }
        return branchHeadsMap;
    }
    
    public static VCSContext buildVCSContext (VCSFileProxy[] roots) {
        List<Node> nodes = new ArrayList<>(roots.length);
        for (VCSFileProxy root : roots) {
            nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(root)));
        }
        return VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
    }

    public static <T> T runWithoutIndexing (Callable<T> callable, List<VCSFileProxy> files) throws HgException {
        return runWithoutIndexing(callable, files.toArray(new VCSFileProxy[files.size()]));
    }

    static ThreadLocal<Set<VCSFileProxy>> indexingFiles = new ThreadLocal<>();
    public static <T> T runWithoutIndexing (Callable<T> callable, final VCSFileProxy... files) throws HgException {
        try {
            Set<VCSFileProxy> recursiveRoots = indexingFiles.get();
            if (recursiveRoots != null) {
                assert indexingFilesSubtree(recursiveRoots, files) 
                        : "Recursive call does not permit different roots: "  //NOI18N
                        + recursiveRoots + " vs. " + Arrays.asList(files); //NOI18N
                return callable.call();
            } else {
                try {
                    if (Mercurial.LOG.isLoggable(Level.FINER)) {
                        Mercurial.LOG.log(Level.FINER, "Running block with disabled indexing: on {0}", Arrays.asList(files)); //NOI18N
                    }
                    indexingFiles.set(new HashSet<>(Arrays.asList(files)));
                    return FileObjectIndexingBridgeProvider.getInstance().runWithoutIndexing(callable, files);
                } finally {
                    indexingFiles.remove();
                }
            }
        } catch (HgException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            Mercurial.LOG.log(Level.INFO, "Cannot run block without indexing", ex); //NOI18N
            throw new HgException("Cannot run without indexing due to: " + ex.getMessage()); //NOI18N
        }
    }

    private static boolean indexingFilesSubtree (Set<VCSFileProxy> recursiveRoots, VCSFileProxy[] files) {
        for (VCSFileProxy f : files) {
            if (!recursiveRoots.contains(f)) {
                boolean contained = false;
                for (VCSFileProxy root : recursiveRoots) {
                    if (VCSFileProxySupport.isAncestorOrEqual(root, f)) {
                        contained = true;
                        break;
                    }
                }
                if (!contained) {
                    return false;
                }
            }
        }
        return true;
    }
}
