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

package io.mochalog.sarl.beliefs.social.analysis.contest;

import io.mochalog.sarl.beliefs.social.BeliefDisclosure;
import io.mochalog.sarl.beliefs.social.analysis.AbstractSocialExperiment;
import io.mochalog.sarl.beliefs.social.analysis.ExperimentEvaluator;

import io.sarl.lang.core.EventSpace;

import io.sarl.util.Collections3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * Implementation of agent contest paradigm.
 */
public final class AgentContestImpl extends AbstractSocialExperiment
    implements AgentContestBallot
{
    // Evaluation function used to aggregate entrants into either
    // eligible or ineligible categories
    private final ExperimentEvaluator<? super AgentContestImpl> evaluator;
    // Function used to determine contest winners, if any, from
    // set of eligible entrants
    private final Function1<? super Set<UUID>, ? extends List<UUID>> winnerSelector;
    
    // Prize to offer to contest winners
    private List<Object> prizeParams;
    
    /**
     * Implementation of executor service for AgentContestImpl instances.
     */
    public static class Executor extends AbstractSocialExperiment.Executor<Executor, AgentContestImpl>
    {
        // Function used by contest platform to determine contest winners
        // from eligible entrants
        private Function1<? super Set<UUID>, ? extends List<UUID>> winnerSelector;
        // Prize to be offered to winners of the contest
        private List<Object> prizeParams;
        
        /**
         * Constructor.
         */
        public Executor()
        {
            super();
            
            // Default evaluation function simply partitions responses
            // in terms of sentiment
            // Contains no early contest termination (runs through
            // to timeout)
            final ExperimentEvaluator<AgentContestBallot> defaultEvaluator = (contest, response) ->
            {
                if (response.isBelieved)
                {
                    contest.addPositiveResponse(response);
                }
                else
                {
                    contest.addNegativeResponse(response);
                }
            };
            setEvaluator(defaultEvaluator);
        }
        
        /**
         * Set selection function used to filter winners
         * from eligible entrants.
         * @param winnerSelector Selection function
         * @return Executor instance
         */
        public Executor 
            setWinnerSelector(Function1<? super Set<UUID>, ? extends List<UUID>> winnerSelector)
        {
            this.winnerSelector = winnerSelector;
            return this;
        }
        
        /**
         * Set prize to be offered to winning contest
         * entrants.
         * @param params Prize parameters
         * @return Executor instance
         */
        public Executor setPrize(List<Object> params)
        {
            prizeParams = params;
            return this;
        }
        
        @Override
        protected void onTimeout(AgentContestImpl contest)
        {
            // Given contest has not been closed and announced
            // prior to timeout, assume positive responders are
            // eligible entrants and delegate to winner selector
            // function for announcement
            contest.announceContestResult(contest.getPositiveResponders());
        }

        @Override
        protected AgentContestImpl build(EventSpace space, 
            ExperimentEvaluator<? super AgentContestImpl> evaluator)
        {
            // Winner selection function must be supplied
            if (winnerSelector == null)
            {
                return null;
            }
            
            AgentContestImpl contest = new AgentContestImpl(space, evaluator, winnerSelector);
            
            // Ensure a contest prize was set given it was
            // offered
            if (prizeParams != null)
            {
                contest.setPrize(prizeParams);
            }
            
            return contest;
        }

        @Override
        protected Executor self()
        {
            return this;
        }
    }

    /**
     * Constructor.
     * @param space Space to organise contest in
     * @param evaluator Evaluation function
     * @param winnerSelector Function which will be used to filter
     * winners from eligible entrants
     */
    private AgentContestImpl(EventSpace space, ExperimentEvaluator<? super AgentContestImpl> evaluator,
        Function1<? super Set<UUID>, ? extends List<UUID>> winnerSelector)
    {
        super(space);
        
        this.evaluator = evaluator;
        this.winnerSelector = winnerSelector;
    }
    
    @Override
    public List<Object> getPrize()
    {
        return prizeParams;
    }
    
    /**
     * Set the prize to offer to winning entrants of
     * the contest.
     * @param params Parameters of the prize object
     */
    public void setPrize(List<Object> params)
    {
        prizeParams = params;
    }

    @Override
    public void evaluateResponse(BeliefDisclosure response)
    {
        evaluator.evaluate(this, response);
    }

    @Override
    public void announceContestResult(Set<UUID> eligibleEntrants)
    {
        if (inProgress())
        {
            end();
            
            // Determine contest winners from eligible
            // contest entrants
            // TODO: Expensive performance-wise
            Set<UUID> unsynchronizedWinners = new HashSet<UUID>(winnerSelector.apply(eligibleEntrants));
            Set<UUID> winners = Collections3.synchronizedSet(unsynchronizedWinners, new Object());
            
            // Announce the results of the contest to all
            // participants
            EventSpace space = getSpace();
            space.emit(new ContestAnnouncement(this, winners));
        }
    }
}