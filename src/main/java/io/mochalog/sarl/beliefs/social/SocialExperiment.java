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
import io.mochalog.sarl.beliefs.social.analysis.AbstractDisclosureListener;
import io.mochalog.sarl.beliefs.util.EventSpaceUtils;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import io.sarl.lang.util.SynchronizedSet;

import io.sarl.util.Collections3;
import io.sarl.util.Scopes;

import java.security.Principal;

import java.util.HashSet;
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
    private final Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator;

    // Space in which experiment is taking place
    private final EventSpace space;
    // Surveys active during experiment progression
    private final SynchronizedSet<BeliefQuery> activeSurveys;
    
    // Query responses deemed to support and oppose the experiment
    // hypothesis by the evaluator
    private final SynchronizedSet<UUID> positiveResponders;
    private final SynchronizedSet<UUID> negativeResponders;
    
    /**
     * Constructor.
     * @param space Space to conduct experiment in
     * @param evaluator Evaluation function
     * @throws IllegalArgumentException Social experiment could not
     * be successfully initialised in the given space.
     */
    public SocialExperiment(EventSpace space,
        Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator)
        throws IllegalArgumentException
    {
        this(space, null, evaluator);
    }
    
    /**
     * Constructor.
     * @param space Space to conduct experiment in
     * @param principal Principal to verify access permissions against
     * @param evaluator Evaluation function
     * @throws IllegalArgumentException Social experiment could not
     * be successfully initialised in the given space.
     */
    public SocialExperiment(EventSpace space, Principal principal,
        Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator)
        throws IllegalArgumentException
    {
        super();
        
        this.evaluator = evaluator;
        this.space = space;
        
        activeSurveys = Collections3.synchronizedSet(new HashSet<BeliefQuery>(), new Object());
        
        positiveResponders = Collections3.synchronizedSet(new HashSet<UUID>(), new Object());
        negativeResponders = Collections3.synchronizedSet(new HashSet<UUID>(), new Object());
        
        if (!EventSpaceUtils.registerInEventSpace(this, space, principal))
        {
            throw new IllegalArgumentException("Social experiment could not be " + 
                "conducted in space (" + space.getSpaceID() + "). Access restricted.");
        }
        else
        {
            inProgress = true;
        }
    }
    
    /**
     * Survey all experiment participants with
     * a given query.
     * @param query Query to ask
     * @return True if experiment is ongoing, false otherwise.
     */
    public boolean survey(BeliefQuery query)
    {
        return survey(query, Scopes.<Address>allParticipants());
    }
    
    /**
     * Survey group of experiment participants with
     * a given query.
     * @param query Query to ask
     * @param scope Scope of participant group to ask
     * @return True if experiment is ongoing, false otherwise.
     */
    public boolean survey(BeliefQuery query, Scope<Address> scope)
    {
        if (inProgress)
        {
            // Set social experiment to source to act
            // as 'bucket' for all responses
            Address sourceAddress = space.getAddress(getID());
            query.setSource(sourceAddress);
            
            space.emit(query, scope);
            activeSurveys.add(query);
            
            return true;
        }
        
        return false;    
    }
    
    @Override
    public void overhear(BeliefDisclosure disclosure)
    {
        // Check if the experiment is running and if the
        // disclosure pertains to an active query
        if (inProgress && activeSurveys.contains(disclosure.query))
        {
            // Reason about the response and stop the experiment
            // on the signal of the evaluator
            if (evaluator.apply(this, disclosure))
            {
                kill();
            }
        }
    }
    
    /**
     * Kill the running experiment.
     */
    public void kill()
    {
        // Ensure experiment is currently in progress
        if (inProgress)
        {
            inProgress = false;
            // Detach the experiment from the event space
            EventSpaceUtils.unregisterFromEventSpace(this, space);
        }
    }
    
    /**
     * Check if experiment is currently running.
     * @return True if experiment running, false otherwise.
     */
    public boolean inProgress()
    {
        return inProgress;
    }
    
    /**
     * Get the surveys currently actively being evaluated.
     * @return Unmodifiable set of active survey queries
     */
    public SynchronizedSet<BeliefQuery> getActiveSurveys()
    {
        return Collections3.unmodifiableSynchronizedSet(activeSurveys);
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