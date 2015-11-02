package org.black.kotlin.highlighter;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;

import org.black.kotlin.highlighter.netbeans.KotlinCharStream;
import org.black.kotlin.highlighter.netbeans.KotlinToken;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.model.KotlinLightVirtualFile;
import org.black.kotlin.project.KotlinProjectConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.JetLanguage;
import org.jetbrains.kotlin.psi.JetFile;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinTokenScanner {

    private final KotlinTokensFactory kotlinTokensFactory;

    private JetFile jetFile = null;
    private int offset = 0;
    private int rangeEnd = 0;
    private PsiElement lastElement = null;
    private List<KotlinToken> kotlinTokens = null;
    private int tokensNumber = 0;
    
    
    public KotlinTokenScanner(){
        kotlinTokensFactory = new KotlinTokensFactory();
//        try {
//            jetFile = parseFile(getOpenedFile());
//            this.rangeEnd = jetFile.getTextLength();
//            createListOfKotlinTokens();
//
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    private FileObject getOpenedFile(){
        JTextComponent ed = EditorRegistry.lastFocusedComponent();
        OpenProjects op = OpenProjects.getDefault();
        Sources sources = org.netbeans.api.project.ProjectUtils.getSources(op.getOpenProjects()[0]);
        for (SourceGroup srcGrp : sources.getSourceGroups(KotlinProjectConstants.KOTLIN_SOURCE.toString())) {
               for (FileObject ktFile : srcGrp.getRootFolder().getChildren()){
                   try {
                       if (ktFile.asText().equals(ed.getText())){
                           return ktFile;
                       }
                   } catch (IOException ex) {
                       Exceptions.printStackTrace(ex);
                   }
               }
            
        }
        return null;
    }
    
    private void createListOfKotlinTokens() {
        kotlinTokens = new ArrayList();
        for (;;) {

            if (lastElement != null) {
                if (lastElement.getTextOffset() > rangeEnd) {
                    kotlinTokens.add(new KotlinToken(
                            new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 6), "",
                            lastElement.getTextOffset(), TokenType.EOF));
                    tokensNumber = kotlinTokens.size();
                    break;
                }
            }

            if (jetFile == null) {
                kotlinTokens.add(new KotlinToken(
                        new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 6), "",
                        0, TokenType.EOF));
                tokensNumber = kotlinTokens.size();
                break;
            }

            lastElement = jetFile.findElementAt(offset);
            if (lastElement != null) {
                offset = lastElement.getTextRange().getEndOffset();
                TokenType tokenType = kotlinTokensFactory.getToken(lastElement);

                kotlinTokens.add(new KotlinToken(
                        new KotlinTokenId(tokenType.name(), tokenType.name(), tokenType.getId(tokenType)),
                        lastElement.getText(), lastElement.getTextOffset(),
                        tokenType));

            }
        }
    }
    
    
    @Nullable
    private JetFile parseFile(@NotNull FileObject file) throws IOException{
        File ioFile = new File(file.getPath());
        return parseText(FileUtil.loadFile(ioFile, null, true),file);
    }
    
    @Nullable
    private JetFile parseText(@NotNull String text, @NotNull FileObject file){
        StringUtil.assertValidSeparators(text);
        
        Project project = KotlinEnvironment.getEnvironment(
            OpenProjects.getDefault().getOpenProjects()[0]).getProject();
        
        LightVirtualFile virtualFile = new KotlinLightVirtualFile(file,text);
        virtualFile.setCharset(CharsetToolkit.UTF8_CHARSET);
        
        PsiFileFactoryImpl psiFileFactory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);
        
        return (JetFile) psiFileFactory.trySetupPsiForFile(virtualFile, JetLanguage.INSTANCE, true, false);
    }
    
    
        
    public JetFile createJetFileFromCode(String code) {
        OpenProjects op = OpenProjects.getDefault();
        
//         DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                Message(op.getDefault().getMainProject().getProjectDirectory().getName()));
        
        KotlinEnvironment kotlinEnvironment = KotlinEnvironment.getEnvironment(op.getOpenProjects()[0]);
        Document jetDocument = new DocumentImpl(code);
        PsiFile psiFile = PsiDocumentManager.getInstance(kotlinEnvironment.getProject()).getPsiFile(jetDocument);
        FileViewProvider viewProvider = psiFile.getViewProvider();
        return new JetFile(viewProvider, false);
    }

    public KotlinToken<KotlinTokenId> getNextToken() {

//        KotlinToken ktToken = null;
//        
//        if (tokensNumber > 0){
//            ktToken = kotlinTokens.get(kotlinTokens.size()-tokensNumber);
//            tokensNumber--;
//        }
//            
//        return ktToken;
        
        return new KotlinToken(
                        new KotlinTokenId(TokenType.KEYWORD.name(), TokenType.KEYWORD.name(), TokenType.KEYWORD.getId(TokenType.KEYWORD)),
                        "", 0,
                        TokenType.KEYWORD);
    }

}
