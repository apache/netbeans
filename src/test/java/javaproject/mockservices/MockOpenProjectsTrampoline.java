package javaproject.mockservices;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

public final class MockOpenProjectsTrampoline implements OpenProjectsTrampoline {

    private final Collection<Project> openProjects = new ArrayList<Project>();

    @Override
    public Project[] getOpenProjectsAPI() {
        return openProjects.toArray(new Project[0]);
    }

    @Override
    public void openAPI(Project[] projects, boolean openRequiredProjects, boolean bool) {
        openProjects.addAll(Arrays.asList(projects));
        if (projects.length > 0) {
            mainProject = projects[projects.length - 1];
        }
    }

    @Override
    public void closeAPI(Project[] projects) {
        openProjects.removeAll(Arrays.asList(projects));
    }

    @Override
    public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
    }

    @Override
    public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
    }
    private Project mainProject;

    @Override
    public Project getMainProject() {
        return mainProject;
    }

    @Override
    public void setMainProject(Project mainProject) {
        if (mainProject != null && !openProjects.contains(mainProject)) {
            throw new IllegalArgumentException("Project " + ProjectUtils.getInformation(mainProject).getDisplayName() + " is not open and cannot be set as main.");
        }
        this.mainProject = mainProject;
    }


    @Override
    public Future<Project[]> openProjectsAPI() {
        return null;
    }

    @Override
    public ProjectGroup getActiveProjectGroupAPI() {
        return null;
    }

    @Override
    public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener pl) {
    }

    @Override
    public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener pl) {
    }
}
