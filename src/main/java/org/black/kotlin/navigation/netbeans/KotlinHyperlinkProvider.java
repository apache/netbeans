package org.black.kotlin.navigation.netbeans;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import kotlin.Pair;
import kotlin.sequences.Sequence;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.navigation.NavigationUtil;
import org.black.kotlin.navigation.references.KotlinReference;
import org.black.kotlin.navigation.references.ReferenceUtils;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.resolve.lang.kotlin.NetBeansVirtualFileFinder;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.descriptors.SourceFile;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType = "text/x-kt", service = HyperlinkProvider.class)
public class KotlinHyperlinkProvider implements HyperlinkProvider {

    private KtReferenceExpression referenceExpression;
    
    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        if (ProjectUtils.getKotlinProjectForFileObject(fo) == null){
            return false;
        }
        try {
            referenceExpression = NavigationUtil.getReferenceExpression(doc, offset);
            return referenceExpression != null;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (isHyperlinkPoint(doc,offset)){
            Pair<Integer, Integer> span = NavigationUtil.getSpan();
            if (span == null){
                return null;
            }
            return new int[]{span.getFirst(),span.getSecond()};
        }
        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset) {
        if (referenceExpression == null){
            return;
        }
        
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null){
            return;
        }
        
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        if (project == null){
            return;
        }
        
        NavigationData navigationData = getNavigationData(referenceExpression, project);
        if (navigationData == null){
            gotoKotlinStdlib(referenceExpression, project);
            return;
        }
        
        NavigationUtil.gotoElement(navigationData.getSourceElement(), navigationData.getDeclarationDescriptor(),
                referenceExpression, project, file);
  }
    
    private void gotoKotlinStdlib(KtReferenceExpression referenceExpression, Project project) {
        BindingContext context = KotlinAnalyzer.analyzeFile(project, referenceExpression.getContainingKtFile()).
            getAnalysisResult().getBindingContext();
        List<KotlinReference> refs = ReferenceUtils.createReferences(referenceExpression);
        for (KotlinReference ref : refs){
            Collection<? extends DeclarationDescriptor> descriptors = ref.getTargetDescriptors(context);
            for (DeclarationDescriptor desc : descriptors) {
                //TODO get file name!!!
                VirtualFile virtFile = KotlinEnvironment.getEnvironment(project).
                    getVirtualFileInJar(ProjectUtils.buildLibPath("kotlin-runtime-sources"), "kotlin/io/Console.kt");
            
                if (NavigationUtil.gotoKotlinStdlib(virtFile, desc)) {
                    return;
                }
            }
        }
    }
    
    @Nullable
    private NavigationData getNavigationData(KtReferenceExpression referenceExpression,
            Project project) {
        BindingContext context = KotlinAnalyzer.analyzeFile(project, referenceExpression.getContainingKtFile()).
                getAnalysisResult().getBindingContext();
        List<KotlinReference> refs = ReferenceUtils.createReferences(referenceExpression);
        
        for (KotlinReference ref : refs){
            Collection<? extends DeclarationDescriptor> descriptors = ref.getTargetDescriptors(context);
            for (DeclarationDescriptor descriptor : descriptors){
                SourceElement elementWithSource = NavigationUtil.getElementWithSource(descriptor, project);
                if (elementWithSource != null){
                    return new NavigationData(elementWithSource, descriptor);
                }
            }
        }
        
        return null;
        
    }
    
    private class NavigationData {
        
        private final SourceElement sourceElement;
        private final DeclarationDescriptor descriptor;
        
        public NavigationData(SourceElement sourceElement, DeclarationDescriptor descriptor){
            this.sourceElement = sourceElement;
            this.descriptor = descriptor;
        }
        
        public SourceElement getSourceElement(){
            return sourceElement;
        }
        
        public DeclarationDescriptor getDeclarationDescriptor(){
            return descriptor;
        }
    
}
    
}
