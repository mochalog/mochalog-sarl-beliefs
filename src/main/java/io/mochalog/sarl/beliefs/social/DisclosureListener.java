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

import io.mochalog.sarl.beliefs.social.BeliefDisclosure;

import io.sarl.lang.core.EventListener;

/**
 * EventListener specifically designed for overhearing
 * and reacting to belief disclosures.
 */
public interface DisclosureListener extends EventListener
{
    /**
     * Observe and react to belief disclosures made
     * within a registered EventSpace.
     * @param disclosure Belief disclosure
     */
    public void onDisclosure(BeliefDisclosure disclosure);
}