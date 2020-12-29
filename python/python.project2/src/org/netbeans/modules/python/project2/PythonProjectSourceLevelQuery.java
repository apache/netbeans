package org.netbeans.modules.python.project2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.source.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

class PythonProjectSourceLevelQuery implements SourceLevelQueryImplementation {

    private final PythonProject2 project;
    private final Result result;

    PythonProjectSourceLevelQuery(@NonNull final PythonProject2 project) {
        assert project != null;
        this.project = project;
        this.result = new R(project);
    }

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        return this.result;
    }

    @CheckForNull
    static String findSourceLevel (@NonNull final PythonProject2 project) {
        return findValue(project);
    }

    @CheckForNull
    private static String findValue(
            @NonNull final PythonProject2 project) {
        final PythonPlatform activePlatform = project.getActivePlatform();
        return activePlatform.getSourceLevel();
    }

    private class R implements Result, PropertyChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);

        @SuppressWarnings("LeakingThisInConstructor")
        private R(final PythonProject2 project) {
            project.addPropertyChangeListener(WeakListeners.propertyChange(this, project));
        }

        @Override
        public String getSourceLevel() {
            return findSourceLevel(project);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            this.cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            this.cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
//            final String name = evt.getPropertyName();
//            if (name == null ||
//                PLATFORM_ACTIVE.equals(name)) {
                this.cs.fireChange();
//            }
        }

        @Override
        public String toString() {
            final String sl = getSourceLevel();
            return sl == null ? "" : sl; //NOI18M
        }

    }

}
