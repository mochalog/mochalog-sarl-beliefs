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

import io.mochalog.sarl.beliefs.social.analysis.SocialExperiment;

import java.util.List;

/**
 * Interface for contests in which agents compete
 * for an arbitrary result.
 */
public interface AgentContest extends SocialExperiment
{
    /**
     * Get the prize to offer to winners of
     * the contest. The prize comes in the form
     * of a series of objects which can
     * be fit to any given generic 'protocol'.
     * @return Prize parameters
     */
    public List<Object> getPrize();
}