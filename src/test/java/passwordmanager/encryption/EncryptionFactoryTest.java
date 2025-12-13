package passwordmanager.encryption;

import passwordmanager.model.EncryptionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionFactoryTest {

    @Test
    void testCreateAllStrategies() {
        assertInstanceOf(PlainEncryption.class,
                EncryptionFactory.createStrategy(EncryptionType.PLAIN));
        assertInstanceOf(Base64Encryption.class,
                EncryptionFactory.createStrategy(EncryptionType.BASE64));
        assertInstanceOf(SaltedEncryption.class,
                EncryptionFactory.createStrategy(EncryptionType.SALTED));
        assertInstanceOf(FeistelEncryption.class,
                EncryptionFactory.createStrategy(EncryptionType.FEISTEL));
    }

    @Test
    void testUnknownEncryptionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Создаем несуществующий тип
            EncryptionType fakeType = EncryptionType.valueOf("UNKNOWN");
        });
    }
}