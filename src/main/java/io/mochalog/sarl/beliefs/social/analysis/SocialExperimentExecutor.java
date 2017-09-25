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
import io.mochalog.sarl.beliefs.social.BeliefDisclosure;

import io.sarl.lang.core.EventSpace;

import io.sarl.lang.util.SynchronizedSet;

import java.security.Principal;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * Interface for executor services of social experiments in event spaces.
 * <p>
 * Acts as both an experiment builder and executor.
 */
public interface SocialExperimentExecutor<S extends SocialExperiment>
{   
    /**
     * Set space to conduct executed experiment in.
     * @param space Event space
     * @return Executor instance
     */
    public SocialExperimentExecutor<S> setSpace(EventSpace space);
    
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
    public SocialExperimentExecutor<S> setAccessPrincipal(Principal principal);
    
    /**
     * Access principal with which space access will be authenticated.
     * @return Access principal
     */
    public Principal getAccessPrincipal();
    
    /**
     * Set the evaluation function experiment will use to evaluate responses.
     * @param evaluator Evaluation function
     * @return Executor instance
     */
    public SocialExperimentExecutor<S> setEvaluator(
        Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> evaluator);
    
    /**
     * Get evaluation function used to evaluate responses.
     * @return Evaluation function
     */
    public Procedure2<? super AnalyticalSocialExperiment, ? super BeliefDisclosure> getEvaluator();

    /**
     * Add a survey query to be posed to experiment participants
     * on execution.
     * @param survey Survey to ask
     * @return Executor instance
     */
    public SocialExperimentExecutor<S> addSurvey(BeliefQuery survey);
    
    /**
     * Get all surveys which will be asked on experiment
     * execution.
     * @return Surveys to ask
     */
    public SynchronizedSet<BeliefQuery> getSurveys();
    
    /**
     * Provide a timeout after which the executed experiment
     * will be finalised.
     * @param timeout Time in ms
     * @return Executor instance
     */
    public SocialExperimentExecutor<S> endExperimentAfter(long timeout);
    
    /**
     * Get timeout value which will be used to cap
     * experiment duration.
     * @return Time in ms
     */
    public long getExperimentTimeout();
    
    /**
     * Provide callback function to be invoked when experiment finalised
     * and result collected.
     * @param callback Callback function
     * @return Executor instance
     */
    public SocialExperimentExecutor<S> onExperimentResult(Procedure1<? super Boolean> callback);
    
    /**
     * Get the callback function which will be executed
     * on experimental result collection.
     * @return Callback function
     */
    public Procedure1<? super Boolean> getExperimentResultCallback();
    
    /**
     * Execute a generated experiment based on the
     * current executor configuration.
     * @return Experiment instance
     * @throws ExecutionFailedException Experiment was unable
     * to be executed in the given event space.
     */
    public S execute() throws ExecutionFailedException;
}