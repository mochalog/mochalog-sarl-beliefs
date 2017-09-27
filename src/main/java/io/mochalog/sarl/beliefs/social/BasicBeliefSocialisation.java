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
import io.mochalog.sarl.beliefs.social.analysis.SocialExperimentBallot;
import io.mochalog.sarl.beliefs.social.analysis.SocialPoll;
import io.mochalog.sarl.beliefs.social.analysis.SocialPollBallot;
import io.mochalog.sarl.beliefs.social.analysis.SocialPollImpl;
import io.mochalog.sarl.beliefs.util.EventSpaceUtils;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Skill;

import io.sarl.lang.util.ClearableReference;
import io.sarl.lang.util.SynchronizedSet;

import io.sarl.core.ExternalContextAccess;
import io.sarl.util.Scopes;

import java.security.Principal;

import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

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
    public boolean answer(BeliefQuery query, BeliefDisclosure disclosure)
    {
        // Fetch sender data to allow reply
        Address querySource = query.getSource();
        if (querySource == null)
        {
            // Unable to answer query with no source specified
            return false;
        }
        
        EventSpace querySpace = EventSpaceUtils.getSpaceEventEmittedIn(query, getExternalContextAccessSkill());
        
        setSourceToMe(disclosure, querySpace);
        // Tell the source agent the belief answer
        tellIn(querySpace, Scopes.addresses(querySource), disclosure);
        
        return true;
    }

    @Override
    public SocialPoll isBelievedByAll(EventSpace space, Scope<Address> scope, BeliefQuery query, 
        long timeout, Procedure1<? super Boolean> plan)
    {
        return allBelieveThat(space, scope, query, true, timeout, plan);
    }
    
    @Override
    public SocialPoll isBelievedByAny(EventSpace space, Scope<Address> scope, BeliefQuery query, 
        long timeout, Procedure1<? super Boolean> plan)
    {
        // Conduct poll, evaluating based on whether
        // query is believed by any participants
        return conductPoll(
            space, scope, query, timeout,
            (e, r) -> isBelievedByAnyEvaluator(space, e, r), plan
        );
    }
    
    /**
     * Evaluation function definition for the isBelievedByAny poll
     * interface.
     * @param space Space in which experiment is being conducted
     * @param poll Social poll access
     * @param response Current response
     */
    private void isBelievedByAnyEvaluator(EventSpace space, SocialPollBallot poll,
        BeliefDisclosure response)
    {
        UUID sourceId = response.getSource().getUUID();
        if (response.isBelieved && EventSpaceUtils.isMemberOfEventSpace(sourceId, space))
        {
            poll.finalisePollResult(true);
        }
    }

    @Override
    public SocialPoll isBelievedByNone(EventSpace space, Scope<Address> scope, BeliefQuery query, 
        long timeout, Procedure1<? super Boolean> plan)
    {
        return allBelieveThat(space, scope, query, false, timeout, plan);
    }
    
    @Override
    public SocialPoll allBelieveThat(EventSpace space, Scope<Address> scope, BeliefQuery query, 
        boolean isTrue, long timeout, Procedure1<? super Boolean> plan)
    {
        // Conduct poll, evaluating based on whether
        // all participants agree that a given query is either true or false
        return conductPoll(
            space, scope, query, timeout,
            (e, r) -> allBelieveThatEvaluator(isTrue, e, r), plan
        );
    }
    
    /**
     * Evaluation function definition for the allBelieveThat poll
     * interface.
     * @param isTrue Whether all should agree that query is true, or not.
     * @param poll Social poll access
     * @param response Current response
     */
    private void allBelieveThatEvaluator(boolean isTrue, SocialPollBallot poll,
        BeliefDisclosure response)
    {
        if (response.isBelieved == isTrue)
        {
            // Agreement with expected result is supporting evidence
            // towards hypothesis that all agree
            poll.addPositiveResponse(response);
            
            // Poll is finished if we have received positive
            // responses from all participants
            SynchronizedSet<UUID> participants = poll.getParticipants();
            if (poll.getPositiveResponders().equals(participants))
            {
                poll.finalisePollResult(true);
            }
        }
        else
        {
            poll.finalisePollResult(false);
        }
    }
    
    @Override
    public SocialPoll conductPoll(EventSpace space, Scope<Address> scope, BeliefQuery query, 
        long timeout, Procedure2<? super SocialPollBallot, ? super BeliefDisclosure> evaluator,
        Procedure1<? super Boolean> onResult)
    {
        SocialPollImpl.Executor executor = new SocialPollImpl.Executor();
        return executor
            .setSpace(space)
            .setAccessPrincipal(principal)
            .addSurvey(query)
            .setSurveyScope(scope)
            .endExperimentAfter(timeout)
            .setEvaluator(evaluator)
            .onPollResult(onResult)
            .execute();
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
