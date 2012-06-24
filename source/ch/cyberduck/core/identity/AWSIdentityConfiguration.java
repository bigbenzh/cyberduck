package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.ErrorListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainFactory;
import ch.cyberduck.core.threading.BackgroundException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;

/**
 * @version $Id:$
 */
public class AWSIdentityConfiguration implements IdentityConfiguration {

    private Host host;

    /**
     * Callback
     */
    private ErrorListener listener;

    public AWSIdentityConfiguration(Host host, ErrorListener listener) {
        this.host = host;
        this.listener = listener;
    }

    @Override
    public void deleteUser(final String username) {
        try {
            // Create new IAM credentials
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(
                    new com.amazonaws.auth.AWSCredentials() {
                        public String getAWSAccessKeyId() {
                            return host.getCredentials().getUsername();
                        }

                        public String getAWSSecretKey() {
                            return host.getCredentials().getPassword();
                        }
                    }
            );
            final ListAccessKeysResult keys
                    = iam.listAccessKeys(new ListAccessKeysRequest().withUserName(username));

            for(AccessKeyMetadata key : keys.getAccessKeyMetadata()) {
                iam.deleteAccessKey(new DeleteAccessKeyRequest(key.getAccessKeyId()).withUserName(username));
            }

            final ListUserPoliciesResult policies = iam.listUserPolicies(new ListUserPoliciesRequest(username));
            for(String policy : policies.getPolicyNames()) {
                iam.deleteUserPolicy(new DeleteUserPolicyRequest(username, policy));
            }
            iam.deleteUser(new DeleteUserRequest(username));
        }
        catch(AmazonClientException e) {
            listener.error(new BackgroundException(host, null, "Cannot write user configuration", e));
        }
    }

    @Override
    public Credentials getUserCredentials(final String username) {
        try {
            // Create new IAM credentials
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(
                    new com.amazonaws.auth.AWSCredentials() {
                        public String getAWSAccessKeyId() {
                            return host.getCredentials().getUsername();
                        }

                        public String getAWSSecretKey() {
                            return host.getCredentials().getPassword();
                        }
                    }
            );
            try {
                final ListAccessKeysResult keys
                        = iam.listAccessKeys(new ListAccessKeysRequest().withUserName(username));

                for(AccessKeyMetadata key : keys.getAccessKeyMetadata()) {
                    final String secret = KeychainFactory.instance().getPassword(host.getProtocol().getScheme().name(), host.getPort(),
                            host.getHostname(), key.getAccessKeyId());
                    return new Credentials(key.getAccessKeyId(), secret) {
                        @Override
                        public String getUsernamePlaceholder() {
                            return host.getProtocol().getUsernamePlaceholder();
                        }

                        @Override
                        public String getPasswordPlaceholder() {
                            return host.getProtocol().getPasswordPlaceholder();
                        }
                    };
                }
            }
            catch(NoSuchEntityException e) {
                return null;
            }
        }
        catch(AmazonClientException e) {
            listener.error(new BackgroundException(host, null, "Cannot read user configuration", e));
        }
        return null;
    }

    @Override
    public void createUser(final String username) {
        final String document = "{" +
                "\"Statement\": [" +
                "{" +
                "  \"Action\": [" +
                "    \"s3:Get*\"," +
                "    \"s3:List*\"," +
                "    \"s3:ListAllMyBuckets\"" +
                "  ]," +
                "  \"Effect\": \"Allow\"," +
                "  \"Resource\": \"arn:aws:s3:::*\"" +
                "}," +
                "{" +
                "  \"Action\": [" +
                "    \"cloudfront:Get*\"," +
                "    \"cloudfront:List*\"" +
                "  ]," +
                "  \"Effect\": \"Allow\"," +
                "  \"Resource\": \"*\"" +
                "}" +
                "]" +
                "}";
        this.createUser(username, document);
    }

    protected void createUser(final String username, String policy) {
        try {
            // Create new IAM credentials
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(
                    new com.amazonaws.auth.AWSCredentials() {
                        public String getAWSAccessKeyId() {
                            return host.getCredentials().getUsername();
                        }

                        public String getAWSSecretKey() {
                            return host.getCredentials().getPassword();
                        }
                    }
            );
            User user;
            try {
                user = iam.createUser(new CreateUserRequest().withUserName(username)).getUser();
            }
            catch(EntityAlreadyExistsException e) {
                user = iam.getUser(new GetUserRequest().withUserName(username)).getUser();
            }
            final CreateAccessKeyResult key = iam.createAccessKey(
                    new CreateAccessKeyRequest().withUserName(user.getUserName()));

            // Write policy document to get read access
            iam.putUserPolicy(new PutUserPolicyRequest(user.getUserName(), "Policy", policy));

            // Save secret
            KeychainFactory.instance().addPassword(
                    host.getProtocol().getScheme().name(), host.getPort(), host.getHostname(),
                    key.getAccessKey().getAccessKeyId(), key.getAccessKey().getSecretAccessKey());
        }
        catch(AmazonClientException e) {
            listener.error(new BackgroundException(host, null, "Cannot write user configuration", e));
        }
    }
}
