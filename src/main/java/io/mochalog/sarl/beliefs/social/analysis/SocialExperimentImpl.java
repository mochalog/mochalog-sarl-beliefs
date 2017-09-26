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

import io.mochalog.sarl.beliefs.query.BeliefQuery;
import io.mochalog.sarl.beliefs.social.BeliefDisclosure;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import io.sarl.lang.util.SynchronizedSet;

import io.sarl.util.Collections3;
import io.sarl.util.Scopes;

import java.util.HashSet;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * Implementation of the social experiment paradigm with
 * multiple surveys and result callbacks.
 */
public class SocialExperimentImpl extends AbstractSocialExperiment
{
    // Evaluation function
    // Allows evaluation of each successive disclosure response
    // which is captured during the experiment
    private final Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> evaluator;
    // Callback function to invoke once result has been computed
    private Procedure1<? super Boolean> callback;
    
    // Surveys active during experiment progression
    private final SynchronizedSet<BeliefQuery> activeSurveys;

    // Current result of experiment
    private boolean result;
    
    /**
     * Implementation of social experiment executor service
     * for SocialExperimentImpl instances.
     */
    public static class Executor extends AbstractSocialExperiment.Executor<Executor>
    {
        // Evaluation function to apply to the given
        // experiment
        private Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> evaluator;
        // Callback function to be invoked on result computation
        private Procedure1<? super Boolean> callback;
        
        /**
         * Get evaluation function used to evaluate responses.
         * @return Evaluation function
         */
        public Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> getEvaluator()
        {
            return evaluator;
        }

        /**
         * Set the evaluation function experiment will use to evaluate responses.
         * @param evaluator Evaluation function
         * @return Executor instance
         */
        public Executor 
            setEvaluator(Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> evaluator)
        {
            this.evaluator = evaluator;
            return this;
        }
        
        /**
         * Get the callback function which will be executed
         * on experimental result collection.
         * @return Callback function
         */
        public Procedure1<? super Boolean> getExperimentResultCallback()
        {
            return callback;
        }
        
        /**
         * Provide callback function to be invoked when experiment finalised
         * and result collected.
         * @param callback Callback function
         * @return Executor instance
         */
        public Executor onExperimentResult(Procedure1<? super Boolean> callback)
        {
            this.callback = callback;
            return this;
        }
        
        @Override
        protected SocialExperimentImpl build()
        {
            // Experiments require a space and evaluation function
            if (getSpace() == null || evaluator == null)
            {
                return null;
            }
            
            SocialExperimentImpl experiment = new SocialExperimentImpl(getSpace(), evaluator);
            
            // Register a result callback function given
            // it was provided
            if (callback != null)
            {
                experiment.onResult(callback);
            }
            
            return experiment;
        }

        @Override
        protected Executor self()
        {
            return this;
        }
    }
    
    /**
     * Constructor.
     * @param space Space to conduct experiment in
     * @param evaluator Evaluation function
     */
    private SocialExperimentImpl(EventSpace space, 
        Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure>evaluator)
    {
        super(space);
        this.evaluator = evaluator;
        
        activeSurveys = Collections3.synchronizedSet(new HashSet<BeliefQuery>(), new Object());
        
        // Result should default to negative
        result = false;
    }

    @Override
    public synchronized boolean surveyParticipants(BeliefQuery query)
    {
        return surveyParticipants(query, Scopes.<Address>allParticipants());
    }
    
    @Override
    public synchronized boolean surveyParticipants(BeliefQuery query, Scope<Address> scope)
    {
        if (inProgress())
        {
            // Set social experiment to source to act
            // as 'bucket' for all responses
            EventSpace space = getSpace();
            Address sourceAddress = space.getAddress(getID());
            query.setSource(sourceAddress);
            
            space.emit(query, scope);
            activeSurveys.add(query);
            
            return true;
        }
        
        return false; 
    }

    @Override
    public void onDisclosure(BeliefDisclosure disclosure)
    {
        // Check if the experiment is running and if the
        // disclosure pertains to an active query
        if (inProgress() && activeSurveys.contains(disclosure.query))
        {
            // Evaluate the current response
            evaluator.apply(this, disclosure);
        }
    }

    @Override
    public SynchronizedSet<BeliefQuery> getActiveSurveys()
    {
        return Collections3.unmodifiableSynchronizedSet(activeSurveys);
    }
    
    @Override
    public void onResult(Procedure1<? super Boolean> callback)
    {
        this.callback = callback;
    }
    
    @Override
    public synchronized void finaliseResult(boolean result)
    {
        if (inProgress())
        {
            this.result = result;
            end();
        }
    }
    
    @Override
    public synchronized boolean end()
    {
        // On experiment end, ensure the
        // result callback is invoked given one 
        // was registered
        if (super.end() && callback != null)
        {
            callback.apply(result);
            return true;
        }
        
        return false;
    }
}