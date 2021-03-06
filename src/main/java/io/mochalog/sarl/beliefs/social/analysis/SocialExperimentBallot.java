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
import io.mochalog.sarl.beliefs.social.DisclosureListener;

import io.sarl.lang.util.SynchronizedSet;

import java.util.UUID;

/**
 * Interface for access to ballot of social experiment with
 * survey responses. Facilitates analysis of the experimental
 * response sentiment.
 */
public interface SocialExperimentBallot extends SocialExperiment, DisclosureListener
{    
    /**
     * Mark a given disclosure response as being supportive of the
     * survey hypothesis.
     * @param disclosure Belief disclosure
     * @return Success status
     */
    public boolean addPositiveResponse(BeliefDisclosure disclosure);
    
    /**
     * Mark a given responder as supportive of the
     * survey hypothesis.
     * @param responder Responder identifier
     * @return Success status
     */
    public boolean addPositiveResponder(UUID responder);
    
    /**
     * Mark a given disclosure response as refuting of the
     * survey hypothesis.
     * @param disclosure Belief disclosure
     * @return Success status
     */
    public boolean addNegativeResponse(BeliefDisclosure disclosure);
    
    /**
     * Mark a given responder as oppositional of the
     * survey hypothesis.
     * @param responder Responder identifier
     * @return Success status
     */
    public boolean addNegativeResponder(UUID responder);
    
    /**
     * Get participants who have provided responses supporting
     * the survey hypothesis.
     * @return Set of supporting responders
     */
    public SynchronizedSet<UUID> getPositiveResponders();
    
    /**
     * Get participants who have provided responses opposing
     * the survey hypothesis.
     * @return Set of supporting responders
     */
    public SynchronizedSet<UUID> getNegativeResponders();
}