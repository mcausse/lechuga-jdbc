package org.frijoles.mapper.ents;

public class IdDog {
    Long idDog;
    String name;

    public IdDog() {
        super();
    }

    public IdDog(Long idDog, String name) {
        super();
        this.idDog = idDog;
        this.name = name;
    }

    public Long getIdDog() {
        return idDog;
    }

    public void setIdDog(Long idDog) {
        this.idDog = idDog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "IdDog [idDog=" + idDog + ", name=" + name + "]";
    }

}
