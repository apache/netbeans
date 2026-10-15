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

package org.netbeans.modules.mercurial.ui.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;

/**
 *
 * @author jr140578
 */
public class HgLogMessage {
    public static char HgModStatus = 'M';
    public static char HgAddStatus = 'A';
    public static char HgDelStatus = 'D';
    public static char HgCopyStatus = 'C';
    public static char HgRenameStatus = 'R';

    private final List<HgLogMessageChangedPath> paths;
    private final List<HgLogMessageChangedPath> dummyPaths;
    private final HgRevision rev;
    private final String author;
    private final String username;
    private final String desc;
    private final String shortdesc;
    private final Date date;
    private String timeZoneOffset;

    private HgRevision parentOneRev;
    private HgRevision parentTwoRev;
    private final boolean bMerged;
    private final String rootURL;
    private OutputLogger logger;
    private final HashMap<File, HgRevision> ancestors = new HashMap<File, HgRevision>();
    private final String[] branches;
    private final String[] tags;
    private static final String MARK_ACTIVE_HEAD = "*"; //NOI18N

    private void updatePaths(List<String> pathsStrings, String path, char status) {
        paths.add(new HgLogMessageChangedPath(path, null, status));
        if (pathsStrings != null){
            pathsStrings.add(path);
        }
    }

    public HgLogMessage(String rootURL, List<String> filesShortPaths, String rev, String auth, String username, String desc, String date, String id,
            String parents, String fm, String fa, String fd, String fc, String branches, String tags) {

        this.rootURL = rootURL;
        this.rev = new HgRevision(id, rev);
        this.author = auth;
        this.username = username;
        this.desc = desc;
        this.shortdesc = getFirstLine(desc);
        this.date = new Date(Long.parseLong(date.split(" ")[0]) * 1000); // UTC in miliseconds
        String[] parentSplits;
        parentSplits = parents != null ? parents.split(" ") : null;
        if ((parentSplits != null) && (parentSplits.length == 2)) {
            parentOneRev = createRevision(parentSplits[0]);
            parentTwoRev = createRevision(parentSplits[1]);
        }
        this.bMerged = this.parentOneRev != null && this.parentTwoRev != null && !this.parentOneRev.getRevisionNumber().equals("-1") && !this.parentTwoRev.getRevisionNumber().equals("-1");

        this.paths = new ArrayList<HgLogMessageChangedPath>();
        List<String> apathsStrings = new ArrayList<String>();
        List<String> dpathsStrings = new ArrayList<String>();
        List<String> cpathsStrings = new ArrayList<String>();

        // Mercurial Bug: Currently not seeing any file_copies coming back from Mercurial
        if (fd != null && !fd.equals("")) {
            for (String s : fd.split("\t")) {
                updatePaths(dpathsStrings, s, HgDelStatus);
            }
        }
        if (fc != null && !fc.equals("")) {
            String[] copyPaths = fc.split("\t");
            for (int i = 0; i < copyPaths.length / 2; ++i) {
                String path = copyPaths[i * 2];
                String original = copyPaths[i * 2 + 1];
                cpathsStrings.add(path);
                if (dpathsStrings.contains(original)) {
                    for (ListIterator<HgLogMessageChangedPath> it = paths.listIterator(); it.hasNext(); ) {
                        HgLogMessageChangedPath msg = it.next();
                        if (original.equals(msg.getPath())) {
                            it.remove();
                        }
                    }
                    paths.add(new HgLogMessageChangedPath(path, original, HgRenameStatus));
                } else {
                    paths.add(new HgLogMessageChangedPath(path, original, HgCopyStatus));
                }
            }
        }
        if (fa != null && !fa.equals("")) {
            for (String s : fa.split("\t")) {
                if(!cpathsStrings.contains(s)){
                    updatePaths(apathsStrings, s, HgAddStatus);
                }
            }
        }
        if (fm != null && !fm.equals("")) {
            for (String s : fm.split("\t")) {
                //#132743, incorrectly reporting files as added/modified, deleted/modified in same changeset
                if (!apathsStrings.contains(s) && !dpathsStrings.contains(s) && !cpathsStrings.contains(s)) {
                    updatePaths(null, s, HgModStatus);
                }
            }
        }
        this.dummyPaths = new ArrayList<HgLogMessageChangedPath>(filesShortPaths.size());
        for (String fileSP : filesShortPaths) {
            dummyPaths.add(new HgLogMessageChangedPath(fileSP, null, '?'));
        }
        if (branches.isEmpty()) {
            this.branches = new String[0];
        } else {
            this.branches = branches.split("\t");
        }
        if (tags.isEmpty()) {
            this.tags = new String[0];
        } else {
            this.tags = tags.split("\t");
        }
    }

