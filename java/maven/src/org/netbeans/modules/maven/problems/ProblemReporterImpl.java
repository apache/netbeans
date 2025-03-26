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

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.PluginArtifactsCache;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */

@ActionReferences({
    @ActionReference(
        id=@ActionID(id="org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory",category="Project"),
        position = 3100,
        path = "Projects/org-netbeans-modules-maven/Actions")
})
@SuppressWarnings("deprecation")
public final class ProblemReporterImpl implements ProblemReporter, Comparator<ProblemReport>, ProjectProblemsProvider {
    
    private static final Logger LOG = Logger.getLogger(ProblemReporterImpl.class.getName());
    public static final RequestProcessor RP = new RequestProcessor(ProblemReporterImpl.class);

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final Set<ProblemReport> reports;
    private final Set<File> missingArtifacts;
    private final File projectPOMFile;
    private final RequestProcessor.Task reloadTask = RP.create(new Runnable() {
        @Override public void run() {
            LOG.log(Level.FINE, "actually reloading {0}", projectPOMFile);
            nbproject.fireProjectReload(true);
        }
    });
    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override public void fileDataCreated(FileEvent fe) {
            LOG.log(Level.FINE, "due to {0} scheduling reload of {1}", new Object[] {fe.getFile(), projectPOMFile});
            reloadTask.schedule(1000);
            File f = FileUtil.toFile(fe.getFile());
            if (f != null) {
                BatchProblemNotifier.resolved(f);
            } else {
                LOG.log(Level.FINE, "no java.io.File from {0}", fe);
            }
        }
    };
    private final NbMavenProjectImpl nbproject;
    
    /** Creates a new instance of ProblemReporter */
    public ProblemReporterImpl(NbMavenProjectImpl proj) {
        reports = new TreeSet<ProblemReport>(this);
        missingArtifacts = new HashSet<File>();
        nbproject = proj;
        projectPOMFile = nbproject.getPOMFile();
    }
    
    public void addChangeListener(ChangeListener list) {
        synchronized (listeners) {
            listeners.add(list);
        }
    }
    
    public void removeChangeListener(ChangeListener list) {
         synchronized (listeners) {
             listeners.remove(list);
         }
    }
    
    @Override public void addReport(ProblemReport report) {
        assert report != null;
        synchronized (reports) {
            reports.add(report);
        }
        fireChange();
        firePropertyChange();
    }
    
    @Override public void addReports(ProblemReport[] report) {
        assert report != null;
        synchronized (reports) {
            for (int i = 0; i < report.length; i++) {
                assert report[i] != null;
                reports.add(report[i]);
            }
        }
        fireChange();
        firePropertyChange();
    }
    
    @Override public void removeReport(ProblemReport report) {
        synchronized (reports) {
            reports.remove(report);
        }
        fireChange();
        firePropertyChange();
    }
    
    private void fireChange() {
        ArrayList<ChangeListener> list;
        synchronized (listeners) {        
            list = new ArrayList<ChangeListener>(listeners);
        }
        for (ChangeListener li : list) {
            li.stateChanged(new ChangeEvent(this));
        }
    }

    /** @return true if {@link #getReports} is nonempty */
    public boolean isBroken() {
        synchronized (reports) {
            for (ProblemReport report : reports) {
                if (report.getSeverityLevel() < ProblemReport.SEVERITY_LOW) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override public Collection<ProblemReport> getReports() {
        synchronized (reports) {
            return new ArrayList<ProblemReport>(reports);
        }
    }

    /**
     * Note an artifact whose absence in the local repository is implicated among the problems.
     * Note that some problems are not caused by missing artifacts,
     * and some problems encapsulate several missing artifacts.
     * @param a an artifact (scope permitted but ignored)
     */
    void addMissingArtifact(Artifact a, boolean checkMissing) {
        synchronized (reports) {
            a = EmbedderFactory.getProjectEmbedder().getLocalRepository().find(a);
            //a.getFile should be already normalized but the find() method can pull tricks on us.
            //#225008
            File f = FileUtil.normalizeFile(a.getFile());
            if (f.exists() && f.canRead() && checkMissing) {
                try {
                    MavenExecutionRequest rq = EmbedderFactory.getProjectEmbedder().createMavenExecutionRequest();
                    List<ArtifactRepository> repos = nbproject.getOriginalMavenProject().getRemoteArtifactRepositories();
                    if (repos.isEmpty()) {
                        repos = rq.getRemoteRepositories();
                    }
                    EmbedderFactory.getProjectEmbedder().resolve(a, repos, EmbedderFactory.getProjectEmbedder().getLocalRepository());
                } catch (ArtifactResolutionException | ArtifactNotFoundException ex) {
                    return;
                }
                throw new ArtifactFoundException(a, f);
            }
            if (missingArtifacts.add(f)) {                
                LOG.log(Level.FINE, "listening to {0} from {1}", new Object[] {f, projectPOMFile});                
                FileUtil.addFileChangeListener(fcl, f);
            }
        }
    }
    
    /**
     * Indicates that the cached data that report a missing artifact is obsolete. 
     */
    public static class ArtifactFoundException extends IllegalStateException {
        private final Artifact artifact;
        private final File artifactFile;

        public ArtifactFoundException(Artifact artifact, File artifactFile) {
            this.artifact = artifact;
            this.artifactFile = artifactFile;
        }
    }
    
    

    public Set<File> getMissingArtifactFiles() {
        synchronized (reports) {
            return new TreeSet<File>(missingArtifacts);
        }
    }

    public boolean hasReportWithId(String id) {
        return getReportWithId(id) != null;
    }

    public ProblemReport getReportWithId(String id) {
        assert id != null;
        synchronized (reports) {
            for (ProblemReport rep : reports) {
                if (id.equals(rep.getId())) {
                    return rep;
                }
            }
        }
        return null;
    }
    
    public void clearReports() {
        boolean hasAny;
        synchronized (reports) {
            hasAny = !reports.isEmpty();
            reports.clear();
            Iterator<File> as = missingArtifacts.iterator();
            while (as.hasNext()) {
                File f = as.next();
                if (f != null) {
                    LOG.log(Level.FINE, "ceasing to listen to {0} from {1}", new Object[] {f, projectPOMFile});
                    // a.getFile() should be normalized
                    FileUtil.removeFileChangeListener(fcl, f);
                    if (f.isFile()) {
                        BatchProblemNotifier.resolved(f);
                    }
                }
                as.remove();
            }
            missingArtifacts.clear();
        }
        if (hasAny) {
            fireChange();
            firePropertyChange();
        }
        EmbedderFactory.getProjectEmbedder().lookupComponent(PluginArtifactsCache.class).flush(); // helps with #195440
    }
    
    @Override public int compare(ProblemReport o1, ProblemReport o2) {
        int ret = o1.getSeverityLevel() - o2.getSeverityLevel();
        if (ret != 0) {
            return ret;
        }
        return o1.hashCode() - o2.hashCode();
    }
    
    public static Action createOpenFileAction(FileObject fo) {
        return new OpenActions(fo);
    }
    
    private static class OpenActions extends AbstractAction {

        private final FileObject fo;

        @NbBundle.Messages({"TXT_OPEN_FILE=Open File",
            "ACT_OPEN_FILE_START=Affected file was opened."
        })
        OpenActions(FileObject file) {
            putValue(Action.NAME, TXT_OPEN_FILE());
            putValue(ProblemReporterImpl.ACT_START_MESSAGE, ACT_OPEN_FILE_START());
            fo = file;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    //---------------------------------------
    //projectproblem provider related methods

    private final PropertyChangeSupport chs = new PropertyChangeSupport(this);
    //constant for action.getValue() holding the text to show to users..
    public static final String ACT_START_MESSAGE = "START_MESSAGE";
    
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        chs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        chs.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        for (ProblemReport pr : getReports()) {
            ProjectProblemResolver res = new MavenProblemResolver(pr.getCorrectiveAction(), pr.getId() + "|" + this.nbproject.getPOMFile());
            ProjectProblem pp = pr.getSeverityLevel() == ProblemReport.SEVERITY_HIGH ? 
                    ProjectProblem.createError(pr.getShortDescription(), pr.getLongDescription(), res) :
                    ProjectProblem.createWarning(pr.getShortDescription(), pr.getLongDescription(), res);
            toRet.add(pp);
        }
        return toRet;
    }

    
    private void firePropertyChange() {
        chs.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }

    public static class MavenProblemResolver implements ProjectProblemResolver {
        private final Action action;
        private final String id;

        public MavenProblemResolver(Action correctiveAction, String id) {
            this.action = correctiveAction;
            this.id = id;
        }

        @Override
        @Messages("TXT_No_Res=No resolution for the problem")
        public Future<ProjectProblemsProvider.Result> resolve() {
            FutureTask<Result> toRet = new FutureTask<ProjectProblemsProvider.Result>(new Callable<ProjectProblemsProvider.Result>() {

                                   @Override
                                   public ProjectProblemsProvider.Result call() throws Exception {
                                       if (action != null) {
                                            SwingUtilities.invokeAndWait(new Runnable() {
                                                @Override
                                                public void run() {
                                                    action.actionPerformed(null);
                                                }
                                            });
                                           String text = (String) action.getValue(ACT_START_MESSAGE);
                                           if (text != null) {
                                               return ProjectProblemsProvider.Result.create(Status.RESOLVED, text);
                                           } else {
                                               return ProjectProblemsProvider.Result.create(Status.RESOLVED);
                                           }
                                       } else {
                                           return ProjectProblemsProvider.Result.create(Status.UNRESOLVED, TXT_No_Res());
                                       }
                                       
                                   }
                               });
            RP.post(toRet);
            return toRet;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MavenProblemResolver other = (MavenProblemResolver) obj;
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            return true;
        }
        
        
    }
    
}
