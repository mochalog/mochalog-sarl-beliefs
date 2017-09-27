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

import io.mochalog.sarl.beliefs.social.analysis.SocialExperimentBallot;

/**
 * Interface for access to ballot of a poll being
 * conducted in an event space. Allows for computation
 * of decision result.
 */
public interface SocialPollBallot extends SocialPoll, SocialExperimentBallot
{
    /**
     * Compute the result of the poll and subsequently
     * stop accepting submissions.
     * @param result Result of the experiment
     */
    public void finalisePollResult(boolean result);
}