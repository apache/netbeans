package org.black.kotlin.highlighter.occurrences;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Александр
 */
public class KotlinOccurrencesFinder extends OccurrencesFinder<KotlinParserResult> {

    private int caretPosition = 0;
    boolean cancel = false;
    Map<OffsetRange, ColoringAttributes> highlighting = new HashMap<OffsetRange, ColoringAttributes>();
    
    @Override
    public void setCaretPosition(int position) {
        caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return highlighting;
    }

    @Override
    public void run(KotlinParserResult result, SchedulerEvent event) {
        cancel = false;
        highlighting.clear();
        KtFile ktFile = result.getKtFile();
        
        PsiElement psiElement = ktFile.findElementAt(caretPosition);
        ktFile.accept(PsiElementVisitor.EMPTY_VISITOR);
        
        List<PsiElement> psiElements = findOccurrencesInFile(ktFile, psiElement);
        psiElements.add(psiElement);
        
        for (PsiElement psi : psiElements){
            ColoringAttributes attributes = ColoringAttributes.MARK_OCCURRENCES;
            OffsetRange offset = new OffsetRange(psi.getTextRange().getStartOffset(), psi.getTextRange().getEndOffset());
            highlighting.put(offset, attributes);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private List<PsiElement> findOccurrencesInFile(KtFile ktFile, PsiElement psiElement) {
        List<PsiElement> elements = Lists.newArrayList();
        
        for (PsiElement psi : ktFile.getChildren()){
            elements.addAll(findOccurrencesInPsiElement(psiElement, psi));
        }
        
        return elements;
    }
    
    private List<PsiElement> findOccurrencesInPsiElement(PsiElement toFind, PsiElement psiElement) {
        List<PsiElement> elements = Lists.newArrayList();
        
        if (psiElement.getText().equals(toFind.getText()) && psiElement.getUseScope().equals(toFind.getUseScope())){
            elements.add(psiElement);
        }
        
        for (PsiElement psi : psiElement.getChildren()){
            elements.addAll(findOccurrencesInPsiElement(toFind, psi));
        }
        
        return elements;
    }
    
}
