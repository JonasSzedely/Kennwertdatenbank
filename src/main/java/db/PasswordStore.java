package db;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import model.AppLogger;

class PasswordStore {

    private static final String SERVICE = "Kennwertdatenbank";
    private static final String ACCOUNT = "db";

    static boolean save(String password) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(SERVICE, ACCOUNT, password);
            return true;
        } catch (Exception e) {
            AppLogger.error("Passwort konnte nicht gespeichert werden: " + e.getMessage());
            return false;
        }
    }

    static String load() {
        try (Keyring keyring = Keyring.create()) {
            return keyring.getPassword(SERVICE, ACCOUNT);
        } catch (PasswordAccessException e) {
            return null; // noch kein Passwort hinterlegt
        } catch (Exception e) {
            AppLogger.error("Passwort konnte nicht gelesen werden: " + e.getMessage());
            return null;
        }
    }
}
