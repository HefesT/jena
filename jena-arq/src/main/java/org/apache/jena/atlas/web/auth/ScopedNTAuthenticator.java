/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.web.auth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;

/**
 * <p>
 * A credentials based authenticator where credentials are scoped to URIs. This
 * allows for a single authenticator to present different credentials to
 * different URIs as appropriate. Works with the NTLM authentication schemes.
 * </p>
 * <p>
 * See {@link ScopedAuthenticator} for an implementation that works for the
 * Basic and Digest authentication schemes.
 * </p>
 * 
 */
public class ScopedNTAuthenticator extends AbstractScopedAuthenticator<Pair<Tuple<String>, char[]>> {

    private Map<URI, Pair<Tuple<String>, char[]>> credentials = new HashMap<>();

    /**
     * Creates an authenticator with credentials for the given URI
     * 
     * @param target
     *            URI
     * @param username
     *            User name
     * @param password
     *            Password
     * @param workstation
     *            Workstation, this is the ID of your local workstation
     * @param domain
     *            Domain, this is the domain you are authenticating in which may
     *            not necessarily be the domain your workstation is in
     */
    public ScopedNTAuthenticator(URI target, String username, char[] password, String workstation, String domain) {
        if (target == null)
            throw new IllegalArgumentException("Target URI cannot be null");
        this.credentials.put(target, Pair.create(TupleFactory.tuple(username, workstation, domain), password));
    }

    /**
     * Creates an authenticator with a set of credentials for URIs
     * 
     * @param credentials
     *            Credentials, the left of the pair should be a tuple with at
     *            least three fields where the first contains the user name, the
     *            second the workstation and the third the domain. The right of
     *            the pair should be the password.
     */
    public ScopedNTAuthenticator(Map<URI, Pair<Tuple<String>, char[]>> credentials) {
        for (Entry<URI, Pair<Tuple<String>, char[]>> entry : credentials.entrySet()) {
            if (entry.getValue() == null)
                continue;
            if (entry.getValue().getLeft() == null)
                throw new IllegalArgumentException("Credentials tuple should be non-null");
            if (entry.getValue().getLeft().len() < 3)
                throw new IllegalArgumentException(
                        "Credentials tuple should contain at least three fields, 0 = user name, 1 = workstation, 2 = domain");
            this.credentials.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds/Overwrites credentials for a given URI
     * 
     * @param target
     *            Target
     * @param username
     *            User name
     * @param password
     *            Password
     * @param workstation
     *            Workstation, this is the ID of your local workstation
     * @param domain
     *            Domain, this is the domain you are authenticating in which may
     *            not necessarily be the domain your workstation is in
     */
    public void addCredentials(URI target, String username, char[] password, String workstation, String domain) {
        if (target == null)
            throw new IllegalArgumentException("Target URI cannot be null");
        this.credentials.put(target, Pair.create(TupleFactory.tuple(username, workstation, domain), password));
    }

    @Override
    protected Credentials createCredentials(URI target) {
        Pair<Tuple<String>, char[]> credentials = this.getCredentials(target);
        if (credentials == null)
            return super.createCredentials(target);

        return new NTCredentials(credentials.getLeft().get(0), new String(credentials.getRight()), credentials.getLeft().get(1),
                credentials.getLeft().get(2));
    }

    @Override
    protected Pair<Tuple<String>, char[]> getCredentials(URI target) {
        return this.credentials.get(target);
    }

    @Override
    protected String getUserNameFromCredentials(Pair<Tuple<String>, char[]> credentials) {
        return credentials != null ? credentials.getLeft().get(0) : null;
    }

    @Override
    protected char[] getPasswordFromCredentials(Pair<Tuple<String>, char[]> credentials) {
        return credentials != null ? credentials.getRight() : null;
    }

}
