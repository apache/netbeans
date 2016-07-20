package mockproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.HELPER_CALLBACK;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.LOG;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_NS;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_XML_PATH;
import org.netbeans.modules.project.ant.ProjectXMLCatalogReader;
import org.netbeans.modules.project.ant.ProjectXMLKnownChecksums;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProjectGetter extends NbTestCase {
    
    public final static JavaProjectGetter INSTANCE = new JavaProjectGetter();
    private Project project;
    
    private JavaProjectGetter(){
        super("JavaProjectGetter");
    }
    
    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }

    }
    
    private FileObject getUnzippedFolder(String path) throws IOException {
        assertNotNull(path);
        File archiveFile = new File(path);
        
        FileObject destFileObject = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObject);
        assertTrue(destFileObject.isValid());
        
        String folderName = null;
        String[] pathParts = path.split("\\\\");
        folderName = pathParts[pathParts.length-1].replace(".zip", "");
        
        FileObject unzippedFolder = destFileObject.getFileObject(folderName);
        
        assertNotNull(unzippedFolder);
        
        return unzippedFolder;
    }
    
    private void getKtHomeForTests() throws IOException {
        FileObject kotlincFolder = getUnzippedFolder(".\\src\\main\\resources\\org\\black\\kotlin\\kotlinc\\kotlinc.zip");
        
        ProjectUtils.KT_HOME = kotlincFolder.getPath() + ProjectUtils.FILE_SEPARATOR;
        
    }
    
    private void createProject() throws IOException {
        getKtHomeForTests();
        FileObject testApp = getUnzippedFolder(".\\src\\test\\resources\\projForTest.zip");
        AntBasedProjectFactorySingleton singleton = new AntBasedProjectFactorySingleton();
        project = singleton.loadProject(testApp, new DummyProjectState());
//        project = ProjectManager.getDefault().findProject(testApp);

        
        OpenProjects.getDefault().open(new Project[]{project}, false);
    }

    public Project getProject() {
        if (project == null) {
            MockServices.setServices(J2SEProjectFactory.class);
            MockServices.setServices(MockOpenProjectsTrampoline.class);
            MockServices.setServices(AntBasedProjectFactorySingleton.class);
            MockServices.setServices(org.netbeans.modules.project.ant.StandardAntArtifactQueryImpl.class);
            MockServices.setServices(TestEnvironmentFactory.class);
            MockServices.setServices(MockKotlinParserFactory.class);
            MockServices.setServices(MockActiveDocumentProvider.class);
            try {
                createProject();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return project;
    }
    
    public static class J2SEProjectFactory implements ProjectFactory{

        @Override
        public boolean isProject(FileObject fo) {
            return true;
        }

        @Override
        public Project loadProject(FileObject fo, ProjectState ps) throws IOException {
            AntBasedProjectType t = new AntBasedProjectType(){
                @Override
                public String getType() {
                    return "org-netbeans-modules-java-j2seproject";
                }

                @Override
                public Project createProject(AntProjectHelper aph) throws IOException {
                    return new J2SEProject(aph);
                }

                @Override
                public String getPrimaryConfigurationDataElementName(boolean bln) {
                    return "";
                }

                @Override
                public String getPrimaryConfigurationDataElementNamespace(boolean bln) {
                    return "";
                }
            };
            File projectDiskFile = FileUtil.toFile(fo);
            assertNotNull(projectDiskFile);

            Document projectX = loadProjectXml(projectDiskFile);
            assertNotNull(projectX);

            Element typeEl = XMLUtil.findElement(projectX.getDocumentElement(), "type", PROJECT_NS);
            assertNotNull(projectX);

            String type = XMLUtil.findText(typeEl);
            assertNotNull(type);
            
            AntProjectHelper helper = HELPER_CALLBACK.createHelper(fo, projectX, new DummyProjectState(), t);
            
            return new J2SEProject(helper);
        }

        @Override
        public void saveProject(Project prjct) throws IOException, ClassCastException {
        }
        
        private Document loadProjectXml(File projectDiskFile) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = new FileInputStream(projectDiskFile);
            try {
                FileUtil.copy(is, baos);
            } finally {
                is.close();
            }
            byte[] data = baos.toByteArray();
            InputSource src = new InputSource(new ByteArrayInputStream(data));
            src.setSystemId(BaseUtilities.toURI(projectDiskFile).toString());
            try {
    //            Document projectXml = XMLUtil.parse(src, false, true, Util.defaultErrorHandler(), null);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder;
                try {
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException x) {
                    throw new SAXException(x);
                }
                builder.setErrorHandler(XMLUtil.defaultErrorHandler());
                Document projectXml = builder.parse(src);
                LOG.finer("parsed document");
    //            dumpFields(projectXml);
                Element projectEl = projectXml.getDocumentElement();
                LOG.finer("got document element");
    //            dumpFields(projectXml);
    //            dumpFields(projectEl);
                String namespace = projectEl.getNamespaceURI();
                LOG.log(Level.FINER, "got namespace {0}", namespace);
                if (!PROJECT_NS.equals(namespace)) {
                    LOG.log(Level.FINE, "{0} had wrong root element namespace {1} when parsed from {2}",
                            new Object[] {projectDiskFile, namespace, baos});
    //                dumpFields(projectXml);
    //                dumpFields(projectEl);
                    return null;
                }
                if (!"project".equals(projectEl.getLocalName())) { // NOI18N
                    LOG.log(Level.FINE, "{0} had wrong root element name {1} when parsed from {2}",
                            new Object[] {projectDiskFile, projectEl.getLocalName(), baos});
                    return null;
                }
                ProjectXMLKnownChecksums checksums = new ProjectXMLKnownChecksums();
                if (!checksums.check(data)) {
                    LOG.log(Level.FINE, "Validating: {0}", projectDiskFile);
                    try {
                        ProjectXMLCatalogReader.validate(projectEl);
                        checksums.save();
                    } catch (SAXException x) {
                        Element corrected = ProjectXMLCatalogReader.autocorrect(projectEl, x);
                        if (corrected != null) {
                            projectXml.replaceChild(corrected, projectEl);
                            // Try to correct on disk if possible.
                            // (If not, any changes from the IDE will write out a corrected file anyway.)
                            if (projectDiskFile.canWrite()) {
                                OutputStream os = new FileOutputStream(projectDiskFile);
                                try {
                                    XMLUtil.write(projectXml, os, "UTF-8");
                                } finally {
                                    os.close();
                                }
                            }
                        } else {
                            throw x;
                        }
                    }
                }
                return projectXml;
            } catch (SAXException e) {
                IOException ioe = new IOException(projectDiskFile + ": " + e, e);
                String msg = e.getMessage().
                        // org/apache/xerces/impl/msg/XMLSchemaMessages.properties validation (3.X.4)
                        replaceFirst("^cvc-[^:]+: ", ""). // NOI18N
                        replaceAll("http://www.netbeans.org/ns/", ".../"); // NOI18N
                Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(AntBasedProjectFactorySingleton.class,
                                                                            "AntBasedProjectFactorySingleton.parseError",
                                                                            projectDiskFile.getName(), msg));
                throw ioe;
            }
        }
    }
    
    public static class DummyProjectState implements ProjectState{

        @Override
        public void markModified() {
        }

        @Override
        public void notifyDeleted() throws IllegalStateException {
        }
        
    }
    
}
