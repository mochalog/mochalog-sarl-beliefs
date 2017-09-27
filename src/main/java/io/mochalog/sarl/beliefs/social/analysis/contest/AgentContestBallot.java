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

import io.mochalog.sarl.beliefs.social.analysis.SocialExperimentBallot;

import java.util.Set;
import java.util.UUID;

/**
 * Interface for access to ballot of a contest being
 * conducted in an event space. Enables announcement
 * of contest results to entrants.
 */
public interface AgentContestBallot extends AgentContest, SocialExperimentBallot
{
    /**
     * Filter eligible entrants into contest winners using the win
     * condition. Announce the identities of said winners to all
     * contest entrants.
     * @param eligibleEntrants Identities of eligible entrants
     */
    public void announceContestResult(Set<UUID> eligibleEntrants);
}