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

import io.sarl.lang.core.EventSpace;

import java.util.UUID;

/**
 *
 */
public class AgentContestImpl extends AbstractSocialExperiment
    implements AgentContestBallot
{

    /**
     * @param space
     */
    protected AgentContestImpl(EventSpace space)
    {
        super(space);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void onDisclosure(BeliefDisclosure disclosure)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see io.mochalog.sarl.beliefs.social.analysis.contest.AgentContestBallot#announceContestResult(java.util.UUID[])
     */
    @Override
    public void announceContestResult(UUID... winningEntrants)
    {
        // TODO Auto-generated method stub
        
    }

}