/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

public class PythonSources implements Sources, ChangeListener, PropertyChangeListener {

    private final Project project;
    private final ChangeSupport changeSupport;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Sources delegate;
    private boolean dirty;

    public PythonSources(final Project project, final AntProjectHelper helper, final PropertyEvaluator eval,
            final SourceRoots sources, final SourceRoots tests) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert sources != null;
        assert tests != null;
        this.project = project;
        this.helper = helper;
        this.evaluator = eval;
        this.sourceRoots = sources;
        this.testRoots = tests;
        this.changeSupport = new ChangeSupport(this);
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);        
        this.evaluator.addPropertyChangeListener(this);
        delegate = initSources();
    }

    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            @Override
            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (PythonSources.this) {
                    if (dirty) {
                        delegate.removeChangeListener(PythonSources.this);
                        delegate = initSources();
                        delegate.addChangeListener(PythonSources.this);
                        dirty = false;
                    }
                    _delegate = delegate;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);                
                return groups;
            }
        });
    }    
    
    private Sources initSources() {
        SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH
        register(sourcesHelper, sourceRoots);
        register(sourcesHelper, testRoots);
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return sourcesHelper.createSources();
    }

    private void register(SourcesHelper sourcesHelper, SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            sourcesHelper.addPrincipalSourceRoot(loc, displayName, null, null); // NOI18N
            sourcesHelper.addTypedSourceRoot(loc, PythonProjectType.SOURCES_TYPE_PYTHON, displayName, null, null);
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        synchronized (this) {
            dirty = true;
        }
        changeSupport.fireChange();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName)) {
            this.fireChange();
        }
    }
    
    @Override
    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
