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

package io.mochalog.sarl.beliefs.social;

import io.mochalog.sarl.beliefs.query.BeliefQuery;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import io.sarl.lang.util.SynchronizedSet;

import io.sarl.util.Collections3;

import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function2;

/**
 * Experiment performed on participants of a social group (EventSpace)
 * in order to confirm or deny a hypothesis about group beliefs.
 */
public final class SocialExperiment extends AbstractDisclosureListener
{    
    // Flag indicating whether experiment is in progress
    private volatile boolean inProgress;
    
    // Evaluation function
    // Allows evaluation of each successive disclosure response
    // which is captured during the experiment
    private Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator;

    // Space in which experiment is taking place
    private EventSpace experimentSpace;
    // Queries active during experiment progression
    private SynchronizedSet<BeliefQuery> activeQueries;
    
    // Query responses deemed to support and oppose the experiment
    // hypothesis by the evaluator
    private SynchronizedSet<UUID> positiveResponders;
    private SynchronizedSet<UUID> negativeResponders;
    
    /**
     * Constructor.
     * @param space Space to conduct experiment in
     * @param evaluator Evaluation function
     */
    public SocialExperiment(EventSpace space, 
        Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator)
    {
        super();
        inProgress = false;
        
        this.evaluator = evaluator;
        
        experimentSpace = space;
        activeQueries = Collections3.emptySynchronizedSet();
        
        positiveResponders = Collections3.emptySynchronizedSet();
        negativeResponders = Collections3.emptySynchronizedSet();
    }
    
    @Override
    public void overhear(BeliefDisclosure disclosure)
    {
        // Check if the experiment is running and if the
        // disclosure pertains to an active query
        if (inProgress && activeQueries.contains(disclosure.query))
        {
            // Reason about the response and stop the experiment
            // on the signal of the evaluator
            if (!evaluator.apply(this, disclosure))
            {
                conclude();
            }
        }
    }
    
    /**
     * Perform an experiment on a series of queries.
     * @param queries Queries to ask
     */
    public void conduct(BeliefQuery... queries)
    {
        conduct(null, queries);
    }
    
    /**
     * Perform an experiment on a series of queries.
     * @param scope Scope of experiment
     * @param queries Queries to ask
     */
    public void conduct(Scope<Address> scope, BeliefQuery... queries)
    {
        inProgress = true;
        for (BeliefQuery query : queries)
        {
            experimentSpace.emit(query, scope);
            activeQueries.add(query);
        }
    }
    
    /**
     * Stop the experiment.
     */
    public void conclude()
    {
        inProgress = false;
        
        activeQueries.clear();
        
        positiveResponders.clear();
        negativeResponders.clear();
    }
    
    /**
     * Check if experiment is currently running.
     * @return True if experiment running, false otherwise.
     */
    public boolean isInProgress()
    {
        return inProgress;
    }
    
    /**
     * Get the queries currently being experimented on.
     * @return Unmodifiable set of active queries
     */
    public SynchronizedSet<BeliefQuery> getActiveQueries()
    {
        return Collections3.unmodifiableSynchronizedSet(activeQueries);
    }
    
    /**
     * Get participants who have provided responses supporting
     * the experimental hypothesis.
     * @return Unmodifiable set of supporting responders
     */
    public SynchronizedSet<UUID> getPositiveResponders()
    {
        return Collections3.unmodifiableSynchronizedSet(positiveResponders);
    }

    /**
     * Mark a given disclosure response as being supportive of the
     * experimental hypothesis.
     * @param disclosure Belief disclosure
     * @return Success status
     */
    public boolean addPositiveResponse(BeliefDisclosure disclosure)
    {
        return addResponse(positiveResponders, disclosure);
    }
    
    /**
     * Get participants who have provided responses opposing
     * the experimental hypothesis.
     * @return Unmodifiable set of opposing responders
     */
    public SynchronizedSet<UUID> getNegativeResponders()
    {
        return Collections3.unmodifiableSynchronizedSet(negativeResponders);
    }
    
    /**
     * Mark a given disclosure response as refuting the
     * experimental hypothesis.
     * @param disclosure Belief disclosure
     * @return Success status
     */
    public boolean addNegativeResponse(BeliefDisclosure disclosure)
    {
        return addResponse(negativeResponders, disclosure);
    }
    
    /**
     * Mark a given disclosure response as belonging to a specified partition
     * of the participant set.
     * @param responders Set of participants to add response to
     * @param disclosure Belief disclosure
     * @return Success status
     */
    private boolean addResponse(SynchronizedSet<UUID> responders, 
        BeliefDisclosure disclosure)
    {
        UUID source = disclosure.getSource().getUUID();
        return responders.add(source);
    }
}