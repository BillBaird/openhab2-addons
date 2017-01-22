/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.easytouch;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Pentair.EasyTouchBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bill Baird - Initial contribution
 */
public class BindingConstants {

    public static final String BINDING_ID = "pentaireasytouch";

    // List of all Thing Type UIDs
    public final static ThingTypeUID EASYTOUCH_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "panel");

    // List of all Channel ids
    public final static String EASYTOUCH_CIRCUIT_1 = "equipment-circuit1";

    // List of all RS485 addresses
    public final static byte MIN_REMOTE_ADDRESS = 0x20;
    public final static byte MAX_REMOTE_ADDRESS = 0x22;
    public final static byte DEFAULT_BINDER_ADDRESS = MIN_REMOTE_ADDRESS;
    public final static byte DEFAULT_PROTOCOL_ADAPTER_ADDRESS = 0x22;

}
