package org.black.kotlin.navigation.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.black.kotlin.navigation.NavigationUtil;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.java.editor.overridden.GoToImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@MimeRegistration(mimeType = "text/x-java", service = HyperlinkProviderExt.class)
public final class JavaHyperlinkProvider implements HyperlinkProviderExt {
    
    public JavaHyperlinkProvider() {
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION, HyperlinkType.ALT_HYPERLINK);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return GoToSupport.getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        switch (type) {
            case GO_TO_DECLARATION:
                Element element = FromJavaToKotlinNavigationUtils.getElement(doc, offset);
                if (element == null) {
                    GoToSupport.goTo(doc, offset, false);
                }
                FileObject file = ProjectUtils.getFileObjectForDocument(doc);
                Project project = ProjectUtils.getKotlinProjectForFileObject(file);
                KtFile ktFile = FromJavaToKotlinNavigationUtils.findKotlinFileToNavigate(element, project);
                
                if (ktFile != null) {
                    String filePath = ktFile.getVirtualFile().getPath();
                    FileObject fileToOpen = FileUtil.toFileObject(new File(filePath));
                    try {
                        StyledDocument docToOpen = ProjectUtils.getDocumentFromFileObject(fileToOpen);
                        NavigationUtil.openFileAtOffset(docToOpen, fileToOpen, 0);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else GoToSupport.goTo(doc, offset, false);
                break;
            case ALT_HYPERLINK:
                JTextComponent focused = EditorRegistry.focusedComponent();
                
                if (focused != null && focused.getDocument() == doc) {
                    focused.setCaretPosition(offset);
                    GoToImplementation.goToImplementation(focused);
                }
                break;
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return GoToSupport.getGoToElementTooltip(doc, offset, false, type);
    }

}
