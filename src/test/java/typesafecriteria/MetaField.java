package typesafecriteria;

public class MetaField<E, T> {

    final String propertyName;

    public MetaField(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

}