/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.util.Mutex;

public class PythonSharabilityQuery implements SharabilityQueryImplementation, PropertyChangeListener  {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sources;
    private final SourceRoots tests;
    private volatile SharabilityQueryImplementation delegate;
    
    PythonSharabilityQuery (final AntProjectHelper helper, final PropertyEvaluator evaluator,
            final SourceRoots sources, final SourceRoots tests) {
        assert helper != null;
        assert evaluator != null;
        assert sources != null;
        assert tests != null;
        
        this.helper = helper;
        this.evaluator = evaluator;
        this.sources = sources;
        this.tests = tests;
        
        this.sources.addPropertyChangeListener(this);
        this.tests.addPropertyChangeListener(this);        
    }

    @Override
    public int getSharability(final File file) {
        assert file != null;
        return ProjectManager.mutex().readAccess(new Mutex.Action<Integer>() {
            @Override
            public Integer run() {
                synchronized (PythonSharabilityQuery.this) {
                    if (delegate == null) {
                        delegate = createDelegate();
                    }
                    return delegate.getSharability(file);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(evt.getPropertyName())) {
            delegate = null;
        }
    }
    
    private SharabilityQueryImplementation createDelegate() {
        String[] srcProps = sources.getRootProperties();
        String[] testProps = tests.getRootProperties();

        int size = srcProps.length;
        size += testProps.length;
        
        List<String> props = new ArrayList<>(size);

        for (String src : srcProps) {
            props.add("${" + src + "}"); // NOI18N
        }
        for (String test : testProps) {
            props.add("${" + test + "}"); // NOI18N
        }

        return helper.createSharabilityQuery(evaluator, props.toArray(new String[props.size()]), new String[0]);
    }

}
