package passwordmanager.encryption;

/**
 * Реализация "шифрования" - хранение данных в открытом виде.
 * <p>
 * Данный класс используется в основном для тестирования и отладки,
 * так как не выполняет никакого реального шифрования. Все данные
 * возвращаются в исходном виде. <b>Не рекомендуется</b> использовать
 * в производственной среде из-за отсутствия безопасности.
 *
 * @version 1.0
 * @since 2024
 * @see EncryptionStrategy
 */
public class PlainEncryption implements EncryptionStrategy {

    /**
     * Возвращает данные без изменений.
     *
     * @param data исходные данные
     * @return те же самые данные без изменений
     */
    @Override
    public String encrypt(String data) {
        // Просто возвращаем данные как есть
        return data;
    }

    /**
     * Возвращает данные без изменений.
     *
     * @param encryptedData "зашифрованные" данные
     * @return те же самые данные без изменений
     */
    @Override
    public String decrypt(String encryptedData) {
        // Просто возвращаем данные как есть
        return encryptedData;
    }
}