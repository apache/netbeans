/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

public class PythonProjectFileEncodingQuery extends FileEncodingQueryImplementation implements PropertyChangeListener {
    
    private final PropertyEvaluator eval;
    private Charset cache;
    
    PythonProjectFileEncodingQuery (final PropertyEvaluator eval) {
        assert eval != null;
        this.eval = eval;
        this.eval.addPropertyChangeListener(this);
    }

    @Override
    public Charset getEncoding(FileObject file) {
        assert file != null;
        synchronized (this) {
            if (cache != null) {
                return cache;
            }
        }
        String enc = eval.getProperty(PythonProjectProperties.SOURCE_ENCODING);
        synchronized (this) {
            if (cache == null) {
                try {
                    cache = enc == null ? Charset.defaultCharset() : Charset.forName(enc);
                } catch (IllegalCharsetNameException exception) {
                    return null;
                }
            }
            return cache;
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {    
        String propName = evt.getPropertyName();
        if (propName == null || propName.equals(PythonProjectProperties.SOURCE_ENCODING)) {
            synchronized (this) {
                cache = null;
            }
        }
    }

}
