package lk.ijse.dep8.backend.DTO;

import java.sql.Date;

public class OrderItemDTO {
    private int id;
    private String nic;
    private String isbn;
    private Date date;

    public OrderItemDTO(String nic, String isbn, Date date) {
        this.nic = nic;
        this.isbn = isbn;
        this.date = date;
    }

    public OrderItemDTO() {
    }

    public OrderItemDTO(String nic, String isbn) {
        this.nic = nic;
        this.isbn = isbn;
    }

    public OrderItemDTO(int id, String nic, String isbn, Date date) {
        this.id = id;
        this.nic = nic;
        this.isbn = isbn;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "IssueDTO{" +
                "id=" + id +
                ", nic='" + nic + '\'' +
                ", isbn='" + isbn + '\'' +
                ", date=" + date +
                '}';
    }
}
