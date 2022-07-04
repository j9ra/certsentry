package pl.grabojan.certsentry.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="APPLICATION")
public class Application implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="APPLICATION_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Column(name="NAME", nullable = false)
	private String name;
	
	@Column(name="DESCRIPTION", nullable = true)
	private String description;
	
	@Column(name="API_KEY", nullable = false, unique = true)
	private String apiKey; 
	
	@OneToOne(mappedBy="application")
	private SecUser secUser;
}
