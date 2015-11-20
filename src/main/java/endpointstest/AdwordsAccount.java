package endpointstest;

/**
 * Created by <a href="mailto:mpoehler@gmail.com">Marco P&ouml;hler</a> on 30.11.14.
 */
public class AdwordsAccount {

    private String name;
    private String id;

    public AdwordsAccount(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public AdwordsAccount() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdwordsAccount that = (AdwordsAccount) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AdwordsAccount{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}