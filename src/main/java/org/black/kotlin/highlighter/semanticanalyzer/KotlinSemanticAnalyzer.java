package org.black.kotlin.highlighter.semanticanalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinSemanticAnalyzer extends SemanticAnalyzer<KotlinParserResult> {

    boolean cancel = false;
    private final Map<OffsetRange, Set<ColoringAttributes>> highlighting = 
            new HashMap<OffsetRange, Set<ColoringAttributes>>();
    
    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return highlighting;
    }

    @Override
    public void run(KotlinParserResult result, SchedulerEvent event) {
        highlighting.clear();
        resumeCancel();
        try {
            KotlinSemanticHighlightingVisitor highlightingVisitor =
                    new KotlinSemanticHighlightingVisitor(result.getKtFile(), 
                            result.getAnalysisResult().getAnalysisResult());
            highlighting.putAll(highlightingVisitor.computeHighlightingRanges());
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel = true;
    }
    
    private void resumeCancel() {
        cancel = false;
    }
    
}
