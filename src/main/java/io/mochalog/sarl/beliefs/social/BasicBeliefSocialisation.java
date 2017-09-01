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

import io.mochalog.sarl.beliefs.query.BeliefQuery;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.SpaceID;

import io.sarl.lang.util.ClearableReference;
import io.sarl.lang.util.SynchronizedSet;

import io.sarl.core.ExternalContextAccess;
import io.sarl.util.Scopes;

import java.security.Principal;

import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Skill allowing for basic belief-reasoning interactions with 
 * other agents in a multi-agent domain.
 */
public class BasicBeliefSocialisation extends Skill implements SocialBeliefs
{
    // Principal identifier through which the
    // skill can operate on behalf of the owner agent
    // in restricted access domains
    private Principal principal;
    
    // Buffered reference to built-in ExternalContextAccess skill
    private ClearableReference<Skill> bufferedExternalContextAccessSkill;

    /**
     * Constructor.
     * @param principal Principal for accessing restricted
     * domains
     */
    public BasicBeliefSocialisation(Principal principal)
    {
        super();
        this.principal = principal;
    }
    
    /**
     * Constructor.
     * @param agent Owner agent
     */
    public BasicBeliefSocialisation(Agent agent)
    {
        this(agent, null);
    }
    
    /**
     * Constructor.
     * @param agent Owner agent
     * @param principal Principal for accessing restricted
     * domains on behalf of the skill owner
     */
    public BasicBeliefSocialisation(Agent agent, Principal principal)
    {
        super(agent);
        this.principal = principal;
    }
    
    @Override
    public void askIn(EventSpace space, Scope<Address> scope, String query, Object... args)
    {
        askIn(space, scope, new BeliefQuery(query, args));
    }
    
    @Override
    public void askIn(EventSpace space, Scope<Address> scope, BeliefQuery query)
    {
        setSourceToMe(query, space);
        space.emit(query, scope);
    }

    @Override
    public void tellIn(EventSpace space, Scope<Address> scope, BeliefDisclosure disclosure)
    {
        setSourceToMe(disclosure, space);
        space.emit(disclosure, scope);
    }

    @Override
    public void answer(BeliefQuery query, BeliefDisclosure disclosure)
    {
        // Fetch sender data to allow reply
        Address querySource = query.getSource();
        EventSpace querySpace = getSpaceEventEmittedIn(query);
        
        setSourceToMe(disclosure, querySpace);
        // Tell the source agent the belief answer
        tellIn(querySpace, Scopes.addresses(querySource), disclosure);
    }

    @Override
    public void isBelievedByAll(EventSpace space, Scope<Address> scope, BeliefQuery query, long timeout,
        Procedure1<? super Boolean> plan)
    {
        SynchronizedSet<UUID> participants = space.getParticipants();
        
        // Conduct social experiment, evaluating based on whether
        // query is believed by all participants
        conductSocialExperiment(space, scope, query, 
            (experiment, disclosure) ->
            {
                if (disclosure.isBelieved)
                {
                    // Belief of the query is supporting evidence
                    // towards hypothesis that all believe
                    experiment.addPositiveResponse(disclosure);
                    
                    // Experiment should only continue given we have not
                    // received positive responses from all participants yet
                    SynchronizedSet<UUID> positiveResponders = experiment.getPositiveResponders();
                    if (!positiveResponders.equals(participants))
                    {
                        return false;
                    }
                }
                
                // Experiment is concluded, deploy the plan
                // with the experiment result
                plan.apply(disclosure.isBelieved);
                return true;
            }
        );
    }
    
    @Override
    public void isBelievedByAny(EventSpace space, Scope<Address> scope, BeliefQuery query, long timeout,
            Procedure1<? super Boolean> plan)
    {
    }

    @Override
    public void isBelievedByNone(EventSpace space, Scope<Address> scope, BeliefQuery query, long timeout,
            Procedure1<? super Boolean> plan)
    {
    }

    @Override
    public SocialExperiment conductSocialExperiment(EventSpace space, Scope<Address> scope, BeliefQuery query,
            Function2<? super SocialExperiment, ? super BeliefDisclosure, ? extends Boolean> evaluator)
    {
        SocialExperiment experiment = new SocialExperiment(space, principal, evaluator);
        experiment.survey(query, scope);
        
        return experiment;
    }
    
    /**
     * Set the principal used to allow skill to
     * operate on behalf of owner agent in restricted
     * domains.
     * @param principal Principal to use
     */
    public void setPrincipal(Principal principal)
    {
        this.principal = principal;
    }
    
    /**
     * Ensure event source has been correctly attributed
     * to skill owner.
     * @param event Event to set source for
     * @param spaceToEmitIn Space event will be emitted in
     */
    // Necessary currently due to
    // https://github.com/sarl/sarl/pull/707 being unmerged
    // as yet - Event source not automatically sent for
    // non-default space emission
    protected void setSourceToMe(Event event, EventSpace spaceToEmitIn)
    {
        Address source = spaceToEmitIn.getAddress(getID());
        event.setSource(source);
    }
    
    /**
     * Fetch the space a received event was emitted in.
     * @param event Received event
     * @return Space event emitted in
     */
    protected EventSpace getSpaceEventEmittedIn(Event event)
    {
        // Get the ID of the space the event originated
        // from
        Address source = event.getSource();   
        SpaceID spaceId = source.getSpaceId();
        
        // Retrieve the context the space with the
        // given ID belongs to
        AgentContext sourceContext = getExternalContextAccessSkill()
            .getContext(spaceId.getContextID());
        
        return sourceContext.getSpace(spaceId.getID()); 
    }
    
    // Following skill buffering implementations are sourced and modified from
    // BIC skill implementations in Janus runtime available at
    // https://github.com/sarl/sarl/blob/master/sre/
    // io.janusproject/io.janusproject.plugin/src/io/janusproject/kernel/bic

    /**
     * Fetch the attached ExternalContextAccess skill.
     * @return ExternalContextAccess skill
     */
    protected final ExternalContextAccess getExternalContextAccessSkill()
    {
        if (this.bufferedExternalContextAccessSkill == null || this.bufferedExternalContextAccessSkill.get() == null)
        {
            // Cache the skill for faster access later
            this.bufferedExternalContextAccessSkill = $getSkill(ExternalContextAccess.class);
        }

        return $castSkill(ExternalContextAccess.class, this.bufferedExternalContextAccessSkill);
    }
}
