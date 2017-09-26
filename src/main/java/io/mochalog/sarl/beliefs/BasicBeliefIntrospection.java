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
import io.sarl.lang.core.Skill;

import java.io.IOException;

import java.util.UUID;

/**
 * Skill allowing for management of knowledge base constrained to single agent
 */
public class BasicBeliefIntrospection extends Skill implements SelfBeliefs
{
    // Interface to Prolog knowledge base
    private PrologContext knowledgeBase;

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
    public boolean loadKnowledgeBase(String path)
    {
        try
        {
            return knowledgeBase.importFile(path);
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    @Override
    public boolean adopt(String belief, Object... args)
    {
        return adoptFirst(belief, args);
    }
    
    @Override
    public boolean adoptFirst(String belief, Object... args)
    {
        return knowledgeBase.assertLast(belief, args);
    }

    @Override
    public boolean adoptLast(String belief, Object... args)
    {
        return knowledgeBase.assertLast(belief, args);
    }
    
    @Override
    public boolean drop(String belief, Object... args)
    {
        return knowledgeBase.retract(belief, args);
    }
    
    @Override
    public boolean dropAll(String belief, Object... args)
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

    /**
     * Get the name associated with the underlying
     * knowledge base.
     * @return Unique name of knowledge base
     */
    public String getUniqueKnowledgeBaseName()
    {
        return knowledgeBase.toString();
    }
}
