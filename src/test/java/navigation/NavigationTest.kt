package navigation

import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.navigation.netbeans.KotlinHyperlinkProvider
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType
import org.openide.filesystems.FileObject
import utils.*

/**
 *
 * @author Alexander.Baratynski
 */
class NavigationTest : NbTestCase("Navigation test") {
    private val project: Project
    private val navigationDir: FileObject
    private val hyperlinkProvider: KotlinHyperlinkProvider

    init {
        project = JavaProject.javaProject
        navigationDir = project.projectDirectory.getFileObject("src").getFileObject("navigation")
        hyperlinkProvider = KotlinHyperlinkProvider()
    }

    private fun doTest(fromName: String, toName: String = fromName) {
        val from = getDocumentForFileObject(navigationDir, fromName)
        assertNotNull(from)
        val caret = getCaret(from)
        assertNotNull(caret)
        val offset = caret + "<caret>".length + 1
        
        hyperlinkProvider.isHyperlinkPoint(from, offset, HyperlinkType.GO_TO_DECLARATION)
        hyperlinkProvider.performClickAction(from, offset, HyperlinkType.GO_TO_DECLARATION)
        val navigationData = hyperlinkProvider.navigationCache
        assertNotNull(navigationData)
        
        val to = navigationData.first
        val toFO = ProjectUtils.getFileObjectForDocument(to)
        assertNotNull(toFO)
        
        val expectedFO = navigationDir.getFileObject(toName)
        assertEquals(expectedFO.path, toFO.path)
        
        val expectedOffset = getCaret(getDocumentForFileObject(navigationDir, toName.replace(".kt", ".caret")))
        assertEquals(expectedOffset, navigationData.second)
    }

    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(navigationDir)
    }

    fun tstNavigationToFunction() = doTest("checkNavigationToFunction.kt", "functionToNavigate.kt")

    fun tstNavigationToClass() = doTest("checkNavigationToClass.kt", "KotlinClass.kt")

    fun tstNavigationToVariable() = doTest("checkNavigationToVariable.kt")
    
}