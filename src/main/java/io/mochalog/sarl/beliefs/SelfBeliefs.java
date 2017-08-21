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

package io.mochalog.sarl.beliefs;

import io.mochalog.bridge.prolog.PrologContext;
import io.mochalog.bridge.prolog.SandboxedPrologContext;
import io.mochalog.bridge.prolog.query.Query;

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Skill;

import io.sarl.core.Behaviors;
import io.janusproject.kernel.bic.InternalEventBusCapacity;

import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.util.ClearableReference;

import java.util.Collection;
import java.util.UUID;

/**
 * Skill allowing for management of knowledge base
 * constrained to single agent
 */
public class SelfBeliefs extends Skill implements Beliefs
{
    /**
     * Behavior allowing for the hooking of a belief
     * query to a given SARL event
     * @param <T> SARL event to hook on to
     */
    private class EventBoundBeliefQuery<T extends Event> extends Behavior 
    {
        // Query to perform
        private Query query;
        
        /**
         * Constructor.
         * @param agent Owner agent
         * @param query Query to perform
         */
        public EventBoundBeliefQuery(Agent agent, Query query)
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
            handlers.add(
                () -> knowledgeBase.prove(query)
            );
        }
        
        /**
         * Set the Prolog query to perform on event
         * @param query Query to perform
         */
        public void setQuery(Query query)
        {
            this.query = query;
        }
    }
    
    // Interface to Prolog knowledge base
    private PrologContext knowledgeBase;
    
    // Buffered reference to built-in InternalEventBusCapacity skill
    private ClearableReference<Skill> bufferedInternalEventBusSkill;
    // Buffered reference to built-in Behaviors skill
    private ClearableReference<Skill> bufferedBehaviorsSkill;
    
    /**
     * Constructor.
     * @param id ID to assign to knowledge base
     */
    public SelfBeliefs(UUID id)
    {
        super();
        initialiseKnowledgeBase(id);
    }
    
    /**
     * Constructor.
     * @param agent Owner agent
     */
    public SelfBeliefs(Agent agent)
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
        String module = id.toString();
        knowledgeBase = new SandboxedPrologContext(module);
    }
    
    @Override
    public <T extends Event> void onSensed(String text, Object... args) 
    {
        Query query = Query.format(text, args);
        // Bind the given event type to a belief query behavior
        Behavior eventBoundBeliefQuery = new EventBoundBeliefQuery<T>(getOwner(), query);
        // Attach the generated behavior unit to the behavior registry
        getBehaviorsSkill().registerBehavior(eventBoundBeliefQuery);
    }
    
    // Following skill buffering implementations are sourced and modified from 
    // BIC skill implementations in Janus runtime available at https://github.com/sarl/sarl/blob/master/sre/
    // io.janusproject/io.janusproject.plugin/src/io/janusproject/kernel/bic
    
    /**
     * Fetch the attached InternalEventBusCapacity skill
     * @return InternalEventBusCapacity skill
     */
    protected final InternalEventBusCapacity getInternalEventBusCapacitySkill() {
        if (this.bufferedInternalEventBusSkill == null || this.bufferedInternalEventBusSkill.get() == null) 
        {
            // Cache the skill for faster access later
            this.bufferedInternalEventBusSkill = $getSkill(InternalEventBusCapacity.class);
        }
        
        return $castSkill(InternalEventBusCapacity.class, this.bufferedInternalEventBusSkill);
    }
    
    /**
     * Fetch the attached Behaviors skill
     * @return Behaviors skill
     */
    protected final Behaviors getBehaviorsSkill() {
        if (this.bufferedBehaviorsSkill == null || this.bufferedBehaviorsSkill.get() == null) 
        {
            // Cache the skill for faster access later
            this.bufferedBehaviorsSkill = $getSkill(Behaviors.class);
        }
        
        return $castSkill(Behaviors.class, this.bufferedBehaviorsSkill);
    }
}
