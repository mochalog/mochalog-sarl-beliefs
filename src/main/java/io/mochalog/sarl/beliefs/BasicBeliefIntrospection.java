/**
 * Copyright 2017 The Mochalog-SARL-Beliefs Authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.mochalog.sarl.beliefs;

import io.mochalog.sarl.beliefs.query.BeliefQuery;

import io.mochalog.bridge.prolog.PrologContext;
import io.mochalog.bridge.prolog.SandboxedPrologContext;
import io.mochalog.bridge.prolog.query.QuerySolution;
import io.mochalog.bridge.prolog.query.QuerySolutionList;

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Skill;

import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.util.ClearableReference;

import io.sarl.core.Behaviors;
import io.sarl.core.Schedules;

import java.io.IOException;
import java.nio.file.Path;

import java.util.Collection;
import java.util.UUID;

/**
 * Skill allowing for management of knowledge base constrained to single agent
 */
public class BasicBeliefIntrospection extends Skill implements SelfBeliefs
{
    /**
     * Behavior allowing for the hooking of a belief query to a given SARL event
     * @param <T> SARL event to hook on to
     */
    private class EventBoundBeliefQuery<T extends Event> extends Behavior
    {
        // Query to perform
        private BeliefQuery query;

        /**
         * Constructor.
         * @param agent Owner agent
         * @param query Query to perform
         */
        public EventBoundBeliefQuery(Agent agent, BeliefQuery query)
        {
            super(agent);
            setQuery(query);
        }

        /**
         * SARL behavior unit binding given event type to the specified query
         * @param event Event instance
         * @param handlers Asynchronous event handlers
         */
        @PerceptGuardEvaluator
        public void onEventGuard(final T event, Collection<Runnable> handlers)
        {
            // Asynchronously perform a Prolog query
            handlers.add(() -> believes(query));
        }

        /**
         * Set the Prolog query to perform on event
         * @param query Query to perform
         */
        public void setQuery(BeliefQuery query)
        {
            this.query = query;
        }
    }

    // Interface to Prolog knowledge base
    private PrologContext knowledgeBase;

    // Buffered reference to built-in Behaviors skill
    private ClearableReference<Skill> bufferedBehaviorsSkill;
    // Buffered reference to built-in Behaviors skill
    private ClearableReference<Skill> bufferedSchedulesSkill;

    /**
     * Constructor.
     * @param id ID to assign to knowledge base
     */
    public BasicBeliefIntrospection(UUID id)
    {
        super();
        initialiseKnowledgeBase(id);
    }

    /**
     * Constructor.
     * @param agent Owner agent
     */
    public BasicBeliefIntrospection(Agent agent)
    {
        super(agent);
        initialiseKnowledgeBase(agent.getID());
    }

    /**
     * Initialise interface to Prolog knowledge base
     * @param id ID to assign to knowledge base
     */
    private void initialiseKnowledgeBase(UUID id)
    {
        // Ensure the module name is a valid Prolog
        // atom
        String module = "agent_" + id.toString().replace('-', '_');
        knowledgeBase = new SandboxedPrologContext(module);
    }

    @Override
    public boolean load(Path path)
    {
        try
        {
            return knowledgeBase.loadFile(path);
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    @Override
    public boolean adopt(String belief, Object... args)
    {
        return knowledgeBase.assertLast(belief, args);
    }
    
    @Override
    public boolean renounce(String belief, Object... args)
    {
        return knowledgeBase.retract(belief, args);
    }
    
    @Override
    public boolean renounceAll(String belief, Object... args)
    {
        return knowledgeBase.retractAll(belief, args);
    }
    
    @Override
    public boolean believes(String query, Object... args)
    {
        return knowledgeBase.prove(query, args);
    }

    @Override
    public boolean believes(BeliefQuery query)
    {
        return knowledgeBase.prove(query.queryToAsk);
    }

    @Override
    public QuerySolution ask(String query, Object... args)
    {
        return knowledgeBase.askForSolution(query, args);
    }

    @Override
    public QuerySolution ask(BeliefQuery query)
    {
        return knowledgeBase.askForSolution(query.queryToAsk);
    }

    @Override
    public QuerySolutionList askAll(String query, Object... args)
    {
        return knowledgeBase.askForAllSolutions(query, args);
    }

    @Override
    public QuerySolutionList askAll(BeliefQuery query)
    {
        return knowledgeBase.askForAllSolutions(query.queryToAsk);
    }
    
    @Override
    public <T extends Event> void askOn(String query, Object... args)
    {
        askOn(new BeliefQuery(query, args));
    }

    @Override
    public <T extends Event> void askOn(BeliefQuery query)
    {
        // Bind the given event type to a belief query behavior
        Behavior eventBoundBeliefQuery = new EventBoundBeliefQuery<T>(getOwner(), query);
        // Attach the generated behavior unit to the behavior registry
        getBehaviorsSkill().registerBehavior(eventBoundBeliefQuery);
    }

    // Following skill buffering implementations are sourced and modified from
    // BIC skill implementations in Janus runtime available at
    // https://github.com/sarl/sarl/blob/master/sre/
    // io.janusproject/io.janusproject.plugin/src/io/janusproject/kernel/bic

    /**
     * Fetch the attached Behaviors skill
     * @return Behaviors skill
     */
    protected final Behaviors getBehaviorsSkill()
    {
        if (this.bufferedBehaviorsSkill == null || this.bufferedBehaviorsSkill.get() == null)
        {
            // Cache the skill for faster access later
            this.bufferedBehaviorsSkill = $getSkill(Behaviors.class);
        }

        return $castSkill(Behaviors.class, this.bufferedBehaviorsSkill);
    }
    
    /**
     * Fetch the attached Schedules skill
     * @return Schedules skill
     */
    protected final Schedules getSchedulesSkill()
    {
        if (this.bufferedSchedulesSkill == null || this.bufferedSchedulesSkill.get() == null)
        {
            // Cache the skill for faster access later
            this.bufferedSchedulesSkill = $getSkill(Schedules.class);
        }

        return $castSkill(Schedules.class, this.bufferedSchedulesSkill);
    }
}
