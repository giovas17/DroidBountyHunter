package training.edu.models;

/**
 * @author Giovani González
 * Created by darkgeat on 8/16/17.
 */

public class Fugitivo {
    private int id;
    private String name;
    private String status;
    private String photo;

    public Fugitivo(int id, String name, String status, String photo) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
