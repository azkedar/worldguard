// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.FlatRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import java.util.ArrayList;
import java.util.HashSet;

public class ApplicableRegionSetTest {

    @Test
    public void testStateFlagPriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag STATE1 = new StateFlag(null, false);
        StateFlag STATE2 = new StateFlag(null, false);

        region = mock.add(0);
        region.setFlag(STATE1, StateFlag.State.ALLOW);
        region.setFlag(STATE2, StateFlag.State.DENY);
        
        region = mock.add(1);
        region.setFlag(STATE1, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.allows(STATE1));
        assertFalse(set.allows(STATE2));
    }

    @Test
    public void testNonStateFlagPriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StringFlag STRING1 = new StringFlag(null);
        StringFlag STRING2 = new StringFlag(null);
        StringFlag STRING3 = new StringFlag(null);
        StringFlag STRING4 = new StringFlag(null);

        region = mock.add(0);
        region.setFlag(STRING1, "Beans");
        region.setFlag(STRING2, "Apples");

        region = mock.add(1);
        region.setFlag(STRING1, "Cats");
        region.setFlag(STRING3, "Bananas");

        ApplicableRegionSet set = mock.getApplicableSet();
        assertEquals(set.getFlag(STRING1), "Cats");
        assertEquals(set.getFlag(STRING2), "Apples");
        assertEquals(set.getFlag(STRING3), "Bananas");
        assertEquals(set.getFlag(STRING4), null);
    }

    @Test
    public void testStateFlagMultiplePriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StringFlag STRING1 = new StringFlag(null);
        StringFlag STRING2 = new StringFlag(null);
        StringFlag STRING3 = new StringFlag(null);
        StringFlag STRING4 = new StringFlag(null);

        region = mock.add(0);
        region.setFlag(STRING1, "Beans");
        region.setFlag(STRING2, "Apples");
        region.setFlag(STRING3, "Dogs");

        region = mock.add(1);
        region.setFlag(STRING1, "Cats");
        region.setFlag(STRING3, "Bananas");

        region = mock.add(10);
        region.setFlag(STRING3, "Strings");

        ApplicableRegionSet set = mock.getApplicableSet();
        assertEquals(set.getFlag(STRING1), "Cats");
        assertEquals(set.getFlag(STRING2), "Apples");
        assertEquals(set.getFlag(STRING3), "Strings");
        assertEquals(set.getFlag(STRING4), null);
    }

    @Test
    public void testStateGlobalDefault() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag STATE1 = new StateFlag(null, false);
        StateFlag STATE2 = new StateFlag(null, false);
        StateFlag STATE3 = new StateFlag(null, false);
        StateFlag STATE4 = new StateFlag(null, true);
        StateFlag STATE5 = new StateFlag(null, true);
        StateFlag STATE6 = new StateFlag(null, true);

        region = mock.global();
        region.setFlag(STATE1, StateFlag.State.ALLOW);
        region.setFlag(STATE2, StateFlag.State.DENY);
        region.setFlag(STATE4, StateFlag.State.ALLOW);
        region.setFlag(STATE5, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.allows(STATE1));
        assertFalse(set.allows(STATE2));
        assertFalse(set.allows(STATE3));
        assertTrue(set.allows(STATE4));
        assertFalse(set.allows(STATE5));
        assertTrue(set.allows(STATE6));
    }

    @Test
    public void testStateGlobalWithRegionsDefault() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag STATE1 = new StateFlag(null, false);
        StateFlag STATE2 = new StateFlag(null, false);
        StateFlag STATE3 = new StateFlag(null, false);
        StateFlag STATE4 = new StateFlag(null, true);
        StateFlag STATE5 = new StateFlag(null, true);
        StateFlag STATE6 = new StateFlag(null, true);

        region = mock.global();
        region.setFlag(STATE1, StateFlag.State.ALLOW);
        region.setFlag(STATE2, StateFlag.State.DENY);
        region.setFlag(STATE4, StateFlag.State.ALLOW);
        region.setFlag(STATE5, StateFlag.State.DENY);

        region = mock.add(0);
        region.setFlag(STATE1, StateFlag.State.DENY);
        region.setFlag(STATE2, StateFlag.State.DENY);
        region.setFlag(STATE4, StateFlag.State.DENY);
        region.setFlag(STATE5, StateFlag.State.DENY);

        region = mock.add(1);
        region.setFlag(STATE5, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.allows(STATE1));
        assertFalse(set.allows(STATE2));
        assertFalse(set.allows(STATE3));
        assertFalse(set.allows(STATE4));
        assertTrue(set.allows(STATE5));
        assertTrue(set.allows(STATE6));
    }

}
