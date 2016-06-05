package org.black.kotlin.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Class for ant-based Kotlin project.
 *
 * @author Александр
 */
@AntBasedProjectRegistration(type = "org.black.kotlin.project.KotlinProject",
        iconResource = "org/black/kotlin/kotlin.png",
        sharedName = "data",
        sharedNamespace = "http://www.netbeans.org/ns/kotlin-project/1",
        privateName = "project-private",
        privateNamespace = "http://www.netbeans.org/ns/kotlin-project-private/1")
public class KotlinProject implements Project {

    private final AntProjectHelper helper;
    private final KotlinSources kotlinSources;
    private final GlobalPathRegistry pathRegistry = GlobalPathRegistry.getDefault();
    private final AuxiliaryConfiguration auxiliaryConfig;
    private final PropertyEvaluator propertyEvaluator;
    private final ReferenceHelper referenceHelper;
    private AntBuildExtender buildExtender;
    private Lookup lkp;
    
    public KotlinProject(AntProjectHelper helper) {
        this.helper = helper;
        kotlinSources = new KotlinSources(this);
        propertyEvaluator = createEvaluator();
        auxiliaryConfig = helper.createAuxiliaryConfiguration();
        referenceHelper = new ReferenceHelper(helper, auxiliaryConfig, propertyEvaluator);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new KotlinExtenderImplementation(),
                referenceHelper);
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(
                    KotlinProject.this,
                    auxiliaryConfig,
                    helper.createAuxiliaryProperties(),
                    helper.createCacheDirectoryProvider(),
                    buildExtender,
                    new KotlinProjectInfo(this),
                    new KotlinProjectLogicalView(this),
                    new KotlinActionProvider(this),
                    new KotlinPrivilegedTemplates(),
                    new KotlinProjectOpenedHook(this),
                    new KotlinClassPathProvider(this)
            );
        }
        return lkp;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    public PropertyEvaluator getPropertEvaluator(){
        return propertyEvaluator;
    }
    
    public AuxiliaryConfiguration getAuxiliaryConfiguration(){
        return auxiliaryConfig;
    }
    
    public ReferenceHelper getReferenceHelper(){
        return referenceHelper;
    }
    
    public GlobalPathRegistry getPathRegistry() {
        return pathRegistry;
    }

    public KotlinSources getKotlinSources() {
        return kotlinSources;
    }
    
    private PropertyEvaluator createEvaluator(){
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(), 
                helper.getPropertyProvider("nbproject/private/config.properties"));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(), 
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        ConfigPropertyProvider configPropertyProvider1 = 
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper);
        baseEval1.addPropertyChangeListener(configPropertyProvider1);
        ConfigPropertyProvider configPropertyProvider2 = new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper); // NOI18N
        baseEval1.addPropertyChangeListener(configPropertyProvider2);
        
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider("nbproject/private/config.properties"),
                configPropertyProvider1,
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2, "user.properties.file", FileUtil.toFile(getProjectDirectory())),
                configPropertyProvider2,
helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }
    
    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener{
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper){
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent event){
            if (event.getPropertyName().equals("config")){
                setDelegate(computeDelegate(baseEval,prefix,helper));
            }
        }
        
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, 
                String prefix, AntProjectHelper helper){
            String config = baseEval.getProperty("config");
            if (config != null){
                return helper.getPropertyProvider(prefix + "/" + config + ".properties");
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
    }
    
    private static final class KotlinPrivilegedTemplates implements PrivilegedTemplates {

        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/Kotlin/content.kt",
            "Templates/Classes/Class.java",
            "Templates/Classes/Interface.java",
            "Templates/Classes/Package"
        };

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

    }
    
    private class KotlinExtenderImplementation implements AntBuildExtenderImplementation {
        
        @Override
        public List<String> getExtensibleTargets() {
            String[] targets = new String[]{
                "-do-init", "-init-check", 
                "-post-clean", "jar", 
                "-pre-pre-compile", "-do-compile", 
                "-do-compile-single"
            };
            return Arrays.asList(targets);
        }

        @Override
        public Project getOwningProject() {
            return KotlinProject.this;
        }
    }
}
