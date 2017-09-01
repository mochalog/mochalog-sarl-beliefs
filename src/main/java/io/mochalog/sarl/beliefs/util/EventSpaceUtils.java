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

import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpace;

import io.sarl.util.OpenEventSpace;
import io.sarl.util.RestrictedAccessEventSpace;

import java.security.Principal;

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
        return isListenerInEventSpace(listener, space);
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
        return !isListenerInEventSpace(listener, space);
    }
    
    /**
     * Assess whether a given listener is a participant
     * in the specified event space.
     * @param listener Listener object
     * @param space Space to search in
     * @return True if listener is registered, false otherwise.
     */
    public static boolean isListenerInEventSpace(EventListener listener, EventSpace space)
    {
        return space.getAddress(listener.getID()) != null;
    }
}