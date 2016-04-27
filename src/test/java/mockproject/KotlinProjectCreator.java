package mockproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.black.kotlin.project.KotlinProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.HELPER_CALLBACK;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.LOG;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_NS;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_XML_PATH;
import org.netbeans.modules.project.ant.ProjectXMLCatalogReader;
import org.netbeans.modules.project.ant.ProjectXMLKnownChecksums;
import org.netbeans.modules.projectapi.nb.NbProjectManager;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Александр
 */
public class KotlinProjectCreator extends NbTestCase {

    public static KotlinProjectCreator INSTANCE
            = new KotlinProjectCreator();

    private KotlinProject project;

    private KotlinProjectCreator() {
        super("Project creator");
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

    private void createProject() throws IOException {
        String zipPath = ".\\src\\test\\resources\\projForTest.zip";
        assertNotNull(zipPath);
        File archiveFile = new File(zipPath);

        FileObject destFileObj = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObj);
        assertTrue(destFileObj.isValid());
        FileObject testApp = destFileObj.getFileObject("projForTest");
        
        project = doABPFS();
        
//        FileObject projectXml = testApp.getFileObject("nbproject").getFileObject("project.xml");
//        if (projectXml != null){
//            projectXml.delete();
//        }
//        AntProjectHelper helper = ProjectGenerator.createProject(testApp,
//                "org.black.kotlin.project.KotlinProject");
//        
//        assertNotNull(helper);

//        Project proj = ProjectManager.getDefault().findProject(testApp);
//        assertNotNull(proj);
        OpenProjects.getDefault().open(new Project[]{project}, false);
    }

    public KotlinProject getProject() {
        if (project == null) {
//            MockServices.setServices(MockAntBasedProjectType.class);
            MockServices.setServices(MockOpenProjectsTrampoline.class);
            MockServices.setServices(AntBasedProjectFactorySingleton.class);
            MockServices.setServices(org.netbeans.modules.project.ant.StandardAntArtifactQueryImpl.class);
            try {
                createProject();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return project;
    }

    private KotlinProject doABPFS() throws IOException{
        File file = new File("C:\\Users\\Александр\\Documents\\NetBeansProjects\\kotlin\\Kotlin\\src\\test\\resources\\projForTest");
        FileObject app = FileUtil.toFileObject(file);
        
        FileObject projectFile = app.getFileObject(PROJECT_XML_PATH);
        assertNotNull(projectFile);
        
        File projectDiskFile = FileUtil.toFile(projectFile);
        assertNotNull(projectDiskFile);
        
        Document projectX = loadProjectXml(projectDiskFile);
        assertNotNull(projectX);
        
        Element typeEl = XMLUtil.findElement(projectX.getDocumentElement(), "type", PROJECT_NS);
        assertNotNull(projectX);
        
        String type = XMLUtil.findText(typeEl);
        assertNotNull(type);
        
        AntBasedProjectType t = new MockAntBasedProjectType();
        assertNotNull(t);
        
        AntProjectHelper helper = HELPER_CALLBACK.createHelper(app, projectX, new DummyProjectState(), t);
        assertNotNull(helper);
        
        KotlinProject kotProj = new KotlinProject(helper);
        assertNotNull(kotProj);
        
        return kotProj;
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
    
    public class DummyProjectState implements ProjectState{

        @Override
        public void markModified() {
        }

        @Override
        public void notifyDeleted() throws IllegalStateException {
        }
        
    }
    
}
