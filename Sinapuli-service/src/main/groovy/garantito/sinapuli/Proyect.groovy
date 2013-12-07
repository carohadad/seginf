package garantito.sinapuli

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "proyect")
class Proyect {

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField()
	String nombre;
	@DatabaseField()
	String descripcion;
    @DatabaseField()
	Date fechaCreacion;
    @DatabaseField()
	Date fechaInicioLicitacion;
	@DatabaseField()
	Date fechaFinLicitacion;

	Proyect() {
			// all persisted classes must define a no-arg constructor with at least package visibility
	}
	
	public Proyect(String nombre, String descripcion, Date fechaInicioLicitacion, Date fechaFinLicitacion) {		
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.fechaInicioLicitacion = fechaInicioLicitacion;		
		this.fechaCreacion = new Date();
		this.fechaFinLicitacion = fechaFinLicitacion;
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
		return hash.equals(((Proyect) other).hash);
	}
}
