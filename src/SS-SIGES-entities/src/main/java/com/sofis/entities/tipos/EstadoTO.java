package com.sofis.entities.tipos;

public class EstadoTO {

	private Integer id;
	private String nombre;

	public EstadoTO() {
	}

	public EstadoTO(Integer id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
