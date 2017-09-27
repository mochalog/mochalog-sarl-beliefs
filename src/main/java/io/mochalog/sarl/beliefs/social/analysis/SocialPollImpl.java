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

import io.mochalog.sarl.beliefs.social.BeliefDisclosure;

import io.sarl.lang.core.EventSpace;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * Implementation of interface allowing agents to conduct
 * polls involving other members of a space.
 */
public final class SocialPollImpl extends AbstractSocialExperiment
    implements SocialPollBallot
{
    // Evaluation function
    // Allows evaluation of each successive disclosure response
    // which is captured during the poll
    private final Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> evaluator;
    // Callback function to invoke once result has been computed
    private Procedure1<? super Boolean> callback;

    // Current result of poll
    private boolean result;
    
    /**
     * Implementation of social poll executor service
     * for SocialPollImpl instances.
     */
    public static class Executor extends AbstractSocialExperiment.Executor<Executor, SocialPollImpl>
    {
        // Evaluation function to apply to the given
        // poll
        private Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> evaluator;
        // Callback function to be invoked on result computation
        private Procedure1<? super Boolean> callback;
        
        /**
         * Get evaluation function used to evaluate responses.
         * @return Evaluation function
         */
        public Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> getEvaluator()
        {
            return evaluator;
        }

        /**
         * Set the evaluation function the poll will use to evaluate responses.
         * @param evaluator Evaluation function
         * @return Executor instance
         */
        public Executor 
            setEvaluator(Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> evaluator)
        {
            this.evaluator = evaluator;
            return this;
        }
        
        /**
         * Get the callback function which will be executed
         * on poll result collection.
         * @return Callback function
         */
        public Procedure1<? super Boolean> getPollResultCallback()
        {
            return callback;
        }
        
        /**
         * Provide callback function to be invoked when poll finalised
         * and result collected.
         * @param callback Callback function
         * @return Executor instance
         */
        public Executor onPollResult(Procedure1<? super Boolean> callback)
        {
            this.callback = callback;
            return this;
        }
        
        @Override
        protected void onExperimentTimeout(SocialPollImpl poll)
        {
            poll.finalisePollResult(false);
        }
        
        @Override
        protected SocialPollImpl build()
        {
            // Experiments require a space and evaluation function
            if (getSpace() == null || evaluator == null)
            {
                return null;
            }
            
            SocialPollImpl poll = new SocialPollImpl(getSpace(), evaluator);
            
            // Register a result callback function given
            // it was provided
            if (callback != null)
            {
                poll.onPollResult(callback);
            }
            
            return poll;
        }

        @Override
        protected Executor self()
        {
            return this;
        }
    }
    
    /**
     * Constructor.
     * @param space Space to conduct poll in
     * @param evaluator Evaluation function
     */
    private SocialPollImpl(EventSpace space, 
        Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> evaluator)
    {
        super(space);
        this.evaluator = evaluator;
        
        // Result should default to negative
        result = false;
    }

    @Override
    public void onDisclosure(BeliefDisclosure disclosure)
    {
        // Check if the poll is running and if the
        // disclosure pertains to an active query
        if (inProgress() && getActiveSurveys().contains(disclosure.query))
        {
            // Evaluate the current response
            evaluator.apply(this, disclosure);
        }
    }

    @Override
    public void onPollResult(Procedure1<? super Boolean> callback)
    {
        this.callback = callback;
    }
    
    @Override
    public synchronized void finalisePollResult(boolean result)
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
        // On poll end, ensure the
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