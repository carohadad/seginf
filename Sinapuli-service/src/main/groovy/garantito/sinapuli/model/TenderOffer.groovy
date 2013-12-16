package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tenderOffers")
class TenderOffer {

	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private String hash;
        @DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] document;	
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Offerer offerer;
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Project project;
    

        TenderOffer() {
                // all persisted classes must define a no-arg constructor with at least package visibility
        }

        public TenderOffer(String hash, Offerer offerer, Project project) {
                this.hash = hash;
		this.offerer = offerer;		
		this.project = project;
        }

        public int getId() {
                return id;
        }

        public String getHash() {
                return hash;
        }

        public void setHash(String hash) {
                this.hash = hash;
        }

        public String getDocument() {
                return document;
        }

        public void setDocument(String document) {
                this.document = document;
        }

        public Offerer getOfferer() {
                return offerer;
        }

        public void setOfferer(String offerer) {
                this.offerer = offerer;
        }

        public Project getProject() {
                return project;
        }

        public void setProject(Project idProject) {
                this.project = project;
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
                return hash.equals(((TenderOffer) other).hash);
        }
}
