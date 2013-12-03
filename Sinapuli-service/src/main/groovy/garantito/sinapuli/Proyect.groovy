package garantito.sinapuli

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "proyect")
class Proyect {

	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField()
	private String nombre;
    	@DatabaseField()
	private String empresa;
    	@DatabaseField()
	private String descripcion;
    	@DatabaseField()
	private Date fechaCreacion;
    	@DatabaseField()
	private Date fechaInicioLicitacion;
    	@DatabaseField()
	private int horasDuracionLicitacion;
    

	Proyect() {
			// all persisted classes must define a no-arg constructor with at least package visibility
	}
	
	public Proyect(String nombre, String empresa, String descripcion, Date fechaInicioLicitacion, int horasDuracionLicitacion) {
		this.nombre = nombre;
		this.empresa = empresa;
		this.descripcion = descripcion;
		this.fechaInicioLicitacion = fechaInicioLicitacion;
		this.horasDuracionLicitacion = horasDuracionLicitacion;
		this.fechaCreacion = new Date();
	}

	public int getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}
	
	public String getEmpresa() {
		return empresa;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public Date getFechaInicioLicitacion() {
		return fechaInicioLicitacion;
	}

	public int getHorasDuracionLicitacion() {
		return horasDuracionLicitacion;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public void setFechaInicioLicitacion(Date fechaInicioLicitacion) {
		this.fechaInicioLicitacion = fechaInicioLicitacion;
	}
	
	public void setHorasDuracionLicitacion(int horasDuracionLicitacion) {
		this.horasDuracionLicitacion = horasDuracionLicitacion;
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
