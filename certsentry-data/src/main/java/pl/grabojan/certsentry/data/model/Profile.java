package pl.grabojan.certsentry.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROFILE")
public class Profile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PROFILE_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Column(name="NAME", nullable = false, unique = true)
	private String name;
	
	@Column(name="TERRITORY", nullable = false)
	private String territory;
	
	@Column(name="PROVIDER", nullable = true)
	private String provider;
	
	@Column(name="SERVICE_INFO", nullable = true)
	private String serviceInfo;
	
}
