package com.douineau.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.*;
//import java.util.stream.Stream;

public class PropertyFileUserStorageProvider
        implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, CredentialInputUpdater {


    protected KeycloakSession session;
    protected Properties properties;
    protected ComponentModel model;
    // map of loaded users in this transaction
    protected Map<String, UserModel> loadedUsers = new HashMap<>();

    public PropertyFileUserStorageProvider(KeycloakSession session, ComponentModel model, Properties properties) {
        this.session = session;
        this.model = model;
        this.properties = properties;
    }

    /**
     * Checks to see if the credential type is password.
     * If it is, a ReadOnlyException is thrown.
     * @param realm
     * @param user
     * @param input
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (input.getType().equals(CredentialModel.PASSWORD)) throw new ReadOnlyException("user is read only for this update");

        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {

    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realmModel, UserModel userModel) {
        return Collections.EMPTY_SET;
    }

//    @Override
//    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
//        return null;
//    }

    /**
     * Returns whether validation is supported for a specific credential type.
     * We check to see if the credential type is password.
     * @param credentialType
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(CredentialModel.PASSWORD);
    }

    /**
     * To determine if a specific credential type is configured for the user.
     * This method checks to see that the password is set for the user.
     * @param realm
     * @param user
     * @param credentialType
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        String password = properties.getProperty(user.getUsername());
       return credentialType.equals(CredentialModel.PASSWORD) && password != null;
    }

    /**
     * Responsible for validating passwords.
     * We make sure that we support the credential type and also that it is an instance of UserCredentialModel.
     * When a user logs in through the login page, the plain text of the password input is put into an instance of UserCredentialModel.
     * @param realm
     * @param user
     * @param input
     * @return
     */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())) return false;

       String password = properties.getProperty(user.getUsername());
       if (password == null) return false;
       return password.equals(input.getChallengeResponse());
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
       StorageId storageId = new StorageId(id);
       String username = storageId.getExternalId();
       return getUserByUsername(username, realm);
    }

    /**
     * Invoked by the Keycloak login page when a user logs in.
     * In our implementation we first check the loadedUsers map to see if the user has already been loaded within this transaction.
     * If it hasn’t been loaded we look in the property file for the username.
     * If it exists we create an implementation of UserModel, store it in loadedUsers for future reference, and return this instance.
     * @param username
     * @param realm
     * @return
     */
    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        UserModel adapter = loadedUsers.get(username);
        if (adapter == null) {
            String password = properties.getProperty(username);
            if (password != null) {
                adapter = createAdapter(realm, username);
                loadedUsers.put(username, adapter);
            }
        }
        return adapter;

    }

    /**
     * It automatically generates a user id based on the required storage id format using the username of the user as the external id :
     * "f:" + component id + ":" + username
     * @param realm
     * @param username
     * @return
     */
    protected UserModel createAdapter(RealmModel realm, final String username) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }
        };
    }

    @Override
    public UserModel getUserByEmail(String s, RealmModel realmModel) {
        return null;
    }

    @Override
    public void preRemove(RealmModel realm) {

    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {

    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

    }
}
