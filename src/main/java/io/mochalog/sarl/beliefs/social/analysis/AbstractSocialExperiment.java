/**
 * Copyright 2017 The Mochalog-SARL-Beliefs Authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mochalog.sarl.beliefs.social.analysis;

import io.mochalog.sarl.beliefs.exceptions.ExecutionFailedException;
import io.mochalog.sarl.beliefs.query.BeliefQuery;
import io.mochalog.sarl.beliefs.social.AbstractDisclosureListener;
import io.mochalog.sarl.beliefs.social.BeliefDisclosure;
import io.mochalog.sarl.beliefs.util.EventSpaceUtils;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import io.sarl.lang.util.SynchronizedSet;

import io.sarl.util.Collections3;
import io.sarl.util.Scopes;

import java.security.Principal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of social experiment interface.
 */
public abstract class AbstractSocialExperiment extends AbstractDisclosureListener 
    implements AnalyticalSocialExperiment
{
    // Flag indicating whether experiment is in progress
    private volatile boolean inProgress;

    // Space in which experiment is taking place
    private final EventSpace space;
    
    // Query responses deemed to support and oppose the experiment
    // hypothesis by the evaluator
    private final SynchronizedSet<UUID> positiveResponders;
    private final SynchronizedSet<UUID> negativeResponders;

    /**
     * Abstract implementation of a social experiment
     * execution service.
     * @param <E> Social experiment executor type to use
     */
    public static abstract class Executor<E extends Executor<E>> implements SocialExperimentExecutor
    {
        // Space to execute experiment in
        private EventSpace space;
        // Principal to use in order to access space
        private Principal principal;

        // Belief surveys to be asked of experiment participants upon
        // execution
        private Set<BeliefQuery> surveys;
        // Scope of participants to ask surveys of
        private Scope<Address> surveyScope;
        
        // Number of scheduling threads to queue
        private static final int NUM_EXPERIMENT_SCHEDULING_THREADS = 20;
        // Scheduler of experiment timeouts
        private final ScheduledThreadPoolExecutor scheduler;
        
        // Default timeout - Represents uncapped experiment duration
        public static final long DEFAULT_TIMEOUT = -1;
        // Time in ms to cap experiments at
        private long experimentTimeout;
        
        /**
         * Constructor.
         */
        public Executor()
        {
            surveys = new HashSet<BeliefQuery>();
            // Default survey scope to all experiment participants
            surveyScope = Scopes.<Address>allParticipants();
            
            scheduler = new ScheduledThreadPoolExecutor(NUM_EXPERIMENT_SCHEDULING_THREADS);
            experimentTimeout = DEFAULT_TIMEOUT;
        }
        
        @Override
        public EventSpace getSpace()
        {
            return space;
        }
        
        @Override
        public E setSpace(EventSpace space)
        {
            this.space = space;
            return self();
        }
        
        @Override
        public Principal getAccessPrincipal()
        {
            return principal;
        }

        @Override
        public E setAccessPrincipal(Principal principal)
        {
            this.principal = principal;
            return self();
        }
        
        @Override
        public Set<BeliefQuery> getSurveys()
        {
            return surveys;
        }
        
        @Override
        public E addSurvey(BeliefQuery survey)
        {
            surveys.add(survey);
            return self();
        }
        
        @Override
        public E addSurveys(Collection<BeliefQuery> surveys)
        {
            surveys.addAll(surveys);
            return self();
        }

        @Override
        public Scope<Address> getSurveyScope()
        {
            return surveyScope;
        }
        
        @Override
        public E setSurveyScope(Scope<Address> scope)
        {
            surveyScope = scope;
            return self();
        }
        
        @Override
        public long getExperimentTimeout()
        {
            return experimentTimeout;
        }
        
        @Override
        public E endExperimentAfter(long timeout)
        {
            this.experimentTimeout = timeout;
            return self();
        }

        @Override
        public SocialExperiment execute() throws ExecutionFailedException
        {
            AbstractSocialExperiment experiment = build();
            // Attempt to start experiment in the given event space
            if (experiment == null || !EventSpaceUtils.registerInEventSpace(experiment, space, principal))
            {
                throw new ExecutionFailedException("Social experiment could not be " + 
                    "conducted in space (" + space.getSpaceID() + "). Access restricted.");
            }
            
            // Signal that experiment has started
            experiment.inProgress = true;
            
            // Ask each survey query in experiment space
            for (BeliefQuery survey : surveys)
            {
                experiment.surveyParticipants(survey);
            }
            
            // Schedule an experiment timeout (after time elapsed, kill
            // the experiment and produce a negative result
            if (experimentTimeout != DEFAULT_TIMEOUT)
            {
                scheduler.schedule(() -> experiment.finaliseResult(false), 
                    experimentTimeout, TimeUnit.MILLISECONDS);
            }
            
            return experiment;
        }
        
        /**
         * Build a new experiment to be executed by the Executor.
         * @return Social experiment instance
         */
        protected abstract AbstractSocialExperiment build();
        
        /**
         * Get current executor instance for correct chaining
         * of executor methods.
         * @return Executor instance
         */
        protected abstract E self();
    }
    
    /**
     * Constructor.
     * @param space Space to conduct experiment in
     */
    protected AbstractSocialExperiment(EventSpace space)
    {
        this.space = space;
        
        positiveResponders = Collections3.synchronizedSet(new HashSet<UUID>(), new Object());
        negativeResponders = Collections3.synchronizedSet(new HashSet<UUID>(), new Object());
    }
    
    @Override
    public boolean inProgress()
    {
        return inProgress;
    }

    @Override
    public synchronized boolean end()
    {
        // Ensure experiment is currently in progress
        if (inProgress())
        {
            inProgress = false;
            // Detach the experiment from the event space
            EventSpaceUtils.unregisterFromEventSpace(this, space);
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean addPositiveResponse(BeliefDisclosure disclosure)
    {
        return addResponse(positiveResponders, disclosure);
    }

    @Override
    public boolean addPositiveResponder(UUID responder)
    {
        return addResponder(positiveResponders, responder);
    }

    @Override
    public boolean addNegativeResponse(BeliefDisclosure disclosure)
    {
        return addResponse(negativeResponders, disclosure);
    }

    @Override
    public boolean addNegativeResponder(UUID responder)
    {
        return addResponder(negativeResponders, responder);
    }

    /**
     * Mark a given disclosure response as belonging to a specified partition
     * of the participant set.
     * @param responders Set of participants to add response to
     * @param disclosure Belief disclosure
     * @return Success status
     */
    private boolean addResponse(SynchronizedSet<UUID> responders, BeliefDisclosure disclosure)
    {
        return addResponder(responders, disclosure.getSource().getUUID());
    }
    
    /**
     * Mark a given responder as belonging to a specified partition
     * of the participant set.
     * @param responders Set of participants to add responder to
     * @param responder Identifier of responder
     * @return Success status
     */
    private boolean addResponder(SynchronizedSet<UUID> responders, UUID responder)
    {
        return responders.add(responder);
    }

    @Override
    public SynchronizedSet<UUID> getPositiveResponders()
    {
        return Collections3.unmodifiableSynchronizedSet(positiveResponders);
    }

    @Override
    public SynchronizedSet<UUID> getNegativeResponders()
    {
        return Collections3.unmodifiableSynchronizedSet(negativeResponders);
    }
    
    @Override
    public EventSpace getSpace()
    {
        return space;
    }
}