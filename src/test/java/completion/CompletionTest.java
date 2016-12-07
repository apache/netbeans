package completion;

import utils.TestUtilsKt;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.completion.CompletionUtilsKt;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import static junit.framework.TestCase.assertNotNull;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.modules.csl.api.CompletionProposal;

/**
 *
 * @author Alexander.Baratynski
 */
public class CompletionTest extends NbTestCase {

    private final Project project;
    private final FileObject completionDir;

    public CompletionTest() {
        super("Completion test");
        project = JavaProject.INSTANCE.getJavaProject();
        completionDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("completion");
    }

    private void doTest(String fileName, Collection<String> items) {
        Document doc = TestUtilsKt.getDocumentForFileObject(completionDir, fileName);
        Collection<CompletionProposal> completionItems = null;
        List<CharSequence> completions = new ArrayList<CharSequence>();

        Integer caret = TestUtilsKt.getCaret(doc);
        assertNotNull(caret);

        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        KtFile ktFile = null;
        try {
            ktFile = KotlinPsiManager.INSTANCE.getParsedFile(file);

            AnalysisResultWithProvider resultWithProvider
                    = KotlinAnalyzer.INSTANCE.analyzeFile(project, ktFile);
            KotlinParser.setAnalysisResult(ktFile, resultWithProvider);
            
            completionItems = CompletionUtilsKt.createProposals(doc,
                    caret, resultWithProvider, "");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertNotNull(completionItems);

        for (CompletionProposal it : completionItems) {
            completions.add(it.getSortText());
        }

        boolean contains = false;
        if (completions.containsAll(items)) {
            contains = true;
        }

        assertEquals(true, contains);
    }

    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(completionDir);
    }

    @Test
    public void testStringCompletion() {
        doTest("checkStringCompletion.kt", Lists.newArrayList("toString()"));
    }

    @Test
    public void testBasicInt() {
        doTest("checkBasicInt.kt", Lists.newArrayList("Int"));
    }

    @Test
    public void testBasicAny() {
        doTest("checkBasicAny.kt", Lists.newArrayList("Any"));
    }

    @Test
    public void testAutoCastAfterIf() {
        doTest("checkAutoCastAfterIf.kt", Lists.newArrayList("value"));
    }

    @Test
    public void testAutoCastAfterIfMethod() {
        doTest("checkAutoCastAfterIfMethod.kt", Lists.newArrayList("test()"));
    }

    @Test
    public void testAutoCastForThis() {
        doTest("checkAutoCastForThis.kt", Lists.newArrayList("destroy()"));
    }

    @Test
    public void testAutoCastInWhen() {
        doTest("checkAutoCastInWhen.kt", Lists.newArrayList("left", "right"));
    }

    @Test
    public void testCompletionBeforeDotInCall() {
        doTest("checkCompletionBeforeDotInCall.kt",
                Lists.newArrayList("TestSample", "testVar", "testTop()", "testFun()"));
    }

    @Test
    public void testLocalLambda() {
        doTest("checkLocalLambda.kt", Lists.newArrayList("test()"));
    }

    @Test
    public void testCompanion() {
        doTest("checkCompanion.kt", Lists.newArrayList("companionVal", "companionFun()"));
    }

    @Test
    public void testExtendsClass() {
        doTest("checkExtendClass.kt", Lists.newArrayList("MyFirstClass", "MySecondClass"));
    }

    @Test
    public void testImport() {
        doTest("checkImport.kt", Lists.newArrayList("Proxy"));
    }

    @Test
    public void testInCallExpression() {
        doTest("checkInCallExpression.kt", Lists.newArrayList("func()"));
    }

    @Test
    public void testInClassInit() {
        doTest("checkInClassInit.kt", Lists.newArrayList("valExternal", "valInternal"));
    }

    @Test
    public void testInClassPropertyAccessor() {
        doTest("checkInClassPropertyAccessor.kt", Lists.newArrayList("test", "testParam"));
    }

    @Test
    public void testInImport() {
        doTest("checkInImport.kt", Lists.newArrayList("Proxy", "Base"));
    }

    @Test
    public void testInParameterType() {
        doTest("checkInParameterType.kt", Lists.newArrayList("Int"));
    }

}
