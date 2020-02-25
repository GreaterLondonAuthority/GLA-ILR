/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.encryption;

import liquibase.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptedTextService {

    @Autowired
    private EncryptedTextRepository encryptedTextRepository;

    @Autowired
    private TextEncryptor textEncryptor;

    public String getText(EncryptedTextType type) {
        EncryptedText encryptedText = find(type);
        if (encryptedText != null && StringUtils.isNotEmpty(encryptedText.getEncryptedText())) {
            return textEncryptor.decrypt(encryptedText.getEncryptedText());
        }
        return null;
    }

    private EncryptedText find(EncryptedTextType type) {
        return encryptedTextRepository.findById(type).orElse(null);
    }


    public void save(EncryptedTextType type, String text) {
        String encryptedText = textEncryptor.encrypt(text);
        encryptedTextRepository.save(new EncryptedText(type, encryptedText));
    }

}
