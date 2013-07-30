package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.versioning.VersioningConfiguration;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3VersioningFeatureTest extends AbstractTestCase {

    @Test
    public void testGetConfigurationDisabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE));
        assertNotNull(configuration);
        assertFalse(configuration.isEnabled());
        assertFalse(configuration.isMultifactor());
        session.close();
    }

    @Test
    public void testGetConfigurationEnabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("versioning.test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE));
        assertNotNull(configuration);
        assertTrue(configuration.isEnabled());
        session.close();
    }

    @Test
    public void testSetConfiguration() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
        new S3DirectoryFeature(session).mkdir(container, null);
        final Versioning feature = new S3VersioningFeature(session);
        feature.setConfiguration(container, new DisabledLoginController(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(container).isEnabled());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(container));
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testForbidden() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        new S3VersioningFeature(session).getConfiguration(new Path("/dist.springframework.org", Path.DIRECTORY_TYPE));
    }
}
