package javaproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.LOG;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_NS;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.PROJECT_XML_PATH;
import static org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton.HELPER_CALLBACK;
import org.netbeans.modules.project.ant.ProjectXMLCatalogReader;
import org.netbeans.modules.project.ant.ProjectXMLKnownChecksums;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
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
public class JavaProjectFactory implements ProjectFactory {

    @Override
    public boolean isProject(FileObject fo) {
        return fo.getFileObject("nbproject") != null;
    }

    @Override
    public Project loadProject(FileObject fo, ProjectState ps) throws IOException {
        return new J2SEProject(generateAntProjecthelper(fo, ps));
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {}
    
    private AntProjectHelper generateAntProjecthelper(FileObject app, ProjectState ps) throws IOException {
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
        
        AntBasedProjectType t = new JavaAntBasedProjectType();
        assertNotNull(t);
        
        AntProjectHelper helper = HELPER_CALLBACK.createHelper(app, projectX, ps, t);
        assertNotNull(helper);
        
        return helper;
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
            Element projectEl = projectXml.getDocumentElement();
            LOG.finer("got document element");
            String namespace = projectEl.getNamespaceURI();
            LOG.log(Level.FINER, "got namespace {0}", namespace);
            if (!PROJECT_NS.equals(namespace)) {
                LOG.log(Level.FINE, "{0} had wrong root element namespace {1} when parsed from {2}",
                        new Object[] {projectDiskFile, namespace, baos});
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
                    replaceFirst("^cvc-[^:]+: ", ""). // NOI18N
                    replaceAll("http://www.netbeans.org/ns/", ".../"); // NOI18N
            Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(AntBasedProjectFactorySingleton.class,
                                                                        "AntBasedProjectFactorySingleton.parseError",
                                                                        projectDiskFile.getName(), msg));
            throw ioe;
        }
    }
}
