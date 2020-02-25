/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.encryption;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "encrypted_text")
public class EncryptedText {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "text_type")
    private EncryptedTextType type;

    @Column(name = "encrypted_text")
    private String encryptedText;

    public EncryptedText() {}

    public EncryptedText(EncryptedTextType type, String encryptedText) {
        this.type = type;
        this.encryptedText = encryptedText;
    }

    public EncryptedTextType getType() {
        return type;
    }

    public void setType(EncryptedTextType type) {
        this.type = type;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public void setEncryptedText(String encryptedText) {
        this.encryptedText = encryptedText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptedText that = (EncryptedText) o;
        return type == that.type &&
                Objects.equals(encryptedText, that.encryptedText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, encryptedText);
    }

}
