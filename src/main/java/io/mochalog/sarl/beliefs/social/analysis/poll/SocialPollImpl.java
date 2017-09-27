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

package io.mochalog.sarl.beliefs.social.analysis.poll;

import io.mochalog.sarl.beliefs.social.BeliefDisclosure;
import io.mochalog.sarl.beliefs.social.analysis.AbstractSocialExperiment;
import io.mochalog.sarl.beliefs.social.analysis.ExperimentEvaluator;

import io.sarl.lang.core.EventSpace;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Implementation of interface allowing agents to conduct
 * polls involving other members of a space.
 */
public final class SocialPollImpl extends AbstractSocialExperiment
    implements SocialPollBallot
{
    // Allows evaluation of each successive disclosure response
    // which is captured during the poll
    private final ExperimentEvaluator<? super SocialPollImpl> evaluator;
    // Callback function to invoke once result has been computed
    private Procedure1<? super Boolean> callback;

    /**
     * Implementation of executor service for SocialPollImpl instances.
     */
    public static class Executor extends AbstractSocialExperiment.Executor<Executor, SocialPollImpl>
    {
        // Callback function to be invoked on result computation
        private Procedure1<? super Boolean> callback;
        
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
        protected void onTimeout(SocialPollImpl poll)
        {
            poll.finalisePollResult(false);
        }
        
        @Override
        protected SocialPollImpl build(EventSpace space, ExperimentEvaluator<? super SocialPollImpl> evaluator)
        {   
            SocialPollImpl poll = new SocialPollImpl(space, evaluator);
            
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
    private SocialPollImpl(EventSpace space, ExperimentEvaluator<? super SocialPollImpl> evaluator)
    {
        super(space);
        this.evaluator = evaluator;
    }
    
    @Override
    public void evaluateResponse(BeliefDisclosure response)
    {
        evaluator.evaluate(this, response);
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
            end();
            if (callback != null)
            {
                callback.apply(result);
            }
        }
    }
}