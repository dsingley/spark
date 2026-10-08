/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.ssl;

/**
 * The keystore and truststore settings used to serve HTTPS: where the stores are and their
 * passwords, which certificate in the keystore to use, and whether clients have to present
 * a certificate of their own (mutual TLS).
 * <p>
 * Instances are immutable and created with one of the {@code create} methods, normally by
 * {@link spark.Service#secure(String, String, String, String)} and its overloads, and are then used to
 * configure the embedded server's TLS. The truststore settings are only needed to verify client
 * certificates.
 */
public class SslStores {

    /** The path of the keystore file. */
    protected final String keystoreFile;

    /** The password of the keystore. */
    protected final String keystorePassword;

    /** The alias of the certificate to use from the keystore, or null to use the default. */
    protected final String certAlias;

    /** The path of the truststore file, or null if there is none. */
    protected final String truststoreFile;

    /** The password of the truststore, or null if there is none. */
    protected final String truststorePassword;

    /** Whether clients must present a certificate. */
    protected final boolean needsClientCert;

    /**
     * Creates a Stores instance.
     *
     * @param keystoreFile the keystoreFile
     * @param keystorePassword the keystorePassword
     * @param truststoreFile the truststoreFile
     * @param truststorePassword the truststorePassword
     * @return the SslStores instance.
     */
    public static SslStores create(String keystoreFile,
                                String keystorePassword,
                                String truststoreFile,
                                String truststorePassword) {

        return new SslStores(keystoreFile, keystorePassword, null, truststoreFile, truststorePassword, false);
    }

    /**
     * Creates a Stores instance that uses the given certificate alias and does not require client
     * certificates.
     *
     * @param keystoreFile the keystoreFile
     * @param keystorePassword the keystorePassword
     * @param certAlias the alias of the certificate to use from the keystore
     * @param truststoreFile the truststoreFile
     * @param truststorePassword the truststorePassword
     * @return the SslStores instance.
     */
    public static SslStores create(String keystoreFile,
                                String keystorePassword,
                                String certAlias,
                                String truststoreFile,
                                String truststorePassword) {

        return new SslStores(keystoreFile, keystorePassword, certAlias, truststoreFile, truststorePassword, false);
    }

    /**
     * Creates a Stores instance, optionally requiring client certificates.
     *
     * @param keystoreFile the keystoreFile
     * @param keystorePassword the keystorePassword
     * @param truststoreFile the truststoreFile
     * @param truststorePassword the truststorePassword
     * @param needsClientCert whether clients must present a certificate
     * @return the SslStores instance.
     */
    public static SslStores create(String keystoreFile,
                                   String keystorePassword,
                                   String truststoreFile,
                                   String truststorePassword,
                                   boolean needsClientCert) {

        return new SslStores(keystoreFile, keystorePassword, null, truststoreFile, truststorePassword, needsClientCert);
    }

    /**
     * Creates a Stores instance that uses the given certificate alias, optionally requiring client
     * certificates.
     *
     * @param keystoreFile the keystoreFile
     * @param keystorePassword the keystorePassword
     * @param certAlias the alias of the certificate to use from the keystore
     * @param truststoreFile the truststoreFile
     * @param truststorePassword the truststorePassword
     * @param needsClientCert whether clients must present a certificate
     * @return the SslStores instance.
     */
    public static SslStores create(String keystoreFile,
                                   String keystorePassword,
                                   String certAlias,
                                   String truststoreFile,
                                   String truststorePassword,
                                   boolean needsClientCert) {

        return new SslStores(keystoreFile, keystorePassword, certAlias, truststoreFile, truststorePassword, needsClientCert);
    }

    private SslStores(String keystoreFile,
                      String keystorePassword,
                      String certAlias,
                      String truststoreFile,
                      String truststorePassword,
                      boolean needsClientCert) {
        this.keystoreFile = keystoreFile;
        this.keystorePassword = keystorePassword;
        this.certAlias = certAlias;
        this.truststoreFile = truststoreFile;
        this.truststorePassword = truststorePassword;
        this.needsClientCert = needsClientCert;
    }

    /**
     * @return the path of the keystore file
     */
    public String keystoreFile() {
        return keystoreFile;
    }

    /**
     * @return the password of the keystore
     */
    public String keystorePassword() {
        return keystorePassword;
    }

    /**
     * @return the alias of the certificate to use from the keystore, or null if none was given
     */
    public String certAlias() {
        return certAlias;
    }

    /**
     * @return the path of the truststore file, or null if there is none
     */
    public String trustStoreFile() {
        return truststoreFile;
    }

    /**
     * @return the password of the truststore, or null if there is none
     */
    public String trustStorePassword() {
        return truststorePassword;
    }

    /**
     * @return true if clients must present a certificate
     */
    public boolean needsClientCert() {
        return needsClientCert;
    }
}
