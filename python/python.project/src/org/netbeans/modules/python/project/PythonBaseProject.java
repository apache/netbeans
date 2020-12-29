/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.python.api.PythonPlatformProvider;
import org.netbeans.modules.python.project.gsf.ClassPathProviderImpl;
import org.netbeans.modules.python.project.ui.customizer.PythonCustomizerProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
//import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistry;
import org.netbeans.modules.python.editor.codecoverage.PythonCoverageProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

//public class PythonBaseProject implements Project {
//
//    private static final ImageIcon PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/python/project/resources/py_25_16.png", false);
//  
//    protected AntProjectHelper helper;
//    protected  UpdateHelper updateHelper;
//    protected  LogicalViewProvider logicalView = new PythonLogicalView(this);
//    protected  SourceRoots sourceRoots;
//    protected  SourceRoots testRoots;
//    protected  Lookup lkp;
//    protected  PropertyEvaluator evaluator;
//    protected  ReferenceHelper refHelper;
//    protected  AuxiliaryConfiguration aux;
//
//    public PythonBaseProject(final AntProjectHelper helper) {
//        assert helper != null;
//        this.helper = helper;
//        this.updateHelper = new UpdateHelper(UpdateImplementation.NULL,helper);
//        this.evaluator = createEvaluator();
//        this.aux  = helper.createAuxiliaryConfiguration();
//        refHelper = new ReferenceHelper(helper, aux, evaluator);
//        this.sourceRoots = SourceRoots.create(updateHelper, evaluator, refHelper, false);
//        this.testRoots = SourceRoots.create(updateHelper, evaluator, refHelper, true);
//        this.lkp = createLookup();
//    }
//public PythonBaseProject()
//{
//
//}
//    public FileObject getProjectDirectory() {
//        return helper.getProjectDirectory();
//    }
//
//    public PropertyEvaluator createEvaluator() {
//        PropertyEvaluator privateProps = PropertyUtils.sequentialPropertyEvaluator(
//                helper.getStockPropertyPreprovider(),
//                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
//        return PropertyUtils.sequentialPropertyEvaluator(
//                helper.getStockPropertyPreprovider(),
//                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
//                PropertyUtils.userPropertiesProvider(privateProps,
//                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
//                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
//    }
//
//    private Lookup createLookup () {
//        return Lookups.fixed(new Object[]{
//                this, //project spec requires a project be in it's own lookup
//                aux,  //Auxiliary configuartion to store bookmarks and so on
//                new PythonActionProvider(this), //Provides Standard like build and cleen
//                new ClassPathProviderImpl(this),
//                new Info(), // Project information Implementation
//                logicalView, // Logical view if project implementation
//                new PythonOpenedHook(), //Called by project framework when project is opened (closed)
//                new PythonProjectXmlSavedHook(),  //Called when project.xml changes
//                new PythonSources(helper,evaluator,sourceRoots,testRoots),    //Python source grops - used by package view, factories, refactoring, ...
//                new PythonProjectOperations(this),  //move, rename, copy of project
//                new RecommendedTemplatesImpl(this.updateHelper), // Recommended Templates
//                new PythonCustomizerProvider(this),     //Project custmoizer
//                new PythonProjectFileEncodingQuery(getEvaluator()),     //Provides encoding of the project - used by editor, runtime
//                new PythonSharabilityQuery(helper, getEvaluator(), getSourceRoots(), getTestRoots()),   //Sharabilit info - used by VCS
//                helper.createCacheDirectoryProvider(),  //Cache provider
//                helper.createAuxiliaryProperties(),     // AuxiliaryConfiguraion provider - used by bookmarks, project Preferences, etc
//                new PythonPlatformProvider(getEvaluator()),
//                new PythonCoverageProvider(this)
//            });
//    }
//
//    public Lookup getLookup() {
//        return lkp;
//    }
//
//    public SourceRoots getSourceRoots () {
//        return this.sourceRoots;
//    }
//
//    public SourceRoots getTestRoots () {
//        return this.testRoots;
//    }
//
//    public FileObject[] getSourceRootFiles() {
//        return getSourceRoots().getRoots();
//    }
//
//    public FileObject[] getTestSourceRootFiles() {
//        return getTestRoots().getRoots();
//    }
//
//    public PropertyEvaluator getEvaluator () {
//        return evaluator;
//    }
//
//    AntProjectHelper getHelper () {
//        return this.helper;
//    }
//
//    public FileObject getSrcFolder() {
//        return getProjectDirectory();
//    }
//
//   public String getName () {
//        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
//            public String run() {
//                Element data = getHelper().getPrimaryConfigurationData(true);
//                NodeList nl = data.getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
//                if (nl.getLength() == 1) {
//                    nl = nl.item(0).getChildNodes();
//                    if (nl.getLength() == 1
//                            && nl.item(0).getNodeType() == Node.TEXT_NODE) {
//                        return ((Text) nl.item(0)).getNodeValue();
//                    }
//                }
//                return "???"; // NOI18N
//            }
//        });
//    }
//
//    void setName(final String name) {
//        ProjectManager.mutex().writeAccess(new Runnable() {
//            public void run() {
//                Element data = getHelper().getPrimaryConfigurationData(true);
//                NodeList nl = data.getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
//                Element nameEl;
//                if (nl.getLength() == 1) {
//                    nameEl = (Element) nl.item(0);
//                    NodeList deadKids = nameEl.getChildNodes();
//                    while (deadKids.getLength() > 0) {
//                        nameEl.removeChild(deadKids.item(0));
//                    }
//                } else {
//                    nameEl = data.getOwnerDocument().createElementNS(
//                            PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
//                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
//                }
//                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
//                getHelper().putPrimaryConfigurationData(data, true);
//            }
//        });
//    }
//
//    private final class Info implements ProjectInformation{
//
//        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
//
//        public void addPropertyChangeListener(PropertyChangeListener  listener) {
//            propertyChangeSupport.addPropertyChangeListener(listener);
//        }
//
//        public void removePropertyChangeListener(PropertyChangeListener listener) {
//            propertyChangeSupport.removePropertyChangeListener(listener);
//        }
//
//        public String getDisplayName() {
//            return getName();
//        }
//
//        public Icon getIcon() {
//            return PROJECT_ICON;
//        }
//
//        public String getName() {
//            return PythonBaseProject.this.getName();
//        }
//
//        public Project getProject() {
//            return PythonBaseProject.this;
//        }
//
//        void firePropertyChange(String prop) {
//            propertyChangeSupport.firePropertyChange(prop , null, null);
//        }
//    }
//
//    public final class PythonOpenedHook extends ProjectOpenedHook {
//        protected void projectOpened() {
//            // register project's classpaths to GlobalPathRegistry
//            final ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
//            assert cpProvider != null;
//            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
//            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
//
//            // Ensure that code coverage is initialized in case it's enabled...
//            PythonCoverageProvider codeCoverage = getLookup().lookup(PythonCoverageProvider.class);
//            if (codeCoverage.isEnabled()) {
//                codeCoverage.notifyProjectOpened();
//            }
//        }
//
//        protected void projectClosed() {
//            // unregister project's classpaths to GlobalPathRegistry
//            final ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
//            assert cpProvider != null;
//            //GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
//            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
//            try {
//                ProjectManager.getDefault().saveProject(PythonBaseProject.this);
//            } catch (IOException e) {
//                Exceptions.printStackTrace(e);
//            }
//        }
//    }
//
//    public final class PythonProjectXmlSavedHook extends ProjectXmlSavedHook {
//
//       public PythonProjectXmlSavedHook() {}
//
//        protected void projectXmlSaved() throws IOException {
//            Info info = getLookup().lookup(Info.class);
//            assert info != null;
//            info.firePropertyChange(ProjectInformation.PROP_NAME);
//            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
//        }
//    }
//    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
//
//        RecommendedTemplatesImpl (UpdateHelper helper) {
//            this.helper = helper;
//        }
//
//        private final UpdateHelper helper;
//
//        // List of primarily supported templates
//
//        private static final String[] APPLICATION_TYPES = new String[] {
//            "python",         // NOI18N
//            "XML",                  // NOI18N
//            "simple-files"          // NOI18N
//        };
//
//        private static final String[] PRIVILEGED_NAMES = new String[] {
//            "Templates/Python/_package", // NOI18N
//            "Templates/Python/_module.py", //NOI18N
//            "Templates/Python/_main.py", // NOI18N
//            "Templates/Python/_empty_module.py", // NOI18N
//            "Templates/Python/_test.py", // NOI18N
//        };
//
//        public String[] getRecommendedTypes() {
//            return APPLICATION_TYPES;
//        }
//
//        public String[] getPrivilegedTemplates() {
//            return PRIVILEGED_NAMES;
//        }
//
//    }
//}
