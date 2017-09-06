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

package io.mochalog.sarl.beliefs.util;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.SpaceID;

import io.sarl.core.ExternalContextAccess;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.RestrictedAccessEventSpace;

import java.security.Principal;

import java.util.UUID;

/**
 * Helper methods for accessing and interacting
 * with SARL event spaces.
 */
public class EventSpaceUtils
{
    /**
     * Register a listener to a given event space. 
     * Registration will only succeed given the event space has no
     * access restrictions.
     * @param listener Listener to register
     * @param space Event space to register to
     * @return True if registration was successful, false otherwise.
     */
    public static boolean registerInUnrestrictedEventSpace(EventListener listener, EventSpace space)
    {
        return registerInEventSpace(listener, space, null);
    }
    
    /**
     * Register a listener to a given event space.
     * Registration is agnostic of space access restrictions
     * (assuming built-in EventSpace type).
     * @param listener Listener to register (with principal attached)
     * @param space Event space to register to
     * @return True if registration was successful, false otherwise.
     */
    public static <P extends EventListener & Principal> boolean registerInEventSpace(P listener, 
        EventSpace space)
    {
        return registerInEventSpace(listener, space, listener);
    }
    
    /**
     * Register a listener to a given event space.
     * Registration is agnostic of space access restrictions
     * (assuming built-in EventSpace type).
     * @param listener Listener to register
     * @param space Event space to register to
     * @param principal Principal to identify access permissions
     * @return True if registration was successful, false otherwise.
     */
    public static boolean registerInEventSpace(EventListener listener, EventSpace space, 
        Principal principal)
    {
        // Event space has no access restrictions
        if (space instanceof OpenEventSpace)
        {
            OpenEventSpace openEventSpace = (OpenEventSpace) space;
            openEventSpace.register(listener);
        }
        // Event space has access restrictions (principal object
        // with correct permissions must be used)
        else if (space instanceof RestrictedAccessEventSpace)
        {
            // Principal must exist in order for access to
            // be possible
            if (principal == null)
            {
                return false;
            }
            
            // Attempt to register to space
            RestrictedAccessEventSpace restrictedAccessEventSpace = 
                (RestrictedAccessEventSpace) space;
            restrictedAccessEventSpace.register(listener, principal);
        }
        
        // Ensure that registration was successful
        return isMemberOfEventSpace(listener, space);
    }
    
    /**
     * Unregister a listener from a given event space. Procedure
     * is agnostic of the space access restrictions (assuming built-in
     * EventSpace type).
     * @param listener Listener to unregister
     * @param space Space to unregister from
     * @return True if deregistration was successful, false otherwise.
     */
    public static boolean unregisterFromEventSpace(EventListener listener, EventSpace space)
    {
        // Event space has no access restrictions
        if (space instanceof OpenEventSpace)
        {
            OpenEventSpace openEventSpace = (OpenEventSpace) space;
            openEventSpace.unregister(listener);
        }
        // Event space has access restrictions
        else if (space instanceof RestrictedAccessEventSpace)
        {
            RestrictedAccessEventSpace restrictedAccessEventSpace = 
                (RestrictedAccessEventSpace) space;
            restrictedAccessEventSpace.unregister(listener);
        }
        
        // Ensure that deregistration was successful
        return !isMemberOfEventSpace(listener, space);
    }
    
    /**
     * Assess whether a given listener is a participant
     * in the specified event space.
     * @param listener Listener object
     * @param space Space to search in
     * @return True if listener is registered, false otherwise.
     */
    public static boolean isMemberOfEventSpace(EventListener listener, EventSpace space)
    {
        return isMemberOfEventSpace(listener.getID(), space);
    }
    
    /**
     * Assess whether a participant with the given ID exists
     * in the specified event space.
     * @param id Participant ID
     * @param space Space to search in
     * @return True if participant exists, false otherwise.
     */
    public static boolean isMemberOfEventSpace(UUID id, EventSpace space)
    {
        return space.getAddress(id) != null;
    }
    
    /**
     * Fetch the event space a received event was emitted in.
     * <p>
     * Access is <i>strictly required</i> to the enclosing context
     * via the given ExternalContextAccess capacity.
     * @param event Event received
     * @param contextAccess Access to enclosing context
     * @return Space event was emitted in
     * @throws IllegalArgumentException No space information available or
     * context in which event was emitted is not accessible through
     * the given capacity.
     */
    public static EventSpace getSpaceEventEmittedIn(Event event, ExternalContextAccess contextAccess)
        throws IllegalArgumentException
    {
        // Get the ID of the space the event originated
        // from
        Address source = event.getSource();
        if (source == null)
        {
            throw new IllegalArgumentException("Specified event has no space information.");
        }
        
        SpaceID spaceId = source.getSpaceId();

        try 
        {
            // Retrieve the context the space with the
            // given ID belongs to
            AgentContext sourceContext = contextAccess
                .getContext(spaceId.getContextID());
            return sourceContext.getSpace(spaceId.getID()); 
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Access to context in which " + 
                "event was emitted is not granted through the give ExternalContextAccess " +
                "capacity.");
        }
    }
}