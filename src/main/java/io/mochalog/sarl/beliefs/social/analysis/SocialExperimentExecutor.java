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

import io.mochalog.sarl.beliefs.exceptions.ExecutionFailedException;
import io.mochalog.sarl.beliefs.query.BeliefQuery;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;

import java.security.Principal;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for executor services of social experiments in event spaces.
 * @param <E> Executor type to use (used for chaining)
 * @param <S> Social experiment type to execute
 */
public interface SocialExperimentExecutor<E extends SocialExperimentExecutor<E, S>, S extends SocialExperiment>
{   
    /**
     * Set space to conduct executed experiment in.
     * @param space Event space
     * @return Executor instance
     */
    public E setSpace(EventSpace space);
    
    /**
     * Space in which experiment will be conducted.
     * @return Event space
     */
    public EventSpace getSpace();
    
    /**
     * Set principal object with which space access will be authenticated.
     * @param principal Access principal
     * @return Executor instance
     */
    public E setAccessPrincipal(Principal principal);
    
    /**
     * Access principal with which space access will be authenticated.
     * @return Access principal
     */
    public Principal getAccessPrincipal();
    
    /**
     * Add a survey query to be posed to experiment participants
     * on execution.
     * @param survey Survey to ask
     * @return Executor instance
     */
    public E addSurvey(BeliefQuery survey);
    
    /**
     * Add a collection of survey queries to be posed to experiment participants
     * on execution.
     * @param surveys Surveys to ask
     * @return Executor instance
     */
    public E addSurveys(Collection<BeliefQuery> surveys);
    
    /**
     * Get all surveys which will be asked on experiment
     * execution.
     * @return Surveys to ask
     */
    public Set<BeliefQuery> getSurveys();
    
    /**
     * Set the scope of experiment participants to which
     * surveys will be posed.
     * @param scope Survey scope
     * @return Executor instance
     */
    public E setSurveyScope(Scope<Address> scope);
    
    /**
     * Get the scope of experiment participants to which
     * surveys will be posed.
     * @return Survey scope
     */
    public Scope<Address> getSurveyScope();
    
    /**
     * Provide a timeout after which the executed experiment
     * will be finalised.
     * @param timeout Time in ms
     * @return Executor instance
     */
    public E endExperimentAfter(long timeout);
    
    /**
     * Get timeout value which will be used to cap
     * experiment duration.
     * @return Time in ms
     */
    public long getExperimentTimeout();
  
    /**
     * Execute a generated experiment based on the
     * current executor configuration.
     * @return Experiment instance
     * @throws ExecutionFailedException Experiment was unable
     * to be executed in the given event space.
     */
    public S execute() throws ExecutionFailedException;
}