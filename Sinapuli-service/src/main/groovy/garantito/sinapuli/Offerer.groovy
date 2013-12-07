package garantito.sinapuli

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "offerer")
class Offerer {

	// for QueryBuilder to be able to find the fields
	public static final String NAME_FIELD_NAME = "name";
  
	@DatabaseField(generatedId = true)
	int id
	
	@DatabaseField
	String name
	
	@DatabaseField
	String username
	
	@DatabaseField
	String password

	@DatabaseField
	String publicKey

	Offerer() {
		// all persisted classes must define a no-arg constructor with at least package visibility
	}

	public Offerer(String name) {
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

	@Override
	public String toString() {
		"<Offerer ${id}, username=${username}>"
	}

}
