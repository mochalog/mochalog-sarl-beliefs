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

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import io.sarl.lang.util.SynchronizedSet;

import java.util.Collection;
import java.util.UUID;

/**
 * Interface to high-level state of a social
 * experiment to conduct within a given space.
 */
public interface SocialExperiment
{
    /**
     * Check if experiment is currently running.
     * @return True if experiment running, false otherwise.
     */
    public boolean inProgress();
    
    /**
     * Get the surveys currently actively being evaluated.
     * @return Set of active survey queries
     */
    public SynchronizedSet<BeliefQuery> getActiveSurveys();
    
    /**
     * Survey all experiment participants with
     * a given query.
     * @param query Query to ask
     * @return True if experiment is ongoing, false otherwise.
     */
    public boolean surveyParticipants(BeliefQuery query);
    
    /**
     * Survey group of experiment participants with
     * a given query.
     * @param query Query to ask
     * @param scope Scope of participant group to ask
     * @return True if experiment is ongoing, false otherwise.
     */
    public boolean surveyParticipants(BeliefQuery query, Scope<Address> scope);
    
    /**
     * Survey group of experiment participants with
     * collection of queries.
     * @param queries Queries to ask
     * @param scope Scope of participant group to ask
     * @return True if experiment is ongoing, false otherwise.
     */
    public boolean surveyParticipants(Collection<BeliefQuery> queries, Scope<Address> scope);
    
    /**
     * Close an in-progress experiment. Result will
     * be computed immediately.
     * @return True if experiment ended successfully,
     * false if experiment already completed.
     */
    public boolean end();
    
    /**
     * Get space in which social experiment
     * is being conducted.
     * @return Space experiment being conducted in
     */
    public EventSpace getSpace();
    
    /**
     * Get all participants of the social experiment.
     * @return Experiment participants
     */
    public SynchronizedSet<UUID> getParticipants();
}