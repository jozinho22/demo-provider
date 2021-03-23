package com.douineau.readonly;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.jboss.logging.Logger;

public class PropertyFileUserStorageProviderFactory implements UserStorageProviderFactory<PropertyFileUserStorageProvider> {

    public static final String PROVIDER_NAME = "readonly-property-file";

    private static final Logger logger = Logger.getLogger(PropertyFileUserStorageProviderFactory.class);
    protected Properties properties = new Properties();

    /**
     * We simply allocate the PropertyFileUserStorageProvider class.
     * This create method will be called once per transaction.
     * @param session
     * @param model
     * @return
     */
    @Override
    public PropertyFileUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        logger.warn("session " + session);
        logger.warn("model " + model);
        logger.warn("properties " + properties);

        return new PropertyFileUserStorageProvider(session, model, properties);
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    /**
     * In our init() method implementation, we find the property file containing our user declarations from the classpath.
     * We then load the properties field with the username and password combinations stored there.
     * The Config.Scope parameter is factory configuration that can be set up within standalone.xml, standalone-ha.xml, or domain.xml.
     * @param config
     */
    @Override
    public void init(Config.Scope config) {
        logger.warn("public void init(Config.Scope config) ");

        InputStream is = getClass().getClassLoader().getResourceAsStream("/users.properties");

        if (is == null) {
            logger.warn("Could not find users.properties in classpath");
        } else {
            try {
                properties.load(is);
            } catch (IOException ex) {
                logger.error("Failed to load users.properties file \n" + ex);
            }
        }
    }

    // methods from super-super interfaces
    // probleme normalement inexistant car méthodes déjà définies par défaut
    @Override
    public void postInit(KeycloakSessionFactory var1) {

    }

    @Override
    public void close() {

    }

    @Override
    public UserStorageProvider create(KeycloakSession var1) {
        return null;
    }

    @Override
    public String getHelpText() {
        return "";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.EMPTY_LIST;
    }
    // methods from super-super interfaces

}
