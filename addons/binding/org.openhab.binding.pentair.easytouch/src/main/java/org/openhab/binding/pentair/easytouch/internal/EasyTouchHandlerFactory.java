/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.easytouch.internal;

import static org.openhab.binding.pentair.easytouch.BindingConstants.EASYTOUCH_THING_TYPE_UID;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;

/**
 * The {@link Pentair.EasyTouchHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bill Baird - Initial contribution
 */
public class EasyTouchHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(EASYTOUCH_THING_TYPE_UID);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(EASYTOUCH_THING_TYPE_UID)) {
            return new EasyTouchHandler(thing);
        }

        return null;
    }
}
