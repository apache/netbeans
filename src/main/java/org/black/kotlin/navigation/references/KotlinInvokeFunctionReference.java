package org.black.kotlin.navigation.references;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.psi.Call;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.calls.callUtil.CallUtilKt;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall;

public class KotlinInvokeFunctionReference implements KotlinReference{

    private final KtCallExpression expression;
    
    public KotlinInvokeFunctionReference(KtCallExpression expression) {
        this.expression = expression;
    }
    
    @Override
    public KtReferenceExpression getReferenceExpression() {
        return expression;
    }

    @Override
    public Collection<? extends DeclarationDescriptor> getTargetDescriptors(BindingContext context) {
        Call call = CallUtilKt.getCall(expression, context);
        ResolvedCall resolvedCall = CallUtilKt.getResolvedCall(call, context);
        
        if (resolvedCall instanceof VariableAsFunctionResolvedCall){
            return Lists.newArrayList(
                    ((VariableAsFunctionResolvedCall) resolvedCall).
                            getFunctionCall().getCandidateDescriptor());
        } else if (call != null && resolvedCall != null && call.getCallType() == Call.CallType.INVOKE){
            return Lists.newArrayList(resolvedCall.getCandidateDescriptor());
        } else {
            return Collections.emptyList();
        }
        
    }
    
}
