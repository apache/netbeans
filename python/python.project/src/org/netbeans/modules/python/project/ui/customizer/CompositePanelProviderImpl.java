/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CompositePanelProviderImpl implements CompositeCategoryProvider {
    
    public static final String SOURCES = "Sources"; // NOI18N
    public static final String PYTHON_PATH = "PythonPath";  //NOI18N
    public static final String RUN = "Run"; // NOI18N        

    private final String name;

    public CompositePanelProviderImpl(String name) {
        this.name = name;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final ProjectCustomizer.Category[] categories = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    NbBundle.getMessage(CompositePanelProviderImpl.class, "LBL_Config_Sources"),
                    null,
                    categories);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    NbBundle.getMessage(CompositePanelProviderImpl.class, "LBL_Config_RunConfig"),
                    null,
                    categories);
        } else if (PYTHON_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    PYTHON_PATH,
                    NbBundle.getMessage(CompositePanelProviderImpl.class, "LBL_Config_PhpIncludePath"),
                    null,
                    categories);
        }
        assert toReturn != null : "No category for name: " + name;
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        PythonProjectProperties uiProps = context.lookup(PythonProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (PYTHON_PATH.equals(nm)) {            
            return new CustomizerPythonPath(uiProps);
        }
        return new JPanel();
    }

    public static CompositePanelProviderImpl createSources() {
        return new CompositePanelProviderImpl(SOURCES);
    }

    public static CompositePanelProviderImpl createRunConfig() {
        return new CompositePanelProviderImpl(RUN);
    }
    
    public static CompositePanelProviderImpl createPythonPath() {
        return new CompositePanelProviderImpl(PYTHON_PATH);
    }

}
