package org.black.kotlin.highlighter;

import com.intellij.psi.PsiElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.black.kotlin.highlighter.netbeans.KotlinToken;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.model.KotlinLightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFile;
//import org.jetbrains.kotlin.idea.JetLanguage;
//import org.jetbrains.kotlin.psi.JetFile;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.lexer.LexerInput;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Александр
 */
public class KotlinTokenScanner {

    private final KotlinTokensFactory kotlinTokensFactory;

    private KtFile ktFile = null;
    private int offset = 0;
    private int rangeEnd = 0;
    private PsiElement lastElement = null;
    private List<KotlinToken> kotlinTokens = null;
    private int tokensNumber = 0;
    private LexerInput input;
    private File file;

    public KotlinTokenScanner(LexerInput input) {
        kotlinTokensFactory = new KotlinTokensFactory();
        this.input = input;
        file = new File(getOpenedFile());
//        FileObject fileObj = org.openide.filesystems.FileUtil.toFileObject(
//                org.openide.filesystems.FileUtil.normalizeFile(file));
//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(file.getName()));
        try {
            offset = 0;
            ktFile = parseFile(file);
            this.rangeEnd = (int) file.length();
            createListOfKotlinTokens();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String getOpenedFile() {
        TopComponent topActive = TopComponent.getRegistry().getActivated();
        if (WindowManager.getDefault().isOpenedEditorTopComponent(topActive)) {
            String path;
            if (topActive.getToolTipText().split("\\(")[0] != null)
                path = topActive.getToolTipText().split("\\(")[0];
            else
                path = topActive.getToolTipText();
        
            return path;
        }
        return null;
    }

    private void createListOfKotlinTokens() {
        kotlinTokens = new ArrayList();
        for (;;) {

            lastElement = ktFile.findElementAt(offset);

            if (ktFile == null) {
                kotlinTokens.add(new KotlinToken(
                        new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                        0, TokenType.EOF));
                tokensNumber = kotlinTokens.size();
                break;
            }

            if (lastElement != null) {

//                if (lastElement.getTextOffset() > rangeEnd) {
//                    kotlinTokens.add(new KotlinToken(
//                            new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), lastElement.getText(),
//                            lastElement.getTextOffset(), TokenType.EOF));
//                    tokensNumber = kotlinTokens.size();
//                    break;
//                }

                offset = lastElement.getTextRange().getEndOffset();
                TokenType tokenType = kotlinTokensFactory.getToken(lastElement);

                kotlinTokens.add(new KotlinToken(
                        new KotlinTokenId(tokenType.name(), tokenType.name(), tokenType.getId(tokenType)),
                        lastElement.getText(), lastElement.getTextOffset(),
                        tokenType));
                tokensNumber = kotlinTokens.size();
            } else {
                tokensNumber = kotlinTokens.size();
                break;
            }

        }
    }

    @Nullable
    private KtFile parseFile(@NotNull File file) throws IOException {
//        File ioFile = new File(file.getPath());
        return parseText(FileUtil.loadFile(new File(file.getPath()), null, true), file);
    }

    @Nullable
    private KtFile parseText(@NotNull String text, @NotNull File file) {
        StringUtil.assertValidSeparators(text);

        Project project = KotlinEnvironment.getEnvironment(
                OpenProjects.getDefault().getOpenProjects()[0]).getProject();

        LightVirtualFile virtualFile = new KotlinLightVirtualFile(file, text);
        virtualFile.setCharset(CharsetToolkit.UTF8_CHARSET);

        PsiFileFactoryImpl psiFileFactory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);

        return (KtFile) psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false);
    }

    public KotlinToken<KotlinTokenId> getNextToken() {

        KotlinToken ktToken = null;

        if (tokensNumber > 0) {
            ktToken = kotlinTokens.get(kotlinTokens.size() - tokensNumber);
            tokensNumber--;
            if (ktToken != null) {
                int num = ktToken.length();

                while (num > 0) {
                    input.read();
                    num--;
                }

            }
            return ktToken;
        } else {
            input.read();
            return new KotlinToken(
                    new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                    0, TokenType.EOF);
        }

    }

}
