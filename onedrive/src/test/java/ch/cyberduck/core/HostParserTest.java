package ch.cyberduck.core;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


import ch.cyberduck.core.onedrive.OneDriveProtocol;
import ch.cyberduck.core.onedrive.SharepointProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class HostParserTest {

    @Test
    public void testParseUsername() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Collections.singletonList(new OneDriveProtocol() {
            @Override
            public String getDefaultHostname() {
                return "graph.microsoft.com";
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isUsernameConfigurable() {
                return false;
            }
        })));
        final Host host = new HostParser(factory).get("onedrive:user@/");
        assertEquals("user", host.getCredentials().getUsername());
    }
}