    public HgLogMessageChangedPath [] getChangedPaths(){
        return paths.toArray(new HgLogMessageChangedPath[0]);
    }

    HgLogMessageChangedPath [] getDummyChangedPaths () {
        return dummyPaths.toArray(new HgLogMessageChangedPath[0]);
    }

    /**
     * Equal to getHgRevision().getRevisionNumber()
     * @return
     */
    public String getRevisionNumber () {
        return rev.getRevisionNumber();
    }

    public long getRevisionAsLong() {
        long revLong;
        try{
            revLong = Long.parseLong(rev.getRevisionNumber());
        }catch(NumberFormatException ex){
            // Ignore number format errors
            return 0;
        }
        return revLong;
    }
    
    public HgRevision getHgRevision () {
        return rev;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getUsername () {
        return username;
    }

    /**
     * Equal to getHgRevision().getChangesetId()
     * @return
     */
    public String getCSetShortID() {
        return rev.getChangesetId();
    }
    
    public boolean isMerge () {
        return bMerged;
    }

    public HgRevision getAncestor (File file) {
        HgRevision ancestor = getAncestorFromMap(file);
        if (ancestor != null) {
            return ancestor;
        }
        if(bMerged){
            try{
                ancestor = HgCommand.getCommonAncestor(rootURL, parentOneRev.getRevisionNumber(), parentTwoRev.getRevisionNumber(), getLogger());
            } catch (HgException ex) {
                Mercurial.LOG.log(ex instanceof HgException.HgCommandCanceledException ? Level.FINE : Level.INFO, null, ex);
                return HgRevision.EMPTY;
            }
        } else {
            try{
                ancestor = HgCommand.getParent(new File(rootURL), file, rev.getRevisionNumber());
            } catch (HgException ex) {
                Mercurial.LOG.log(ex instanceof HgException.HgCommandCanceledException ? Level.FINE : Level.INFO, null, ex);
                return HgRevision.EMPTY;
            }
            if (ancestor == null) {
                // fallback to the old impl in case of any error
                try {
                    Integer.toString(Integer.parseInt(rev.getRevisionNumber()) - 1);
                } catch (NumberFormatException ex) {
                    ancestor = HgRevision.EMPTY;
                }
            }
        }
        addAncestorToMap(file, ancestor);
        return ancestor;
    }

    private OutputLogger getLogger() {
        if (logger == null) {
            logger = Mercurial.getInstance().getLogger(rootURL);
        }
        return logger;
    }

    public String getMessage() {
        return desc;
    }

    public String getShortMessage () {
        return shortdesc;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String[] getBranches () {
        return branches;
    }

    public String[] getTags () {
        return tags;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("rev: ");
        sb.append(rev.getRevisionNumber());
        sb.append("\nauthor: ");
        sb.append(this.author);
        sb.append("\ndesc: ");
        sb.append(this.desc);
        sb.append("\ndate: ");
        sb.append(this.date);
        sb.append("\nid: ");
        sb.append(rev.getChangesetId());
        sb.append("\npaths: ");
        sb.append(this.paths);
        return sb.toString();
    }

    public File getOriginalFile (File root, File file) {
        for (HgLogMessageChangedPath path : paths) {
            if (file.equals(new File(root, path.getPath()))) {
                if (path.getCopySrcPath() == null) {
                    return file;
                } else {
                    return new File(root, path.getCopySrcPath());
                }
            }
        }
        return null;
    }

    private void addAncestorToMap (File file, HgRevision ancestor) {
        ancestors.put(file, ancestor);
    }

    private HgRevision getAncestorFromMap(File file) {
        return ancestors.get(file);
    }

    private HgRevision createRevision (String revisionString) {
        String[] ps1 = revisionString.split(":"); // NOI18N
        String revisionNumber = ps1 != null && ps1.length >= 1 ? ps1[0] : null;
        String changesetId = ps1 != null && ps1.length >= 2 ? ps1[1] : revisionNumber;
        return revisionNumber == null ? null : new HgRevision(changesetId, revisionNumber);
    }

    public void refreshChangedPaths (HgLogMessageChangedPath[] newPaths) {
        assert getChangedPaths().length == 0 : "Why refreshing already loaded change paths??? length=" + getChangedPaths().length;
        paths.clear();
        dummyPaths.clear();
        paths.addAll(Arrays.asList(newPaths));
    }

    public String toAnnotatedString (String wcparentCSetId) {
        StringBuilder sb = new StringBuilder().append(getRevisionNumber());
        if (wcparentCSetId.equals(getCSetShortID())) {
            sb.append(MARK_ACTIVE_HEAD);
        }
        StringBuilder labels = new StringBuilder();
        for (String branch : getBranches()) {
            labels.append(branch).append(' ');
        }
        for (String tag : getTags()) {
            labels.append(tag).append(' ');
            break; // just one tag
        }
        sb.append(" (").append(labels).append(labels.length() == 0 ? "" : "- ") //NOI18N
                .append(getCSetShortID().substring(0, 7)).append(")"); //NOI18N
        if (!getShortMessage().isEmpty()) {
            sb.append(" - ").append(getShortMessage()); //NOI18N
        }
        return sb.toString();
    }

    private static String getFirstLine (String desc) {
        String firstLine = ""; //NOI18N
        if (desc != null) {
            firstLine = desc.split("\n")[0]; //NOI18N
        }
        return firstLine;
    }
    
    @NbBundle.Messages({
        "MSG_HgRevision.name.BASE=Working Directory Parent",
        "MSG_HgRevision.name.LOCAL=Local Changes",
        "MSG_HgRevision.name.QDIFFBASE=QDiff Base"
    })
    public static class HgRevision {
        private final String changesetId;
        private final String revisionNumber;
        
        public static final HgRevision EMPTY = new HgRevision("-1", "-1") { //NOI18N
            @Override
            public String toString () {
                return getChangesetId();
            }
        };
        public static final HgRevision BASE = new HgRevision(".", "BASE") { //NOI18N
            @Override
            public String toString () {
                return Bundle.MSG_HgRevision_name_BASE();
            }
        };
        public static final HgRevision CURRENT = new HgRevision("LOCAL", "LOCAL") { //NOI18N
            @Override
            public String toString () {
                return Bundle.MSG_HgRevision_name_LOCAL();
            }
        };
        public static final HgRevision QDIFF_BASE = new HgRevision("qtip~1", "qtip~1") { //NOI18N
            @Override
            public String toString () {
                return Bundle.MSG_HgRevision_name_QDIFFBASE();
            }
        };

        public HgRevision(String changesetId, String revisionNumber) {
            this.changesetId = changesetId;
            this.revisionNumber = revisionNumber;
        }

        public String getChangesetId() {
            return changesetId;
        }

        public String getRevisionNumber() {
            return revisionNumber;
        }

        @Override
        public String toString () {
            return revisionNumber + " - " + changesetId; //NOI18N
        }
    }
}
