package passwordmanager.encryption;

import passwordmanager.model.EncryptionType;

/**
 * Фабрика для создания объектов шифрования
 */
public class EncryptionFactory {

    /**
     * Создает стратегию шифрования по типу
     * @param type тип шифрования
     * @return объект стратегии шифрования
     */
    public static EncryptionStrategy createStrategy(EncryptionType type) {
        switch (type) {
            case PLAIN:
                return new PlainEncryption();
            case BASE64:
                return new Base64Encryption();
            case SALTED:
                return new SaltedEncryption();
            case FEISTEL:
                return new FeistelEncryption();
            default:
                throw new IllegalArgumentException("Неизвестный тип шифрования: " + type);
        }
    }
}