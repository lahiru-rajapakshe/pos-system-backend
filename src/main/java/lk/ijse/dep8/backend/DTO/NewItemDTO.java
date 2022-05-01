package lk.ijse.dep8.backend.DTO;

import java.io.Serializable;

public class NewItemDTO implements Serializable {
    private String nic;
    private String name;
    private String contact;


    public NewItemDTO(String nic, String name, String contact) {
        this.nic = nic;
        this.name = name;
        this.contact = contact;
    }


    public NewItemDTO() {
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "MemberDTO{" +
                "nic='" + nic + '\'' +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
