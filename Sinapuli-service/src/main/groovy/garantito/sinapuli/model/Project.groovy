package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "projects")
class Project {

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField()
	String name;
	@DatabaseField()
	String description;
	@DatabaseField()
	Date creationDate;
	@DatabaseField()
	Date startTenderDate;
	@DatabaseField()
	Date endTenderDate;
        @DatabaseField(dataType = DataType.BYTE_ARRAY)
	byte[] tender;	

	Project() {
		// all persisted classes must define a no-arg constructor with at least package visibility
	}
	
	public Project(String name, String description, Date startTenderDate, Date endTenderDate, byte[] tender) {		
		this.name = name;
		this.description = description;
		this.startTenderDate = startTenderDate;		
		this.creationDate = new Date();
		this.endTenderDate = endTenderDate;
		this.tender = tender;
	}
	
	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || other.getClass() != getClass()) {
				return false;
		}
		return hash.equals(((Project) other).hash);
	}
}
