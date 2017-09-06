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

import io.sarl.lang.core.Event;

import java.util.UUID;

/**
 * Abstract implementation of a basic DisclosureListener.
 */
public abstract class AbstractDisclosureListener implements DisclosureListener
{
    // Listener ID
    private final UUID id;
    
    /**
     * Constructor. Assigns random ID to listener.
     */
    public AbstractDisclosureListener()
    {
        // Generate random UUID
        this(UUID.randomUUID());
    }
    
    /**
     * Constructor.
     * @param id ID to assign to listener
     */
    public AbstractDisclosureListener(UUID id)
    {
        this.id = id;
    }
    
    @Override
    public UUID getID()
    {
        return id;
    }

    @Override
    public final void receiveEvent(Event event)
    {
        // Delegate event handling to overhear() method
        // given event type is BeliefDisclosure
        final Class<? extends Event> eventType = event.getClass();
        if (BeliefDisclosure.class.equals(eventType))
        {
            BeliefDisclosure disclosure = (BeliefDisclosure) event;
            overhear(disclosure);
        }
    }
}