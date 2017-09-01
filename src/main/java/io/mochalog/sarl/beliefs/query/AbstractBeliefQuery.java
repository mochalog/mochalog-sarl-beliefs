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

package io.mochalog.sarl.beliefs.query;

import io.mochalog.bridge.prolog.query.Query;

import io.sarl.lang.core.Event;

import java.util.Objects;

/**
 * Abstract event wrapper of the Mochalog Query
 * object. Necessary to ensure equals() and hashCode()
 * implementations are correctly overridden.
 */
abstract class AbstractBeliefQuery extends Event
{
    // Version UID assigned to this object for event
    // serialization purposes
    private static final long serialVersionUID = -5177651570317784287L;
    
    // Mochalog Query being asked
    public final Query queryToAsk;
    
    /**
     * Constructor.
     * @param queryToAsk Query intended to be asked
     * @param args Arguments to format query with
     */
    public AbstractBeliefQuery(final String queryToAsk, final Object... args) 
    {
        this(
            Query.format(queryToAsk, args)
        );
    }
    
    /**
     * Constructor.
     * @param queryToAsk Query intended to be asked
     */
    public AbstractBeliefQuery(final Query queryToAsk) 
    {
        this.queryToAsk = queryToAsk;
    }
    
    @Override
    public boolean equals(final Object obj) 
    {
        if (super.equals(obj))
        {
            // Ensure query specifics are taken into account when
            // checking for event equality
            AbstractBeliefQuery beliefQuery = (AbstractBeliefQuery) obj;
            return queryToAsk.equals(beliefQuery.queryToAsk);
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        // Ensure query specifics are taken into account
        // when computing event hashcode
        return Objects.hash(queryToAsk, super.hashCode());
    }
}
