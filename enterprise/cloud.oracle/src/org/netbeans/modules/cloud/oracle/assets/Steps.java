/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * An implementation of the {@link Step} interface used for multiple commands or actions.
 * 
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "MSG_CollectingItems=Loading OCI contents",
    "FetchingDevopsProjects=Fetching DevOps projects",
    "SelectItem=Select {0}",
    "LoadingItems=Loading items for the next step"
})
public final class Steps {
    private static final Logger LOG = Logger.getLogger(Steps.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Steps"); //NOI18N
    private static Steps instance = null;

    public static synchronized Steps getDefault() {
        if (instance == null) {
            instance = new Steps();
        }
        return instance;
    }

    public CompletableFuture<Values> executeMultistep(AbstractStep firstStep, Lookup lookup) {
        DialogDisplayer dd = DialogDisplayer.getDefault();
        CompletableFuture future = new CompletableFuture();
        RP.post(() -> {
            try {
                Multistep multistep = new Multistep(firstStep, lookup);
                NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddSuggestedItem(), 3, multistep.createInput());
                dd.notifyFuture(ci).handle((result, exception) -> {
                    if (exception != null) {
                        future.completeExceptionally(exception);
                    } else {
                        future.complete(multistep.getResult());
                    }
                    return null;
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }
    
    /**
     * Provider class that supplies the next {@link Step} for navigation from the current {@link Step}.
     * 
     */
    public static class NextStepProvider {
        private final Map<Class<? extends AbstractStep>, Function<AbstractStep, AbstractStep>> steps;
        
        /**
         * Private constructor to initialize the NextStepProvider with a map of steps.
         *
         * @param steps a map associating classes with their corresponding {@link Step} instances
         */
        private NextStepProvider(Map<Class<? extends AbstractStep>, Function<AbstractStep, AbstractStep>> steps) {
            this.steps = steps;
        }
        
        /**
         * Retrieves the next {@link Step} for the specified {@link Step}.
         *
         * @param currentStep the current step for which the next step is to be retrieved
         * @return the {@link Step} associated with the specified class, or null if no step is found
         */
        public AbstractStep nextStepFor(AbstractStep currentStep) {
            Function<AbstractStep, AbstractStep> nextStep = steps.get(currentStep.getClass());
            return nextStep != null ? nextStep.apply(currentStep) : null;
        }
        
        /**
         * Creates a new Builder for constructing a NextStepProvider.
         *
         * @return a new Builder instance
         */
        public static Builder builder() {
            return new Builder();
        }
        
        /**
         * Builder class for constructing a NextStepProvider.
         */
        public static class Builder {
            private final Map<Class<? extends AbstractStep>, Function<AbstractStep, AbstractStep>> steps = new HashMap<> ();

            /**
             * Private constructor for the Builder.
             */
            private Builder() {
            }
            
            /**
             * Associates a {@link Step} function with a class in the builder.
             *
             * @param clazz the class to be associated with the step function
             * @param stepFunction the function to be associated with the class
             * @return the current Builder instance for method chaining
             */
            public Builder stepForClass(Class<? extends AbstractStep> clazz, Function<AbstractStep, AbstractStep> stepFunction) {
                steps.put(clazz, stepFunction);
                return this;
            }
            
            /**
             * Builds and returns a NextStepProvider with the configured steps.
             *
             * @return a new NextStepProvider instance
             */
            public NextStepProvider build() {
                return new NextStepProvider(steps);
            }
        }
    }
    
    /**
     * Provides values for steps in the multi step dialog.
     * 
     */
    public interface Values {
        
        /**
         * Returns a value for a given {@link Step}.
         * @param step
         * @return 
         */
        public <T> T getValueForStep(Class<? extends AbstractStep<T>> step);
    }

    private static class Multistep {

        private final LinkedList<AbstractStep> steps = new LinkedList<>();
        private final Lookup lookup;
        private final Values values;

        Multistep(AbstractStep firstStep, Lookup lookup) {
            steps.add(firstStep);
            values = new MultistepValues(steps);
            this.lookup = new ProxyLookup(Lookups.fixed(values), lookup);
        }

        NotifyDescriptor.ComposedInput.Callback createInput() {
            return new NotifyDescriptor.ComposedInput.Callback() {
                private int lastNumber = 0;

                private void readValue(AbstractStep step, NotifyDescriptor desc) {
                    String selected = null;
                    if (!step.onlyOneChoice()) {
                        if (desc instanceof NotifyDescriptor.QuickPick) {
                            for (NotifyDescriptor.QuickPick.Item item : ((NotifyDescriptor.QuickPick) desc).getItems()) {
                                if (item.isSelected()) {
                                    selected = item.getLabel();
                                    break;
                                }
                            }
                        } else if (desc instanceof NotifyDescriptor.InputLine) {
                            selected = ((NotifyDescriptor.InputLine) desc).getInputText();
                        }
                        step.setValue(selected);
                    }
                }

                @Override
                public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
                    if (number == 1) {
                        while (steps.size() > 1) {
                            steps.removeLast();
                        }
                        prepare(steps.getLast());
                    } else if (lastNumber > number) {
                        steps.removeLast();
                        while (steps.getLast().onlyOneChoice() && steps.size() > 1) {
                            steps.removeLast();
                        }
                        lastNumber = number;
                        return steps.getLast().createInput();
                    } else {
                        readValue(steps.getLast(), input.getInputs()[number - 2]);
                        steps.add(getNextFor(steps.getLast()));
                    }
                    lastNumber = number;

                    while (steps.getLast() != null && steps.getLast().onlyOneChoice()) {
                        steps.add(getNextFor(steps.getLast()));
                    }
                    if (steps.getLast() == null) {
                        steps.removeLast();
                        return null;
                    }
                    return steps.getLast().createInput();
                }
                
                private void prepare(AbstractStep step) {
                    ProgressHandle h = ProgressHandle.createHandle(Bundle.LoadingItems());
                    h.start();
                    try {
                        h.progress(Bundle.LoadingItems());
                        step.prepare(h, values);
                    } finally {
                        h.finish();
                    }
                }
                
                private AbstractStep getNextFor(AbstractStep step) {
                    Steps.NextStepProvider nsProvider = lookup.lookup(Steps.NextStepProvider.class);
                    if (nsProvider != null) {
                        AbstractStep ns = nsProvider.nextStepFor(step);
                        if (ns != null) {
                            prepare(ns);
                            return ns;
                        }
                    } 
                    return null;
                }
            };
        }

        public Values getResult() {
            return values;
        }
        
        private static class MultistepValues implements Values {
            private final List<AbstractStep> steps;

            public MultistepValues(List<AbstractStep> steps) {
                this.steps = steps;
            }
            
            @Override
            public <T> T getValueForStep(Class<? extends AbstractStep<T>> forStep) {
                for (AbstractStep step : steps) {
                    if (step.getClass().equals(forStep)) {
                        return (T) step.getValue();
                    }
                }
                return null;
            }
        }
    }

    public static <T extends OCIItem> NotifyDescriptor.QuickPick createQuickPick(Map<String, T> ociItems, String title) {
        List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<> ();
        for (Map.Entry<String, T> entry : ociItems.entrySet()) {
            String description = entry.getValue().getDescription();
            if (description == null || description.isBlank()) {
                description = entry.getValue().getName();
            }
            items.add(new NotifyDescriptor.QuickPick.Item(entry.getKey(), description));
        }
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }

}
