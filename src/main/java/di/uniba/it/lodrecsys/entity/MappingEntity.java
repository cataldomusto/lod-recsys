package di.uniba.it.lodrecsys.entity;

/**
 * Class which represents POJO class which stores
 * specific information for the items of the dataset
 */
public class MappingEntity {
    protected String dbpediaURI;
    protected String name;
    protected String itemID;

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public MappingEntity(String itemID, String dbpediaURI, String name) {
        this.dbpediaURI = dbpediaURI;
        this.name = name;
        this.itemID = itemID;
    }

    public String getDbpediaURI() {
        return dbpediaURI;
    }

    public void setDbpediaURI(String dbpediaURI) {
        this.dbpediaURI = dbpediaURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MappingEntity)) return false;

        MappingEntity that = (MappingEntity) o;

        if (dbpediaURI != null ? !dbpediaURI.equals(that.dbpediaURI) : that.dbpediaURI != null) return false;
        if (itemID != null ? !itemID.equals(that.itemID) : that.itemID != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = dbpediaURI != null ? dbpediaURI.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (itemID != null ? itemID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MappingEntity{" +
                "dbpediaURI='" + dbpediaURI + '\'' +
                ", name='" + name + '\'' +
                ", itemID='" + itemID;
    }
}
