package org.netbeans.modules.python.project2.ui.customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CompositePanelProviderImpl implements CompositeCategoryProvider {

    private static final String PYTHON_PATH = "PythonPath";  //NOI18N
    private static final String RUN = "Run"; // NOI18N

    private final String name;

    public CompositePanelProviderImpl(String name) {
        this.name = name;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final ProjectCustomizer.Category[] categories = null;
        if (RUN.equals(name)) {
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
        PythonProject2 project = context.lookup(PythonProject2.class);
        if (RUN.equals(nm)) {
            return new CustomizerRun(project);
        } else if (PYTHON_PATH.equals(nm)) {
            return new CustomizerPythonPath(project);
        }
        return new JPanel();
    }

    public static CompositePanelProviderImpl createRunConfig() {
        return new CompositePanelProviderImpl(RUN);
    }

    public static CompositePanelProviderImpl createPythonPath() {
        return new CompositePanelProviderImpl(PYTHON_PATH);
    }

}
