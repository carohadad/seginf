import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "offerer")
class Offerer {

        // for QueryBuilder to be able to find the fields
        public static final String NAME_FIELD_NAME = "name";
  
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String name;
    

        Offerer() {
                // all persisted classes must define a no-arg constructor with at least package visibility
        }

        public Offerer(String name) {
                this.name = name;
        }

        public int getId() {
                return id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        @Override
        public int hashCode() {
                return name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
                if (other == null || other.getClass() != getClass()) {
                        return false;
                }
                return name.equals(((Offerer) other).name);
        }

}
