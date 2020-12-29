/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.python.source.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

class PythonProjectSourceLevelQuery implements SourceLevelQueryImplementation {
    
    private static final String PLATFORM_ACTIVE = "platform.active";    //NOI18N
    
    private final PropertyEvaluator eval;
    private final Result result;

    PythonProjectSourceLevelQuery(
        @NonNull final PropertyEvaluator eval,
        @NonNull final String platformType) {
        assert eval != null;
        assert platformType != null;
        this.eval = eval;
        this.result = new R();
    }

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        return this.result;
    }

    @CheckForNull
    static String findSourceLevel (
            @NonNull final PropertyEvaluator eval) {
        return findValue(eval);
    }
    
    @CheckForNull
    private static String findValue(
            @NonNull final PropertyEvaluator eval) {
        final String activePlatform = eval.getProperty(PLATFORM_ACTIVE);
        return activePlatform;
    }

    private class R implements Result, PropertyChangeListener {
        
        private final ChangeSupport cs = new ChangeSupport(this);

        @SuppressWarnings("LeakingThisInConstructor")
        private R() {
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }

        @Override
        public String getSourceLevel() {
            return findSourceLevel(eval);
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
            final String name = evt.getPropertyName();
            if (name == null ||
                PLATFORM_ACTIVE.equals(name)) {
                this.cs.fireChange();
            }
        }

        @Override
        public String toString() {
            final String sl = getSourceLevel();
            return sl == null ? "" : sl; //NOI18M
        }

    }

}
