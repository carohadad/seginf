package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "offerers")
class Offerer {

	// for QueryBuilder to be able to find the fields
	public static final String NAME_FIELD_NAME = "name";
  
	@DatabaseField(generatedId = true)
	int id
	
	@DatabaseField(canBeNull = false)
	String name
	
	@DatabaseField(canBeNull = false, unique = true)
	String username
	
	@DatabaseField(canBeNull = false)
	String password

	@DatabaseField(canBeNull = false)
	String salt

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
