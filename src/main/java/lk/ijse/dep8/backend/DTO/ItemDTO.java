package lk.ijse.dep8.backend.DTO;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

public class ItemDTO implements Serializable {
    private String isbn;
    private String name;
    private String author;
    private String price;
    @JsonbTransient
    private  byte[] preview;
    private boolean availability;

    public ItemDTO() {
    }

    public ItemDTO(String isbn, String name, String author) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
    }

    public ItemDTO(String isbn, String name, String author, byte[] preview) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
        this.preview = preview;
    }

    public ItemDTO(String isbn, String name, String author, byte[] preview, boolean availability) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
        this.preview = preview;
        this.availability = availability;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public byte[] getPreview() {
        return preview;
    }

    @JsonbProperty(value = "preview", nillable = true)
    public String getPreviewAsDataURI(){
        return (preview == null)? null: "data:image/*;base64," +
                Base64.getEncoder().encodeToString(preview);
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }

    public boolean isAvailability() {
        return availability;
    }

    @JsonbTransient
    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "isbn='" + isbn + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", preview=" + Arrays.toString(preview) +
                '}';
    }
}
