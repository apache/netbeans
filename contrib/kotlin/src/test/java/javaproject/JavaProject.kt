package javaproject

import javaproject.mockservices.MockActiveDocumentProvider
import javaproject.mockservices.MockEditorMimeTypesImpl
import javaproject.mockservices.MockKotlinParserFactory
import javaproject.mockservices.MockOpenProjectsTrampoline
import javaproject.mockservices.TestEnvironmentFactory
import org.netbeans.api.java.platform.JavaPlatformManager
import org.netbeans.api.project.Project
import org.netbeans.api.project.ProjectManager
import org.netbeans.api.project.ui.OpenProjects
import org.netbeans.junit.MockServices
import org.netbeans.junit.NbTestCase
import org.netbeans.modules.java.source.parsing.JavacParserFactory
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton

/**
 *
 * @author Alexander.Baratynski
 */
object JavaProject : NbTestCase("Java project") {
    val javaProject: Project

    init {
        createMockLookup()
        javaProject = createJavaProject()
        OpenProjects.getDefault().open(arrayOf<Project>(javaProject), false)
    }

    private fun createJavaProject(): Project {
        return ProjectManager.getDefault().findProject(JavaProjectUnzipper.INSTANCE.getTestProject())
    }

    private fun createMockLookup() {
        MockServices.setServices(JavaAntBasedProjectType::class.java,
                AntBasedProjectFactorySingleton::class.java,
                org.netbeans.modules.project.ant.StandardAntArtifactQueryImpl::class.java,
                TestEnvironmentFactory::class.java,
                MockKotlinParserFactory::class.java,
                MockActiveDocumentProvider::class.java,
                MockOpenProjectsTrampoline::class.java,
                JavaPlatformManager::class.java,
                JavacParserFactory::class.java,
                MockEditorMimeTypesImpl::class.java)
    }
}