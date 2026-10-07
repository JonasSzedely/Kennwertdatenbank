package db;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the database password in the operating system's keyring.
 * <p>
 * Uses Windows Credential Manager, macOS Keychain or the Secret Service API on Linux,
 * depending on the platform.
 */
class PasswordStore {

    private static final String SERVICE = "Kennwertdatenbank";
    private static final String ACCOUNT = "db";
    private static final Logger LOG = LoggerFactory.getLogger(PasswordStore.class);

    private PasswordStore() {
    }

    /**
     * Saves the password in the keyring and replaces any existing entry.
     *
     * @param password the password to store
     * @return {@code true} if the password was stored successfully, {@code false} otherwise
     */
    static boolean save(String password) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(SERVICE, ACCOUNT, password);
            return true;
        } catch (Exception e) {
            LOG.error("Password could not be saved", e);
            return false;
        }
    }

    /**
     * Reads the password from the keyring.
     *
     * @return the stored password, or {@code null} if none is stored or the keyring
     *         cannot be accessed
     */
    static String load() {
        try (Keyring keyring = Keyring.create()) {
            return keyring.getPassword(SERVICE, ACCOUNT);
        } catch (PasswordAccessException e) {
            return null; // no password stored yet
        } catch (Exception e) {
            LOG.error("Password could not be read", e);
            return null;
        }
    }
}