package DTO;

public class Position {
    private int Id_Position;
    private String Name_Position;

    public Position() {
        super();
    }
    public Position(int Id_Position, String Name_Position) {
        super();
        this.Id_Position = Id_Position;
        this.Name_Position = Name_Position;
    }
    public Position(String Name_Position) {
        super();
        this.Name_Position = Name_Position;
    }
    public int getId_Position() {
        return Id_Position;
    }

    public void setId_Position(int id_Position) {
        Id_Position = id_Position;
    }

    public String getName_Position() {
        return Name_Position;
    }

    public void setName_Position(String name_Position) {
        Name_Position = name_Position;
    }

}
