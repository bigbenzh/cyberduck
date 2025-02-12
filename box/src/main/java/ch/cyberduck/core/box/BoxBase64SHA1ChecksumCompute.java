package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Base64;

import java.io.InputStream;

public class BoxBase64SHA1ChecksumCompute extends AbstractChecksumCompute {

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws ChecksumException {
        return new Checksum(HashAlgorithm.sha1, Base64.encodeBase64String(this.digest("SHA-1",
                this.normalize(in, status))));
    }
}
